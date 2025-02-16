package com.swj.ics.java.language;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/05/22 16:15
 */
public class FinallyExcept {
  public static void main(String[] args) {
    try {
      System.out.println("main body");
      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        System.out.println("shutdown hook");
      }));
      System.exit(1);
    } finally {
      System.out.println("Print from finally");
    }
  }
}
