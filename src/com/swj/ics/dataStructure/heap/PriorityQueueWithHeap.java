package com.swj.ics.dataStructure.heap;

import java.util.ArrayList;
import java.util.List;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2022/12/29 15:16
 * 优先级队列，采用堆结构的，参考 算法4的 2.6 部分和源码 https://algs4.cs.princeton.edu/24pq/Heap.java.html
 * 参考 labuladong 的算法二叉堆 https://github.com/labuladong/fucking-algorithm/blob/master/%E6%95%B0%E6%8D%AE%E7%BB%93%E6%9E%84%E7%B3%BB%E5%88%97/%E4%BA%8C%E5%8F%89%E5%A0%86%E8%AF%A6%E8%A7%A3%E5%AE%9E%E7%8E%B0%E4%BC%98%E5%85%88%E7%BA%A7%E9%98%9F%E5%88%97.md
 * 二叉堆（Binary Heap）没什么神秘的，性质比二叉搜索树（BST）还简单。主要操作就是 2 个：sink(下沉）和 swim（上浮），用以维护二叉堆的性质。
 * 其主要应用有两个，首先是一种排序方法「堆排序」，第二是一种很有用的数据结构「优先级队列」
 */
public class PriorityQueueWithHeap {
  static <T extends Comparable<T>> void topMax(int size, Comparable<T>[] array) {
    BinaryHeap heap = new BinaryHeap(size);
    for (Comparable<T> item : array) {
      heap.insert(item);
    }
    List<Comparable<T>> topNList = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      topNList.add(heap.delMax());
    }
    System.out.println(topNList);
  }

  public static void main(String[] args) {
    int[] array = new int[] {6, 7, 8, 9, 10, 5, 4, 3, 2, 1};
    int size = 3;
    // topMax(size, array);
    PriorityQueueWithHeap pq = new PriorityQueueWithHeap();
    System.out.println(pq.findKthLargest(array, size));
  }

  /**
   * leetcode 解题 215. Kth Largest Element in an Array
   * 根据 Jdk priority Queue 的思想，不利用外部存储空间，直接在原数组建大顶堆
   * 然后 在循环 k 次删除最小元素
   * jdk 的 swim 和 sink 并不是用采用 交换的方式，而是直接替换，直到找到正确的位置，并将之替换掉
   *
   * @param nums
   * @param k
   * @return
   */
  public int findKthLargest(int[] nums, int k) {
    for (int i = nums.length >>> 1; i >= 0; i--) {
      // heapify
      sink(nums, i, nums.length - 1);
    }
    int result = nums[0];
    for (int i = 0; i < k; i++) {
      int size = nums.length - 1 - i;
      result = delMax(nums, size);
    }
    return result;
  }

  public int delMax(int[] arr, int size) {
    int result = arr[0];
    swap(arr, 0, size);
    sink(arr, 0, size - 1);
    return result;
  }

  void sink(int[] arr, int k, int heapSize) {
    int left = k * 2 + 1;
    int right = left + 1;
    while (left <= heapSize) {
      int child = left;
      if (right <= heapSize && arr[right] > arr[left]) {
        child = right;
      }
      if (arr[k] >= arr[child]) {
        break;
      }
      swap(arr, k, child);
      k = child;
      left = k * 2 + 1;
      right = left + 1;
    }
  }

  void swap(int[] arr, int i, int j) {
    int temp = arr[i];
    arr[i] = arr[j];
    arr[j] = temp;
  }

}
