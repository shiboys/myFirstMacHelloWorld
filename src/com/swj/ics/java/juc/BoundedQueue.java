package com.swj.ics.java.juc;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/03/12 18:41
 * 利用 Lock+Condition 来实现简易版本的 ArrayBlockingQueue
 */
public class BoundedQueue<T> {
  private Lock lock;
  private Condition notFull;
  private Condition notEmpty;
  private int putIndex;
  private int removeIndex;
  private int count;
  private Object[] items;

  public BoundedQueue(int capacity) {
    if (capacity < 1) {
      throw new IllegalArgumentException();
    }
    items = new Object[capacity];
    lock = new ReentrantLock();
    notFull = lock.newCondition();
    notEmpty = lock.newCondition();
  }

  public void put(T item) throws InterruptedException {
    lock.lock();
    try {
      while (count == items.length) {
        notFull.await();
      }
      items[putIndex] = item;
      if (++putIndex >= items.length) {
        putIndex = 0;
      }
      count++;
      notEmpty.signal();
    } finally {
      lock.unlock();
    }
  }

  public T remove() throws InterruptedException {
    lock.lock();
    try {
      while (count == 0) {
        notEmpty.await();
      }
      T item = (T) items[removeIndex];
      if (++removeIndex >= items.length) {
        removeIndex = 0;
      }
      count--;
      notEmpty.signal();
      return item;
    } finally {
      lock.unlock();
    }
  }
}
