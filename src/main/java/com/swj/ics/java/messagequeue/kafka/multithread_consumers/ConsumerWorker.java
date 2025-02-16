package com.swj.ics.java.messagequeue.kafka.multithread_consumers;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2024/06/15 22:06
 * ConsumerWorker 重写
 * ConsumerWorker 的设计原则是，一个实例负责消费一个 分区的一批消息，消费完，使用 future 记录这批记录的最大 offset,
 * 这个 ConsumerWorker 的使命就完成了，可以从容器中移除；同时该实例也可以看做是一个 runnable 对象
 * 只是没有明确地重写 run 方法。使用 CompletableFuture.supplyAsync(worker::run, executor) 的静态方式来提交到线程池
 */
public class ConsumerWorker {

  private final List<ConsumerRecord<String, String>> onePartitionRecords;
  private ReentrantLock lock;

  private volatile boolean started = false;
  private volatile boolean stopped = false;
  private final static long INVALID_COMMITTED_OFFSET = -1;
  // 使用 该变量来保存 Worker 当前已消费的最新位移
  private final AtomicLong currentLatestOffset = new AtomicLong(INVALID_COMMITTED_OFFSET);
  // 使用 CompletableFuture 来保存 Worker 要提交的位移。
  private final CompletableFuture<Long> offsetFuture = new CompletableFuture<>();
  private final AtomicLong consumedMessageCounter;

  public ConsumerWorker(
      List<ConsumerRecord<String, String>> onePartitionRecords, AtomicLong consumedMessageCounter) {
    this.onePartitionRecords = onePartitionRecords;
    this.consumedMessageCounter = consumedMessageCounter;
  }

  /**
   * @return worker 成功与非的标志是看 future 能否将 currentLatestOffset 值封装到结果中
   */
  public boolean run() {
    if (stopped)
      return false;

    // 会存在多线程运行同一个 ConsumerWorker ?
    lock.lock();
    started = true;
    lock.unlock();

    for (ConsumerRecord<String, String> record : onePartitionRecords) {
      if (stopped) {
        break;
      }
      handleRecord(record);
      // 设置当前分区当前消费集合的最大 offset
      if (record.offset() + 1 > currentLatestOffset.get()) {
        currentLatestOffset.set(record.offset() + 1);
      }
    }
    return offsetFuture.complete(currentLatestOffset.get());
  }

  public long getLastedOffset() {
    return currentLatestOffset.get();
  }

  /**
   * 模拟 10 毫秒左右处理一条消息。
   *
   * @param record
   */
  private void handleRecord(ConsumerRecord<String, String> record) {
    try {
      Thread.sleep(ThreadLocalRandom.current().nextInt(10));
      consumedMessageCounter.incrementAndGet();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    System.out.println(
        Thread.currentThread().getName() + " finished message process. record offset is " + record.offset());
  }

  public void close() {
    lock.lock();
    if (!started) {
      offsetFuture.complete(currentLatestOffset.get());
    }
    stopped = true;
    lock.unlock();
  }


  public boolean isFinished() {
    return offsetFuture.isDone();
  }

  public long awaitForCompletion(long timeout, TimeUnit unit) {
    try {
      return offsetFuture.get(timeout, unit);
    } catch (Exception e) {
      if (e instanceof InterruptedException) {
        Thread.currentThread().interrupt();
      }
      return INVALID_COMMITTED_OFFSET;
    }
  }

}
