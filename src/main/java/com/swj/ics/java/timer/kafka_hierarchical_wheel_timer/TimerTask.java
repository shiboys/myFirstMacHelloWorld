package com.swj.ics.java.timer.kafka_hierarchical_wheel_timer;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2024/06/13 21:08
 */
public abstract class TimerTask implements Runnable {

  private final long delayMs;

  TimerTaskEntry timerTaskEntry;

  public synchronized void setTimerTaskEntry(TimerTaskEntry entry) {
    if (this.timerTaskEntry != null) {
      this.timerTaskEntry.remove();
    }
    this.timerTaskEntry = entry;
  }

  public TimerTask(long delayMs) {
    this.delayMs = delayMs;
  }

  public synchronized void cancel() {
    if (this.timerTaskEntry != null) {
      this.timerTaskEntry.remove();
    }
    // 将任务跟 entry 的引用关系 去掉，这个会被 entry.cancelled() 判断用到
    this.timerTaskEntry = null;
  }

  public long getDelayMs() {
    return delayMs;
  }
}
