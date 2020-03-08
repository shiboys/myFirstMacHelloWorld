package com.swj.ics.java.juc;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

/**
 * Created by swj on 2020/2/23.
 */
public class BlockQueueTest {

    public static void main(String[] args) throws InterruptedException {
        BlockingQueue<Integer> queue = new SynchronousQueue<>();
        System.out.println(queue.offer(1) + "");
        System.out.println(queue.offer(2) + "");
        System.out.println(queue.offer(3) + "");
        System.out.println(queue.take());
        System.out.println(queue.size());
        /**
         * 打印结果：
         * false
         * false
         * false
         * 阻塞
         *
         */
    }
}
