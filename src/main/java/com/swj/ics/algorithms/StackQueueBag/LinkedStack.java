package com.swj.ics.algorithms.StackQueueBag;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * author shiweijie
 * date 2018/5/5 下午3:30
 * 基于链表实现栈
 */
public class LinkedStack<Item> implements Iterable<Item> {

    private Node first;
    private int n;

    private class Node {
        Item item;
        Node next;

        public Node(Item item) {
            this.item = item;
            this.next = null;
        }
    }

    public void push(Item item) {
        Node node = new Node(item);
        if (first == null) {
            first = node;
        } else {
            node.next = first;
            first = node;
        }
        n++;
    }

    public Item pop() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        Node oldNode = first;
        first = oldNode.next;
        n--;

        return oldNode.item;
    }

    public boolean isEmpty() {
        return first == null;
    }

    public int size() {
        return n;
    }

    class MyLinkedStackIterator implements Iterator<Item> {

        private Node current = first ;


        @Override
        public boolean hasNext() {
            return current != null;
        }

        @Override
        public Item next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            Node oldNode = current;
            current = current.next;
            return oldNode.item;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public Iterator<Item> iterator() {
        return new MyLinkedStackIterator();
    }

    public static void main(String[] args) {
        LinkedStack<Integer> myStack = new LinkedStack<>();
        int[] arr = {1, 3, 5, 7, 9, 2, 4, 6, 8};
        for (int item : arr) {
            myStack.push(item);
        }

        for (int item : myStack) {
            System.out.print(item + ",");
        }
        System.out.println();
    }
}
