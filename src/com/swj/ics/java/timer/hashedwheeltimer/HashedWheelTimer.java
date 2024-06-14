package com.swj.ics.java.timer.hashedwheeltimer;

import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2024/05/17 20:58
 * Timer 实现类
 */
@Slf4j
public class HashedWheelTimer implements Timer {

  private static final AtomicIntegerFieldUpdater<HashedWheelTimer> WORKER_STATE_UPDATER =
      AtomicIntegerFieldUpdater.newUpdater(HashedWheelTimer.class, "workerState");
  // 为啥要统计 timer 的个数，是因为 每个 Timer 都会拥有一个 worker 线程，线程是操作系统的珍贵资源。
  //最大 64 个实例
  private static final int INSTANCE_COUNT_LIMIT = 64;
  private static final AtomicInteger INSTANCE_COUNTER = new AtomicInteger(0);
  private static final AtomicBoolean WARN_INSTANCE_TOO_MANY_REPORT = new AtomicBoolean();
  // 1 ms 代表的 纳秒数
  private static final long ONE_MILLI_SECOND_NANOS = 1_000_000;

  // 组成时间轮的数组
  private HashedWheelBucket[] wheel;
  private int mask;
  private long tickDuration;

  // todo:先用 ConcurrentLinkedQueue ，后面改为自己改造后的 JCTools 中的 Mpsc queue
  private final Queue<HashedWheelTimeout> timeouts = new ConcurrentLinkedQueue<>();
  private final Queue<HashedWheelTimeout> cancelledTimeouts = new ConcurrentLinkedQueue<>();
  // 当前挂起的任务数量
  private final AtomicLong pendingTimeoutCounter = new AtomicLong(0L);
  private final CountDownLatch startTimeInitialized = new CountDownLatch(1);


  // 一个 Timer 一个 worker 工作线程，这点跟 jdk 的 timer 保持一致。
  // 不一致的目测有 4 点：
  // 1、netty timer 的 worker runnable 有具体的类 Worker，对象是个具体对象，jdk runnable 是一个匿名对象
  // 2、jdk 是在 runnable 里面处理用户的逻辑， netty timer 是在 timerTask 的这个非 runnable 里面处理用户逻辑，runnable 只是用来提供给 WorkerThread 线程使用的
  // 3、timer 退出的时候，jdk timer是直接情况队列中未处理的任务，netty timer 是将队列中和时间轮中所有的未处理任务全部任务返回，这点跟 Jdk ThreadPoolExecutor 的 shutdownNow() 的返回值异曲同工之妙。
  // 4、timer 的退出机制，jdk timer 是用户任务抛出异常就退出，没有控制机制，相反， netty timer 有控制机制，可以通过外部线程最终通过状态字段来控制 timer 的运行
  private final Worker worker = new Worker();
  private Thread workerThread;

  // Timer 开启的时间。ns 计时
  private volatile long startTime;

  private static final int WORKER_STATE_INIT = 0;
  private static final int WORKER_STATE_STARTED = 1;
  private static final int WORKER_STATE_SHUTDOWN = 2;
  // worker 线程的状态，由于worker 线程需要被主线程开启和关闭，其是通过更改状态字段来达到的，因此这个字段必须放在 Worker 线程之外。
  private volatile int workerState;// 0: init, 1: started, 2: shutdown

  // 设置当前待处理的最大任务个数，小于 0 ，则表示无限制
  private long maxPendingTimeouts;

  public HashedWheelTimer() {
    this(Executors.defaultThreadFactory());
  }

  public HashedWheelTimer(ThreadFactory threadFactory) {
    this(threadFactory, 100, TimeUnit.MILLISECONDS);
  }

  public HashedWheelTimer(ThreadFactory threadFactory, long tickDuration, TimeUnit unit) {
    this(threadFactory, tickDuration, unit, 512);
  }

  public HashedWheelTimer(ThreadFactory threadFactory, long tickDuration, TimeUnit unit,
      int ticksPerWheel) {
    this(threadFactory, tickDuration, unit, ticksPerWheel, -1);
  }

