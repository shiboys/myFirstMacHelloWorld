package com.swj.ics.java.juc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/03/12 14:36
 * 公平锁与非公平锁测试，使用 ReentrantLock
 */
public class FairAndNonFairLockTest {

  public static void main(String[] args) throws InterruptedException {
    ReentrantLock2 lock2 = new ReentrantLock2(true);
    for (int i = 0; i < 5; i++) {
      (new Job(lock2, i)).start();
    }

    Thread.sleep(2000);
    System.out.println("start to test nonFair Thread");
    ReentrantLock2 lock22 = new ReentrantLock2(false);
    for (int i = 0; i < 5; i++) {
      (new Job(lock22, i)).start();
    }
    /**
     * 可以看出公平与非公平的线程排队情况
     *current lock is true, current acquired lock thread is 0, and wait thread list are [Thread[1,5,main], Thread[2,5,main]]
     * current lock is true, current acquired lock thread is 1, and wait thread list are [Thread[2,5,main], Thread[3,5,main], Thread[4,5,main], Thread[0,5,main]]
     * current lock is true, current acquired lock thread is 2, and wait thread list are [Thread[3,5,main], Thread[4,5,main], Thread[0,5,main], Thread[1,5,main]]
     * current lock is true, current acquired lock thread is 3, and wait thread list are [Thread[4,5,main], Thread[0,5,main], Thread[1,5,main], Thread[2,5,main]]
     * current lock is true, current acquired lock thread is 4, and wait thread list are [Thread[0,5,main], Thread[1,5,main], Thread[2,5,main], Thread[3,5,main]]
     * current lock is true, current acquired lock thread is 0, and wait thread list are [Thread[1,5,main], Thread[2,5,main], Thread[3,5,main], Thread[4,5,main]]
     * current lock is true, current acquired lock thread is 1, and wait thread list are [Thread[2,5,main], Thread[3,5,main], Thread[4,5,main]]
     * current lock is true, current acquired lock thread is 2, and wait thread list are [Thread[3,5,main], Thread[4,5,main]]
     * current lock is true, current acquired lock thread is 3, and wait thread list are [Thread[4,5,main]]
     * current lock is true, current acquired lock thread is 4, and wait thread list are []
     * start to test nonFair Thread
     * current lock is false, current acquired lock thread is 0, and wait thread list are []
     * current lock is false, current acquired lock thread is 0, and wait thread list are []
     * current lock is false, current acquired lock thread is 2, and wait thread list are [Thread[1,5,main]]
     * current lock is false, current acquired lock thread is 2, and wait thread list are [Thread[1,5,main], Thread[3,5,main]]
     * current lock is false, current acquired lock thread is 1, and wait thread list are [Thread[3,5,main]]
     * current lock is false, current acquired lock thread is 1, and wait thread list are [Thread[3,5,main], Thread[4,5,main]]
     * current lock is false, current acquired lock thread is 3, and wait thread list are [Thread[4,5,main]]
     * current lock is false, current acquired lock thread is 3, and wait thread list are [Thread[4,5,main]]
     * current lock is false, current acquired lock thread is 4, and wait thread list are []
     * current lock is false, current acquired lock thread is 4, and wait thread list are []
     */
  }

  static class Job extends Thread {
    private ReentrantLock2 lock;

    public Job(ReentrantLock2 lock, int index) {
      super(String.valueOf(index));
      this.lock = lock;
    }



    @Override
    public void run() {
      for (int i = 0; i < 2; i++) {
        lock.lock();
        try {
          System.out.println(
              "current lock is " + lock.isFair() + ", current acquired lock thread is "
                  + Thread.currentThread().getName()
                  + ", and wait thread list are " + lock.getBlockedThreadByCurrentLock());
        } finally {
          lock.unlock();
        }
      }
    }
  }


  static class ReentrantLock2 extends ReentrantLock {
    public ReentrantLock2(boolean fair) {
      super(fair);
    }

    public Collection<Thread> getBlockedThreadByCurrentLock() {
      // ReentrantLock.getQueuedThreads 的结果是逆序的, 是从链表的尾部向前遍历，这里进行翻转
      List<Thread> threadList = new ArrayList<>(super.getQueuedThreads());
      Collections.reverse(threadList);
      return threadList;
    }
  }

}
