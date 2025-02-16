package com.swj.ics.dataStructure.queue;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/01/19 09:28
 * 使用链表实现的队列，参考算法4的实现
 * https://algs4.cs.princeton.edu/13stacks/Queue.java.html
 */
public class Queue<T> implements Iterable<T> {

  private static class Node<T> {
    public T item;
    public Node<T> next;
  }


  Node<T> first, last;
  private int size;

  public boolean isEmpty() {
    return first == null;
  }


  public void enqueue(T item) {
    Node<T> node = new Node<>();
    node.item = item;
    if (first == null) {
      first = last = node;
    } else {
      last.next = node;
      last = node;
    }
    size++;
  }

  public int size() {
    return size;
  }
  public T dequeue() {
    if (isEmpty()) {
      throw new RuntimeException("queue is empty now.");
    }
    Node<T> node = first;
    first = first.next;
    if (first == null) {
      last = null;
    }
    size--;
    return node.item;
  }

  public T peek() {
    if (isEmpty()) {
      throw new NoSuchElementException("queue is empty");
    }
    return first.item;
  }

  @Override
  public Iterator<T> iterator() {
    return new QueueIterator();
  }

  /**
   * 队列的实现，重要的方法时 enqueue 和 dequeue，同时还要实现 可迭代接口
   */

  class QueueIterator implements Iterator<T> {
    Node<T> cursor = null;

    public QueueIterator() {
      cursor = first;
    }

    @Override
    public boolean hasNext() {
      return cursor != null;
    }

    @Override
    public T next() {
      T item = cursor.item;
      cursor = cursor.next;
      return item;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("can not remove");
    }
  }

  /*public static void main(String[] args) {
    int max = 1 << 6;
    int middle = max >> 1;
    Queue<Integer> queue = new Queue<>();
    for (int i = 1; i <= max; i++) {
      queue.enqueue(i);
    }
    int counter = 1;
    System.out.println("iterator:");
    for (Integer val : queue) {
      System.out.print(val + "\t");
      if (counter++ % 10 == 0) {
        System.out.println();
      }
    }
    System.out.println();
    System.out.println("dequeue items:");
    for (int i = 0; i < middle; i++) {
      System.out.print(queue.dequeue()+"\t");
    }
    System.out.println();
  }*/
}
