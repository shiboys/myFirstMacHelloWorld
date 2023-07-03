package com.swj.ics.jvm;

import lombok.SneakyThrows;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/04/08 17:46
 * Java 4 中引用类型测试
 */
public class ReferenceTest {

  static ReferenceQueue<User> userReferenceQueue = null;

  public static void main(String[] args) throws InterruptedException {
    // softReferenceTest();
    weakReferenceTest();
    //TraceCanReliveObj.runPhantomReferenceTest();
  }

  private static void LinkedHashMapTest() {

  }

  private static void softReferenceTest() throws InterruptedException {
    userReferenceQueue = new ReferenceQueue<>();

    CheckRefQueueThread t = new CheckRefQueueThread();
    t.setDaemon(true);
    t.start();

    User u1 = new User(1, "tom");
    // softReferenceBasic(u1);
    UserSoftReference userSoftReference = new UserSoftReference(u1, userReferenceQueue);
    u1 = null;
    System.out.println(userSoftReference.get());
    System.gc();
    /**
     * 如果内存够用，即使发生 gc，也仍然不会被回收
     */
    System.out.println("After gc:");
    System.out.println(userSoftReference.get());

    try {
      System.out.println("generate new byte array and gc again");
      /**
       * 抛出 OOM Error 之前回收软引用的对象内存
       */
      byte[] bytes = new byte[1024 * 1024 * 7];
      System.gc();
      System.out.println(userSoftReference.get());
    } catch (Throwable e) {
      e.printStackTrace();
      /**
       * OOM 异常后，到这里，对象肯定已经被回收，这个的 get() 返回的是 null
       * user id :1 is delete.
       * error occured.
       * null
       * user id :1 is delete. 出现在 catch 的打印之前，更能充分说明在抛出 OOM Error 之前，软引用先被回收，然后
       * 放入 ReferenceQueue 中
       *  -XX:+PrintGCDetails -XX:+PrintReferenceGC 使用 gvm 参数可以跟踪虚、弱、软应用的回收情况
       */
      System.out.println("error occurred.");
      System.out.println(userSoftReference.get());
    }
    Thread.sleep(1000);
  }

  static class CheckRefQueueThread extends Thread {
    @Override
    public void run() {
      while (true) {
        try {
          UserSoftReference obj = null;
          Reference<?> reference = userReferenceQueue.poll();
          if (reference instanceof UserSoftReference) {
            obj = (UserSoftReference) reference;
          }
          if (obj != null) {
            // 此时 调用 get() 返回 Null，但是 obj 并不是 null。主要用户跟踪对象是否被回收
            System.out.println("user id :" + obj.uid + " is delete.");
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }


  static class UserSoftReference extends SoftReference<User> {
    int uid;

    public UserSoftReference(User referent, ReferenceQueue<? super User> q) {
      super(referent, q);
      uid = referent.id;
    }
  }

  private static void softReferenceBasic(User u1) {
    ReferenceQueue<User> userReferenceQueue = new ReferenceQueue<>();
    SoftReference<User> softReference = new SoftReference<>(u1, userReferenceQueue);
    u1 = null;

    System.out.println(softReference.get());
    System.gc();
    System.out.println("after gc:");
    System.out.println(softReference.get());
    // 分配一个大对象 再试试
    System.out.println(userReferenceQueue.poll());
    try {
      byte[] bytes = new byte[1024 * 1024 * 7];
      System.gc();
      System.out.println(softReference.get());
    } catch (Throwable e) {
      e.printStackTrace();
      System.out.println(softReference.get());
      // 如果发生了 OOM ，则 虚拟机会把 引用对象放入队列，这是我们再去尝试获取下试试
      Reference<?> userReference = userReferenceQueue.poll();
      System.out.println(userReference.get());
    }
  }

  static class User {
    int id;

    String name;

    public User(int id, String name) {
      this.id = id;
      this.name = name;
    }

    @Override
    public String toString() {
      return "[id=" + id + " ,name=" + name + "]";
    }


  }

  static void weakReferenceTest() {
    User u = new User(1, "tom");
    WeakReference<User> userWeakReference = new WeakReference<>(u);
    u = null;
    System.out.println(userWeakReference.get());
    System.gc();
    System.out.println("after gc:");
    System.out.println(userWeakReference.get());
  }

  static class TraceCanReliveObj {
    static ReferenceQueue<TraceCanReliveObj> phantomQueue = null;
    private static TraceCanReliveObj obj;

    @Override
    public String toString() {
      return "I am TraceCanRelive obj"; // 重活对象
    }

    @Override
    protected void finalize() throws Throwable {
      super.finalize();
      System.out.println("TraceCanRelive finalize() called");
      obj = this;
    }

    public static void runPhantomReferenceTest() throws InterruptedException {
      PhantomThread t = new PhantomThread();
      t.setDaemon(true);
      t.start();
      phantomQueue = new ReferenceQueue<>();
      obj = new TraceCanReliveObj();
      PhantomReference<TraceCanReliveObj> phantomReference = new PhantomReference<>(obj, phantomQueue);
      obj = null;
      System.gc();
      Thread.sleep(1000);
      if (obj == null) { // 在 finalize 里面被抢救了一次
        System.out.println("obj is null");
      } else {
        System.out.println("obj is not null");
      }

      System.out.println("第 2 次 gc：");
      obj = null;
      System.gc();
      Thread.sleep(1000);
      if (obj == null) { // 在 finalize 里面被抢救了一次
        System.out.println("obj is null");
      } else {
        System.out.println("obj is not null");
      }
    }

    static class PhantomThread extends Thread {

      @SneakyThrows
      @Override
      public void run() {
        while (true) {
          tryToPollItemFromPhantomQueue();
        }
      }

      private void tryToPollItemFromPhantomQueue() throws InterruptedException {
        if (phantomQueue != null) {
          Reference<?> reference = phantomQueue.remove();
          if (reference instanceof PhantomReference) {
            System.out.println(" PhantomReference of class TraceCanReliveObj is deleted.");
          }
        }
      }
    }

  }
}
