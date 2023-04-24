package com.swj.ics.jvm.classload;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/04/23 18:18
 */
public class FinalFieldClass {
  public static final String CONST_STRING = "CONST";
  static {
    System.out.println("FinalFieldClass init");
  }
}
