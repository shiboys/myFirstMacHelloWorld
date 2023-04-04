package com.swj.ics.java.juc;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/04/04 21:15
 */
public class MyThreadLocalTest {

  private static MyThreadLocal<Integer> threadLocalValue = new MyThreadLocal<Integer>() {
    @Override
    protected Integer initialValue() {
      return 0;
    }
  };


  public static void main(String[] args) {
    int threadsCount =32;
    MyThreadLocalThread[] threads = new MyThreadLocalThread[threadsCount];

    for (int i = 0; i < threadsCount; i++) {
      threads[i] = new MyThreadLocalThread() {
        @Override
        public void run() {
          for (int i = 0; i <  10; i++) {
            int v = threadLocalValue.get();
            threadLocalValue.set(++v);
            System.out.println(Thread.currentThread().getName() + ",index = " + v);
          }
        }
      };
      threads[i].start();
    }
  }
}
