package com.swj.ics.dataStructure;

import java.util.AbstractMap;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by swj on 2020/3/9.
 * 实现一个LRU缓存，当缓存到达N之后，需要逐步淘汰最近最少使用的数据。
 * N小时之内，没有被访问的数据也需要淘汰
 */
@Slf4j
public class MapLruCache extends AbstractMap {

    //检查是否超期的线程池
    private ExecutorService checkTimeoutPool;
    private static final int MAX_SIZE = 16;
    private static final ArrayBlockingQueue<Node> QUEUE = new ArrayBlockingQueue<Node>(MAX_SIZE);
    private volatile boolean flag = true;
    //expire time
    private static final int EXPIRE_TIME = 1000;// 60 * 60 * 1000;
    private static final int DEFAULT_ARRAY_SIZE = 8;
    private int arraySize;
    private Object[] array;
    private volatile AtomicInteger size;


    public MapLruCache() {
        arraySize = DEFAULT_ARRAY_SIZE;
        array = new Object[DEFAULT_ARRAY_SIZE];
        //开启一个线程检查最新放入队列的值是否超期,设置为守护线程。
        startCheckTimeoutThread();
    }

    @Override
    public Set<Entry> entrySet() {
        return super.keySet();
    }

    @Override
    public Object put(Object key, Object value) {
        int hash = hash(key);
        int index = hash % arraySize;
        Node currentNode = (Node) array[index];
        if (currentNode == null) {
            currentNode = new Node(null, null, key, value);
            array[index] = currentNode;
            //放入队列
            QUEUE.offer(currentNode);
            sizeUp();
        } else { //hash 冲突了，本次参考JDK7的链地址法
            //
            if (Objects.equals(key, currentNode.key)) {//key存在，就覆盖
                Object oldValue = currentNode.value;
                currentNode.value = value;
                return oldValue;
            } else {
                while (currentNode.next != null) { //不存在则遍历链表
                    currentNode = currentNode.next;
                    if (Objects.equals(key, currentNode.key)) {
                        Object oldValue = currentNode.value;
                        currentNode.value = value;
                        return oldValue;
                    }
                }
                //没有找到匹配的，则新增元素，将元素放到链表的最后，jdk7，将新增加的元素放到链表的最前面
                sizeUp();//保持size的大小，实行LRU
                Node newNode = new Node(currentNode, null, key, value);
                currentNode.next = newNode;
                /**
                 *  Entry<K,V> e = table[bucketIndex];
                 Entry(int h, K k, V v, Entry<K,V> n) {value = v;next = n;key = k;hash = h;}
                 table[bucketIndex] = new Entry<>(hash, key, value, e);//Entry的构造器，创建一个新的结点作为头节点（头插法）
                 size++;//将当前hash表中的数量加1
                 */
                QUEUE.offer(newNode);
            }
        }

        return null;
    }

    @Override
    public Object remove(Object key) {
        int hash = hash(key);
        int index = hash % arraySize;
        Node curNode = (Node) array[index];
        if (curNode == null) { //没有找到元素
            return null;
        }
        if (Objects.equals(key, curNode.key)) {
            array[index] = null;
            sizeDown();
            //从队列移除元素
            QUEUE.poll();
            return curNode;
        } else {
            Node nNode = curNode;
            while (nNode.next != null) {
                nNode = nNode.next;
                if (Objects.equals(key, nNode.key)) {
                    //链表移除节点
                    nNode.prev.next = nNode.next;
                    nNode = null;
                    sizeDown();
                    QUEUE.poll();
                }
            }
        }
        return null;
    }

    @Override
    public Object get(Object key) {
        int hash = hash(key);
        int index = hash % arraySize;
        Node node = (Node)array[index];
        if(node == null) {
            return null;
        }
        if(Objects.equals(key,node.key)) {
            node.setUpdateTime(System.currentTimeMillis());
            return node.value;
        }
        Node curNode = node;
        while (curNode.next != null) {
            curNode = curNode.next;
            if(Objects.equals(key,curNode.key)) {
                node.setUpdateTime(System.currentTimeMillis());
                return curNode.value;
            }
        }
        return null;
    }

