package com.swj.ics.dataStructure.graph.shortest_path;

import com.swj.ics.dataStructure.graph.GraphUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/02/26 19:07
 * 贝尔曼-福特最短路径算法，
 * 该算法最牛逼的地方在于能够检测到权重为负数的情况，Dijkstra 算法这个做不到
 * 经典的贝尔曼福特算法，书上很多都是介绍的 3 个for 循环，
 * 看了下算法 4 的代码，发现这是个半成品
 */
public class BellmanFordSP {
  // 解决浮点精度问题
  private static final double EPSILON = 1E-14;

  private DirectedEdgeWeightedGraph graph;
  private double[] distanceToArray;
  private DirectedEdge[] edgeToArray;
  boolean hasNegativeCycle;

  public BellmanFordSP(DirectedEdgeWeightedGraph graph, int startNodeIndex) {
    this.distanceToArray = new double[graph.getNodeSize()];
    for (int i = 0; i < graph.getNodeSize(); i++) {
      distanceToArray[i] = Double.POSITIVE_INFINITY;
    }
    distanceToArray[startNodeIndex] = 0;
    this.edgeToArray = new DirectedEdge[graph.getNodeSize()];
    this.graph = graph;
  }

  public boolean generateBellmanFordSp() {
    // 每条边松弛次数 V-1 次？ 这不是 V 次吗？
    for (int i = 0; i <= graph.getNodeSize() - 1; i++) {
      for (DirectedEdge edge : graph.edges()) {
        relax(edge);
      }
    }
    return false;
  }

  /**
   * 查找最短路径
   *
   * @param startNodeIndex
   * @param toNodeIndex
   * @return
   */
  public Iterable<Integer> pathTo(int startNodeIndex, int toNodeIndex) {
    List<Integer> nodeList = new ArrayList<>();
    for (int v = toNodeIndex; v != startNodeIndex; v = edgeToArray[v].getHeadNodeIndex()) {
      nodeList.add(v);
    }
    //if()
    nodeList.add(startNodeIndex);
    Collections.reverse(nodeList);
    return nodeList;
  }

  private boolean relax(DirectedEdge edge) {
    int head = edge.getHeadNodeIndex();
    int tail = edge.getTailNodeIndex();
    double weight = edge.getWeight();
    if (distanceToArray[tail] > distanceToArray[head] + weight + EPSILON) {
      distanceToArray[tail] = distanceToArray[head] + weight;
      edgeToArray[tail] = edge;
      return true;
    }
    return false;
  }

  public boolean hasNegativeCycle() {
    return hasNegativeCycle;
  }

  public static void main(String[] args) {
    String fileName = "tinyEWDn.txt";
    String filePath = GraphUtil.getGraphFilePath(DirectedEdgeWeightedGraph.class, fileName);
    DirectedEdgeWeightedGraph directedGraph = new DirectedEdgeWeightedGraph(filePath, " ");
    int startNodeIndex = 0;
    BellmanFordSP bellmanFordSP = new BellmanFordSP(directedGraph, startNodeIndex);
    bellmanFordSP.generateBellmanFordSp();
    if (bellmanFordSP.hasNegativeCycle()) {
      System.err.println("current graph has negative cycle");
    } else {

      int endIndex = 1;
      Iterable<Integer> nodeList = bellmanFordSP.pathTo(startNodeIndex, endIndex);
      System.out.println("from " + startNodeIndex + " to " + endIndex + ": ");
      // double totalWeight = 0;
      for (Integer nodeIndex : nodeList) {
        System.out.print(nodeIndex + " ");
      }
    }
  }
}
