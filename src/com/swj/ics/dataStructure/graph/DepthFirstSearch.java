package com.swj.ics.dataStructure.graph;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/02/18 15:17
 * 图的深度优先遍历 DFS
 */
public class DepthFirstSearch implements GraphSearchApi {

  private boolean[] markedNodeArray;

  private int visitedNodeCount;

  /**
   * 构造搜索对象
   *
   * @param g              指定的一个图对象
   * @param startNodeIndex 图中的一个起始点
   */
  public DepthFirstSearch(Graph g, int startNodeIndex) {
    this.markedNodeArray = new boolean[g.nodeSize()];
    dfs(g, startNodeIndex);
  }

  /**
   * 深度优先遍历
   *
   * @param nodeIndex 当前节点
   */
  private void dfs(Graph graph, int nodeIndex) {
    markedNodeArray[nodeIndex] = true;
    visitedNodeCount++;
    Iterable<Integer> neighbors = graph.linkedNeighbors(nodeIndex);
    for (int neighborNodeIdx : neighbors) {
      if (!markedNodeArray[neighborNodeIdx]) {
        dfs(graph, neighborNodeIdx);
      }
    }
  }

  @Override
  public boolean marked(int nodeIndex) {
    return markedNodeArray[nodeIndex];
  }

  @Override
  public int nodeLinkedCount() {
    return visitedNodeCount;
  }

  public static void main(String[] args) {
    Graph graph = GraphUtil.buildDemoGraph("tinyG.txt");
    int startNodeIndex = 0;
    DepthFirstSearch dfs = new DepthFirstSearch(graph, startNodeIndex);
    printVisitedNode(dfs, graph, startNodeIndex);
    // 从 9 节点开始访问
    startNodeIndex = 9;
    dfs = new DepthFirstSearch(graph, startNodeIndex);
    printVisitedNode(dfs, graph, startNodeIndex);
  }

  private static void printVisitedNode(DepthFirstSearch dfs, Graph graph, int startNode) {
    System.out.println("the dfs visited node from beginning node :" + startNode);
    for (int i = startNode; i < graph.nodeSize(); i++) {
      if (dfs.marked(i)) {
        System.out.print(i + " ");
      }
    }
    System.out.println();
    String connectedResult = "the graph is ";
    if (dfs.nodeLinkedCount() == graph.nodeSize()) {
      connectedResult += "connected";
    } else {
      connectedResult += "not connected";
    }
    System.out.println(connectedResult);
  }
}
