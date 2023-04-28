package com.swj.ics.java.juc.SpinLock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/04/28 16:31
 */
public class TicketLock implements Lock {
  private AtomicInteger queueNumber = new AtomicInteger(0);
  private AtomicInteger currentNumber = new AtomicInteger(0);
  private int count;
  private static ThreadLocal<Integer> threadLocalSeqNumber = new ThreadLocal<>();

  public void lock() {
    Integer seqNumber = threadLocalSeqNumber.get();
    if (seqNumber != null && seqNumber == currentNumber.get()) {
      count++;// 重入
      return;
    }

    seqNumber = queueNumber.getAndIncrement();
    threadLocalSeqNumber.set(seqNumber);
    while (seqNumber != currentNumber.get()) ;
  }

  @Override
  public void lockInterruptibly() throws InterruptedException {

  }

  @Override
  public boolean tryLock() {
    Integer seqNumber = threadLocalSeqNumber.get();
    return (seqNumber != null && seqNumber == currentNumber.get());
  }

  @Override
  public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
    return tryLock();
  }

  public void unlock() {
    Integer seqNumber = threadLocalSeqNumber.get();
    if (seqNumber != null && seqNumber == currentNumber.get()) {
      if (count > 0) {
        count--; // 减少重入次数
      } else {
        threadLocalSeqNumber.remove();
        // 其实这里有隐藏的 bug，如果 increment 的值，没有线程在这个值上监听，那么所有的线程就都阻塞在自旋上
        // 就跟叫号一样，如果某一个号的人不在线程了，叫号系统在交了 n 次以后会叫下一个人，
        // AQS 也有这个设置，如果后面的节点被 Cancel，那么就会一直找到一个正常 Signal 的线程进行唤醒
        // 不过这里为了演示，我们知道意思就行
        currentNumber.incrementAndGet();
      }
    }


  }

  @Override
  public Condition newCondition() {
    throw new UnsupportedOperationException();
  }

  public static void main(String[] args) {
    LockUtil.lockTest(new TicketLock());
  }

}

/**
 * 队列锁
 * SMP 与 NUMA
 * 在进一步介绍队列锁之前，我们先来了解下计算机中长江的两种体系结构，SMP，NUMA
 * SMP(Symmetric Multi Processing) 对称多处理器结构，所谓对称指的是多个 CPU 的地位是平等的，一致的。他们共享全部的支援，包括内存，IO、总线等。
 * 所以如果多个 CPU 同时访问某个资源(例如某部分内存)，就需要锁机制来解决资源争抢的问题。事实上，我们日常所使用的的大部分计算机都是 SMP 结构的
 * 而在 NUMA (Non-Uniform Memory Access) 非一致存储访问结构下，其基本特征是包含多个 CPU 模块。而每个 CPU 模块则是由多个 CPU，独立的本地内存
 * IO 组成。各CPU模块则通过所谓的互联模块进行连接。这样一个CPU访问其自身CPU模块的本地内存的速度将远远高于访问其他CPU模块的本地内存，
 * 即所谓访问存储表现上具有非一致性
 * 
 * CLH 队列锁 是常见的 队列锁。Java 中 AQS 就是用了 CLH 队列锁的变体。在之前的两种自旋锁实现 SpinLock、TicketLock 中，实际上
 * 存在一个明显的性能问题。各线程均在同一个共享变量上自旋，竞争非常激烈时，会导致非常繁重的总线、内存流量。因为当一个线程释放锁、更新共享变量时，会引起其他 CPU
 * 该共享变量对应的 Cache Line(缓存行)失效，并重新经由总线、内存读取
 * 
 * 而 CLH 队列锁则是将各个加锁线程包装成为 Node 节点并构建为一个隐式链表。各 Node 不断自旋检查前驱结点的状态，当某个线程进行解锁时，
 * 则会修改自身 Node 节点的状态让其后继节点结束自旋，获取到锁。当某个线程解锁时，则会修改自身 Node 节点的状态以让其后继节点结束自旋，获取到锁
 * 其实现过程中，之所以成为隐式链表，是因为所谓的链表事实上是通过每个线程利用 ThreadLocal 类型的 prevNode 变量保存其前驱节点来维护的。
 * 由前文可知，各线程是在其前驱节点的 state 变量上进行自旋的，故在构建 CLH 队列锁的实例过程中首先向隐式链表添加了一个哨兵节点，
 * 以便处理第一个加锁。值得一提的是，在解锁时，通过执行 currentNode.remove() 是必须的。其目的是为了保证该线程在下一次加锁调用 lock() 时
 * 能够通过 currentNode.get() 获取到一个新的 Node 实例，防止死锁。至此可以看出 CLH 队列是一个公平的自旋锁实现，其次由于各线程自旋在不同的 Node 实例的
 * status 变量上避免了繁重的总线，内存流量
 */
