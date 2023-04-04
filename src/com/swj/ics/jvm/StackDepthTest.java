package com.swj.ics.jvm;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/03/30 18:49
 * 栈深度测试
 */
public class StackDepthTest {
  private static int loopCount = 0;
  private static final int _1M = 1024 * 1024;

  public static void main(String[] args) {
    //recursionDemo();
    StackDepthTest instance = new StackDepthTest();
    //instance.localVarGc1();
    //instance.localVarGc2();
    //instance.localVarGc3();
    //instance.localVarGc4();
    instance.localVarGc5();

  }

  private static void recursionDemo() {
    try {
      recursion();
    } catch (Throwable e) {
      System.out.println("depth of calling recursion() is " + loopCount);
      e.printStackTrace();
    }

    loopCount = 0;
    try {
      recursion(0, 0, 0);
    } catch (Throwable e) {
      System.out.println("depth of calling recursion(a,b,c) is " + loopCount);
      e.printStackTrace();
    }
  }

  private static void recursion() {
    loopCount++;
    recursion();
  }

  private static void recursion(long a, long b, long c) {
      long x1=1, x2=2, x3=3, x4=4, x5=5, x6=6, x7=7, x8=8, x9=9, x10=10;
      loopCount++;
    recursion(a, b, c);
  }

  public void localVar1() {
    int a=0;
    System.out.println(a);
    int b = 0;
  }

  public void localVar2() {
    {
      int a=0;
      System.out.println(a);
    }
    int b =0;
  }

  public void localVarGc1() {
    byte[] a= new byte[6 * _1M];
    System.gc();
  }
  public void localVarGc2() {
    byte[] a = new byte[6 * _1M];
    a = null;
    System.gc();
  }

  public void localVarGc3() {
    {
      byte[] a = new byte[6 * _1M];
    }
    System.gc();
  }

  public void localVarGc4() {
    {
      byte[] a = new byte[6 * _1M];
    }
    int c =10;
    System.gc();
  }


  public void localVarGc5() {
    localVarGc1();
    System.gc();
  }
}