  // 构造函数也能玩出花
  public HashedWheelTimer(ThreadFactory threadFactory, long tickDuration, TimeUnit unit,
      int ticksPerWheel, int maxPendingTimeouts) {
    if (threadFactory == null) {
      throw new NullPointerException("thread factory is null");
    }
    if (unit == null) {
      throw new NullPointerException("unit is null");
    }
    if (tickDuration <= 0) {
      throw new IllegalArgumentException("tickDuration should be greater than 0");
    }
    if (ticksPerWheel <= 0) {
      throw new IllegalArgumentException("ticksPerWheel should be greater than 0");
    }

    wheel = createWheel(ticksPerWheel);
    mask = wheel.length - 1;

    long td = unit.toNanos(tickDuration);

    // 防止溢出，本来是 Duration * ticksPerWheel > long.max_value, 但是前面的相乘已经溢出，抛异常了
    // 因此这里采用相除的方式判断溢出。
    if (td > Long.MAX_VALUE / wheel.length) {
      throw new IllegalArgumentException(String.format("tickDuration %d (expected 0 < tickDuration in nanos < %d)",
          td, Long.MAX_VALUE / wheel.length));
    } else if (td < ONE_MILLI_SECOND_NANOS) {
      td = ONE_MILLI_SECOND_NANOS;
    }
    this.tickDuration = td;

    workerThread = threadFactory.newThread(worker);
    this.maxPendingTimeouts = maxPendingTimeouts;
    if (INSTANCE_COUNTER.incrementAndGet() > INSTANCE_COUNT_LIMIT) {
      if (WARN_INSTANCE_TOO_MANY_REPORT.compareAndSet(false, true)) {
        reportTooManyInstances();
      }
    }

  }

  // 析构函数中，重写是为了防止用户忘记调用 close 方法，在对象被回收的时候，增加逻辑判断
  @Override
  protected void finalize() throws Throwable {
    try {
      super.finalize();
    } finally {
      // 如果当前对象所拥有的 worker 线程还没有关闭，则关闭之并减少对象数量
      if (WORKER_STATE_UPDATER.getAndSet(this, WORKER_STATE_SHUTDOWN) != WORKER_STATE_SHUTDOWN) {
        INSTANCE_COUNTER.decrementAndGet();
      }
    }
  }

  private void reportTooManyInstances() {
    String instanceType = HashedWheelTimer.class.getSimpleName();
    log.error("you are creating too many {} instances. {} is a shared type and must be reused accross the JVM "
        + "so that only a few instances are created.", instanceType, instanceType);
  }

  private static HashedWheelBucket[] createWheel(int ticksPerWheel) {
    if (ticksPerWheel <= 0) {
      throw new IllegalArgumentException("ticksPerWheel must be greater than  0");
    }
    if (ticksPerWheel > (1 << 30)) {
      throw new IllegalArgumentException("ticketsPerWheel should be less than 2^30");
    }
    int wheelTicks = normalizedTicksPerWheel(ticksPerWheel);
    HashedWheelBucket[] wheel = new HashedWheelBucket[wheelTicks];
    for (int i = 0; i < wheelTicks; i++) {
      wheel[i] = new HashedWheelBucket();
    }
    return wheel;
  }

  private static int normalizedTicksPerWheel(int ticksPerWheel) {
    int ticks = 1;
    // 这里采用了相对比较 low 的方式来实现找到 >=ticksPerWheel 的最小的 2 的幂次方的数值。
    // 没有 HashMap 的扩容时候采用的 tableFor 的位移方式效率高，不过这里只初始化一次，问题不大，不像 hashMap 一样，需要经常扩容
    while (ticks < ticksPerWheel) {
      ticks <<= 1;
    }
    return ticks;
  }

  @Override
  public Timeout newTimeout(TimerTask timerTask, long delay, TimeUnit unit) {
    if (timerTask == null) {
      throw new NullPointerException("task is null");
    }
    if (unit == null) {
      throw new NullPointerException("unit is null");
    }

    // 判断 timeout 任务是否过多
    long pendingTimeouts = pendingTimeoutCounter.incrementAndGet();
    if (maxPendingTimeouts > 0 && pendingTimeouts > maxPendingTimeouts) {
      pendingTimeoutCounter.decrementAndGet();
      throw new RejectedExecutionException(String.format("Number of pending timeout tasks (%d) is greater than "
          + "max pending timeouts (%d)", pendingTimeouts, maxPendingTimeouts));
    }

    // 延迟启动 worker 线程
    startWorker();

    // 添加由 Timeout 封装的 timerTask 任务，将将其返回

    long deadline = System.nanoTime() - startTime + delay;
    if (delay > 0 && deadline < 0) { //防溢出判断
      deadline = Long.MAX_VALUE;
    }
    HashedWheelTimeout timeout = new HashedWheelTimeout(timerTask, this, deadline);
    //加入任务队列
    timeouts.add(timeout);
    return timeout;
  }

