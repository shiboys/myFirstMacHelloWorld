package com.swj.ics.java.messagequeue.kafka.mmap_index;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2024/06/18 19:18
 */
public interface IndexEntry {
  long indexKey();
  long indexValue();
}
