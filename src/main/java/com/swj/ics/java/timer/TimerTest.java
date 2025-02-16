package com.swj.ics.java.timer;

import com.swj.ics.java.timer.kafka_hierarchical_wheel_timer.SystemTimer;
import com.swj.ics.java.timer.kafka_hierarchical_wheel_timer.Timer;
import com.swj.ics.java.timer.kafka_hierarchical_wheel_timer.TimerTask;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2024/04/06 21:22
 */
@Slf4j
public class TimerTest {

  private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

  public static void main(String[] args) throws InterruptedException {
    //invokeNettyTimer();
    testKafkaHierarchicalWheelTimer();
  }

  private static void invokeNettyTimer() throws InterruptedException {
    MyTimer timer = new MyTimer();
    timer.scheduleAtFixedRate(new MyTimerTask() {
      @Override
      public void run() {
        System.out.println(format.format(new Date()));
      }
    }, 100, 5000);

    TimeUnit.MINUTES.sleep(1);
    timer.cancel();
  }


  static void testKafkaHierarchicalWheelTimer() throws InterruptedException {
    Timer timer = new SystemTimer("Test-Kafka-Hierachical-wheel-timer", 20, 20);
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    int cancelledIndex=29;
    TimerTask cancellableTask = new TimerTask(cancelledIndex) {
      @Override
      public void run() {
        log.info(String.format("delayMs is %d and now is %s", getDelayMs(), sdf.format(new Date())));
      }
    };
    for (int i = 0; i < 60; i++) {
      // 每一秒执行一次
      if(i==cancelledIndex-1) {
        timer.add(cancellableTask);
        continue;
      }
      timer.add(new TimerTask((i + 1) * 1000) {
        @Override
        public void run() {
          //System.out.println(String.format("delayMs is %d and now is %s", getDelayMs(), sdf.format(new Date())));

          log.info(String.format("delayMs is %d and now is %s", getDelayMs(), sdf.format(new Date())));
        }
      });
    }

    // 取消可取消的任务，测试已取消的任务是否还会被执行
    cancellableTask.cancel();

    // 每隔 20 毫秒 advanceTimer 推进一下
    try {
      int advanceInterval = 200;
      while (timer.size() > 0) {
        TimeUnit.MILLISECONDS.sleep(advanceInterval);
        timer.advanceClock(advanceInterval);
      }
    } finally {
      System.out.println("timer shutting down...");
      timer.shutdown();
      System.out.println("done");
    }

  }
}
