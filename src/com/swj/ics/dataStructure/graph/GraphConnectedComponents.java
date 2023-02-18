package com.swj.ics.dataStructure.graph;

import com.swj.ics.dataStructure.queue.Queue;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/02/18 19:54
 * 获取相互能连接的子图/子组件的个数。ConnectedComponents 简称 cc
 */
public class GraphConnectedComponents {
  /**
   * 通过深度优先遍历的方式，获取给的图的相关边和节点中总有几个彼此相连的图
   */
  private final Graph graph;
  private final boolean[] markedNodeIndexArray;
  // 图/组件的Id数组, 节点的索引作为下标，当前的图的个数作为 数组的值
  private final int[] componentIdArray;
  private int componentCount;

  public GraphConnectedComponents(Graph graph) {
    this.graph = graph;
    markedNodeIndexArray = new boolean[graph.nodeSize()];
    componentIdArray = new int[graph.nodeSize()];

    for (int i = 0; i < graph.nodeSize(); i++) {
      if (!markedNodeIndexArray[i]) {
        dfs(graph, i);
        componentCount++;
      }
    }
  }

  private void dfs(Graph graph, int nodeIndex) {
    markedNodeIndexArray[nodeIndex] = true;
    componentIdArray[nodeIndex] = componentCount;
    for (int neighborIndex : graph.linkedNeighbors(nodeIndex)) {
      if (!markedNodeIndexArray[neighborIndex]) {
        dfs(graph, neighborIndex);
      }
    }
  }

  public int getComponentId(int nodeIndex) {
    return componentIdArray[nodeIndex];
  }

  public boolean connected(int nodeIndex1, int nodeIndex2) {
    if (nodeIndex1 >= 0 && nodeIndex1 < graph.nodeSize() && nodeIndex2 >= 0 && nodeIndex2 < graph.nodeSize()) {
      return componentIdArray[nodeIndex1] == componentIdArray[nodeIndex2];
    }
    return false;
  }

  public int getComponentCount() {
    return componentCount;
  }

  public static void main(String[] args) {
    String fileName = "tinyG.txt";
    Graph graph = GraphUtil.buildDemoGraph(fileName);
    printCcDetail(fileName, graph);
    fileName = "mediumG.txt";
    graph = GraphUtil.buildDemoGraph(fileName);
    printCcDetail(fileName, graph);
  }

  private static void printCcDetail(String fileName, Graph graph) {
    GraphConnectedComponents gcc = new GraphConnectedComponents(graph);
    System.out.println("current graph from file " + fileName + " has " + gcc.getComponentCount() + " components。");
    Queue<Integer>[] componentQueues = (Queue<Integer>[]) new Queue[gcc.getComponentCount()];
    //Arrays.fill(componentQueues, new Queue<Integer>());
    for (int i = 0; i < gcc.componentCount; i++) {
      componentQueues[i] = new Queue<>();
    }
    for (int i = 0; i < graph.nodeSize(); i++) {
      componentQueues[gcc.getComponentId(i)].enqueue(i);
    }
    for (int j = 0; j < componentQueues.length; j++) {
      for (int nodeIndex : componentQueues[j]) {
        System.out.print(nodeIndex + " ");
      }
      System.out.println();
    }
    System.out.println();
  }
}
