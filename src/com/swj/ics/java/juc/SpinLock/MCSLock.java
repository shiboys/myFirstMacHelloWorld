package com.swj.ics.java.juc.SpinLock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/04/28 18:49
 */
public class MCSLock implements Lock {

  private ThreadLocal<Node> threadLocalCurrNode = ThreadLocal.withInitial(Node::new);

  private AtomicReference<Node> tail = new AtomicReference<>(null);

  @Override
  public void lock() {
    Node currentNode = threadLocalCurrNode.get();
    Node prevNode = tail.getAndSet(currentNode);
    if (prevNode != null) {
      currentNode.status = true;
      prevNode.next = currentNode;
    }
    while (currentNode.status) ;
  }

  @Override
  public void unlock() {
    Node currentNode = threadLocalCurrNode.get();
    Node nextNode = currentNode.next;
    if (nextNode != null) {
      nextNode.status = false;
      currentNode.next = null;
    } else { // nextNode == null
      // 设置尾结点为 null
      if (!tail.compareAndSet(currentNode, null)) {
        // 如果使用 CAS 将 tail 节点改为 null 失败，则说明多线程，比如线程 B，更改了尾结点指针，也就是执行了 22 行的代码
        // 这里等待其他线程 B 执行25 行的代码
        while (currentNode.next == null) ;
      }
    }
    threadLocalCurrNode.remove();
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
    return null;
  }



  private static class Node {
    private volatile boolean status = false;
    private volatile Node next;
  }

  public static void main(String[] args) {
    // LockUtil.lockTest(new MCSLock(), 10);
    System.out.println(-1 / 2);
    System.out.println(-1 >> 1);
    System.out.println(4 >> 1);
    System.out.println(4 >> 1 >> 1);
  }
}
