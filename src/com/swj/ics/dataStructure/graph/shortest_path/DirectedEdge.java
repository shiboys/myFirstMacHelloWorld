package com.swj.ics.dataStructure.graph.shortest_path;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/02/25 14:38
 * 带有方向的边，有向边
 */
public class DirectedEdge {
  private final int headNodeIndex;
  private final int tailNodeIndex;
  private final double weight;

  public DirectedEdge(int head, int tail, double weight) {
    this.headNodeIndex = head;
    this.tailNodeIndex = tail;
    this.weight = weight;
  }

  public int getHeadNodeIndex() {
    return headNodeIndex;
  }

  public int getTailNodeIndex() {
    return tailNodeIndex;
  }

  public double getWeight() {
    return weight;
  }

  @Override
  public String toString() {
    return headNodeIndex + "->" + tailNodeIndex + ", weight=" + String.format("%5.2f", weight);
  }
}
