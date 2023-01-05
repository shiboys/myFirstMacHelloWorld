package com.swj.ics.dataStructure.heap;

import java.util.Iterator;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2022/12/29 15:25
 * 二叉堆，简介详见 PriorityQueueWithHeap。
 * 默认大顶堆，小顶堆可以参考 BinaryHeapMin
 * 堆的动画演示参考 https://www.cs.usfca.edu/~galles/visualization/Heap.html
 */
public class BinaryHeap<Key extends Comparable<Key>> implements Iterable<Key> {
    Key[] pqArray; //存储元素的数组
    int n;
    int size;

    public BinaryHeap(int maxN) {
        pqArray = (Key[]) new Comparable[maxN + 1];
        n = maxN + 1;
    }

    /**
     * @param k 第 k 个节点
     * @return 父节点
     */
    int parent(int k) {
        return k / 2;
    }

    // 2*k 为 left child
    int left(int k) {
        return k << 1;
    }

    int right(int k) {
        return k << 1 + 1;
    }

    boolean less(int i, int j) {
        return pqArray[i].compareTo(pqArray[j]) < 0;
    }

    void exch(int i, int j) {
        Key tmp = pqArray[i];
        pqArray[i] = pqArray[j];
        pqArray[j] = tmp;
    }

    /**
     * sink, swim, delMax, insert
     */

    void swim(int k) {
        int p = 0;
        // 如果浮到堆顶，则不需要上浮，
        while (k > 1 && less((p = parent(k)), k)) {
            // 如果第 k 个元素比父节点大，则二者互换
            exch(p, k);
            // 继续上浮
            k = p;
        }
    }

    void sink(int k) {
        // 如果 沉到底，则不需要沉了。没有子节点的话，不需要沉
        //while (k <= n/2) {
        while (left(k) <= size) {
            int child = left(k);
            int rightChild = right(k);
            // 我这里只判断less(child, rightChild) 的话 不严谨 if (less(child, rightChild)) {
            if (rightChild <= size && less(child, rightChild))
                child = rightChild;

            // 如果 父节点此时已经大于等于子节点了，则不再 sink
            if (!less(k, child)) {
                break;
            }
            exch(k, child);
            k = child;
        }
    }

    void resize(int newCapacity) {
        assert newCapacity > size;
        // Key instance =
        // Array.newInstance((new Key()).getClass().,newCapacity);
        Key[] newArray = (Key[]) new Comparable[newCapacity];
        System.arraycopy(pqArray, 0, newArray, 0, size);
        pqArray = newArray;
    }

    public void insert(Key key) {
        if (size >= n - 1) {
            resize(size << 1);
        }
        // 把新元素加到最后
        pqArray[++size] = key;
        // 如果只有一个元素，就不需要上浮
        if (size <= 1) {
            return;
        }
        // 将新加到最后的元素上浮到正确的位置
        swim(size);
    }

    public Key delMax() {
        //最大堆顶的元素就是最大元素
        Key max = pqArray[1];
        // 把这个最大元素换到最后，删除之
        exch(1, size);
        pqArray[size] = null;
        size--;
        // 让 paArray[1] 下沉到相应的位置
        sink(1);
        if (size > 0 && size == (pqArray.length - 1) / 4) {
            resize(pqArray.length / 2);
        }
        return max;
    }

    public Key getMax() {
        return pqArray[1];
    }

    public int getSize() {
        return size;
    }

    @Override
    public Iterator<Key> iterator() {
        return null;
    }
}
