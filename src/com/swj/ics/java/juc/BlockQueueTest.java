package com.swj.ics.java.juc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;

/**
 * Created by swj on 2020/2/23.
 */
public class BlockQueueTest {

  public static void main(String[] args) throws InterruptedException, BrokenBarrierException {
  /*  BlockingQueue<Integer> queue = new SynchronousQueue<>();
    System.out.println(queue.offer(1) + "");
    System.out.println(queue.offer(2) + "");
    System.out.println(queue.offer(3) + "");
    System.out.println(queue.take());
    System.out.println(queue.size());*/
    /**
     * 打印结果：
     * false
     * false
     * false
     * 阻塞
     *
     */

      /*  // 初始化有 10 个共享资源，第 2 个参数是否公平
        Semaphore semaphore = new Semaphore(10,true);
        // 每次获取一个，如果获取不到，线程就会阻塞
        semaphore.acquire();
        // 用完释放
        semaphore.release();

        CyclicBarrier cyclicBarrier = new CyclicBarrier(10);
        cyclicBarrier.await();*/
    /*int length = 4;
    ExchangeThread[] threads = new ExchangeThread[length];
    Exchanger<String> exchanger = new Exchanger<>();
    for (int i = 0; i < threads.length; i++) {
      threads[i] = new ExchangeThread(String.valueOf((char) ('A' + i)), exchanger, length);
      threads[i].start();
    }
    Thread.sleep(2000);*/
    /*List<String> bufferList1 = new ArrayList<>();
    List<String> bufferList2 = new ArrayList<>();
    Exchanger<List<String>> exchanger = new Exchanger<>();
    Producer producer = new Producer(exchanger, bufferList1);
    Consumer consumer = new Consumer(exchanger,bufferList2);
    producer.start();
    consumer.start();*/
    System.out.println(1|2);
    System.out.println(1&2);
    System.out.println(0&2);
    long l = 0x80000001;
    System.out.println(Integer.toBinaryString((int)l));

    Object a = null;
    Object b = a;
    System.out.println(a == b);
    System.out.println(null == null);

    int[] arr = new int[] {10,20};
    int oldLevel = 0;
    int level=1;
    System.out.println(arr[level = oldLevel]);
    System.out.println(level);
    System.out.println(Integer.numberOfLeadingZeros(1));
    System.out.println(Integer.numberOfLeadingZeros(-1));
    System.out.println(Integer.numberOfLeadingZeros(2));
    System.out.println(Integer.numberOfLeadingZeros(-2));
  }

  static class ExchangeThread extends Thread {
    private String data;
    private Exchanger<String> exchanger;
    private Set<String> dataKeySet;
    private int dataKeySize;

    public ExchangeThread(String data, Exchanger<String> exchanger, int keySize) {
      this.data = data;
      this.exchanger = exchanger;
      dataKeySet = new HashSet<>();
      dataKeySize = Math.max(1, keySize - 1);
    }

    @Override
    public void run() {
      try {
        String exchangeData;
        while ((exchangeData = exchanger.exchange(this.data)) != null && !dataKeySet.contains(exchangeData)) {
          dataKeySet.add(exchangeData);
          System.out.println("self:" + data + ", other:" + exchangeData);
          if (dataKeySet.size() >= dataKeySize) {
            break;
          }
        }

      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }


  static class Producer extends Thread {
    private Exchanger<List<String>> exchanger;
    private List<String> bufferList;

    public Producer(Exchanger<List<String>> exchanger, List<String> bufferList) {
      this.exchanger = exchanger;
      this.bufferList = bufferList;
    }

    @Override
    public void run() {
      for (int i = 1; i < 5; i++) {
        System.out.println("生产者第" + i +"次提供");

        for (int j = 1; j < 4; j++) {
          String seq = i + "__" + j;
          System.out.println("producer:" + seq);
          bufferList.add("buffer:" + seq);
        }
        System.out.println("生产者装满，期待与消费者交换...");

        try {
          // exchange 之后，返回一个空篮子
          // bufferList = exchanger.exchange(bufferList);
          // 接收返回值，也可以不接受返回值，不接收的话，一直用的是生产者的 list 对象，
          // 接收到话，返回的是消费者的 list 对象，消费者这边只要保证传过来的list 是空集合就行
          exchanger.exchange(bufferList);
          TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
  }


  static class Consumer extends Thread {
    private Exchanger<List<String>> exchanger;
    private List<String> bufferList;

    public Consumer(Exchanger<List<String>> exchanger, List<String> bufferList) {
      this.exchanger = exchanger;
      this.bufferList = bufferList;
    }

    @Override
    public void run() {
      for (int i = 1; i < 5; i++) {

        try {
          // 调用 exchange 跟生产者进行交换
          bufferList = exchanger.exchange(bufferList);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }

        System.out.println("消费者第" + i + "次提取");
        for (int j = 1; j < 4; j++) {
          System.out.println("consumer:" + bufferList.get(0));
          bufferList.remove(0);
        }
      }
    }
  }
}
