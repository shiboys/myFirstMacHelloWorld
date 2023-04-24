package com.swj.ics.jvm;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/04/22 18:43
 */
public class HoldCPUMain {

  static class HoldCPUTask implements Runnable {

    @Override
    public void run() {
      while (true) {
        double a = Math.random() * Math.random(); // 占用 CPU
      }
    }
  }


  static class LazyTask implements Runnable {

    @Override
    public void run() {
      try {
        while (true) {
          Thread.sleep(1000); // 空闲线程
        }
      } catch (InterruptedException e) {

      }
    }
  }

  public static void main(String[] args) {
    // 一个占用 cpu 的线程， 3 个空闲线程
    new Thread(new HoldCPUTask()).start();
    new Thread(new LazyTask()).start();
    new Thread(new LazyTask()).start();
    new Thread(new LazyTask()).start();
  }
}
