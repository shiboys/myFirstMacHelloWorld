package com.swj.ics.jvm.classload;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/04/23 21:29
 */
public class SimpleStatic {
  public static int id = 1;
  public static int number;

  static {
    number = 4;
  }
}
