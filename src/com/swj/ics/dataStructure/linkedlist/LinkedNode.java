package com.swj.ics.dataStructure.linkedlist;

import lombok.AllArgsConstructor;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/01/07 16:29
 */
@AllArgsConstructor
public class LinkedNode<T extends Comparable<T>> {
  public T value;
  public LinkedNode<String> next;
}
