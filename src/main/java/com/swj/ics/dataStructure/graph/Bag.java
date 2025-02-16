package com.swj.ics.dataStructure.graph;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class Bag<Item> implements Iterable<Item> {

    private int size;
    private Node<Item> first;

    private static class Node<Item> {
        public Item data;
        public Node<Item> next;

        public Node(Item data, Node<Item> next) {
            this.data = data;
            this.next = next;
        }
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    // 背包只有 add 方法，还是采用头插法
    public void add(Item item) {
        Node<Item> oldFirst = first;
        first = new Node<>(item, oldFirst);
        size++;
    }

    @Override
    public Iterator<Item> iterator() {
        return new NodeIterator();
    }

    private class NodeIterator implements Iterator<Item> {
        private Node<Item> currentNode;

        public NodeIterator() {
            currentNode = first;
        }

        @Override
        public boolean hasNext() {
            return currentNode != null;
        }

        @Override
        public Item next() {
            if (!hasNext()) {
                throw new NoSuchElementException("bag iterator has no more elements");
            }
            Item item = currentNode.data;
            currentNode = currentNode.next;
            return item;
        }
    }
}