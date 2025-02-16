package com.swj.ics.java.timer.kafka_hierarchical_wheel_timer;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2024/06/13 21:08
 */
public class TimerTaskEntry implements Comparable<TimerTaskEntry> {

  TimerTask timerTask;
  // 将延迟时间换算成 entry 的超时时间
  final long expirationMs;

  TimerTaskEntry prev;
  TimerTaskEntry next;

  // 因为 taskEntry 会在不同的 list 之间跳转/降级，所以这里使用 volatile 修饰
  // 因此不适合于构造函数传入，适合于外部更改
  volatile TimerTaskList taskList;

  public TimerTaskEntry(TimerTask timerTask, long expirationMs) {
    this.timerTask = timerTask;
    if (timerTask != null) {
      // 双向引用特别容易让人忘记
      timerTask.setTimerTaskEntry(this);
    }
    this.expirationMs = expirationMs;
  }

  public boolean cancelled() {
    // 这反向引用绝了。这也从侧面说明 timerTask 即使被删除，引用也不会被设置为 null
    return timerTask.timerTaskEntry != this;
  }

  public void remove() {
    TimerTaskList currentList = this.taskList;
    while (currentList != null) {
      currentList.remove(this);
      // 重置当前变量
      currentList = this.taskList;
    }
  }

  @Override
  public int compareTo(TimerTaskEntry o) {
    return Long.compare(expirationMs, o.expirationMs);
  }
}
