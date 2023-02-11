package com.swj.ics.dataStructure.tree.btree;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/02/10 10:43
 */
public class Entry<K,V> {
  private K key;
  private V value;

  public Entry(K key) {
    this.key = key;
  }

  public Entry(K key,V val) {
    this.key = key;
    this.value = val;
  }

  public Entry() {
  }

  public K getKey() {
    return key;
  }

  public void setKey(K key) {
    this.key = key;
  }

  public V getValue() {
    return value;
  }

  public void setValue(V value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return "Entry{" +
        "key=" + key +
        '}';
  }
  public String toShortString() {
    return key.toString();
  }
}