  // 启动 worker 线程。在外部线程启动 worker 线程
  // 注意启动逻辑，需要先判断 worker 线程的启动状态，因为一个 timer 对象只有一个 worker 线程
  private void startWorker() {
    switch (WORKER_STATE_UPDATER.get((this))) {
      case WORKER_STATE_INIT:
        if (WORKER_STATE_UPDATER.compareAndSet(this, WORKER_STATE_INIT, WORKER_STATE_STARTED)) {
          workerThread.start();
        }
        break;
      case WORKER_STATE_STARTED:
        break;
      case WORKER_STATE_SHUTDOWN:
        throw new IllegalStateException("can not be started once  stopped.");
      default:
        throw new Error("Invalid WorkerState");
    }
    // 等待 worker 线程启动完毕
    while (startTime == 0) {
      try {
        startTimeInitialized.await();
      } catch (InterruptedException e) {
        // ignore - will be ready soon.
      }
    }
  }

  @Override
  public Set<Timeout> stop() {
    // 跟 start 一样，也是先判断 线程拥有者，在判断状态
    if (Thread.currentThread() == workerThread) {
      // 之所以这么做是为了防止 timer.newTimeout(()->{ timer.stop(); });
      // 这样的既当裁判又当运动员的情况
      throw new IllegalStateException(HashedWheelTimer.class.getSimpleName() + ".stop() can not be called from "
          + worker.getClass().getSimpleName());
    }
    // 未能 cas 成功的线程
    if (!WORKER_STATE_UPDATER.compareAndSet(this, WORKER_STATE_STARTED, WORKER_STATE_SHUTDOWN)) {
      // worker state can be 0 or 2 right now, let it always to be 2.
      if (WORKER_STATE_UPDATER.getAndSet(this, WORKER_STATE_SHUTDOWN) != WORKER_STATE_SHUTDOWN) {
        INSTANCE_COUNTER.decrementAndGet();
      }
      return Collections.emptySet();
    }
    try {
      boolean interrupted = false;
      while (workerThread.isAlive()) {
        // 通过 先改状态，后中断，使得 worker 线程在 sleep 的时候相应中断，并通过状态判断调出线程的主循环，达到结束线程的目的
        workerThread.interrupt();
        // 等待 100ms
        try {
          workerThread.join(100);
        } catch (InterruptedException ignore) {
          interrupted = true;
        }
      }
      if (interrupted) {
        Thread.currentThread().interrupted();
      }
      // 把 WorkerInstance.decrementAndGet() 忘记了，大 bug 呀
    } finally {
      INSTANCE_COUNTER.decrementAndGet();
    }

    return worker.unprocessedTimeouts();
  }


  /**
   * Worker 线程的主要运行方法体。
   * 方法的主要逻辑是推荐时间的运行，然后在运行一个指定的 时钟指针索引 bucketIndex 的时候,开始处理超时任务。
   * 而真实的超时任务处理显然比我们想象的要复杂，主要包括：
   * 1、处理被取消的任务
   * 2、从超时任务队列中获取超时任务，放入指定的时间轮的槽中
   * 3、执行当前 bucketIndex 槽中所有的的到期任务
   */
  private final class Worker implements Runnable {

    Set<HashedWheelTimeout> unprocessedTasks = new HashSet<>();
    private long tick;

    @Override
    public void run() {
      /**
       * 主要做几件事：
       * 1、更改 startTime 为当前时间，通知外部等待的主线程
       * 2、mainLoop 主要处理的是，等待一个 tick 的的到来之后，开始处理相关的超时任务，
       *  2.1 处理所有取消队列中被取消的任务
       *  2.2 添加所有任务队列中的超时任务到指定槽中
       *  2.3 上述 2 个工作都是顺手做的且不做不行的，第三项就是将当期那 tick 到来时的 bucket 中的所有到期任务执行，不到期任务 remainingRounds--
       * 3、主循环退出，说明 timer 被外部关掉了，则进行首尾工作；收尾工作主要有一下3点：
       *  3.1 将树所有未处理的任务添加到 unprocessedTasks 中
       *  3.2 将队列中所有的正常的未处理的任务添加到 unprocessedTasks 中
       *  3.3 处理所有 cancelled 的任务。
       */

      startTime = System.nanoTime();
      if (startTime == 0) {
        startTime = 1;// 这里只是增加以外判断，如果 startTime == 0 ，则改为 1 是因为外部的主线程判断 timerThread 是否 start 成功的表示就是 startTime != 0
      }
      // 通知主线程不再阻塞
      startTimeInitialized.countDown();
      mainLoop();
      // worker 线程被外部线程关闭, 接下来是处理 worker 线程退出事宜
      // 处理时间轮中的定时任务
      for (HashedWheelBucket bucket : wheel) {
        bucket.moveTimeoutsTo(unprocessedTasks);
      }
      // 处理队列中的任务
      HashedWheelTimeout timeout = timeouts.poll();
      while (timeout != null) {
        if (!timeout.isCancelled()) {
          unprocessedTasks.add(timeout);
        }
        timeout = timeouts.poll();
      }
      // 再处理线程暂停期间的 cancelled task
      processCancelledTasks();

    }

