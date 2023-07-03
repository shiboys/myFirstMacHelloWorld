package com.swj.ics.jvm;

import java.util.List;
import java.util.Vector;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/04/18 21:01
 */
public class LockTest {

  public static void main(String[] args) {
    Biased.testBiased();
  }
  static class Biased {

    private static List<Integer> numberList = new Vector<>();

    /**
     * 开启或者关闭偏向锁
     * -XX:+UseBiasedLocking
     * -XX:BiasedLockingStartupDelay=0 表示虚拟机启动后，立即启用偏向锁
     */
    public static void testBiased() {
      long s = System.currentTimeMillis();
      int count = 0;
      int startNum = 0;
      for (int i = 0; i < 10_000_000; i++) {
        numberList.add(startNum);
        startNum += 2;
        count++;
      }
      long diff = System.currentTimeMillis() - s;
      System.out.println(diff);

      //LinkeMap map =
    }

  }
}
