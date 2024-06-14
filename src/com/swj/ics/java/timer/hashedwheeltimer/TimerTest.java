package com.swj.ics.java.timer.hashedwheeltimer;

import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2024/05/19 22:18
 */
@Slf4j
public class TimerTest {

  static AtomicInteger counter = new AtomicInteger();
  public static void main(String[] args) throws InterruptedException {
    HashedWheelTimer timer = new HashedWheelTimer();
    Timeout timeout1 = timer.newTimeout(timeout -> {
      info();
    }, 10, TimeUnit.SECONDS);
    // 先尝试取消 第一个 任务
    timeout1.cancel();

    // 1 秒后启动任务 2
    timer.newTimeout(timeout -> {
      try {
        info();
        Thread.sleep(5000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }, 1, TimeUnit.SECONDS);

    // 3 秒后启动 任务3
    timer.newTimeout(timeout -> {
      info();
    },3,TimeUnit.SECONDS);

    Thread.sleep(15_000);
    timer.stop();
    // 打印结果如下:
    /**
     * [pool-1-thread-1] INFO com.swj.ics.java.timer.hashedwheeltimer.TimerTest - counter is 1, time is 2024-05-19 10:39:07
     * [pool-1-thread-1] INFO com.swj.ics.java.timer.hashedwheeltimer.TimerTest - counter is 2, time is 2024-05-19 10:39:12
     * 虽然任务2 和任务 3 提交的 delay 相差 2 秒，但是任务 2 sleep 了 5 秒，最终倒是处理 任务2和任务 3 查了 5秒，说明 timer 的 worker 线程确实是单线程的
     */
  }

  private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
  private static void info() {
    //System.out.println(String.format("counter is %d, time is %s",counter.incrementAndGet(),sdf.format(new Date())));
    log.info("counter is {}, time is {}",counter.incrementAndGet(),sdf.format(new Date()));
  }
}
