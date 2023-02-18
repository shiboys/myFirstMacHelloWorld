package com.swj.ics.dataStructure.graph;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/02/18 16:33
 */
public interface GraphPathApi {
  /**
   * 是否有从起点到 toNodeIndex 的路径
   * @param toNodeIndex
   * @return
   */
  boolean hasPathTo(int toNodeIndex);

  /**
   * 从起始节点到 目标节点所经过的节点的集合
   * @param toNodeIndex
   * @return
   */
  Iterable<Integer> pathTo(int toNodeIndex);
}
