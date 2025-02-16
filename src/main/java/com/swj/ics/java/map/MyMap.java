package com.swj.ics.java.map;

/**
 * author shiweijie
 * date 2018/3/29 下午7:40
 */
public interface MyMap<K,V> {
    V get(K k);
    V put(K k, V v);

    interface Entry<K,V> {
        K getKey();
        V getValue();
    }
}
