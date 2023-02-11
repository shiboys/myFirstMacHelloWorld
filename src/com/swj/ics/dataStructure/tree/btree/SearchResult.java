package com.swj.ics.dataStructure.tree.btree;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/02/10 14:23
 */
public class SearchResult<V> {
  public boolean isExist;
  public int index;
  public V value;

  public SearchResult(boolean isExist, int index, V value) {
    this.isExist = isExist;
    this.index = index;
    this.value = value;
  }
}
