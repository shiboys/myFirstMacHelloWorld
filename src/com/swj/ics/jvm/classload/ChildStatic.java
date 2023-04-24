package com.swj.ics.jvm.classload;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/04/23 21:37
 */
public class ChildStatic extends SimpleStatic {
  static {
    number =2;
  }

  public static void main(String[] args) {
    System.out.println(number);
  }
}
