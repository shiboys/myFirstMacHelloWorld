package com.swj.ics.jvm.classload;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/04/24 12:32
 */
public class PrintClassLoader {
  public static void main(String[] args) {
    java.lang.ClassLoader classLoader = PrintClassLoader.class.getClassLoader();

    while (classLoader != null) {
      System.out.println(classLoader);
      classLoader = classLoader.getParent();
    }
  }
}
