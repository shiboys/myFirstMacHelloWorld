package com.swj.ics.jvm.classload;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/04/24 14:39
 */
public class HelloLoader {
  public void print() {
    System.out.println("I am in App ClassLoader");
    System.out.println(HelloLoader.class.getClassLoader());
  }
}
