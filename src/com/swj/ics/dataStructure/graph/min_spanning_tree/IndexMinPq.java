package com.swj.ics.dataStructure.graph.min_spanning_tree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/02/23 21:49
 * 基于索引的 小顶堆，参照 算法 4 的实现，由于算法是4 的实现太过于绕，我这里使用 oop 进行重写
 * https://algs4.cs.princeton.edu/43mst/IndexMinPQ.java.html
 */
public class IndexMinPq<Key extends Comparable<Key>> implements Iterable<Integer> {

  private int[] priorityQueue;

  private List<IndexPqKeyNode<Key>> nodeList;

  private int size;

  public IndexMinPq(int maxN) {
    priorityQueue = new int[maxN + 1];
    nodeList = new ArrayList<>(maxN + 1);
  }

  /**
   * 插入一个元素
   *
   * @param nodeIndex 节点自身的索引，比如在图中/二叉树中/数组中的索引
   * @param key       节点的关键字
   */
  public void insert(int nodeIndex, Key key) {
    if (nodeList.get(nodeIndex) != null) {
      throw new IllegalArgumentException("index already exists in this priority queue. [nodeIndex=" + nodeIndex + "]");
    }
    size++;
    IndexPqKeyNode<Key> indexPqKeyNode = new IndexPqKeyNode<>(size, key);
    nodeList.add(nodeIndex, indexPqKeyNode);
    // todo：后面将 priorityQueue 的泛型参数改为 IndexPqKeyNode
    priorityQueue[size] = nodeIndex;
    // todo: swim
  }

  /************************************** helper functions ************************************/

  private void swim(int pqIndex) {
    int parent = parent(pqIndex);
    while (pqIndex > 1 && greater(parent, pqIndex)) {
      exchange(pqIndex, parent);
      pqIndex = parent;
      parent = parent(pqIndex);
    }
  }

  private void exchange(int i, int j) {
    int temp = priorityQueue[i];
    priorityQueue[i] = priorityQueue[j];
    priorityQueue[j] = temp;
    // i=2,j=3 2 位置存放的是 6，3 位置存放的是 7，现在 6-7对调，所以 list 里面的第 6 个元素和第 7 个元素的所分别引用的在
    // 二叉堆中的索引也要通过进行更新
    IndexPqKeyNode<Key> node1 = nodeList.get(priorityQueue[i]);
    node1.pqIndex = i;

    IndexPqKeyNode<Key> node2 = nodeList.get(priorityQueue[j]);
    node2.pqIndex = j;
  }

  boolean greater(int i, int j) {
    return nodeList.get(i).key.compareTo(nodeList.get(j).key) > 0;
  }

  int parent(int k) {
    return k >>> 1;
  }

  int left(int k) {
    return k << 1;
  }

  int right(int k) {
    return (k << 1) + 1;
  }

  /**
   * 封装关键字在二叉堆中的数组索引和关键字本身
   */
  private static class IndexPqKeyNode<Key> {

    // 当前对象在二叉堆的中索引下标
    public int pqIndex;
    public Key key;

    public IndexPqKeyNode(int pqIndex, Key key) {
      this.pqIndex = pqIndex;
      this.key = key;
    }

  }


  @Override
  public Iterator<Integer> iterator() {
    return null;
  }
}
