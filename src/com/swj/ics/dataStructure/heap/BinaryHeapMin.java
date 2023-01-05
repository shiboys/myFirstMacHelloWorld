package com.swj.ics.dataStructure.heap;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2022/12/29 15:27
 * 小顶堆。
 * 小顶堆的实现跟大顶堆基本类似，主要是涉及一下几个 api
 * main api ：
 * swim
 * sink
 * delMin
 * insert
 * min
 * buildMinHeap
 * helper api：
 * left,right,parent, exch, greater,resize
 * other additional api:
 * iterator,isMinHeap
 */
public class BinaryHeapMin<Key extends Comparable<Key>> implements Iterable<Key> {

    private Key[] pqArray;
    int size;

    public BinaryHeapMin(int n) {
        pqArray = (Key[]) new Comparable[n + 1];
    }

    public BinaryHeapMin() {
        this(1);
    }

    public BinaryHeapMin(Key[] keys) {
        assert keys != null && keys.length > 0;
        size = keys.length;
        pqArray = (Key[]) new Comparable[keys.length + 1];
        System.arraycopy(keys, 0, pqArray, 1, keys.length);
        buildMinHeap(size);
    }


    void buildMinHeap(int n) {
        if (n < 1) {
            return;
        }
        // 这里是 大于等于1 ，堆底层的数组 0 位置不用。
        for (int i = n / 2; i >= 1; i--) {
            sink(i);
        }
        assert isMinHeap();
    }

    void sink(int k) {
        // 数值大的节点下沉。如果已经到达叶子节点，则不需要下沉了
        while (2 * k <= size) {
            int child = left(k);
            int right = right(k);
            // 如果右边比左边小，则取右边（取左右子节点的最小节点）
            if (right <= size && greater(child, right)) {
                child = right;
            }
            if (!greater(k, child)) {
                break;
            }
            swap(k, child);
            k = child;
        }
    }

    private void swim(int k) {
        int p;
        while (k > 1 && greater((p = parent(k)), k)) {
            swap(k, p);
            k = p;
        }
    }

    public void insert(Key key) {
        if (size == pqArray.length - 1) {
            resize(pqArray.length << 1);
        }
        pqArray[++size] = key;
        swim(size);
        assert isMinHeap();
        // 这种写法的问题是，如果扩容之后，没有新元素增加，则属于浪费式扩容
       /* if (getSize == pqArray.length - 1) {
            resize(pqArray.length << 1);
        }*/
    }

    public Key delMin() {
        if (isEmpty()) {
            throw new NoSuchElementException("priority queue is empty");
        }
        Key min = getMin();
        swap(1, size);
        size--;// 这里必须先 把 size --，保证数据的元素大小有效性之后，再 sink
        sink(1);
        pqArray[size + 1] = null;

        // 这里的容量收缩的 resize 一定是 getSize == (pqArray.length - 1) / 4,而不能是小于等于 <=
        if (size > 0 && size == (pqArray.length - 1) / 4) {
            resize(pqArray.length / 2);
        }
        assert isMinHeap();
        return min;
    }

    public Key getMin() {
        if (isEmpty()) {
            throw new NoSuchElementException("priority queue is empty");
        }
        return pqArray[1];
    }

    /******** ******************************** helper methods ********************************/

    int left(int k) {
        return k << 1;
    }

    int right(int k) {
        return k << 1 + 1;
    }

    int parent(int k) {
        return k >> 1;
    }

    boolean greater(int m, int n) {
        return pqArray[m].compareTo(pqArray[n]) > 0;
    }

    void swap(int i, int j) {
        Key temp = pqArray[i];
        pqArray[i] = pqArray[j];
        pqArray[j] = temp;
    }

    void resize(int newSize) {
        Key[] newArr = (Key[]) new Comparable[newSize];
        System.arraycopy(pqArray, 0, newArr, 0, size);
        pqArray = newArr;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int getSize() {
        return size;
    }

    boolean isMinHeap() {
        for (int i = 1; i <= size; i++) {
            if (pqArray[i] == null)
                return false;
        }
        for (int i = size + 1; i < pqArray.length; i++) {
            if (pqArray[i] != null) {
                return false;
            }
        }
        if (pqArray[0] != null) {
            return false;
        }
        return checkMinHeapOrder(1);
    }

    private boolean checkMinHeapOrder(int k) {
        // 主要是递归判断使用
        if (k > size) {
            return true;
        }
        int left = left(k);
        int right = right(k);
        if (left <= size && greater(k, left)) {
            return false;
        }
        if (right <= size && greater(k, right)) {
            return false;
        }
        return checkMinHeapOrder(left) && checkMinHeapOrder(right);
    }

    @Override
    public Iterator<Key> iterator() {
        return null;
    }

    class MinHeapIterator implements Iterator<Key> {

        private BinaryHeapMin<Key> copy;

        public MinHeapIterator() {
            copy = new BinaryHeapMin<>(getSize());
            for (int i = 1; i <= getSize(); i++) {
                copy.insert(pqArray[i]);
            }
        }

        @Override
        public boolean hasNext() {
            return !copy.isEmpty();
        }

        @Override
        public Key next() {
            return copy.delMin();
        }
    }
}
