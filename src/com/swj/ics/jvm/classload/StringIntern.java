package com.swj.ics.jvm.classload;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/04/23 21:18
 */
public class StringIntern {

  public static void main(String[] args) {
    String a = Integer.toString(1) + Integer.toString(2) + Integer.toString(3);
    String b = "123";
    System.out.println("a.equals(b) is " + a.equals(b));
    System.out.println("a == b is " + (a == b ));
    System.out.println("a.intern()==b is " + (a.intern() == b ));
  }
}
