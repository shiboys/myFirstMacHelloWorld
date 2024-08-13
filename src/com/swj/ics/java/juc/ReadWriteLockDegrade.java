package com.swj.ics.java.juc;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/03/12 17:03
 * 读写锁->锁降级示例
 */
public class ReadWriteLockDegrade {
  private volatile boolean update;
  private Lock readLock;
  private Lock writeLock;

  public ReadWriteLockDegrade(ReentrantReadWriteLock readWriteLock) {
    this.readLock = readWriteLock.readLock();
    this.writeLock = readWriteLock.writeLock();
  }

  public boolean isUpdate() {
    return update;
  }

  public void setUpdate(boolean update) {
    this.update = update;
  }

  public void processData() {
    readLock.lock();

    if (!update) {
      // 必须先释放读锁
      readLock.unlock();
      // 锁降级从获取写锁开始
      writeLock.lock();
      try {
        if (!update) {
          //准备数据的流程。。。
          update = true;// 更改标识
        }
        readLock.lock();
      } finally {
        writeLock.unlock();
      }
    }
    try {
       // 使用数据的流程
    } finally {
      readLock.unlock();
    }
  }

  static void testLock() throws Exception {
    ReentrantLock lock = new ReentrantLock();
    Condition cd =  lock.newCondition();
    System.out.println("直接调用 cd.await");
    cd.await(1, TimeUnit.SECONDS);
    System.out.println("调用结束");
  }

  static void testNotifyAll(){
    ReentrantLock lock = new ReentrantLock();
    Condition cd =  lock.newCondition();
    System.out.println("直接调用 cd.await");
    // 这里调用的是 cd 的 notifyAll() 属于 object 的方法，而不是 cd.singalAll();
    // 经测试发现， 在未获取锁的情况直接调用 notifyAll 会抛出 native java.lang.IllegalMonitorStateException
    cd.notifyAll();
    System.out.println("调用结束");
  }

  public static void main(String[] args) {
    testNotifyAll();
  }
}
