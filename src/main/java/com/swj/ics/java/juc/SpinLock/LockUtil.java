package com.swj.ics.java.juc.SpinLock;

import java.util.Date;
import java.util.concurrent.locks.Lock;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/04/28 17:03
 */
public class LockUtil {

  public static void lockTest(Lock lock) {

    lockTest(lock, 2);
  }

  public static void lockTest(Lock lock, int threadNumber) {
    Thread[] threads = new Thread[threadNumber];

    Runnable testTheLock = new Runnable() {
      @Override
      public void run() {
        long threadId = Thread.currentThread().getId();
        System.out.println(threadId + " start to lock at" + new Date());
        lock.lock();
        try {
          System.out.println(threadId + " get the lock at " + new Date());
          Thread.sleep(1000);
          System.out.println(threadId + " done the business work!");
        } catch (InterruptedException e) {
          e.printStackTrace();
        } finally {
          System.out.println(threadId + " unlock the lock");

          lock.unlock();
        }
      }
    };

    for (int i = 0; i < threadNumber; i++) {
      threads[i] = new Thread(testTheLock);
    }
    for (int i = 0; i < threadNumber; i++) {
      threads[i].start();
    }
  }
}
