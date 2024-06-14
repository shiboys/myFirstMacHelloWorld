package com.swj.ics.java.timer.hashedwheeltimer;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2024/05/17 20:10
 */
public interface TimerTask {
  /**
   * 运行到期的任务
   * @param timeout 任务的句柄
   */
  void run(Timeout timeout);
}
