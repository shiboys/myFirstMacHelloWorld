package com.swj.ics.java.messagequeue.kafka;

import java.util.Locale;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2024/06/18 18:31
 */
public class OperationSystem {
  private OperationSystem() {

  }

  public static final boolean IS_WINDOWS;
  public static final boolean IS_ZOS;

  static {
    String osName = System.getProperty("os.name").toLowerCase(Locale.ROOT);
    IS_WINDOWS = osName.startsWith("windows");
    IS_ZOS = osName.startsWith("z/os");
  }
}
