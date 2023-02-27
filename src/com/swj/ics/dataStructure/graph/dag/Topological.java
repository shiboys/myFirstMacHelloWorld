package com.swj.ics.dataStructure.graph.dag;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/02/26 11:47
 * 拓扑排序
 * 图的拓扑排序结果是深度优先遍历的后序遍历结果翻转
 * 如果采用宽度优先顺序遍历，则宽度优先的结果就是遍历的结果。
 * 拓扑排序的结果从图上来看，就是将凌乱的图打平。
 * 结果参照 topological-sort.png 它是 dag.png 的拓扑排序结果。
 * 拓扑排序的一个重要的先决条件是图一定不能有环。
 */
public class Topological {

  private DepthFirstOrder depthFirstOrder;
  private DirectedCycle directedCycle;
  private Iterable<Integer> order;

  public Topological(Digraph digraph) {
    directedCycle = new DirectedCycle(digraph);
    if (!directedCycle.hasCycle()) {
      depthFirstOrder = new DepthFirstOrder(digraph);
      order = depthFirstOrder.reversePostOrder();
    }
  }

  public Iterable<Integer> order() {
    return order;
  }

  public boolean isDAG() {
    return order != null;
  }

  // todo: 宽度优先的拓扑
}
