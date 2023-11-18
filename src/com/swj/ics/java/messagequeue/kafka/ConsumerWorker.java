package com.swj.ics.java.messagequeue.kafka;

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
 * @since 202 3/11/14 14:37
 * 消费业务类
 */
public class ConsumerWorker {
  /**
   * 该类主要是消费 ConsumerRecord 的业务处理类，消费的 records 是一个 topic partition 的
   * 同时该类由于存在被多个线程同时使用的情况，因此存在线程安全的问题
   * 最后该类需要提供 如下几个 api
   * 1、主逻辑 run 方法，负责消费消息处理业务逻辑，在退出业务逻辑的时候返回是否已完成消费的最大位移
   * 2、最终的消费位移 latestOffset 方法
   * 3、同时需要接收外部开关指令 close()，收到外部指令后，需要停止任何消息的消费
   * 4、提供查询是否已经完成消息业务逻辑处理的方法 completed，告知外界当前的任务是否已经完成
   * 5、提供一个紧急返回 api -waitForCompletion，在分区重分配的时候，能够在有限的等待时间内返回已经处理的消息的 offset
   */

  private static final long INVALID_START_OFFSET = -1;
  private volatile boolean started = false;
  private volatile boolean closed = false;

  private CompletableFuture<Long> completableFuture = new CompletableFuture<>();
  private final AtomicLong latestOffset = new AtomicLong(INVALID_START_OFFSET);
  private final ReentrantLock lock = new ReentrantLock();
  private List<ConsumerRecord<String, String>> consumerRecordList;

  public ConsumerWorker(List<ConsumerRecord<String, String>> consumerRecordList) {
    this.consumerRecordList = consumerRecordList;
  }

  public boolean run() {
    //处理多线程逻辑，这里感觉不会有多线程的问题，一会试试吧
    lock.lock();
    try {
      if (closed) {
        return false;
      }
      started = true;
    } finally {
      lock.unlock();
    }
    long oldOffset = 0;
    for (ConsumerRecord<String, String> record : consumerRecordList) {
      if (closed) {
        break;
      }
      handle(record);
      oldOffset = latestOffset.get();
      while (oldOffset < record.offset() + 1 && !latestOffset.compareAndSet(oldOffset, record.offset() + 1)) {
        oldOffset = latestOffset.get();
      }
    }
    System.out.println(
        String.format("consume message. thread:%s, offset:%d", Thread.currentThread().getName(), latestOffset.get()));
    return completableFuture.complete(latestOffset.get());
  }

  private void handle(ConsumerRecord<String, String> record) {
    // 模拟业务处理
    try {
      Thread.sleep(ThreadLocalRandom.current().nextInt(10));
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public long getLatestOffset() {
    return latestOffset.get();
  }

  public void close() {
    // lock 感觉有点多余
    lock.lock();
    try {
      closed = true;
      // 这里需要跟 run 的加锁逻辑形成闭环，设置 completableFuture 的值
      if (!started) {
        // complete 方法使用了 cas 的方式变更 value，别的线程也一定能感知到
        completableFuture.complete(INVALID_START_OFFSET);
      }
    } finally {
      lock.unlock();
    }
  }

  public boolean isCompleted() {
    return completableFuture.isDone();
  }

  public long waitForCompletion() {
    try {
      return completableFuture.get(1, TimeUnit.SECONDS);
    } catch (InterruptedException e) { // 被别的线程中断
      Thread.currentThread().interrupt();// 中断自己
    } catch (Exception e) {
      e.printStackTrace();
    }
    return INVALID_START_OFFSET;
  }
}
