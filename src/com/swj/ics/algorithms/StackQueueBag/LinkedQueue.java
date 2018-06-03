package com.swj.ics.algorithms.StackQueueBag;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * author shiweijie
 * date 2018/5/6 下午4:36
 * 队列的链表实现
 */
public class LinkedQueue<Item> implements Iterable<Item> {

    private int size = 0;
    private Node first;
    private Node last;

    private class Node {
        private Item data;
        private Node next;

        public Node(Item data) {
            this.data = data;
        }
    }

    public boolean isEmpty() {
        return first == null;
    }

    public void enqueue(Item item) {
        Node node = new Node(item);
        if (isEmpty()) {
            first = last = node;
        } else {
            last.next = node;
            last = node;
        }
        size++;
    }

    public Item dequeue() {
        if (isEmpty()) {
            throw new NoSuchElementException("queue is empty !");
        }
        Node oldNode = first;
        Item data = oldNode.data;
        first = first.next;

        if (isEmpty()) {
            last = null;
        }
        oldNode = null;
        size--;
        return data;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Item data : this) {
            stringBuilder.append(data + " ");
        }
        return stringBuilder.toString();
    }

    private class MyLinkedQueueIterator implements Iterator<Item> {
        private Node current = first;

        @Override
        public boolean hasNext() {
            return current != null;
        }

        @Override
        public Item next() {
            if (!hasNext()) {
                throw new NoSuchElementException("queue is empty");
            }
            Item item = current.data;
            current = current.next;
            return item;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }


    @Override
    public Iterator<Item> iterator() {
        return new MyLinkedQueueIterator();
    }

    public static void main(String[] args) {
        LinkedQueue<Integer> myQueue = new LinkedQueue<>();
        int[] arr = {1, 3, 5, 7, 9, 2, 4, 6, 8};
        for (int item : arr) {
            myQueue.enqueue(item);
        }
        System.out.println("deQueue...");
        for (int i = 0; i < 5; i++) {
            System.out.print(myQueue.dequeue() + ",");
        }

        System.out.println("\n enqueue again...");

        int[] arr2 = {11, 13, 15, 17, 19};

        for (int item2 : arr2) {
            myQueue.enqueue(item2);
        }
        System.out.println("iterator...");

        System.out.print(myQueue + ",");

        System.out.println();
    }

}
