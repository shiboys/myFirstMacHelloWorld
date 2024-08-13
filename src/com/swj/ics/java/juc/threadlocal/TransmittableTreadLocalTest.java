package com.swj.ics.java.juc.threadlocal;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.alibaba.ttl.TtlRunnable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2024/04/05 14:58
 */
public class TransmittableTreadLocalTest {
  private static final ExecutorService simpleThreadPool = Executors.newSingleThreadExecutor();
  private static ThreadLocal<String> ttl;
  private static ThreadLocal<String> tll;
  //private static final String ttl_key = "ttl";
  private static volatile boolean taskDone = false;

  static {
    ttl = new TransmittableThreadLocal<>();
    tll = new ThreadLocal<>();
  }

  public static void main(String[] args) {
    try {
      testTransmittableThreadLocalVars();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  static void testTransmittableThreadLocalVars() throws InterruptedException {
    String var = "hello,ttl";

    ttl.set(var);
    tll.set(var+"_raw");

    Runnable task = () -> {
      int i = 0;
      while ((i++) < 10) {
        System.out.println(ttl.get());
        // 读取不到
        System.out.println(tll.get());
        try {
          Thread.sleep(500);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      taskDone = true;
    };
    TtlRunnable ttask = TtlRunnable.get(task);
    ttl.set("i'm changed");
    tll.set(ttl.get()+"_row");
    simpleThreadPool.submit(ttask);
    while (!taskDone) {
      Thread.yield();
    }
    // 再次提交，就可能感受到变化
    taskDone = false;
    // TtlRunnable.get() 的静态方法，每次返回一个新的  TtlRunnable
    // TtlRunnable 就会重新读取父线程的 Thread
    simpleThreadPool.submit(TtlRunnable.get(task));
    while (!taskDone) {
      Thread.yield();
    }
    simpleThreadPool.shutdown();
    while (!simpleThreadPool.awaitTermination(100, TimeUnit.MICROSECONDS)) {
      Thread.yield();
    }
    // todo Transmittable Thread local 还能修饰线程池 ExecutorService 类，具体参考 github 官网 https://github.com/alibaba/transmittable-thread-local
  }

}
