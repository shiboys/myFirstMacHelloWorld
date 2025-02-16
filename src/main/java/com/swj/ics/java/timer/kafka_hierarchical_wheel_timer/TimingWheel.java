package com.swj.ics.java.timer.kafka_hierarchical_wheel_timer;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2024/06/14 11:22
 * Kafka 时间轮最重要的一个数据结构，时间轮。使用链表来实现时间轮的层级时间轮概念
 * 最重要的时间轮类
 * 根据时间轮的设计文档
 * 时间轮的主要成员有 tickMs, wheelSize, buckets, nextTimingWheel,
 * currentTimeMs
 * 主要方法有 addTimerTask, advanceClock 驱动时间轮
 */
public class TimingWheel {
  private final long tickMs;
  private final int wheelSize;
  private final long wholeWheelDurationMs;
  private final TimerTaskList[] buckets;
  private final AtomicInteger taskCounter;

  private long currentTimeMs;
  private final DelayQueue<TimerTaskList> delayQueue;


  // overflowWheel 在调用 add 方法的时候，可能会被两个线程并发的更新或者读取
  // 因此它需要使用 Double-Check 方式的 JVM 检查，确保线程安全
  private volatile TimingWheel overflowWheel;


  /**
   * @param tickMs
   * @param wheelSize
   * @param nowMs       当前时间，以 system.nanoSeconds() 纳秒为单位换算的当前时间
   * @param taskCounter 任务计数器，由外部传入，因为存在多个时间轮，因此所有的时间轮任务都需要由统一的 taskCounter 统计
   * @param delayQueue  由外部传入的延迟队列，管理所有时间轮中的所有 TimerTaskList 链表，理论上是将链表的第一个元素的超时时间 作为 delayed 元素放入 delayQueue
   *                    而非将所有的 TimerTaskEntry 元素都放入 delayQueue 中，如果是这样的话，就背离了 TimerWheel 的设计初衷：延迟元素增加和删除，在 O(1) 的时间内完成
   *                    额热切不存在时间轮的空推进，而 DelayQueue 的存取元素的时间复杂度为 O(logN)，百万甚至千万级别的超时任务，DelayQueue存取至少遍历 10 次以上，性能下降严重
   *                    这里使用 DelayQueue 只将链表对象的 expirationMs 代表的超时时间入 延迟队列，大大减少了元素的个数，同时利用 delayQueue 的线程阻塞功能减少了时间轮空轮询的问题
   *                    而元素出队列的时候，表示元素已超时，说明该元素素在的链表的所有元素也即将超时，因为同一个链表封装的元素超时时间是相近的
   */
  public TimingWheel(long tickMs, int wheelSize, long nowMs, AtomicInteger taskCounter,
      DelayQueue<TimerTaskList> delayQueue) {
    this.tickMs = tickMs;
    this.wheelSize = wheelSize;
    this.delayQueue = delayQueue;
    this.taskCounter = taskCounter;
    wholeWheelDurationMs = tickMs * wheelSize;
    buckets = new TimerTaskList[wheelSize];
    for (int i = 0; i < wheelSize; i++) {
      buckets[i] = new TimerTaskList(taskCounter);
    }
    // 当前时间为 nowMs 的 floor(tickMs) 方式
    this.currentTimeMs = (nowMs / tickMs) * tickMs;
  }

  public boolean add(TimerTaskEntry entry) {
    long expirationMs;
    if (entry == null || entry.cancelled()) {
      return false;
    } else if ((expirationMs = entry.expirationMs) < currentTimeMs + tickMs) { // 超时时间已到，不能再被加入到时间轮
      return false;
    } else if (expirationMs < currentTimeMs + wholeWheelDurationMs) {
      // 在本时间轮之内
      long calculatedIndex = expirationMs / tickMs;
      int bucketIndex = (int) (calculatedIndex % wheelSize);
      TimerTaskList bucket = buckets[bucketIndex];
      bucket.add(entry);
      // 计算 bucket 的过期时间是否被更改, 如果被更改，说明是新的一轮时间周期，需要重新进入延迟队列，等到超时时间后出队列
      // taskEntry  的 expiration 变了，则 entry 所在的 bucket 也需要判断超时时间变了没有
      // 因为同一个 bucket 中的时间都是相近的，比如在 tickMs 为 20ms 的时间论中，wheelSize 为 20 的时间轮中
      // 280-400 之间的所有 entry 都在同一个桶中，而他们 bucket的 超时时间均为 380，因此 如果 setExpiration 能成功，
      // 说明时间轮次发生了变化，比如现在变为时间过了 780ms，则需要重新如队列

      //if (bucket.setExpiration(expirationMs)) {
      // 上述设置超时时间是错误的，不是设置为过期的 expirationMs, 而是应该为 expirationMs 抹平后的值，
      // 也就是 floor(expirationMs) = calculatedIndex * tickMs, 因为时间轮现在走到 floor(expirationMs) 了
      // 比如说 tickMs = 20, expirationMs = 173 ，那 bucket 就是第 9 个桶，index 为 8。
      // 那么 index = 8 的 桶的 expirationMs = 20*8 = 160
      if (bucket.setExpiration(calculatedIndex * tickMs)) { // 桶的到期时间要跟时间轮的一致。时间轮的到期时间在 advanceClock 方法中
        delayQueue.offer(bucket);
      }
      return true;
    } else { // 不在本轮时间轮范围内，递归上级时间轮
      if (overflowWheel == null) {
        initOverflowWheel();
      }
      return overflowWheel.add(entry);
    }
  }

  private void initOverflowWheel() {
    synchronized (this) {
      if (overflowWheel == null) {
        overflowWheel = new TimingWheel(wholeWheelDurationMs, wheelSize, currentTimeMs, taskCounter, delayQueue);
      }
    }
  }

  public void advanceClock(long nowMs) {
    // 至少过了一个 tickMs
    if (nowMs >= this.currentTimeMs + tickMs) {
      this.currentTimeMs = tickMs * (nowMs / tickMs);
      if (overflowWheel != null) {
        overflowWheel.advanceClock(this.currentTimeMs);
      }
    }
  }


}
