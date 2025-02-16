package com.swj.ics.java.timer.kafka_hierarchical_wheel_timer;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2024/06/14 12:34
 */
public class SystemTimer implements Timer {

  private final AtomicInteger taskCounter = new AtomicInteger(0);
  private final DelayQueue<TimerTaskList> delayQueue = new DelayQueue<>();
  private final TimingWheel timingWheel;
  private final String executorName;
  private final ExecutorService threadPool = Executors.newFixedThreadPool(1, new ThreadFactory() {
    @Override
    public Thread newThread(Runnable r) {
      Thread t = new Thread(r, executorName + "-Kafka-Hierarchical-Timer");
      t.setDaemon(false);
      return t;
    }
  });
  private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
  private final Lock readLock = readWriteLock.readLock();
  private final Lock writeLock = readWriteLock.writeLock();

  public SystemTimer(String executorName, long tickMs, int wheelSize) {
    this.executorName = executorName;
    timingWheel = new TimingWheel(tickMs, wheelSize, TimerConstant.currentNanoMs(), taskCounter, delayQueue);
  }

  @Override
  public int size() {
    return taskCounter.get();
  }

  @Override
  public void add(TimerTask timerTask) {
    // 防止写锁工作，同时多线程可以并发地加读锁，进而并发地添加超时任务
    readLock.lock();
    try {
      addTimerTaskEntry(new TimerTaskEntry(timerTask, TimerConstant.currentNanoMs() + timerTask.getDelayMs()));
    } finally {
      readLock.unlock();
    }
  }

  private void addTimerTaskEntry(TimerTaskEntry entry) {
    boolean added = timingWheel.add(entry);
    if (!added) { // 添加任务失败
      // entry 没有被 取消，则说明 entry 超时了, 超时的话，提交异步线程池执行
      if (entry != null && !entry.cancelled()) {
        threadPool.submit(entry.timerTask);
      }
    }
    // 添加到 时间轮成功，则不处理
  }

  @Override
  public void shutdown() {
    threadPool.shutdown();
  }

  @Override
  public boolean advanceClock(long timeoutMs) {
    try {
      // delayQueue 是线程安全的
      TimerTaskList bucket = delayQueue.poll(timeoutMs, TimeUnit.MILLISECONDS);
      if (bucket == null) { // 队列为空或者超时
        return false;
      }
      writeLock.lock();
      try {
        while (bucket != null) {
          // 将时间轮推进到 bucket 的过期时间
          timingWheel.advanceClock(bucket.getExpiration());
          // 执行 bucket 的过期操作 flush
          bucket.flush(this::addTimerTaskEntry);
          bucket = delayQueue.poll();
        }
      } finally {
        writeLock.unlock();
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return false;
  }
}
