package com.swj.ics.dataStructure;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by swj on 2020/3/9.
 * 实现一个LRU缓存，当缓存到达N之后，需要逐步淘汰最近最少使用的数据。
 * N小时之内，没有被访问的数据也需要淘汰
 */
@Slf4j
public class LruCache {

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


    public LruCache() {
        arraySize = DEFAULT_ARRAY_SIZE;
        array = new Object[DEFAULT_ARRAY_SIZE];
        //开启一个线程检查最新放入队列的值是否超期
        startCheckTimeoutThread();
    }

    private void test() throws InterruptedException {
        for (int i = 1; i <= 10; i++) {
            QUEUE.offer(new Node(null, null, i, i));
        }

        Thread.sleep(5 * 1000);
        
        if(QUEUE.size() < 1) {
            flag =false;
        }
        log.info("线程休眠5s之后,队列大小为{},重新向队列装入数据",QUEUE.size());
        for (int i = 11; i <= 20; i++) {
            QUEUE.offer(new Node(null, null, i, i));
        }
        Thread.sleep(5 * 1000);
        log.info("线程休眠10s之后,队列大小为{}",QUEUE.size());
        if(QUEUE.size() > 0) {
            flag =true;
        }
        
        checkTimeoutPool.execute(new CheckTimeThread());
        Thread.sleep(5 * 1000);
        log.info("线程休眠共15s之后,队列大小为{}，checkThread's alive {}",QUEUE.size(),checkTimeoutPool.isTerminated());
    }

    public static void main(String[] args) {
        LruCache lruCache = new LruCache();
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
