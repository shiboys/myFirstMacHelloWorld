package com.swj.ics.dataStructure.graph.dag;

import com.swj.ics.dataStructure.graph.GraphUtil;
import com.swj.ics.dataStructure.queue.Queue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/02/26 17:17
 * 深度优先排序的各种顺序,包括：
 * 前序
 * 后序
 * 逆后续
 */
public class DepthFirstOrder {

  private Queue<Integer> preOrder;
  private Queue<Integer> postOrder;
  private boolean[] marked;

  public DepthFirstOrder(Digraph digraph) {
    DirectedCycle directedCycle = new DirectedCycle(digraph);
    if (directedCycle.hasCycle()) {
      System.err.println("the target graph has a cycle.");
      return;
    }
    preOrder = new Queue<>();
    postOrder = new Queue<>();
    marked = new boolean[digraph.getNodeSize()];
    for (int i = 0; i < digraph.getNodeSize(); i++) {
      if (!marked[i]) {
        dfs(digraph, i);
      }
    }
  }

  private void dfs(Digraph digraph, int nodeIndex) {
    marked[nodeIndex] = true;
    preOrder.enqueue(nodeIndex);
    for (Integer subNodeIndex : digraph.linkedNeighbours(nodeIndex)) {
      if (!marked[subNodeIndex]) {
        dfs(digraph, subNodeIndex);
      }
    }
    postOrder.enqueue(nodeIndex);
  }

  public Iterable<Integer> preOrder() {
    return preOrder;
  }

  /**
   * 逆后续结果
   * @return
   */
  public Iterable<Integer> reversePostOrder() {
    if (postOrder == null || postOrder.isEmpty()) {
      return null;
    }
    List<Integer> reverseList = new ArrayList<>(postOrder.size());
    for (Integer nodeIndex : postOrder) {
      reverseList.add(nodeIndex);
    }
    Collections.reverse(reverseList);
    return reverseList;
  }

  public static void main(String[] args) {
    String fileName = "tinyDAG.txt";
    String filepath = GraphUtil.getGraphFilePath(DepthFirstOrder.class,fileName);
    Digraph digraph = new Digraph(filepath," ");
    DepthFirstOrder dfo = new DepthFirstOrder(digraph);
    System.out.println("pre order:");
    for(Integer nodeIndex : dfo.preOrder()) {
      System.out.print(nodeIndex+" ");
    }
    System.out.println();
    System.out.println("reversed post order:");
    for(Integer nodeIndex : dfo.reversePostOrder()) {
      System.out.print(nodeIndex+" ");
    }
    System.out.println();
  }
}
