package com.swj.ics.java.timer;

import com.google.common.base.Preconditions;

import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2024/03/22 22:13
 * 参考 JDK Timer 组件
 * 虽然 JDM Timer 组件特别 low，但是 我就是不会写
 * 其中的设计想象，至今仍闪耀着牛逼的光芒。
 * 主要的组件为 Timer 本身 + 运行 Timer 的调度线程 + 存储 Timer 任务的优先级队列/小顶堆
 * 总体设计就是一个队列(优先级) + 一个从队列中取任务的线程，判断任务是否到时间后需要执行否？需要执行的取出，并执行
 * 如果是定时任务，则计算下次执行时间，重新扔入队列
 */
public class MyTimer {

  private final TaskQueue queue = new TaskQueue();

  private final TimerThread thread = new TimerThread(queue);

  // 线程收割者
  // 在 Timer 被垃圾回收的时候，用来唤醒前程，防止线程阻塞等待
  // 这种写法我还是第一次见
  Object threadReaper = new Object() {
    @Override
    protected void finalize() throws Throwable {
      synchronized (queue) {
        queue.notify(); // 防止等待在 isEmpty()
        // 不再接收新任务
        thread.newTaskShouldBeScheduled = false;
      }
    }
  };


  private static final AtomicInteger nextSerialNumber = new AtomicInteger(0);

  private static int getSerialNumber() {
    return nextSerialNumber.incrementAndGet();
  }

  public MyTimer() {
    this("Timer-" + getSerialNumber());
  }


  public MyTimer(String name) {
    thread.setName(name);
    thread.start();
  }

  public MyTimer(boolean isDaemon) {
    this("Timer-" + getSerialNumber(), isDaemon);
  }

  public MyTimer(String name, boolean isDaemon) {
    thread.setName(name);
    thread.setDaemon(isDaemon);
    thread.start();
  }

  public void schedule(MyTimerTask timerTask, long delay) {
    sched(timerTask, System.currentTimeMillis() + delay, 0l);
  }

  public void cancel() {
    synchronized (queue) {
      thread.newTaskShouldBeScheduled = false;
      queue.clear();
      queue.notify();//防止线程在 isEmpty() 上面的等待
    }
  }

  public void schedule(MyTimerTask task, Date time) {
    sched(task, time.getTime(), 0);
  }

  // 固定延迟的执行

  /**
   * @param task   任务
   * @param delay  任务被首次执行的延迟
   * @param period 任务首次之后的固定延迟
   */
  public void schedule(MyTimerTask task, long delay, long period) {
    Preconditions.checkArgument(delay >= 0, "Negative delay.");
    Preconditions.checkArgument(period > 0, "Non-positive period.");
    sched(task, System.currentTimeMillis() + delay, -period);
  }

  public void schedule(MyTimerTask task, Date firstTime, long period) {
    Preconditions.checkArgument(period > 0, "Non-positive period.");
    sched(task, firstTime.getTime(), period);
  }

  public void scheduleAtFixedRate(MyTimerTask task, long delay, long period) {
    Preconditions.checkArgument(delay >= 0, "Negative delay.");
    Preconditions.checkArgument(period > 0, "Non-positive period.");
    sched(task, System.currentTimeMillis() + delay, period);
  }

  public void scheduleAtFixedRate(MyTimerTask task, Date firstTime, long period) {
    Preconditions.checkArgument(period > 0, "Non-positive period.");
    sched(task, firstTime.getTime(), period);
  }

  private void sched(MyTimerTask task, long time, long period) {
    Preconditions.checkNotNull(task, "task is null");
    Preconditions.checkArgument(time >= 0, "Illegal execution time");
    if (Math.abs(period) > (Long.MAX_VALUE >> 1)) { // period 边界判断；period 可以为负数
      period >>= 1;
    }

    synchronized (queue) { // 因为 queue 是非线程安全的，主线程在往里面放任务，子线程 timer 线程在取任务
      if (!thread.newTaskShouldBeScheduled) {
        throw new IllegalStateException("Timer already cancelled.");
      }

      synchronized (task.lock) {
        // 多线程安全检查
        if (task.state != MyTimerTask.VIRGIN) {
          throw new IllegalStateException("Task already scheduled or cancelled.");
        }
        task.nextRunTime = time;
        task.state = MyTimerTask.SCHEDULED;
        task.period = period;
      }

      queue.add(task);
      if (task == queue.getMin()) { // 这个判断非常有技巧，因为子线程在刚开始运行的时候，发现队列为空，会阻塞，这里加入一个元素，
        // 如果是第一个元素或者是最小开始时间的元素, 则唤醒阻塞在 isEmpty() 出的子线程或者唤醒阻塞在等待 timeout 处的子线程，无论哪种唤醒，
        // 子线程在被唤醒之后，都会判断当期元素是否已经过期再决定是否运行 timer 任务
        queue.notify();
      }
    }
  }


  /**
   * Timer 定制的线程类。Netty 中的比如 FastThreadLocalThread
   */
  class TimerThread extends Thread {
    // 是否需要添加新任务，当 Timer 被取消的时候，需要通知当前的 Timer 调度线程，不再接收新任务
    private boolean newTaskShouldBeScheduled = true;
    private final TaskQueue queue;

    public TimerThread(TaskQueue queue) {
      this.queue = queue;
    }

    @Override
    public void run() {
      try {
        mainLoop();
      } catch (Exception e) {
        synchronized (queue) {
          newTaskShouldBeScheduled = false;
          queue.clear();
        }
      }
    }

