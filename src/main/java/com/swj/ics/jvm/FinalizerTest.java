package com.swj.ics.jvm;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/04/08 11:21
 * java 的 finalize() 方法测试
 */
public class FinalizerTest {
  private static FinalizerTest obj;

  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    System.out.println("FinalizerTest finalize called.");
    obj = this;
  }

  public static void main(String[] args) throws InterruptedException {
    //finalizeGc1();
    long b = System.currentTimeMillis();
    // 大量生成一个带有 finalize 函数的对象，且finalize() 函数是一个糟糕的耗时操作
    // 配上 jvm 参数 -Xms10M
    //-Xmx10M
    //-XX:+PrintGCDetail
    //-XX:+HeapDumpOnOutOfError
    //-XX:HeapDumpPath=/Volumes/home/github/myFirstMacHelloWorld/src/com/swj/ics/jvm/f.dump
    // 以便实现内存溢出，然后使用 mat 分析 dump 文件
    for (int i = 0; i < 50_000; i++) {
      LongFinalize lf = new LongFinalize();
    }
    long e = System.currentTimeMillis();
    System.out.println(e - b);

  }

  private static void finalizeGc1() throws InterruptedException {
    obj = new FinalizerTest();
    obj = null;
    System.gc();
    // 可以让 Finalizer Thread 运行到当前 finalize() 函数
    Thread.sleep(1000);
    if (obj == null) {
      System.out.println("obj is null");
    } else {
      System.out.println("obj is not null");
    }
    obj = null;
    System.gc();
    if (obj == null) {
      System.out.println("obj is null");
    } else {
      System.out.println("obj is not null");
    }
  }

  // /Volumes/home/github/myFirstMacHelloWorld/src/com/swj/ics/jvm
  static class LongFinalize {
    private byte[] contents = new byte[512];

    /*@Override
    protected void finalize() throws Throwable {
      System.out.println(Thread.currentThread().getId());
      Thread.sleep(1000);
    }*/
  }
}
