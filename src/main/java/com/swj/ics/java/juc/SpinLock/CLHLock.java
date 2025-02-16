package com.swj.ics.java.juc.SpinLock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/04/28 17:49
 */
public class CLHLock implements Lock {

  // 隐式链表队尾
  private AtomicReference<Node> tail = new AtomicReference<>(new Node());

  ThreadLocal<Node> threadLocalCurrNode = ThreadLocal.withInitial(Node::new);

  ThreadLocal<Node> threadLocalPrev = new ThreadLocal<>();

  @Override
  public void lock() {
    Node currentNode = threadLocalCurrNode.get();
    currentNode.status = true;
    Node prevNode = tail.getAndSet(currentNode);
    threadLocalPrev.set(prevNode);

    while (prevNode.status) ;// 自旋在 prevNode 的status 上,首节点不自旋，可以获取锁
  }

  @Override
  public void unlock() {
    Node currentNode = threadLocalCurrNode.get();
    currentNode.status = false;

    threadLocalCurrNode.remove();
    threadLocalPrev.remove();

  }

  @Override
  public void lockInterruptibly() throws InterruptedException {

  }

  @Override
  public boolean tryLock() {
    return false;
  }

  @Override
  public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
    return false;
  }



  @Override
  public Condition newCondition() {
    throw new UnsupportedOperationException();
  }



  class Node {
    private volatile boolean status = false;
  }

  public static void main(String[] args) {
    LockUtil.lockTest(new CLHLock(), 10);
  }
}
/**
 * 
 * 前面提到了 CLH 队列锁的各种好出，看上去好像一切都很完美。其实不然，CLH 在 SMP 结构下非常有效。但是在 NUMA 结构中，如果其前驱结点
 * 不是位于该核 CPU 自身模块的本地内存中，而是位于其他 CPU 模块的内存中，则会导致效率大幅减低。故 John M. Mellor-Crummey、Michael L. Scott
 * 提出并发明了 MCS 队列锁。其与 CLH 队列的最大不同在于：MCS 队列中各种加锁线程是在自身的 Node 节点上进行自旋。这一改进使得其在
 * NUMA 结构下也能具有很好的性能表现。具体地，其同样是先将各加锁线程包装成 Node 节点，然后通过 Node 的 next 变量指向其后继节点
 * 并修改后继节点的状态让后继节点结束自旋、获取锁。实现过程如 MCSLock，当然 MCS 队列同样是一个公平的自旋锁
 */