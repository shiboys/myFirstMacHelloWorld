package com.swj.ics.java.map;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2024/06/13 20:12
 * 线程安全的 CopyOnWriteMap，代码参考与 Kafka 的同名类文件
 */
public class CopyOnWriteMap<K, V> implements ConcurrentMap<K, V> {

  // 因为要 copyOnWrite 的方式替换这个 map, 因此这里必须使用 volatile 来排除内存屏障
  private volatile Map<K, V> map;

  public CopyOnWriteMap() {
    map = Collections.emptyMap();
  }

  public CopyOnWriteMap(Map<K, V> sourceMap) {
    this.map = Collections.unmodifiableMap(new HashMap<>(sourceMap));
  }

  @Override
  public int size() {
    return map.size();
  }

  @Override
  public boolean isEmpty() {
    return map.isEmpty();
  }

  @Override
  public boolean containsKey(Object key) {
    return map.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    return map.containsValue(value);
  }

  @Override
  public V get(Object key) {
    return map.get(key);
  }

  @Override
  public Set<K> keySet() {
    return map.keySet();
  }

  @Override
  public Collection<V> values() {
    return map.values();
  }

  @Override
  public Set<Entry<K, V>> entrySet() {
    return map.entrySet();
  }

  @Override
  public synchronized V put(K key, V value) {
    Map<K, V> newMap = new HashMap<>(map);
    V oldVal = newMap.put(key, value);
    this.map = Collections.unmodifiableMap(newMap);
    return oldVal;
  }

  @Override
  public synchronized V remove(Object key) {
    Map<K, V> thisMap = new HashMap<>(map);
    V value = thisMap.remove(key);
    map = Collections.unmodifiableMap(thisMap);
    return value;
  }

  @Override
  public synchronized void putAll(Map<? extends K, ? extends V> m) {
    Map<K, V> thisMap = new HashMap<>(map);
    thisMap.putAll(m);
    map = Collections.unmodifiableMap(thisMap);
  }

  @Override
  public synchronized void clear() {
    map = Collections.emptyMap();
  }

  @Override
  public V putIfAbsent(K key, V value) {
    if (!containsKey(key)) {
      return put(key, value);
    } else {
      return get(key);
    }
  }

  @Override
  public synchronized boolean remove(Object key, Object value) {
    Map<K, V> thisMap = new HashMap<>(map);
    boolean result = thisMap.remove(key, value);
    map = Collections.unmodifiableMap(thisMap);
    return result;
  }

  @Override
  public boolean replace(K key, V oldValue, V newValue) {
    if (containsKey(key) && get(key).equals(oldValue)) {
      put(key, newValue);
      return true;
    } else {
      return false;
    }
  }

  @Override
  public V replace(K key, V value) {
    if (containsKey(key)) {
      return put(key, value);
    } else {
      return null;
    }
  }
}
