package com.swj.ics.dataStructure.graph;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/02/18 14:59
 * 图搜索 API
 */
public interface GraphSearchApi {

  // 当前节点是否在图进行搜索的时候访问过/标记过
  boolean marked(int nodeIndex);

  // 跟始发节点相连通的节点数量
  int nodeLinkedCount();
}
