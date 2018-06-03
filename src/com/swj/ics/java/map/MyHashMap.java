package com.swj.ics.java.map;

import java.util.ArrayList;
import java.util.List;

/**
 * author shiweijie
 * date 2018/3/27 上午9:59
 */
public class MyHashMap<K,V> implements MyMap<K,V> {

    private static final int DEFAULT_CAPACITY_SIZE = 16;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;


    private int capacitySize;
    private float loadFactor ;

    Entry<K,V>[] table = null;

    private int nodeUsedSize = 0;


    public MyHashMap() {
        this(DEFAULT_CAPACITY_SIZE,DEFAULT_LOAD_FACTOR);
    }

    public MyHashMap(int capacitySize,float loadFactor) {

        if (capacitySize <= 0) {
            throw new IllegalArgumentException("illegal initial capacitySize : " + capacitySize );
        }
        if (loadFactor <= 0 || Float.isNaN(loadFactor)) {
            throw new IllegalArgumentException("illegal load factor:" + loadFactor);
        }
        this.capacitySize = capacitySize;
        this.loadFactor = loadFactor;

        table = (Entry<K, V>[]) new Entry[capacitySize];
    }


    private int hash(K k) {
        int hashCode = k.hashCode();
        hashCode = hashCode ^ (hashCode >>>20) ^ (hashCode >>>12);
        return hashCode ^ (hashCode >>> 7) ^ (hashCode >>> 4);
    }


    class Entry<K,V> implements MyMap.Entry<K,V> {

        private K key;
        private V value;
        private Entry<K,V> next;

        public Entry(K k,V v,Entry<K,V> next) {
            this.key = k;
            this.value = v;
            this.next = next;
        }

        public Entry() {

        }

        @Override
        public K getKey() {

            return key;
        }

        @Override
        public V getValue() {
            return value;
        }
    }

    @Override
    public V get(K key) {
        int bucketIndex = hash(key) & (capacitySize -1);
        if (table[bucketIndex] == null) {
            return null;
        }
        Entry<K,V> node = table[bucketIndex];
        do{
            if(node.getKey() == key || node.getKey().equals(key)) {
                return node.getValue();
            }
            node = node.next;
        } while(node != null);

        return null;
    }

    @Override
    public V put(K key, V value) {
        if (nodeUsedSize >= capacitySize * loadFactor) {
            resize( capacitySize * 2);
        }
        int bucketIndex = hash(key) & (capacitySize - 1);

        Entry<K,V> node = table[bucketIndex];
        if (node == null) {
            table[bucketIndex] = new Entry<K,V>(key,value,null);
            nodeUsedSize++;
        } else {
            Entry<K,V> e = node;
            do{
                if(e.getKey() == key || (key != null && key.equals(e.getKey()))) {
                    V oldValue = e.getValue();
                    e.value = value;
                    return oldValue;
                }
                e = e.next;
            } while (e != null) ;
            //指向链表头节点
            table[bucketIndex] = new Entry<>(key,value,node);
            nodeUsedSize++;
        }

        return null;
    }

    private void resize(int newCapacity) {
        this.nodeUsedSize = 0;
        this.capacitySize = newCapacity;
        rehash(newCapacity);
    }

    private void rehash(int size) {

        List<Entry<K,V>> nodeList = new ArrayList<>();
        for(int i =0 ;i < table.length ;i++) {
            Entry<K,V> node = table[i];
            if (node != null) {
                nodeList.add(node);
                while (node.next != null) {
                    node = node.next;
                    nodeList.add(node);
                }
            }

        }

        table = (Entry<K, V>[]) new Entry[size];
        for(Entry<K,V> node : nodeList) {
            put(node.getKey(),node.getValue());
        }
    }

    public static void main(String[] args) {

       // swapWithoutNewVariableDemo();

        testMyHashMap();

    }

    static void testMyHashMap() {
        MyHashMap<String,String> myMap = new MyHashMap<>();
        for(int i=0;i < 500;i++) {
            myMap.put("key"+i,"value"+i);
        }


        for(int i=0;i < 500;i++) {
            System.out.println("key is key" + i +",value is " + myMap.get("key"+i));
        }

    }

    static void swapWithoutNewVariableDemo() {
        int a =3 ;
        int b = 7 ;

        a = a + b;
        b = a - b;
        a = a - b;

        System.out.println("a = " + a +", b = " + b);

        int x =3 ;
        int y = 7 ;

        //这里要判断 a != b 因为a == b 的话 a^b = 0;
        x = x ^ y;
        y = y ^ x;
        x = x ^ y;

        System.out.println("x = " + a +", y = " + b);

    };

}
