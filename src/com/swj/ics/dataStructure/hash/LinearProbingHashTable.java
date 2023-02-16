package com.swj.ics.dataStructure.hash;

import com.swj.ics.dataStructure.queue.Queue;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/02/15 17:24
 * 使用线性探查法的 hash 表
 */
public class LinearProbingHashTable<Key, Value> {
  private static final int DEFAULT_CAPACITY = 4;
  private static final int MAX_CAPACITY = 1 << 30;
  private int length;
  private int size;
  private Key[] keys;
  private Value[] vals;

  public LinearProbingHashTable(int capacity) {
    if (capacity < DEFAULT_CAPACITY) {
      capacity = DEFAULT_CAPACITY;
    }
    int newCap = getRoundUpToPowerOf2(capacity);
    keys = (Key[]) new Object[newCap];
    vals = (Value[]) new Object[newCap];
  }

  public LinearProbingHashTable() {
    this(DEFAULT_CAPACITY);
  }

  private int getRoundUpToPowerOf2(int capacity) {
    if (capacity > MAX_CAPACITY) {
      return MAX_CAPACITY;
    }
    int cap = capacity - 1;
    cap |= cap >>> 1;
    cap |= cap >>> 2;
    cap |= cap >>> 4;
    cap |= cap >>> 8;
    cap |= cap >>> 16;
    return cap < 0 ? 1 : (cap > MAX_CAPACITY ? MAX_CAPACITY : cap + 1);
  }

  private int hash(Key key) {
    int h = key.hashCode();
    h ^= h >>> 20;
    h ^= h >>> 12;
    h ^= h >>> 7;
    h ^= h >>> 4;
    return h & (length - 1);
  }

  public Value get(Key key) {
    if (key == null) {
      throw new IllegalArgumentException("key can not be null whiling obtaining data from the the hash table.");
    }
    int index = hash(key);
    for (; keys[index] != null; index = (index + 1) % length) {
      if (keys[index].equals(key)) {
        return vals[index];
      }
    }
    return null;
  }

  public int size() {
    return size;
  }

  public boolean isEmtpy() {
    return size == 0;
  }

  /**
   * 删除指定的 key
   *
   * @param key
   */
  public void delete(Key key) {
    // 删除指定的 key
    int idx = hash(key);
    boolean found = false;

    while (keys[idx] != null) {
      if (keys[idx].equals(key)) {
        found = true;
        break;
      }
      idx = (idx + 1) % length;
    }
    if (!found) {
      return;
    }

    keys[idx] = null;
    vals[idx] = null;
    // idx 之后的 所有不为 null 的元素，都需要重新索引 rehash，否则就会出现丢失数据读取情况
    idx = (idx + 1) % length;
    while (keys[idx] != null) {
      Key keyTobeRehash = keys[idx];
      Value valueToBeRehash = vals[idx];
      // 腾出位置
      keys[idx] = null;
      vals[idx] = null;
      size--;
      put(keyTobeRehash, valueToBeRehash);
      idx = (idx + 1) % length;
    }
    size--;
    // 缩容跟扩容不一样，一旦容量到达，立马尝试缩容
    // 如果使用量小于等于容量的 12.5%，则尝试将容量缩小 50%。如果是 小于 50 % 就缩容，会导致一旦有新元素加入，会导致判断元素使用量超过 50%，立马进行扩容
    // 最终的结果是，缩容之后和立马扩容，严重影响性能。所以这里的 缩容的标准讲到 12.5 %，这样在缩容之后不会导致又立马扩容
    if (size < length / 8) {
      resize(length / 2);
    }
    check();
  }

  public void resize(int newCap) {
    LinearProbingHashTable<Key, Value> tempHashTable = new LinearProbingHashTable<>(newCap);
    for (int i = 0; i < length; i++) {
      if (keys[i] == null) {
        continue;
      }
      tempHashTable.put(keys[i], vals[i]);
    }

    this.keys = tempHashTable.keys;
    this.vals = tempHashTable.vals;
    this.size = tempHashTable.size;
    this.length = tempHashTable.length;
  }

  public void put(Key key, Value value) {
    if (key == null) {
      throw new IllegalArgumentException("key can not be null while put new key-value pair");
    }
    if (value == null) {
      delete(key);
      return;
    }
    // 使用量已经超过容器的一半了，就需要扩容
    if (size * 2 > length) {
      resize(length * 2);
    }
    int index = hash(key);
    for (; keys[index] != null; index = (index + 1) % length) {
      if (keys[index].equals(key)) {
        vals[index] = value;
        return;
      }
    }

    keys[index] = key;
    vals[index] = value;
    size++;
  }

  public boolean containsKey(Key key) {
    if (key == null) {
      return false;
    }
    return get(key) != null;
  }

  public Iterable<Key> keys() {
    Queue<Key> queue = new Queue<>();
    for (int i = 0; i < length; i++) {
      if (keys[i] != null) {
        queue.enqueue(keys[i]);
      }
    }
    return queue;
  }

  public boolean check() {
    if (size * 2 > length) {
      System.err.println(
          String.format("the element size is over the half of the container capacity. size is %s, capacity is %s", size,
              length));
      return false;
    }
    for (int i = 0; i < length; i++) {
      if (keys[i] == null) {
        continue;
      }
      if (get(keys[i]) != vals[i]) {
        System.err.println(
            "the value of key " + i + " = " + keys[i] + " is not equals to vals[" + i + "], value1=" + get(keys[i])
                + ", value2=" + vals[i]);
        return false;
      }
    }
    return true;
  }

  public static void main(String[] args) {
    LinearProbingHashTable<String, Integer> hashTable = new LinearProbingHashTable<>(1);
    System.out.println(hashTable);
  }
}
