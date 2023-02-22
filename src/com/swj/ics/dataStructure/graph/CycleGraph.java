package com.swj.ics.dataStructure.graph;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/02/21 19:25
 * 给定的图是否是环形的。
 */
public class CycleGraph {
  /**
   * 深度优先搜索跟其他算法一样，简介的代码下隐藏着复杂的计算。因此研究这些例子，跟踪算法的轨迹并加以扩展，
   * 用算法来解决环的问题和着色的问题。
   * <p>
   * A  D
   * |  |
   * B--C
   * <p>
   * A-B-C-D。如果 C 的 neighbor 里面除了 B 还有 A，那就是环。
   * 同理 如果 D 的 neighbors 里面除了 C 还有 B 或者 A，那也是环
   */

  private boolean[] markedArray;
  private boolean hasCycle = false;

  public CycleGraph(Graph graph, int startNodeIndex) {
    markedArray = new boolean[graph.nodeSize()];
    for (int i = 0; i < graph.nodeSize(); i++) {
      if (!markedArray[i]) {
        dfs(graph, i, i);
      }
    }
  }

  private void dfs(Graph graph, int startNodeIdx, int endNodeIdx) {
    markedArray[startNodeIdx] = true;
    for(int nodeIndex : graph.linkedNeighbors(startNodeIdx)) {
      if(!markedArray[nodeIndex]) {
        dfs(graph,nodeIndex,startNodeIdx);
      } else if(nodeIndex != endNodeIdx) { // 如果 D neighbors 里面已经被标记过的节点，不只有 C ,那么肯定是有环了，
        // 这个逻辑光看代码比价抽象，画出图来就比较好立即
        hasCycle =true;
      }
    }
  }

  public boolean hasCycle() {
    return hasCycle;
  }

}