    private void mainLoop() {
      do {
        long currentTime = waitForNextTick();
        if (currentTime > 0) { // 正常情况
          processCancelledTasks();
          transferTimeoutsToBucket();
          int bucketIdx = (int) (tick & mask);
          wheel[bucketIdx].expiredTimeouts(currentTime);
          tick++;
        }
      } while (WORKER_STATE_UPDATER.get(HashedWheelTimer.this) == WORKER_STATE_STARTED);
    }

    private void transferTimeoutsToBucket() {
      // 每次最多处理队列中 100000 个超时任务
      for (int i = 0; i < 10_000; i++) {
        HashedWheelTimeout timeout = timeouts.poll();
        if (timeout == null) { // 队列空了
          break;
        }
        if (timeout.isCancelled()) { // 这里就看出来了，被取消的timeout 仍然可能在 队列中，由于队列底层用的是数组，移动不便
          continue;
        }
        int bucketIndex = getCorrectBucketIndex(timeout);
        wheel[bucketIndex].addTimeout(timeout);
      }
    }

    private int getCorrectBucketIndex(HashedWheelTimeout timeout) {
      long calculatedTicks = timeout.deadline / tickDuration;
      timeout.remainingRounds = (calculatedTicks - tick) / wheel.length;
      // 这里使用 Max.max 来防止我们使用了一个已经过时的 tick
      long currentTick = Math.max(calculatedTicks, tick);

      return (int) (currentTick & mask);
    }

    /**
     * 等待下一个 tick 的到来
     *
     * @return 当前时间。如果为 LONG.MIN_VALUE，则表明线程池被 shutdown了
     */
    private long waitForNextTick() {
      long deadline = (tick + 1) * tickDuration;

      for (; ; ) {// 在外面加 for(;;) 循环的目的是为了防止 sleep 被无故唤醒
        // Timer 自启动以来经过的 纳秒数
        long currentTime = System.nanoTime() - startTime;
        // 线程休眠至少 1ms，防止被频繁唤醒
        long sleepMs = (deadline - currentTime + 999_999) / 1_000_000;
        if (sleepMs <= 0) {
          if (currentTime == Long.MIN_VALUE) { // 因为 Long.MIN_VALUE 是 shutdown 的标识，所以这里改为 +1 形式
            currentTime = -Long.MAX_VALUE;
          }
          return currentTime;
        }

        try {
          Thread.sleep(sleepMs); // 最终 netty 是通过 sleep 来实现时间推进的，因此也会造成很多无用的推进。
        } catch (InterruptedException ignore) {
          if (WORKER_STATE_UPDATER.get(HashedWheelTimer.this) == WORKER_STATE_SHUTDOWN) {
            return Long.MIN_VALUE;
          }
        }
      }
    }

    private void processCancelledTasks() {
      for (; ; ) {
        HashedWheelTimeout timeout = cancelledTimeouts.poll();
        if (timeout == null) {
          break;
        }
        // 如果当前被取消的任务已经进入时间轮，则将其从时间轮中删除。
        try {
          timeout.remove();
        } catch (Exception e) {
          log.warn("an exception was thrown while processing an cancellation task.");
        }
      }
    }

    public Set<Timeout> unprocessedTimeouts() {
      return Collections.unmodifiableSet(unprocessedTasks);
    }
  }


