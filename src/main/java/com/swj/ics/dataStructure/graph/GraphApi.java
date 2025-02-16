package com.swj.ics.dataStructure.graph;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/02/18 14:57
 */
public interface GraphApi {

  void addEdge(int fromNodeIndex, int toNodeIndex);

  Iterable<Integer> linkedNeighbors(int nodeIndex);

  int nodeSize();

  int edgeSize();
}
