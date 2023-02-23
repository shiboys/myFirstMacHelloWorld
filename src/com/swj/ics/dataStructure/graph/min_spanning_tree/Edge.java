package com.swj.ics.dataStructure.graph.min_spanning_tree;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/02/23 14:50
 * 带权重的图的边
 */
public class Edge implements Comparable<Edge> {
  private int headNodeIndex;
  private int tailNodeIndex;
  private double weight;

  public Edge(int headNodeIndex, int tailNodeIndex, double weight) {
    if (headNodeIndex < 0) {
      throw new IllegalArgumentException("head node index of an edge must be non-negative integer");
    }
    if (tailNodeIndex < 0) {
      throw new IllegalArgumentException("tail node index of an edge must be non-negative integer");
    }
    if (Double.isNaN(weight)) {
      throw new IllegalArgumentException("weight is an nan number");
    }
    this.headNodeIndex = headNodeIndex;
    this.tailNodeIndex = tailNodeIndex;
    this.weight = weight;
  }

  public Edge() {
  }

  public double getWeight() {
    return weight;
  }

  public int head() {
    return headNodeIndex;
  }

  public int tail(int head) {
    if (head == headNodeIndex) {
      return tailNodeIndex;
    } else if (head == tailNodeIndex) {
      return headNodeIndex;
    } else {
      throw new IllegalArgumentException(String.format("illegal endpoint. [head=%d]", head));
    }
  }

  @Override
  public String toString() {
    // %.5f ，后面跟 5 位小数
    return String.format("%d-%d %.5f", headNodeIndex, tailNodeIndex, weight);
  }

  @Override
  public int compareTo(Edge other) {
    return Double.compare(weight, other.weight);
  }
}
