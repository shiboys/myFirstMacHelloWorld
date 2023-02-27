package com.swj.ics.dataStructure.graph.dag;

import com.swj.ics.dataStructure.graph.GraphUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/02/26 14:31
 * 有向图的环形检测。
 * 原理，图形后续遍历，使用一个 onStack[] 或者 onPath 数组，记录遍历过的元素，遍历结束，则将元素取消标记
 * 如果某个元素在遍历之后还能再次遍历到，这说明有环
 * 本例子，处理说明有环之外，还会把环中的元素元素都给存起来，以便能够递归
 */
public class DirectedCycle {
  private boolean[] marked;
  private int[] edgeTo;
  private boolean[] onStack;
  private List<Integer> cycleNodeList;

  public DirectedCycle(Digraph digraph) {
    marked = new boolean[digraph.getNodeSize()];
    onStack = new boolean[digraph.getNodeSize()];
    edgeTo = new int[digraph.getNodeSize()];
    // 遍历图中所有节点，检查图中的环状路径
    for (int i = 0; i < digraph.getNodeSize(); i++) {
      if (!marked[i] && !hasCycle()) {
        dfs(digraph, i);
      }
    }
  }

  private void dfs(Digraph digraph, int nodeIndex) {
    marked[nodeIndex] = true;
    onStack[nodeIndex] = true;

    for (int nextNodeIndex : digraph.linkedNeighbours(nodeIndex)) {
      if (hasCycle()) {
        return;
      }
      if (!marked[nextNodeIndex]) {
        edgeTo[nextNodeIndex] = nodeIndex;
        dfs(digraph, nextNodeIndex);
      } else if (onStack[nextNodeIndex]) {// 遇到环的入口节点
        // 回溯检测，将所有环中的节点存入集合
        cycleNodeList = new ArrayList<>();
        // nodeIndex->NextNodeIndex 的逆向所有节点加入集合
        for (int v = nodeIndex; v != nextNodeIndex; v = edgeTo[v]) {
          cycleNodeList.add(v);
        }
        // 相遇节点加入
        cycleNodeList.add(nextNodeIndex);
        // 再把重复的节点加入集合
        cycleNodeList.add(nodeIndex);
      }
    }
    // 当前节点深度遍历完毕，则从路径上抹去
    onStack[nodeIndex] = false;
  }

  public boolean hasCycle() {
    return cycleNodeList != null && !cycleNodeList.isEmpty();
  }

  public Iterable<Integer> cycle() {
    if (cycleNodeList == null) {
      return null;
    }
    Collections.reverse(cycleNodeList);
    return cycleNodeList;
  }

  public static void main(String[] args) {
    String fileName = "tinyDG.txt";
    String filePath = GraphUtil.getGraphFilePath(DirectedCycle.class, fileName);
    Digraph digraph = new Digraph(filePath, " ");
    DirectedCycle directedCycle = new DirectedCycle(digraph);
    printCycleDetail(fileName, directedCycle);
    fileName = "tinyDAG.txt";
    filePath = GraphUtil.getGraphFilePath(DirectedCycle.class, fileName);
    digraph = new Digraph(filePath, " ");
    directedCycle = new DirectedCycle(digraph);
    printCycleDetail(fileName, directedCycle);
  }

  private static void printCycleDetail(String fileName, DirectedCycle directedCycle) {
    if (directedCycle.hasCycle()) {
      System.out.println(fileName + " has cycle. items are:");
      for (Integer nodeIndex : directedCycle.cycle()) {
        System.out.print(nodeIndex + " ");
      }
      System.out.println();
    } else {
      System.out.println(fileName + " does not has cycle.");
    }
    /**
     * 输出结果
     * tinyDG.txt has cycle. items are:
     * 3 5 4 3
     * tinyDAG.txt does not has cycle.
     * 参考 digraph-cycle.png
     */
  }
}
