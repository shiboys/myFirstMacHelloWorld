package com.swj.ics.java.timer.kafka_hierarchical_wheel_timer;

import java.util.concurrent.TimeUnit;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2024/06/13 21:48
 */
public final class TimerConstant {

  public static final long currentNanoMs() {
    return TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
  }
}
