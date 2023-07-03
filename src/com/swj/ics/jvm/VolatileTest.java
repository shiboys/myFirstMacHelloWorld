package com.swj.ics.jvm;

import java.util.concurrent.TimeUnit;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/05/26 12:37
 */
public class VolatileTest {

  private static int a = 0;
  private static volatile int c = 0;
  private static volatile boolean done = false;


  public static void main(String[] args) {
    // 测试 volatile 的禁止重排序
    new Thread(VolatileTest::process).start();
    new Thread(() -> {
      System.out.println("thread volatile-test start");
      while (!done) { // 这里测试对 done 变量的写入是否会 happen-before 与 done 的读取之前
        System.out.println("done is false. and volatile c is =" + c + ", non volatile a is " + a);
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      System.out.println("done is true. quit loop");
    }, "volatile-test").start();

    // 结果证明，happen-before 是真的，变量 c 和 a 的读取，是在变量 c 设置以后的值
    // 打印结果如下
    /**
     * do something for 5 secs.
     * thread volatile-test start
     * done is false. and volatile c is =1, non volatile a is 5
     * done is false. and volatile c is =1, non volatile a is 5
     * done is false. and volatile c is =1, non volatile a is 5
     * done is false. and volatile c is =1, non volatile a is 5
     * done is false. and volatile c is =1, non volatile a is 5
     * done is true. quit loop
     */
  }

  private static void process() {
    doSomething();
    done = true;
  }

  private static void doSomething() {
    System.out.println("do something for 5 secs.");
    System.out.println(Integer.toBinaryString(-1));
    for (int i = 0; i <= 20; i++) {
      System.out.println("i=" + i + ",number of leading zeros = " + Integer.numberOfLeadingZeros(i) + ", stamp shift ="
          + Integer.toBinaryString(resizeStamp(i) << RESIZE_STAMP_SHIFT));
    }
    a = 5;
    c = 1;
    int n = -1 >>> Integer.numberOfLeadingZeros(16 - 1);
    System.out.println("n is " + n);// n=15
    try {
      TimeUnit.SECONDS.sleep(5);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private static final int RESIZE_STAMP_BITS = 16;

  /**
   * The maximum number of threads that can help resize.
   * Must fit in 32 - RESIZE_STAMP_BITS bits.
   */
  private static final int MAX_RESIZERS = (1 << (32 - RESIZE_STAMP_BITS)) - 1;

  /**
   * The bit shift for recording size stamp in sizeCtl.
   */
  private static final int RESIZE_STAMP_SHIFT = 32 - RESIZE_STAMP_BITS;

  static final int resizeStamp(int n) {
    return Integer.numberOfLeadingZeros(n) | (1 << (16 - 1));
  }

// 0000000000000000
}
