package com.swj.ics.java.juc.SpinLock;

import java.util.concurrent.atomic.AtomicReference;

import javax.xml.bind.SchemaOutputResolver;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/04/28 15:22
 */
public class SpinLock {
  /**
   * 非公平自旋锁，没有使用阻塞队列，过程也比较简单。为了进一步支持锁重入，还提供了一个 count 变量记录锁记录的重入次数。
   * 但是该实现也有缺点，首先就是加锁但是未能获取锁的线程会不停地自旋来检测锁的状态。此举会明显浪费 CPU ，当然这也是自旋锁方案的通用弊端；
   * 其次该实现方式没有使用队列，就没有办法实现公平锁。线程来了就抢锁，如果抢到则可以执行县城自身的业务逻辑，否则就只能在那里自旋
   */

  // 记录重入次数
  private volatile int count;

  /**
   * 持有锁的线程
   */
  private AtomicReference<Thread> threadLock = new AtomicReference<>();

  public void lock() {
    Thread current = Thread.currentThread();
    if (current == threadLock.get()) {
      count++;
      return;
    }

    while (!threadLock.compareAndSet(null, current)) ;
  }

  public void unlock() {
    Thread current = Thread.currentThread();
    if (current == threadLock.get()) {
      if (count > 0) {
        count--;
      } else {
        threadLock.set(null);
      }
    }
  }

  public static void main(String[] args) {
    Thread[] threads = new Thread[2];
    SpinLock spinLock = new SpinLock();
    Runnable testTheLock = new Runnable() {
      @Override
      public void run() {
        spinLock.lock();
        try {
          System.out.println(Thread.currentThread().getId() + " get the lock");
          Thread.sleep(1000);
          System.out.println("done the business work!");
        } catch (InterruptedException e) {
          e.printStackTrace();
        } finally {
          System.out.println(Thread.currentThread().getId() +" unlock the lock");
          spinLock.unlock();
        }
      }
    };

    threads[0]= new Thread(testTheLock);
    threads[1]= new Thread(testTheLock);
    threads[0].start();
    threads[1].start();
  }
}

/**
 * 公平自旋锁，
 * 为了解决实现自旋锁的公平性，我们需要额外引入一个排队机制。
 * 分别用两个原子变量表示排队号码、当前服务号码(即持有锁的号码)。现实中就是营业大厅的发号器，和叫号器。
 * 加锁过程，会首先给当前线程分配一个排队号码，然后该线程开始自旋。直到被它叫到号才退出自旋，即它的排队号码等于当前的服务号码(当前叫到的号)
 * 解锁的过程：计算、更新当期服务号码的值。以便下一个线程能够结束自旋、获取到锁。
 * 具体实现参见 TicketLock
 */
