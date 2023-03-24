package com.swj.ics.java.juc;

import java.util.concurrent.locks.Lock;
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
}
