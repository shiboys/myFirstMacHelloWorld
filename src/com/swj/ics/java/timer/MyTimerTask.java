package com.swj.ics.java.timer;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2024/03/22 22:15
 */
public abstract class MyTimerTask implements Runnable {

  static final int VIRGIN = 0;
  // 重复型任务，加入队列后状态就是 scheduled
  static final int SCHEDULED = 1;
  // 单次任务，执行完成之后，状态就是 EXECUTED
  static final int EXECUTED = 2;
  static final int CANCELLED = 3;

  int state = VIRGIN;

  // 因为 Timer 的优先级队列/二叉堆是普通的数组，不是线程安全的
  // 任何线程都能访问到这个 Task ，但是只能有一个线程能更改当前 Task 的状态 ，就是获取 lock 锁的 线程。
  final Object lock = new Object();

  // 下次执行时间
  long nextRunTime;

  // 时间周期
  long period = 0;

  public abstract void run();

  public boolean cancel() {
    synchronized (lock) {
      boolean result = (state == SCHEDULED);
      state = CANCELLED;
      // 返回是是调度类型的任务
      return result;
    }
  }

  /**
   * 获取下次被调度的时间，注意这里并不是下次任务被执行的时间
   *
   * @return
   */
  public long scheduledExecutionTime() {
    return period < 0 ? nextRunTime + period : nextRunTime - period;
  }

}
