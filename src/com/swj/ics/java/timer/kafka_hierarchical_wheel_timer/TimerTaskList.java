package com.swj.ics.java.timer.kafka_hierarchical_wheel_timer;

import static com.swj.ics.java.timer.kafka_hierarchical_wheel_timer.TimerConstant.currentNanoMs;

import com.google.common.base.Preconditions;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2024/06/13 21:17
 * TimerTaskEntry 所在的链表
 */
public class TimerTaskList implements Delayed {

  private final TimerTaskEntry head;
  private final AtomicInteger taskCounter;

  private AtomicLong expiration = new AtomicLong(-1);

  public TimerTaskList(AtomicInteger taskCounter) {
    this.taskCounter = taskCounter;
    this.head = new TimerTaskEntry(null, -1);
    head.next = head;
    head.prev = head;
  }

  public long getExpiration() {
    return expiration.get();
  }

  /**
   * 设置链表广义上的过期时间
   *
   * @param expiration 过期时间
   * @return true 表示过期时间发生了更改，false 未发生更改。
   */
  public boolean setExpiration(long expiration) {
    // expiration  跟之前的值不相等，则说明 expiration 发生了更改
    return this.expiration.getAndSet(expiration) != expiration;
  }

  /**
   * 将 entry 从链表中删除
   *
   * @param entry
   */
  public synchronized void remove(TimerTaskEntry entry) {
    synchronized (entry) {// 只能单线程操作这个方法，且单线程操作当前节点
      // 只有属于当前链表的节点，才能被当前链表删除
      if(entry.taskList == this) {
        TimerTaskEntry prev = entry.prev;
        TimerTaskEntry next = entry.next;
        if (prev != null) {
          prev.next = next;
        }
        if (next != null) {
          next.prev = prev;
        }

        entry.prev = null;
        entry.next = null;
        // 取消 taskList 属性, 跟设置此属性保持一致，成闭环
        entry.taskList = null;

        // 减少计数器
        taskCounter.decrementAndGet();
      }

    }

  }

  public synchronized void add(TimerTaskEntry entry) {
    Preconditions.checkNotNull(entry);

    TimerTaskEntry tail = head.prev;
    entry.next = head;
    entry.prev = tail;
    head.prev = entry;
    if (tail != null) {
      tail.next = entry;
    }

    // 设置所属 taskList
    entry.taskList = this;

    // 增加计数
    taskCounter.incrementAndGet();
  }

  public void flush(Consumer<TimerTaskEntry> func) {
    TimerTaskEntry p, next;
    p = head.next;
    while (p != head) {
      next = p.next;
      // 先把 p 从链表中移除
      remove(p);
      // 然后调用 f(x) 函数重新加入链表中，在(fx) 中判断 entry 是否过期，是否被取消，或者当前 entry 该降级到其他链表中，或者当前 entry 重新加入当前链表中
      // 就跟初高中的时候，升级调整班级一模一样，该毕业的毕业(自己毕业 timeout，家里不让上cancelled), 该升级升级(时间轮到更低层级)，
      // 该留级留级(班主任一换，还是熟悉的班级 list，还是熟悉的位置, 只是 list 是新一轮的list，班级是新一年的班级)
      func.accept(p);
      p = next;
    }
    // flush 完成之后，当前整个链表都被处理一遍，则 过期时间要重置，需要等新的过期时间来设置，就跟教室需要新的班主任来认领一样
    expiration.set(-1);
  }

  @Override
  public long getDelay(TimeUnit unit) {
    long delay = Math.max(0, expiration.get() - currentNanoMs());
    return unit.convert(delay, TimeUnit.MILLISECONDS);
  }

  @Override
  public int compareTo(Delayed o) {
    TimerTaskList that = (TimerTaskList) o;
    return Long.compare(expiration.get(), that.expiration.get());
  }
}
