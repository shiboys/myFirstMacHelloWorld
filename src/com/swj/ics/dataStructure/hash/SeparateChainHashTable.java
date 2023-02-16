package com.swj.ics.dataStructure.hash;

import com.swj.ics.dataStructure.queue.Queue;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/02/15 14:31
 * 拉链法实现 hash 表
 */

public class SeparateChainHashTable<Key, Value> {
  private static final int DEFAULT_INIT_CAPACITY = 4;
  private static final int MAX_CAPACITY = 1 << 30;
  private int size;
  private int length;
  private SequentialSearchNode<Key, Value>[] array;

  public SeparateChainHashTable(int capacity) {
    if (capacity < DEFAULT_INIT_CAPACITY) {
      capacity = DEFAULT_INIT_CAPACITY;
    }
    length = roundUpToPowerOf2(capacity);
    array = new SequentialSearchNode[length];
  }


  private int roundUpToPowerOf2(int capacity) {
    if (capacity > MAX_CAPACITY) {
      return MAX_CAPACITY;
    }
    int n = capacity - 1;
    n |= (n >>> 1);
    n |= (n >>> 2);
    n |= (n >>> 4);
    n |= (n >>> 8);
    n |= (n >>> 16);

    return (n < 0) ? 1 : (n >= MAX_CAPACITY ? MAX_CAPACITY : n + 1);
  }

  public SeparateChainHashTable() {
    this(DEFAULT_INIT_CAPACITY);
  }

  private int hasTextBox(Key key) {
    return (key.hashCode() & 0x7fffffff) % length;
  }

  /**
   * 采用 jdk 7 的 hash 算法，将 hash 结果进行必要的扰动
   *
   * @param key key
   * @return hashcode
   */
  private int hash(Key key) {
    int hash = key.hashCode();
    hash ^= hash >>> 20;
    hash ^= hash >>> 12;
    hash ^= hash >>> 7;
    hash ^= hash >>> 4;
    // 这里的 length 必须是 2 的幂次方，否则这个取余就有问题了。
    return hash & (length - 1);
  }

  public boolean containsKey(Key key) {
    SequentialSearchNode<Key, Value> node = array[hash(key)];
    if (node == null) {
      return false;
    }
    return node.containsKey(key);
  }

  public int size() {
    return size;
  }

  public boolean isEmtpy() {
    return size == 0;
  }

  public Value get(Key key) {
    SequentialSearchNode<Key, Value> node = array[hash(key)];
    if (node != null) {
      return node.get(key);
    }
    return null;
  }

  public void delete(Key key) {
    if (key == null) {
      throw new IllegalArgumentException("the key o f hash table item to be deleted can not be null");
    }
    SequentialSearchNode<Key, Value> node = array[hash(key)];
    if (node == null) {
      return;
    }
    // 链表中包含当前 key，可以数量要减少
    if (node.containsKey(key)) {
      size--;
    }
    node.delete(key);

    // resize
    if (size > DEFAULT_INIT_CAPACITY && size <= length / 2) {
      resize(length / 2);
    }
  }

  public void put(Key key, Value val) {
    if (key == null) {
      throw new IllegalArgumentException("key can not be null while put key-value to hashtable.");
    }
    if (val == null) {
      delete(key);
      return;
    }
    int index = hash(key);
    SequentialSearchNode<Key, Value> node = array[index];
    if (node == null) {
      node = new SequentialSearchNode<>();
      array[index] = node;
    }
    // todo：resize
    // 如果hash 表底层的链表平均长度 大于等于 10 ，则 hash 表需要扩容值 2 倍。
    if (size >= length * 10) {
      resize(2 * length);
    }
    if (!node.containsKey(key)) {
      size++;
    }
    node.put(key, val);
  }

  public void resize(int newLength) {
    // 这里的 resize 并没有参照 jdk 的操作，使用 Arrays.copy, 而是使用直接创建新的，让旧的进行垃圾回收
    SeparateChainHashTable<Key, Value> tempTable = new SeparateChainHashTable<>(newLength);
    for (int i = 0; i < length; i++) {
      SequentialSearchNode<Key, Value> node = array[i];
      if (node != null) {
        for (Key key : node.keys()) {
          tempTable.put(key, node.get(key));
        }
      }
    }
    this.length = tempTable.length;
    this.size = tempTable.size;
    this.array = tempTable.array;
  }

  public Iterable<Key> keys() {
    Queue<Key> keyQueue = new Queue<Key>();
    for (int i = 0; i < length; i++) {
      if (array[i] == null) {
        continue;
      }
      for (Key key : array[i].keys()) {
        keyQueue.enqueue(key);
      }
    }
    return keyQueue;
  }

  public static void main(String[] args) {
    SeparateChainHashTable<Character, Integer> table = new SeparateChainHashTable();
    char firstCh = 'A';
    int diff = 'z' - firstCh;
    for (int i = 0; i < diff; i++) {
      int pos = firstCh + i;
      char ch = (char) (pos);
      table.put(ch, pos);
    }

    int i = 0;
    for (char key : table.keys()) {
      System.out.println("key=" + key + ", value = " + table.get(key));
      i++;
    }
    System.out.println("length is " + table.length);
  }
}
