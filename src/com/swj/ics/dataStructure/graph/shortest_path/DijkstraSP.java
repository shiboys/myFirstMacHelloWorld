package com.swj.ics.dataStructure.graph.shortest_path;

import com.swj.ics.dataStructure.graph.GraphUtil;
import com.swj.ics.dataStructure.graph.min_spanning_tree.IndexMinPq;
import com.swj.ics.dataStructure.queue.Queue;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/02/25 16:33
 * 使用 Dijkstra 算法思想的最短路径
 * Dijkstra 算法只适用于所有的边的权重都是正值的情况。
 */
public class DijkstraSP {

  // 存放最短路径的边
  private Queue<DirectedEdge> queue;

  // 到达 i 节点的权重累计值
  private double[] distanceTo;

  // 到达 i 节点的最短路径上的上一条边
  private DirectedEdge[] edgeTo;

  // 辅助存储遍历路径过程中的所有优先级的边 key:nodeIndex, Value : weight;
  private IndexMinPq<Double> edgeOfMinWeightQueue;


  /**
   * @param graph          有方向的带权重的图
   * @param startNodeIndex 计算最短路径的起始点
   */
  public DijkstraSP(DirectedEdgeWeightedGraph graph, int startNodeIndex) {
    queue = new Queue<>();
    distanceTo = new double[graph.getNodeSize()];
    edgeTo = new DirectedEdge[graph.getNodeSize()];
    edgeOfMinWeightQueue = new IndexMinPq<>(graph.getEdgeSize());
    // 将到所有节点的权重默认初始化为最大值
    for (int i = 0; i < graph.getNodeSize(); i++) {
      distanceTo[i] = Double.POSITIVE_INFINITY;
    }
    // 从 s 点开始遍历，s 点本身的权重初始化为最小路径为 0
    distanceTo[startNodeIndex] = 0.0;
    //将 起始点压入最小优先级队列
    edgeOfMinWeightQueue.insert(startNodeIndex, distanceTo[startNodeIndex]);
    while (!edgeOfMinWeightQueue.isEmpty()) {
      int headNodeIndex = edgeOfMinWeightQueue.deleteMin();
      for (DirectedEdge edge : graph.linkedNeighbors(headNodeIndex)) {
        // 对当前的边进行松弛，
        // 所谓松弛，就像橡皮筋一样，一条边默认边的权重很大，就像橡皮筋一样被拉扯的很远，现在发现这条边可以有更小的权重
        // 就像橡皮筋一样，可以松弛 relax ，不用绷那么紧。
        relax(edge);
      }
    }
  }

  /**
   * 对边进行松弛
   *
   * @param directedEdge 当前边
   */
  private void relax(DirectedEdge directedEdge) {
    int tail = directedEdge.getTailNodeIndex();
    int head = directedEdge.getHeadNodeIndex();
    if (distanceTo[tail] > distanceTo[head] + directedEdge.getWeight()) {
      // 进行松弛
      distanceTo[tail] = distanceTo[head] + directedEdge.getWeight();
      // 达到 tail 节点的最近的边改为 当前边
      edgeTo[tail] = directedEdge;

      queue.enqueue(directedEdge);

      keepPqBalance(directedEdge);
    }
  }

  /**
   * 保持优先级队列的平衡
   *
   * @param directedEdge 当前续保存入优先级队列的边
   */
  private void keepPqBalance(DirectedEdge directedEdge) {
    int tail = directedEdge.getTailNodeIndex();
    if (!edgeOfMinWeightQueue.contains(tail)) {
      edgeOfMinWeightQueue.insert(tail, distanceTo[tail]);
    } else {
      edgeOfMinWeightQueue.decreaseKey(distanceTo[tail], tail);
    }
  }

  public Iterable<DirectedEdge> edges() {
    return queue;
  }

  public boolean hasPathTo(int targetNodeIndex) {
    //权重不是默认值
    return distanceTo[targetNodeIndex] < Double.POSITIVE_INFINITY;
  }

  public Iterable<DirectedEdge> pathTo(int targetNodeIndex) {
    if (!hasPathTo(targetNodeIndex)) {
      return null;
    }
    Stack<DirectedEdge> edgeStack = new Stack<>();
    for (DirectedEdge edge = edgeTo[targetNodeIndex]; edge != null; edge = edgeTo[edge.getHeadNodeIndex()]) {
      edgeStack.push(edge);
    }
    List<DirectedEdge> edgeList = new ArrayList<>(edgeStack.size());
    while (!edgeStack.isEmpty()) {
      edgeList.add(edgeStack.pop());
    }
    return edgeList;
  }

  public double getDistanceTo(int targetNodeIndex) {
    return distanceTo[targetNodeIndex];
  }

  public static void main(String[] args) {
    String fileName = "tinyEWD.txt";
    String filePath = GraphUtil.getGraphFilePath(DirectedEdgeWeightedGraph.class, fileName);
    DirectedEdgeWeightedGraph directedGraph = new DirectedEdgeWeightedGraph(filePath, " ");
    int startNodeIndex = 0;
    DijkstraSP dijkstraSP = new DijkstraSP(directedGraph, startNodeIndex);
    for (int i = 0; i < directedGraph.getNodeSize(); i++) {
      if (!dijkstraSP.hasPathTo(i)) {
        System.out.println(startNodeIndex + "-" + i + " has not path to be connected.");
        continue;
      }
      System.out.println(
          startNodeIndex + "-" + i + "(" + String.format("%.2f", dijkstraSP.getDistanceTo(i)) + ")" + ":");
      for (DirectedEdge edge : dijkstraSP.pathTo(i)) {
        System.out.print(edge + "; ");
      }
      System.out.println();
    }
  }

}
