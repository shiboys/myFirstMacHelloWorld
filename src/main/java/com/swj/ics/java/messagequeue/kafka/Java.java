package com.swj.ics.java.messagequeue.kafka;

import java.util.StringTokenizer;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2024/06/18 16:24
 */
public final class Java {

  private static final Version VERSION = parseVersion(System.getProperty("java.specification.version"));

  private Java() {
  }

  public static final boolean IS_JAVA9_COMPATIBLE = VERSION.isJava9Compatible();
  public static final boolean IS_JAVA11_COMPATIBLE = VERSION.isJava11Compatible();


  static Version parseVersion(String versionProperty) {
    final StringTokenizer tokenizer = new StringTokenizer(versionProperty, ".");
    int majorVersion = Integer.parseInt(tokenizer.nextToken());
    int minorVersion;
    if (tokenizer.hasMoreTokens()) {
      minorVersion = Integer.parseInt(tokenizer.nextToken());
    } else {
      minorVersion = 0;
    }
    return new Version(majorVersion, minorVersion);
  }


  static class Version {
    private final int majorVersion;
    private final int minorVersion;

    Version(int majorVersion, int minorVersion) {
      this.majorVersion = majorVersion;
      this.minorVersion = minorVersion;
    }

    @Override
    public String toString() {
      return "Version(" +
          "majorVersion=" + majorVersion +
          ", minorVersion=" + minorVersion +
          ')';
    }

    boolean isJava9Compatible() {
      return majorVersion >= 9;
    }

    boolean isJava11Compatible() {
      return majorVersion >= 11;
    }
  }
}
