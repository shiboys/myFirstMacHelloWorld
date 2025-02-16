package com.swj.ics.dataStructure.graph.min_spanning_tree;

import com.swj.ics.dataStructure.graph.GraphUtil;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/02/23 21:07
 * 基于 Prim 算法的非延迟最小生成树——优先级队列中不存在无用的边
 */
public class PrimMST {
  // 到某个节点的最小生成树的边，index 为节点 index，值为边对象
  private Edge[] edgeTo;
  /**
   * 到某个节点的最小权重
   */
  private double[] weightTo;

  private IndexMinPq<Double> indexMinPq;

  private boolean[] marked;

  public PrimMST(EdgeWeightedGraph graph) {
    this.edgeTo = new Edge[graph.nodeSize()];
    this.weightTo = new double[graph.nodeSize()];
    this.marked = new boolean[graph.nodeSize()];
    this.indexMinPq = new IndexMinPq<>(graph.nodeSize());
    for (int i = 0, len = graph.nodeSize(); i < len; i++) {
      weightTo[i] = Double.POSITIVE_INFINITY;
    }

    for (int i = 0, len = graph.nodeSize(); i < len; i++) {
      if (!marked[i]) {
        prim(graph, i);
      }
    }
  }

  private void prim(EdgeWeightedGraph graph, int nodeIndex) {
    // 将当前节点到当前节点的权重改为 0
    weightTo[nodeIndex] = 0.0;
    // 先填充优先级队列
    indexMinPq.insert(nodeIndex, weightTo[nodeIndex]);
    while (!indexMinPq.isEmpty()) {
      // 从优先级队列中取出最小权重的节点
      int minWeightNodeIndex = indexMinPq.deleteMin();
      // 对最小权重的节点所连接的边进行扫描
      scan(graph, minWeightNodeIndex);
    }
  }

  private void scan(EdgeWeightedGraph graph, int minWeightNodeIndex) {
    marked[minWeightNodeIndex] = true;
    Iterable<Edge> linkedNeighbors = graph.linkedNeighbors(minWeightNodeIndex);
    for (Edge edge : linkedNeighbors) {
      int tailNodeIndex = edge.tail(minWeightNodeIndex);
      if (marked[tailNodeIndex]) {
        continue;
      }
      if (weightTo[tailNodeIndex] > edge.getWeight()) {
        weightTo[tailNodeIndex] = edge.getWeight();
        edgeTo[tailNodeIndex] = edge;
        // 维护最小堆
        if (!indexMinPq.contains(tailNodeIndex)) {
          indexMinPq.insert(tailNodeIndex, edge.getWeight());
        } else if (edge.getWeight() < indexMinPq.keyOf(tailNodeIndex)) {
          indexMinPq.decreaseKey(edge.getWeight(), tailNodeIndex);
        }
      }
    }
  }

  public double weight() {
    return Arrays.stream(weightTo).sum();
  }

  public Iterable<Edge> edges() {
    return Arrays.stream(edgeTo).filter(Objects::nonNull).collect(Collectors.toList());
  }

  public static void main(String[] args) {
    String fileName = "tinyEWG.txt";
    String filePath = GraphUtil.getGraphFilePath(EdgeWeightedGraph.class, fileName);
    EdgeWeightedGraph graph = new EdgeWeightedGraph(filePath, " ");

    PrimMST primMST = new PrimMST(graph);
    for (Edge edge : primMST.edges()) {
      System.out.println(edge);
    }
    System.out.println();
    System.out.println("total mst weight = " + primMST.weight());
  }
}
