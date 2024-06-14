package com.swj.ics.java.timer.kafka_hierarchical_wheel_timer;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2024/06/14 12:32
 */
public interface Timer {

  int size();

  void add(TimerTask timerTask);

  void shutdown();

  /**
   * 推动时间轮
   * @param timeoutMs 向前推荐 多少 毫秒
   * @returntrue 表示有任务可执行，false 表示无任务可执行，空转
   */
  boolean advanceClock(long timeoutMs);
}
