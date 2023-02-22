package com.swj.ics.dataStructure.graph;

import com.swj.ics.dataStructure.queue.Queue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/02/18 18:07
 * 广度优先遍历，
 * 广度优先遍历，不管是图还是树，都是一个套路，使用队列，将其第一遍遍历的节点全部存入队列，
 * 然后再从队列弹出，遍历其子节点/图的连接目标节点，再次存入队列中
 */
public class BreadthFirstPath implements GraphPathApi {

  private static final int INFINITY = Integer.MAX_VALUE;

  private boolean[] markedNodeArray;

  private int[] reversedPathNodeArray;

  /**
   * 入口点距离当前节点的记录
   */
  private int[] distanceToArray;

  private Graph graph;

  private int startNodeIndex;

  public BreadthFirstPath(Graph g, int startNodeIndex) {
    this.graph = g;
    this.startNodeIndex = startNodeIndex;
    markedNodeArray = new boolean[graph.nodeSize()];
    distanceToArray = new int[graph.nodeSize()];
    // 默认任何节点距离 startNodeIndex 无穷远。
    Arrays.fill(distanceToArray, INFINITY);
    reversedPathNodeArray = new int[graph.nodeSize()];
    bfs(graph, startNodeIndex);
  }

  /**
   * 广度优先遍历
   *
   * @param g         图
   * @param nodeIndex 当前节点
   */
  private void bfs(Graph g, int nodeIndex) {
    markedNodeArray[nodeIndex] = true;
    Queue<Integer> queue = new Queue<>();
    distanceToArray[nodeIndex] = 0;
    queue.enqueue(nodeIndex);
    while (!queue.isEmpty()) {
      int headIndex = queue.dequeue();
      for (int tailIndex : g.linkedNeighbors(headIndex)) {
        // 这里缺少一个最重要的判断，当前节点是否已经遍历过
        if (!markedNodeArray[tailIndex]) {
          markedNodeArray[tailIndex] = true;
          reversedPathNodeArray[tailIndex] = headIndex;
          distanceToArray[tailIndex] = distanceToArray[headIndex] + 1;
          queue.enqueue(tailIndex);
        }
      }
    }
  }

  @Override
  public boolean hasPathTo(int toNodeIndex) {
    return markedNodeArray[toNodeIndex];
  }

  @Override
  public Iterable<Integer> pathTo(int toNodeIndex) {
    List<Integer> reversedNodeList = new ArrayList<>();
    while (toNodeIndex != startNodeIndex && toNodeIndex < reversedPathNodeArray.length) {
      reversedNodeList.add(0, toNodeIndex);
      toNodeIndex = reversedPathNodeArray[toNodeIndex];
    }
    if (!reversedNodeList.isEmpty()) {
      reversedNodeList.add(0, startNodeIndex);
    }
    return reversedNodeList;
  }

  public int distanceTo(int toNodeIndex) {
    return distanceToArray[toNodeIndex];
  }

  public int getStartNodeIndex() {
    return startNodeIndex;
  }

  public static void main(String[] args) {
    String fileName = "tinyCG.txt";
    Graph graph = GraphUtil.buildDemoGraph(fileName);

    int startIndex = 0;
    BreadthFirstPath bfsPath = new BreadthFirstPath(graph, startIndex);

    for (int i = startIndex; i < graph.nodeSize(); i++) {
      if (bfsPath.hasPathTo(i)) {
        System.out.print(String.format("%s to %s (%s) : ", startIndex, i, bfsPath.distanceTo(i)));
        for (Integer nodeIndex : bfsPath.pathTo(i)) {
          if (nodeIndex != startIndex) {
            System.out.print("-");
          }
          System.out.print(nodeIndex);
        }
        System.out.println();
      }
    }
  }

}
