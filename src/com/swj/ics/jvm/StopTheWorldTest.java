package com.swj.ics.jvm;

import lombok.SneakyThrows;

import java.util.HashMap;
import java.util.Map;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/04/08 21:36
 */
public class StopTheWorldTest {
  private static final int _1M = 1024 * 1024;

  public static void main(String[] args) {
    MySTWThread mySTWThread = new MySTWThread();
    TimerThread timerThread = new TimerThread();
    mySTWThread.start();
    timerThread.start();
  }

  static class MySTWThread extends Thread {
    Map<Long, byte[]> memoryMap = new HashMap<>();

    @SneakyThrows
    @Override
    public void run() {
      while (true) {
        if (memoryMap.size() * 512 /_1M >= 750) {
          memoryMap.clear();
          System.out.println("clear map");
        }
        byte[] bs;
        for (int i = 0; i < 100; i++) {
          bs = new byte[512];
          memoryMap.put(System.nanoTime(), bs);
        }
        Thread.sleep(1);
      }
    }
  }


  static class TimerThread extends Thread {
    private static final long START_TIME = System.currentTimeMillis();

    @Override
    public void run() {
      while (true) {
        long diff = System.currentTimeMillis() - START_TIME;
        System.out.println(diff / 1000 + "." + diff % 1000);
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
  }
}
