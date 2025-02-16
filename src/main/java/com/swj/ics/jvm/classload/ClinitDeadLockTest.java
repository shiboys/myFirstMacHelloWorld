package com.swj.ics.jvm.classload;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/04/23 21:56
 */
public class ClinitDeadLockTest {
  static class StaticA {
    static {

      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      try {
        Class.forName("com.swj.ics.jvm.classload.ClinitDeadLockTest$StaticB");
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      }
      System.out.println("StaticA init OK.");
    }
  }


 public static class StaticB {
    static {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      try {
        Class.forName("com.swj.ics.jvm.classload.ClinitDeadLockTest$StaticA");
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      }
      System.out.println("StaticA init OK");
    }
  }


  public static class StaticDeadLockThread extends Thread {
    private char flag;

    public StaticDeadLockThread(char flag) {
      this.flag = flag;
      setName("Thread-" + flag);
    }

    @Override
    public void run() {
      try {
        Class.forName("com.swj.ics.jvm.classload.ClinitDeadLockTest$Static" + flag);
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      }
      System.out.println(getName()+" over");
    }
  }

  public static void main(String[] args) {
    StaticDeadLockThread threadA = new StaticDeadLockThread('A');
    threadA.start();
    StaticDeadLockThread threadB = new StaticDeadLockThread('B');
    threadB.start();
  }
}
