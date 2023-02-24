package com.swj.ics.dataStructure.graph.min_spanning_tree;

import java.util.Iterator;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/02/23 21:49
 * 基于索引的 小顶堆，参照 算法 4 的实现，由于算法是4 的实现太过于绕，我这里使用 oop 进行重写
 * https://algs4.cs.princeton.edu/43mst/IndexMinPQ.java.html
 */
public class IndexMinPq<Key extends Comparable<Key>> implements Iterable<Integer> {

  private int[] priorityQueue;

  private IndexPqKeyNode<Key>[] nodeArray;

  private int size;

  public IndexMinPq(int maxN) {
    priorityQueue = new int[maxN + 1];
    nodeArray = (IndexPqKeyNode<Key>[]) new IndexPqKeyNode[maxN + 1];
  }

  /**
   * 插入一个元素
   *
   * @param nodeIndex 节点自身的索引，比如在图中/二叉树中/数组中的索引
   * @param key       节点的关键字
   */
  public void insert(int nodeIndex, Key key) {
    if (contains(nodeIndex)) {
      throw new IllegalArgumentException("index already exists in this priority queue. [nodeIndex=" + nodeIndex + "]");
    }
    size++;
    IndexPqKeyNode<Key> indexPqKeyNode = new IndexPqKeyNode<>(size, key);
    nodeArray[nodeIndex] = indexPqKeyNode;
    // todo：后面将 priorityQueue 的泛型参数改为 IndexPqKeyNode
    priorityQueue[size] = nodeIndex;
    swim(size);
    // 这个数据结构比较复杂，就不支持扩容和缩容
  }

  public boolean isEmpty() {
    return size == 0;
  }

  public int getSize() {
    return size;
  }

  /**
   * 指定图中/树中等结构中的索引的节点是否存在
   *
   * @param nodeIndex 元素所在结构的索引
   * @return true or false
   */
  public boolean contains(int nodeIndex) {
    return nodeIndex < nodeArray.length && nodeArray[nodeIndex] != null;
  }

  public int minNodeIndex() {
    return priorityQueue[1];
  }

  public Key minKey() {
    return nodeArray[priorityQueue[1]].key;
  }

  // todo:keyOf, changeKey, increaseKey, decreaseKey, delete
  public Key keyOf(int nodeIndex) {
    return nodeArray[nodeIndex].key;
  }

  public void changeKey(Key key, int nodeIndex) {
    if (!contains(nodeIndex)) {
      throw new IllegalArgumentException(
          "nodeIndex does not exists int the priority queue. [nodeIndex=" + nodeIndex + "]");
    }

    IndexPqKeyNode<Key> pqKeyNode = nodeArray[nodeIndex];
    pqKeyNode.key = key;

    // key 改变了，对应的优先级也需要跟着变动
    // 最小堆，先尝试上浮，然后再下沉
    int pqIndex = pqKeyNode.pqIndex;
    swim(pqIndex);
    sink(pqIndex);
  }

  /**
   * 增大指定节点索引所表示的元素的key 到 指定的 newKey
   *
   * @param newKey    新的 Key
   * @param nodeIndex 节点自身的索引
   */
  public void increaseKey(Key newKey, int nodeIndex) {
    if (newKey == null) {
      throw new IllegalArgumentException("newKey is null.");
    }
    if (!contains(nodeIndex)) {
      throw new IllegalArgumentException("node is not exists in the priority queue. [nodeIndex=" + nodeIndex + "]");
    }
    IndexPqKeyNode<Key> pqKeyNode = nodeArray[nodeIndex];
    if (newKey.compareTo(pqKeyNode.key) == 0) {
      throw new IllegalArgumentException("calling increaseKey with a key is equal to the key in the priority queue");
    }
    if (newKey.compareTo(pqKeyNode.key) < 0) {
      throw new IllegalArgumentException(String.format(
          "calling increaseKey with a key is less than the key in the priority queue. [nweKey=%s, pq key= %s]",
          newKey, pqKeyNode.key));
    }
    pqKeyNode.key = newKey;
    // key 变大了, 需要下沉来维护堆的平衡
    sink(pqKeyNode.pqIndex);
  }

  public void decreaseKey(Key newKey, int nodeIndex) {
    if (newKey == null) {
      throw new IllegalArgumentException("newKey is null.");
    }
    if (!contains(nodeIndex)) {
      throw new IllegalArgumentException("node is not exists in the priority queue. [nodeIndex=" + nodeIndex + "]");
    }
    IndexPqKeyNode<Key> pqKeyNode = nodeArray[nodeIndex];
    if (newKey.compareTo(pqKeyNode.key) == 0) {
      throw new IllegalArgumentException("calling decreaseKey with a key is equal to the key in the priority queue");
    }
    if (newKey.compareTo(pqKeyNode.key) > 0) {
      throw new IllegalArgumentException(String.format(
          "calling decreaseKey with a key is more than the key in the priority queue. [nweKey=%s, pq key= %s]",
          newKey, pqKeyNode.key));
    }
    pqKeyNode.key = newKey;
    // key 变小了，需要上浮以维持堆的平衡
    swim(pqKeyNode.pqIndex);
  }

  public void delete(int nodeIndex) {
    if (!contains(nodeIndex)) {
      throw new IllegalArgumentException(
          "nodeIndex does not exists int the priority queue while deleting. [nodeIndex=" + nodeIndex + "]");
    }
    IndexPqKeyNode<Key> pqKeyNode = nodeArray[nodeIndex];
    int pqIndex = pqKeyNode.pqIndex;
    exchange(pqIndex, size);
    size--;
    swim(pqIndex);
    sink(pqIndex);
    nodeArray[nodeIndex] = null;
    priorityQueue[size + 1] = -1;
  }

  public int deleteMin() {
    if (isEmpty()) {
      throw new RuntimeException("priority queue is null");
    }
    exchange(1, size);
    int oldNodeIndex = priorityQueue[size];
    priorityQueue[size] = -1;
    size--;
    nodeArray[oldNodeIndex] = null;
    sink(1);
    return oldNodeIndex;
  }

  private void sink(int k) {
    int left = left(k);
    int right = right(k);
    // k 节点比父节点大，需要下沉
    while (left <= size) {
      int child = left;
      // 如果右子节点比左子节点小，则将右子节点跟父节点交换, 将当前节点 k 交换到一个相对较小的子节点上。
      if (right <= size && greater(left, right)) {
        child = right;
      }
      // 如果此时 k 节点已经不比父节点大，则停止下沉。
      if (!greater(k, child)) {
        break;
      }
      exchange(k, child);
      k = child;
      left = left(k);
      right = right(k);
    }
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
    IndexPqKeyNode<Key> node1 = nodeArray[priorityQueue[i]];
    node1.pqIndex = i;

    IndexPqKeyNode<Key> node2 = nodeArray[priorityQueue[j]];
    node2.pqIndex = j;
  }

  boolean greater(int i, int j) {
    int nodeIndex1 = priorityQueue[i];
    int nodeIndex2 = priorityQueue[j];
    return nodeArray[nodeIndex1].key.compareTo(nodeArray[nodeIndex2].key) > 0;
  }

  int parent(int k) {
    return k >> 1;
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