    // 单线程循环执行任务
    private void mainLoop() {
      while (true) {
        try {
          boolean taskExpired = false;
          MyTimerTask task;
          synchronized (queue) {
            while (queue.isEmpty() && newTaskShouldBeScheduled) {
              // 队列为空，且当前运行状态为接收新任务的状态，则阻塞
              queue.wait();
            }
            if (queue.isEmpty()) { // 被唤醒之后，如果发现队列仍然为空，且 newTaskShouldBeScheduled == false，表示 timer 被取消了，则不再等待，结束运行
              break;
            }
            // 队列不为空
            task = queue.getMin();
            long currentTime, taskRunTime;
            synchronized (task.lock) {
              // 获取到任务，然后运行任务
              // 运行任务至少要有 2 大前提条件：1、任务的状态判断，必须不能是 Cancelled，2、任务的到期时间判断，如果没有到期，则需要 wait(timeout)
              // 3、任务到期，判断任务是一次性的还是周期的，一次性的则更改任务的状态为 Executed, 弹出堆顶; 周期的则将任务计算任务的下次运行时间，重新加入堆中
              if (task.state == MyTimerTask.CANCELLED) { // 任务被外部线程取消了。Timer 的实现是更改状态，不是直接在堆中删除，ScheduledThreadPoolExecutor 是直接删除堆中的元素？？
                queue.removeMin();
                continue;
              }
              currentTime = System.currentTimeMillis();
              taskRunTime = task.nextRunTime;
              if (taskExpired = (currentTime >= taskRunTime)) {
                if (task.period == 0) { // 单次的任务
                  task.state = MyTimerTask.EXECUTED;
                  queue.removeMin();
                } else {
                  long nextRunTime = task.period < 0 ? currentTime - task.period // 固定延迟，在上一个任务被执行完后，
                      // 由于是单线程的，该任务的下次执行时间是当前时间往后延迟 period 时长
                      : taskRunTime + task.period; // 固定频率。不论任务是否在固定频率的时间周期内执行万否
                  // 由于单线程执行任务，所以 Timer 在多任务的情况下，不是特别准时
                  queue.rescheduleMin(nextRunTime);
                }
              }
            }
            // 释放 task.lock 锁之后，判断是否需要等待。如果不是释放锁之后修改，容易引起死锁。
            if (!taskExpired) {
              // 时间没到继续 await
              queue.wait(taskRunTime - currentTime);
            }
          }
          // 释放 queue 锁之后，判断实现需要执行队列
          if (taskExpired) {
            // 执行任务
            task.run();
          }
        } catch (InterruptedException e) {

        }
      }
    }
  }


  /**
   * 基于二叉堆的优先级队列
   */
  class TaskQueue {
    private MyTimerTask[] queue = new MyTimerTask[128];
    private int size;

    // 队列的 api 主要是 增删改查

    // 增
    void add(MyTimerTask task) {
      // 第 0 位的元素不使用，所以使用 size+1 判断。length = 16，则能使用的元素位置为 1...15,size == 15 的时候，数组就满了，需要扩容
      if (size + 1 >= queue.length) {
        resize();
      }
      queue[++size] = task;
      swimUp(size);
    }

    private void resize() {
      int newLength, oldLen = queue.length;
      if (oldLen > Integer.MAX_VALUE >> 1) {
        newLength = Integer.MAX_VALUE;
      } else {
        newLength = oldLen << 1;
      }
      queue = Arrays.copyOf(queue, newLength);
    }

    // 删

    void removeMin() {
      // 直接将堆顶元素替换
      queue[1] = queue[size];
      queue[size] = null;
      size--;
      sinkDown(1);
    }

    void clear() {
      for (int i = 1; i <= size; i++) {
        queue[i] = null;
      }
      size = 0;
    }

    // 改

    // 将堆顶的任务，如果是重复型的任务，则再次扔入队列中
    // 这里有取巧的操作，直接将堆顶的元素给更改了，然后重新 sink 到堆底
    void rescheduleMin(long newTime) {
      queue[1].nextRunTime = newTime;
      sinkDown(1);
    }


    // 查
    MyTimerTask getMin() {
      return queue[1];
    }

    boolean isEmpty() {
      return size == 0;
    }

    MyTimerTask get(int i) {
      if (i < 1 || i > size) {
        throw new IllegalArgumentException(String.format("i should be between 1 and %s. [i=%d]", size, i));
      }
      return queue[i];
    }

    // 辅助方法

    /**
     * 从 k 位置开始向上浮
     *
     * @param k
     */
    void swimUp(int k) {
      int p;
      while (k > 1) {
        p = parent(k);
        if (queue[p].nextRunTime >= queue[k].nextRunTime) {
          swap(p, k);
          k = p;
        } else { // 父节点的 nextRuntTime < k 的 nextRunTime
          break;
        }
      }
    }

    void sinkDown(int k) {
      int child;
      while (k <= size && (child = child(k)) <= size && child > 0) { // 防止 child 溢出。。。
        // 有 右子节点且右子节点更小
        if (child + 1 <= size && queue[child].nextRunTime > queue[child + 1].nextRunTime) {
          child = child + 1;
        }
        // 找到正确位置
        if (queue[k].nextRunTime <= queue[child].nextRunTime) {
          break;
        }
        // 否则父子节点交换，比表示下沉操作
        swap(k, child);
        k = child;
      }
    }

    void swap(int x, int y) {
      MyTimerTask temp = queue[x];
      queue[x] = queue[y];
      queue[y] = temp;

    }

    int parent(int k) {
      return k >>> 1;
    }

    int child(int k) {
      return k << 1;
    }

    // 初始化一个堆。也叫建堆。
    void heapify() {
      for (int i = size / 2; i >= 1; i--) {
        sinkDown(i);
      }
    }
  }
}