  // 时间轮是由各个点/这里叫桶 的数组元素组成的。
  // 每个桶里装的都是 Timeout 的元素，元素之间用链表连起来。
  private final static class
  HashedWheelBucket {

    HashedWheelTimeout head;
    HashedWheelTimeout tail;

    /**
     * 加入 Bucket 所在的链表中
     *
     * @param timeout
     */
    public void addTimeout(HashedWheelTimeout timeout) {
      if (timeout == null) {
        throw new IllegalArgumentException("timeout can not be null");
      }
      timeout.bucket = this;
      if (head == null) {
        head = tail = timeout;
      } else {
        tail.next = timeout;
        timeout.prev = tail;
        tail = timeout;
      }
    }

    /**
     * 遍历整个链表，将到达 deadline 时间的所有超时任务全部执行并超时
     * 因此这里整个链表的元素不能太多
     *
     * @param deadline 指定时间
     */
    public void expiredTimeouts(long deadline) {
      HashedWheelTimeout timeout = head;
      while (timeout != null) {
        HashedWheelTimeout next = timeout.next;
        if (timeout.remainingRounds <= 0) { // 当前任务到期
          // 将当前解节点从 链表中线删除，并返回
          next = remove(timeout);
          // 再次判断
          if (timeout.deadline <= deadline) {
            // 执行超时任务
            timeout.expire();
          } else { // 异常情况
            throw new IllegalStateException(
                String.format("timeout.deadline (%d) > deadline (%d)", timeout.deadline, deadline));
          }
        } else if (timeout.isCancelled()) { // 这里的话，timeout 就还没有到期，则判断是否被取消了
          next = remove(timeout);
        } else {
          //圈数 --，其他情况下节点都会被移除
          timeout.remainingRounds--;
        }
        timeout = next;
      }
    }

    /**
     * 将当前节点从链表中删除
     *
     * @param timeout
     * @return
     */
    HashedWheelTimeout remove(HashedWheelTimeout timeout) {
      HashedWheelTimeout next = timeout.next;
      HashedWheelTimeout prev = timeout.prev;
//      if (prev == null && next == null) { // 链表只剩一个节点。这里判断有 bug 无法判断该节点是否在链表中,会导致指定随便 new
//       一个节点，调用该方法，就会导致链表链表被清空，所以这里的逻辑有很大的 bug。如果要完善的话，必须加上 head == timeout 这个判断
      // 但是加上这个判断，就显得臃肿了，没有下面官方的判断逻辑简洁了。
//        head = tail = null;
//      }
//      else if (next == null) { // 在队尾
//        tail = prev;
//        prev.next = null;
//      } else if (prev == null) { // 在队首
//        next.prev = null;
//        head = next;
//      } else { // 在中间
//        prev.next = next;
//        next.prev = prev;
//      }

      // 上面的链表操作是我自己写的，我认为跟下面的 Netty 的表述是一致的，但是确实 Netty 的逻辑更清晰，逻辑如下
      // 先判断 prev 和 next
      if (next != null) {
        next.prev = prev;
      }
      if (prev != null) {
        prev.next = next;
      }

      // 再判断 timeout 跟 head 和 tail 的关系
      if (timeout == head) {
        if (timeout == tail) {
          head = tail = next;
        } else {
          head = next;
        }
      } else if (timeout == tail) {
        tail = prev;
      }

      timeout.next = null;
      timeout.prev = null;
      timeout.bucket = null;
      // 任务所在的 timer 挂起任务数量 --
      timeout.timer.pendingTimeoutCounter.decrementAndGet();
      return next;
    }

    /**
     * 清除当前桶中的所有 timeout 任务，并将正常的任务加入 set 中返回。
     * 主要是为了在 timer 线程结束的时候，将未处理完的任务返回。
     *
     * @param set
     */
    public void moveTimeoutsTo(Set<HashedWheelTimeout> set) {
//      HashedWheelTimeout timeout = head;
//      while (timeout != null) {
//        if (timeout.isCancelled() || timeout.isExpired()) {
//          continue;
//        }
//        set.add(timeout);
//        timeout = remove(timeout); //
//      }
      // 上面方法是我编写的，目测起来确实没有任何的错误，对比了下 netty 的源码，发现remove 方法不能在这里使用，remove 方法会减少所在 timer 的 pendingTask 计数
      // 但是这里不应该，这里是指把任务 一个个地 drain 到 set 中去。
      for (; ; ) {
        HashedWheelTimeout timeout = pollTimeout();
        if (timeout == null) { // 链表遍历完毕
          break;
        }
        if (timeout.isExpired() || timeout.isCancelled()) {
          continue;
        }
        set.add(timeout);
      }
    }

    private HashedWheelTimeout pollTimeout() {
      HashedWheelTimeout p = head;
      if (p == null) {
        return null;
      }
      HashedWheelTimeout next = p.next;
      if (next == null) {
        head = tail = null;
      } else {
        head = next;
        next.prev = null;
      }
      p.prev = null;
      p.next = null;
      p.bucket = null;
      return p;
    }

  }


