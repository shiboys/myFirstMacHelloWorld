package com.swj.ics.java.messagequeue.kafka.mmap_index;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2024/06/18 21:09
 */
public class InvalidOffsetException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public InvalidOffsetException(String message) {
    super(message);
  }

  public InvalidOffsetException(String message, Throwable cause) {
    super(message, cause);
  }
}
