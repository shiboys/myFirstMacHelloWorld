package com.swj.ics.jvm;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/04/22 11:07
 * 用 jstack 来查看死锁
 */
public class JStackAndDeadLock {
  static ReentrantLock southLock = new ReentrantLock();
  static ReentrantLock northLock = new ReentrantLock();


  public static class DeadLockThread extends Thread {
    protected Lock lockObject;

    public DeadLockThread(Lock lock) {
      lockObject = lock;
      if (lock == southLock) {
        this.setName("south");
      } else if (lock == northLock) {
        this.setName("north");
      }
    }

    @Override
    public void run() {
      if (lockObject == southLock) {
        lockSouth();
      } else if (lockObject == northLock) {
        lockNorth();
      }
    }

    private void lockSouth() {
      try {
        southLock.lock(); // 先占用 south
        try {
          Thread.sleep(500); // 等待 north 启动
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        northLock.lock(); // 在占用 north，形成死锁的条件1
        System.out.println("car to south has been passed");
      } finally {
        if (southLock.isHeldByCurrentThread()) {
          southLock.unlock();
        }
        if (northLock.isHeldByCurrentThread()) {
          northLock.unlock();
        }
      }
    }

    private void lockNorth() {
      try {
        northLock.lock();
        try {
          Thread.sleep(500);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        southLock.lock();
        System.out.println("car to north has been passed");
      } finally {
        if (southLock.isHeldByCurrentThread()) {
          southLock.unlock();
        }
        if (northLock.isHeldByCurrentThread()) {
          northLock.unlock();
        }
      }
    }
  }

  public static void main(String[] args) throws InterruptedException {
    DeadLockThread car2South = new DeadLockThread(southLock);
    DeadLockThread car2North = new DeadLockThread(northLock);
    car2North.start();
    car2South.start();
    Thread.sleep(1000);
  }
}
