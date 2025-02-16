package com.swj.ics.dataStructure.graph.min_spanning_tree;

import com.swj.ics.dataStructure.graph.GraphUtil;
import com.swj.ics.dataStructure.heap.BinaryHeapMin;
import com.swj.ics.dataStructure.queue.Queue;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/02/23 17:52
 * 基于 Prim 规则的延迟 最小生成树实现
 * Prim 算法的每一步都会为一颗生长中的树添加一条边。一开始这棵树只有一个顶点，然后会向它添加 V-1 条边，每次总是
 * 将下一条连接树中的顶点与不再树中的顶点且权重最小的边加入树中（权重最小的边不一定都是跟最初的最小生成树相连接
 * 为什么叫 Lazy 那？根据算法 4 的说法，是因为最小优先级队列里面包含有无效的边。
 */
public class LazyPrimMST {

  private Queue<Edge> primEdgeQueue;

  private BinaryHeapMin<Edge> minPriorityQueue;

  private EdgeWeightedGraph ewg;

  private boolean[] marked;

  private double weight;

  public LazyPrimMST(EdgeWeightedGraph graph) {
    primEdgeQueue = new Queue<>();
    minPriorityQueue = new BinaryHeapMin<>();
    marked = new boolean[graph.nodeSize()];

    for (int v = 0; v < graph.nodeSize(); v++) {
      if (!marked[v]) {
        prim(graph, v);
      }
    }
  }

  private void prim(EdgeWeightedGraph graph, int v) {
    scan(graph, v);
    while (!minPriorityQueue.isEmpty()) {
      Edge minWeightedEdge = minPriorityQueue.delMin();
      int headNodeIndex = minWeightedEdge.head();
      // 无向图，必须告诉 tail() 函数那个是头结点
      int tailNodeIndex = minWeightedEdge.tail(headNodeIndex);
      // 两个节点都被访问了，这个就是无效边
      if (marked[headNodeIndex] && marked[tailNodeIndex]) {
        continue;
      }
      // 必须是一个节点已经被访问过，另外一个节点还没有被访问。
      if (!marked[headNodeIndex] && !marked[tailNodeIndex]) {
        continue;
      }
      primEdgeQueue.enqueue(minWeightedEdge);
      weight += minWeightedEdge.getWeight();
      // 把当前最小权重的边加入后，则需要扫描边的另一个节点的 neighbours, 确保优先级队列及时更新最小权重的边
      if (!marked[headNodeIndex]) {
        scan(graph, headNodeIndex);
      }
      if (!marked[tailNodeIndex]) {
        scan(graph, tailNodeIndex);
      }
    }
  }

  private void scan(EdgeWeightedGraph graph, int v) {
    marked[v] = true;
    for (Edge edge : graph.linkedNeighbors(v)) {
      int tailNodeIndex = edge.tail(v);
      if (marked[tailNodeIndex]) {
        continue;
      }
      minPriorityQueue.insert(edge);
    }
  }

  public double getWeight() {
    return weight;
  }

  public Iterable<Edge> getPrimEdges() {
    return primEdgeQueue;
  }

  public static void main(String[] args) {
    String fileName = "tinyEWG.txt";
    String filePath = GraphUtil.getGraphFilePath(EdgeWeightedGraph.class, fileName);
    EdgeWeightedGraph graph = new EdgeWeightedGraph(filePath, " ");

    LazyPrimMST lazyPrimMST = new LazyPrimMST(graph);

    for (Edge edge : lazyPrimMST.getPrimEdges()) {
      System.out.println(edge);
    }
    /**
     * 打印结果如下：
     * 0-7 0.16000
     * 1-7 0.19000
     * 0-2 0.26000
     * 2-3 0.17000
     * 5-7 0.28000
     * 4-5 0.35000
     * 6-2 0.40000
     * 跟 prim-lazy.png 的路径一样
     */
  }
}