    private void sizeDown() {
        if (QUEUE.size() < 1) {
            flag = false;
        }
        this.size.decrementAndGet();
    }

    /**
     * size增加
     */
    private void sizeUp() {
        flag = true;
        if (size == null) {
            size = new AtomicInteger();
        }
        int currSize = size.incrementAndGet();
        //这里并没有使用jdk hashmap的负载因子计算方式
        if (currSize >= MAX_SIZE) {
            //找到队列头部的元素，并移除之。这里没有采用真正的LRU，而是从头部移除
            Node headNode = QUEUE.poll();
            if (headNode == null) {
                throw new RuntimeException("data exception");
            }
            remove(headNode.key);
            lruCallback();
        }
    }

    private void lruCallback() {
        log.info("lru remove!!");
    }

    private int hash(Object key) {
        int h;
        //无符号右移16位在进行抑或运算，在经过扰动算法后可以有效降低hash冲突的概率。
        return key == null ? 0 : ((h = key.hashCode()) ^ (h >>> 16));
    }

    private void test() throws InterruptedException {
        for (int i = 1; i <= 10; i++) {
            QUEUE.offer(new Node(null, null, i, i));
        }

        Thread.sleep(5 * 1000);

        if (QUEUE.size() < 1) {
            flag = false;
        }
        log.info("线程休眠5s之后,队列大小为{},重新向队列装入数据", QUEUE.size());
        for (int i = 11; i <= 20; i++) {
            QUEUE.offer(new Node(null, null, i, i));
        }
        Thread.sleep(5 * 1000);
        log.info("线程休眠10s之后,队列大小为{}", QUEUE.size());
        if (QUEUE.size() > 0) {
            flag = true;
        }

        checkTimeoutPool.execute(new CheckTimeThread());
        Thread.sleep(5 * 1000);
        log.info("线程休眠共15s之后,队列大小为{}，checkThread's alive {}", QUEUE.size(), checkTimeoutPool.isTerminated());
    }


    public static void main(String[] args) {
        MapLruCache lruCache = new MapLruCache();
        try {
            lruCache.test();
        } catch (InterruptedException e) {
            log.error("{}", e);
        }
    }

    /**
     * 开启一个线程检查最新放入队列的值是否超期，并设置为守护线程
     */
    private void startCheckTimeoutThread() {
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat("check-thread-%d")
                .setDaemon(true)
                .build();
        this.checkTimeoutPool = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(1), threadFactory, new ThreadPoolExecutor.AbortPolicy());
        checkTimeoutPool.execute(new CheckTimeThread());
    }

    public class CheckTimeThread implements Runnable {
        @Override
        public void run() {
            while (flag) {
                try {
                    Node node = QUEUE.poll();
                    if (node == null) {
                        continue;
                    }
                    log.info("node.key=" + node.key);
                    Long updateTime = node.getUpdateTime();
                    if ((System.currentTimeMillis() - updateTime) > EXPIRE_TIME) {
                        //todo : remove the node.key
                    }
                } catch (Exception e) {
                    log.error("Interrupted Exception", e);
                }
            }
        }
    }

    private class Node {
        private Node prev;
        private Node next;
        private Object key;
        private Object value;
        private long updateTime;

        public Node(Node pre, Node next, Object key, Object value) {
            this.prev = pre;
            this.next = next;
            this.key = key;
            this.value = value;
            this.updateTime = System.currentTimeMillis();
        }

        public long getUpdateTime() {
            return updateTime;
        }

        public void setUpdateTime(long updateTime) {
            this.updateTime = updateTime;
        }

        @Override
        public String toString() {
            return "Node{" +
                    "key=" + key +
                    ", value=" + value +
                    '}';
        }


    }


}
