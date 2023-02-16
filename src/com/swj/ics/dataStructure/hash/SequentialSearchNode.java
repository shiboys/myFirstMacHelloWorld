package com.swj.ics.dataStructure.hash;

import com.swj.ics.dataStructure.queue.Queue;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/02/14 18:21
 * Hash 的 key-value 的封装，相当于 HashMap的 Entry，
 * 这个是基于链表的，实现了 基础的 put 和 get 操作。
 */
public class SequentialSearchNode<Key, Value> {

  private Node<Key, Value> first;
  private int size;


  private class Node<Key, Value> {
    private Key key;
    private Value value;
    private Node<Key, Value> next;

    public Node(Key key, Value value, Node<Key, Value> next) {
      this.key = key;
      this.value = value;
      this.next = next;
    }
  }

  public Value get(Key key) {
    for (Node<Key, Value> p = first; p != null; p = p.next) {
      if (p.key.equals(key)) {
        return p.value;
      }
    }
    return null;
  }

  public boolean containsKey(Key key) {
    return get(key) != null;
  }

  public int size() {
    return size;
  }

  public boolean isEmpty() {
    return size == 0;
  }

  public Value delete(Key key) {
    Node<Key, Value> p = first;
    Node<Key, Value> pre = null;
    Value val = null;
    while (p != null && !p.key.equals(key)) {
      pre = p;
      p = p.next;
    }
    if (p == null) {
      return null;
    }
    if (pre != null) {
      pre.next = p.next;
    } else {
      val = first.value;
      first = first.next;
    }
    size--;
    return val;
  }

  public void put(Key key, Value value) {
    if (value == null) {
      delete(key);
      return;
    }
    for (Node<Key, Value> p = first; p != null; p = p.next) {
      if (p.key.equals(key)) {
        p.value = value;
        return;
      }
    }
    // 此时 p 到了链表末尾，p == null
    // 采用头插入插入链表头部
    first = new Node<>(key, value, first);
    size++;
  }

  public Iterable<Key> keys() {
    Queue<Key> queue = new Queue<>();
    for (Node<Key, Value> p = first; p != null; p = p.next) {
      queue.enqueue(p.key);
    }
    return queue;
  }
}
