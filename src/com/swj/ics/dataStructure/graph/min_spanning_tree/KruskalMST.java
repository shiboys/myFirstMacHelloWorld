package com.swj.ics.dataStructure.graph.min_spanning_tree;

import com.swj.ics.dataStructure.graph.GraphUtil;
import com.swj.ics.dataStructure.graph.UnionFind;
import com.swj.ics.dataStructure.queue.Queue;

import java.util.Arrays;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/02/24 17:24
 * 基于 Kruskal 的最小生成树
 * Kruskal 最小生成树的思想是，按照边的权重顺序（从小到大）处理它们，将边加入最小生成树中，加入的边不会与已经加入的边构成环，直到树中含有 V-1 条边为止
 * 这种最小树生成的方法叫做 Kruskal 方法。
 * 具体实现上主要是利用了 UF 的原理，联通发现，UF 的原理可以参考 UnionFind 的代码实现，里面有对原理的说明
 */
public class KruskalMST {
  private Queue<Edge> mstQueue;
  private double totalWeight;
  private UnionFind uf;

  public KruskalMST(EdgeWeightedGraph graph) {
    uf = new UnionFind(graph.nodeSize());
    mstQueue = new Queue<>();
    // 将图中的边使用 数组重新引用下
    Edge[] edges = new Edge[graph.edgeSize()];
    int k = 0;
    for (Edge edge : graph.edges()) {
      edges[k++] = edge;
    }
    // 将边按照权重进行排序
    Arrays.sort(edges);

    // 从小到大开始遍历所有边，并按照 Kruskal 法则将边依次存入队列中
    for (Edge edge : edges) {
      int head = edge.head();
      int tail = edge.tail(head);

      // 这说明当前边还没有跟 最小生成树连接
      if (uf.find(head) != uf.find(tail)) {
        uf.union(head, tail);
        mstQueue.enqueue(edge);
        totalWeight += edge.getWeight();
      }
      // 如果相等，则说明 head 节点 和 tail 节点已经在最小生成树里面了，这个边可以不用加入最小生成树
    }
  }

  public double getTotalWeight() {
    return totalWeight;
  }

  public Iterable<Edge> mst() {
    return mstQueue;
  }

  public static void main(String[] args) {
    String fileName = "tinyEWG.txt";
    String filePath = GraphUtil.getGraphFilePath(EdgeWeightedGraph.class, fileName);
    EdgeWeightedGraph graph = new EdgeWeightedGraph(filePath, " ");

    KruskalMST kruskalMst = new KruskalMST(graph);
    for (Edge edge : kruskalMst.mst()) {
      System.out.println(edge);
    }
    /**
     * 打印结果
     * 0-7 0.16000
     * 2-3 0.17000
     * 1-7 0.19000
     * 0-2 0.26000
     * 5-7 0.28000
     * 4-5 0.35000
     * 6-2 0.40000
     *
     * Union Find 好牛逼呀
     */
  }
}
