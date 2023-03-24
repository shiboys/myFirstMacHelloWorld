package com.swj.ics.java.juc;

import java.util.concurrent.locks.StampedLock;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/03/14 12:32
 * 基于乐观锁的类似于 MVCC 的多版本并发控制
 */
public class StampLockTest {

  /**
   * 例子使用官方的
   */
  class Point {
    public double x;
    public double y;
    private StampedLock stampedLock = new StampedLock();

    public void move(double deltaX, double deltaY) {
      long stamp = stampedLock.writeLock();
      try {
        x += deltaX;
        y += deltaY;
      } finally {
        stampedLock.unlockWrite(stamp);
      }
    }

    public double distanceFromOriginal() {
      // 使用乐观读
      long stamp = stampedLock.tryOptimisticRead();
      // 将共享变量拷贝到线程栈
      double currentX = x;
      double currentY = y;
      // 读的期间有其他线程修改数据，读到的是脏数据，则放弃，重新升级锁为悲观锁
      if (!stampedLock.validate(stamp)) {
        stamp = stampedLock.readLock();
        try {
          currentX = x;
          currentY = y;
        } finally {
          stampedLock.unlockRead(stamp);
        }
      }
      return Math.sqrt(currentX * currentX + currentY * currentY);
    }
  }
}
