package com.swj.ics.java.timer.hashedwheeltimer;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2024/05/17 20:11
 * 定时任务 TimerTask 的封装类，就像 Netty 中 HandlerContext 对 Handler 的封装一样。
 * 同时 Timeout 也用了了 Timer 对象，提供了定时任务的简单管理
 * 比如 任务是否已过期，是否已取消，取消任务等基本操作
 */
public interface Timeout {
  Timer timer();

  TimerTask task();

  boolean isExpired();

  boolean isCancelled();

  boolean cancel();
}
