package com.swj.ics.dataStructure;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * author shiweijie
 * date 2018/4/16 下午3:25
 */
public class MyLinkedList<T> implements Iterable<T> {

    private Node<T> beforeHead;
    private Node<T> afterEnd;
    private int size;
    private int modCount;

    private Node<T> getNode(int index) {
        int mid = size / 2;
        Node p = null;
        if (index < mid) {
            p = beforeHead.next;
            int start = 0;
            while (start < index) {
                p = p.next;
                start ++;
            }

        } else {
            p = afterEnd;
            int start = size;
            while (start > index) {
                p = p.prev;
                start --;
            }
        }
        return p;
    }

    public T get(int index) {
        if(index < 0 || index > size) {
            throw new IndexOutOfBoundsException();
        }
        Node<T> node = getNode(index);

        return node == null ? null : node.data;
    }

    public T set(int index,T newData) {
        if(index < 0 || index > size) {
            throw new IndexOutOfBoundsException();
        }
        Node<T> node = getNode(index);
        T oldValue = node.data;
        node.data = newData;
        return oldValue;
    }

    private int size() {
        return size;
    }

    private boolean addBefore(T data,int index) {
     Node node  = getNode(index);
     Node<T> newNode = new Node(data,null,node);
     newNode.prev = node.prev;
     node.prev.next = newNode;
     node.prev = newNode;

     size++;
     modCount ++ ;
     return true;
    }

    public boolean add(T data) {
       return add(data,size);
    }

    public boolean add(T data,int idx) {
        if (idx < 0 || idx > size) {
            throw new IndexOutOfBoundsException();
        }
        return addBefore(data,idx);
    }

    public T remove(int idx) {
        Node<T> node = getNode(idx);
        return remove(node);
    }

    public T remove(Node<T> node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;

        size -- ;
        modCount ++;
        return node.data;
    }




    public MyLinkedList() {
        clear();
    }



    private void clear() {
        beforeHead = new Node(null,null,null);
        afterEnd = new Node<>(null,beforeHead,null);
        beforeHead.next = afterEnd;
        size = 0;
    }

    private class Node<T> {
        private T data;
        private Node<T> prev;
        private Node<T> next;

        public Node(T data,Node<T> p,Node<T> n) {
            this.data = data;
            this.prev = p;
            this.next = n;
        }
    }


    @Override
    public Iterator<T> iterator() {
        return new MyLinkedListIterator();
    }

    private class MyLinkedListIterator implements Iterator<T> {

        Node<T> startNode = beforeHead.next;
        boolean canBeDelete = false;
        int expectedCount = modCount;

        @Override
        public boolean hasNext() {

            return startNode.next != null;
        }

        @Override
        public T next() {
            if(!hasNext()) {
                throw new NoSuchElementException("no such element");
            }
            if (modCount != expectedCount) {
                throw new ConcurrentModificationException("并发修改");
            }
            T data = startNode.data;
            startNode = startNode.next;
            canBeDelete = true;
            return data;
        }

        @Override
        public void remove() {
            if(!canBeDelete) {
                throw new IllegalStateException("can not be delete");
            }
            if (modCount != expectedCount) {
                throw new ConcurrentModificationException();
            }
            //MyLinkedList.this.remove(startNode);
            MyLinkedList.this.remove(startNode.prev);//删除当前节点的上一个节点。
            canBeDelete = false;
            expectedCount ++;
        }
    }
}
