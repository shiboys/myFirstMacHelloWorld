package com.swj.ics.dataStructure.hash;

import com.swj.ics.dataStructure.queue.Queue;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/02/21 21:31
 * 轻量级的使用拉链法的 HashTable 实现
 */
public class SequentialLinearLiteST<Key, Value> implements ST<Key, Value> {

  int capacity;
  int size;
  // 由于是轻量级的 hash 表，就暂不支持扩容。
  Node<Key, Value>[] tables;

  public SequentialLinearLiteST(int capacity) {
    tables = (Node<Key, Value>[]) new Node[capacity];
    this.capacity = capacity;
  }

  /**
   * 默认比较大的容量，因为不支持扩容
   */
  public SequentialLinearLiteST() {
    this(997);
  }

  int hash(Key key) {
    if (key == null) {
      return 0;
    }
    int hash = key.hashCode();
    return (hash | (hash >> 16)) % capacity;
  }

  @Override
  public boolean contains(Key key) {
    return get(key) != null;
  }

  @Override
  public Value get(Key key) {
    int hashIndex = hash(key);
    Node<Key, Value> pNode = tables[hashIndex];
    while (pNode != null) {
      if (pNode.key.equals(key)) {
        return pNode.value;
      }
      pNode = pNode.next;
    }
    return null;
  }

  @Override
  public void put(Key key, Value value) {
    if (value == null) {
      // try to delete it....
      return;
    }
    if (key == null) {
      throw new IllegalArgumentException("lite hash table ST ,the key can not be null.");
    }
    int hashIdx = hash(key);
    Node<Key, Value> pNode = tables[hashIdx];
    // 这里使用尾插法，当然 头插法更简单
    if (pNode == null) {
      pNode = new Node<>(key, value, null);
      tables[hashIdx] = pNode;
      size++;
    } else {
      while (true) {
        if (pNode.key.equals(key)) {
          pNode.value = value;
          return;
        }
        if (pNode.next == null) {
          pNode.next = new Node<>(key, value, null);
          size++;
          break;
        }
        pNode = pNode.next;
      }
    }
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public Iterable<Key> keys() {
    Queue<Key> queue = new Queue<>();
    Node<Key, Value> node;
    for (int i = 0; i < capacity; i++) {
      if ((node = tables[i]) == null) {
        continue;
      }
      while (node != null) {
        queue.enqueue(node.key);
        node = node.next;
      }
    }
    return queue;
  }

  private static class Node<Key, Value> {
    public Key key;
    public Value value;
    private Node<Key, Value> next;

    public Node(Key key, Value value, Node<Key, Value> next) {
      this.key = key;
      this.value = value;
      this.next = next;
    }
  }


}