  private final static class HashedWheelTimeout implements Timeout {

    private static final int ST_INIT = 0;
    private static final int ST_CANCELLED = 1;
    private static final int ST_EXPIRED = 2;
    private static final AtomicIntegerFieldUpdater<HashedWheelTimeout> STATE_UPDATER =
        AtomicIntegerFieldUpdater.newUpdater(HashedWheelTimeout.class, "state");

    // 定义任务状态
    private volatile int state = ST_INIT;
    private final TimerTask task;
    private final HashedWheelTimer timer;
    // 由于 netty 的 timer 的实现中都是定时单次的任务，重复的任务本次不涉及，我就先不增加该功能
    // todo: 后面测试通过之后，再增加定时重复功能

    private final long deadline;

    private HashedWheelTimeout(TimerTask task, HashedWheelTimer timer, long deadline) {
      this.task = task;
      this.timer = timer;
      this.deadline = deadline;
    }

    // 就像 HandlerContext 一样组成双链表的指针
    private HashedWheelTimeout prev;
    private HashedWheelTimeout next;

    // 剩余多少圈
    private long remainingRounds;

    // 当前任务所在的 bucket
    private HashedWheelBucket bucket;


    @Override
    public Timer timer() {
      return timer;
    }

    @Override
    public TimerTask task() {
      return task;
    }

    public void expire() {
      if (!compareAndSetState(ST_INIT, ST_EXPIRED)) {
        return;
      }
      // 任务已过期，则执行之
      try {
        task.run(this);
      } catch (Exception e) {
        // if log.isWarnable ....就省略了
        log.warn("unexpected exception was thrown by {}.", TimerTask.class.getSimpleName(), e);
      }
    }

    private boolean compareAndSetState(int currentState, int newState) {
      return STATE_UPDATER.compareAndSet(this, currentState, newState);
    }

    @Override
    public boolean isExpired() {
      return state == ST_EXPIRED;
    }

    @Override
    public boolean isCancelled() {
      return state == ST_CANCELLED;
    }

    public int state() {
      return state;
    }

    @Override
    public boolean cancel() {
      //取消一个任务.

      // 必须得 在任务启动之前取消，任务运行中不允许取消？
      if (!compareAndSetState(ST_INIT, ST_CANCELLED)) {
        return false;
      }
      // 状态已经改为 cancelled, 此时键入 timer 的取消队列，然后由 timer 统一处理。
      timer.cancelledTimeouts.add(this);
      // 这里 netty 用了一个优化，只是将当前任务添加到 cancelledTimeouts 队列中，但是当前任务仍在 普通任务队列或者 时间轮的链表中
      // handlerCancelled 方法调用 remove 方法， 而 remove 方法中是是从 时间轮的链表中删除它，并未从 普通任务队列中中删除它，
      // 这是因为 Netty 的任务队列用的 JC Tools 的 Mpsc 的基于数组的队列，如果对元素删除的话，会导致数组元素位置的移动，或者说 Mpsc 不支持元素的删除，但是这里我用 ConcurrentLinkedQueen 来复写，可以直接从队列中删除
      // 同时 netty 不将已取消的任务移出正常任务队列，就会造成 从正常任务队列中获取任务的时候，必须增加判断元素是否被取消了这个逻辑，否则就是 bug 了，比如在 transferTimeoutToBuckets 方法中
      // 所以从这里可用看到 netty 为了提高性能，也是做了很多妥协。
      return true;
    }


    // 移除当前任务
    public void remove() {
      HashedWheelBucket bucket = this.bucket;
      if (bucket != null) { // 在时间轮中将当前延迟对象任务删除
        bucket.remove(this);
      } else {// 比如才是还没有加入相关桶中，只是在延迟队列中,则维护相关统计量。
        timer.pendingTimeoutCounter.decrementAndGet();
      }
    }

    public TimerTask getTask() {
      return task;
    }

    @Override
    public String toString() {
      long remainingNs = System.nanoTime() - timer.startTime + deadline;
      return "HashedWheelTimeout{" +
          "deadline: " + (remainingNs > 0 ?
          remainingNs + " ns later" :
          remainingNs < 0 ? -remainingNs + " ns ago" : "now") +
          (isCancelled() ? ", cancelled" : "") +
          ", task: " + getTask() +
          '}';
    }
  }

}
