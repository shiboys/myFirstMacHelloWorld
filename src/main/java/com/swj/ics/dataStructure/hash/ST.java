package com.swj.ics.dataStructure.hash;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/02/21 21:28
 * 拉链法的 Hash 解决类抽象接口
 */
public interface ST<Key,Value> {

  boolean contains(Key key);

  Value get(Key key);

  void put(Key key, Value value);

  Iterable<Key> keys();

  int size();
}
