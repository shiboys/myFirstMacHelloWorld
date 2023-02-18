package com.swj.ics.dataStructure.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/02/18 16:40
 * 深度优先遍历的路径
 * 对深度优先遍历的节点进行记录保存。记录的格式有些特别，采用反向记录的形式，啥意思？
 * 就是 比如 深度优先，A->B->C->D，那么 reversedVisited[B]=A,reversedVisited[C]=B... 以此类推
 * 在使用的时候，比如求出 A->D 所经过的所有点，那么从 D 向上递推 到 A，所经过的节点存入栈，最后再遍历这个栈
 * 就得到从 A 到 D 经过的节点。
 */
public class DepthFirstPath implements GraphPathApi {
  boolean[] markedNodeArray;
  private int[] reversedVisited;
  private int startNodeIndex;

  public DepthFirstPath(Graph graph, int startNodeIndex) {
    markedNodeArray = new boolean[graph.nodeSize()];
    reversedVisited = new int[graph.nodeSize()];
    this.startNodeIndex = startNodeIndex;
    dfs(graph, startNodeIndex);
  }

  private void dfs(Graph graph, int startNodeIndex) {
    markedNodeArray[startNodeIndex] = true;
    // neighbors 根据头插法，0-2 表示 2 是最后插入到 0 的 neighbors 链表的，
    // 因此 2 是 起始节点 0 的第一个 neighbor，所以在 深度遍历的时候，会被优先遍历到。
    for (int nodeIndex : graph.linkedNeighbors(startNodeIndex)) {
      if (!markedNodeArray[nodeIndex]) {
        reversedVisited[nodeIndex] = startNodeIndex;
        dfs(graph, nodeIndex);
      }
    }
  }

  @Override
  public boolean hasPathTo(int toNodeIndex) {
    if (toNodeIndex >= markedNodeArray.length) {
      return false;
    }
    return markedNodeArray[toNodeIndex];
  }

  @Override
  public Iterable<Integer> pathTo(int toNodeIndex) {
    Stack<Integer> nodeStack = new Stack<>();
    for (int currNodeIdx = toNodeIndex;
         currNodeIdx >= 0 && currNodeIdx < reversedVisited.length && currNodeIdx != startNodeIndex;
         currNodeIdx = reversedVisited[currNodeIdx]) {
      nodeStack.push(currNodeIdx);
    }
    //todo: 不要把最后一个节点忘了
    nodeStack.push(startNodeIndex);

    //   jdk 的栈的 遍历不是像我们想的那样遵循 LIFO 的，需要我们手动来将结果进行遍历
    List<Integer> arrayList = new ArrayList<>(nodeStack.size());
    while (!nodeStack.isEmpty()) {
      arrayList.add(nodeStack.pop());
    }
    return arrayList;
  }

  public int getStartNodeIndex() {
    return startNodeIndex;
  }

  public static void main(String[] args) {
    String graphFile = "tinyCG.txt";
    Graph graph = GraphUtil.buildDemoGraph(graphFile);
    DepthFirstPath dfsPath = new DepthFirstPath(graph, 0);
    for (int i = 0; i < graph.nodeSize(); i++) {
      // 在我的路径上，则进行打印
      if (dfsPath.hasPathTo(i)) {
        System.out.print(String.format("%s to %s: ", dfsPath.getStartNodeIndex(), i));
        for (int nodeInx : dfsPath.pathTo(i)) {
          if (nodeInx != dfsPath.getStartNodeIndex()) {
            System.out.print("-");
          }
          System.out.print(nodeInx);
        }
        System.out.println();
      }
    }
  }
  /**
   * 打印结果为
   * 0 to 0: 0
   * 0 to 1: 0-2-1
   * 0 to 2: 0-2
   * 0 to 3: 0-2-3
   * 0 to 4: 0-2-3-4
   * 0 to 5: 0-2-3-5
   * 这是因为 深度遍历的原因，第一次先遍历了 0-2，接着沿着2 继续遍历
   */
}
