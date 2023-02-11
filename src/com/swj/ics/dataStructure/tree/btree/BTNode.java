package com.swj.ics.dataStructure.tree.btree;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/02/10 14:20
 */
public class BTNode<K, V> {
  // 关键字集合
  private List<Entry<K, V>> entryList;
  // 子节点
  private List<BTNode<K, V>> children;

  // 是否叶子结点
  private boolean isLeaf;

  private BTNode<K,V> parent;

  private Comparator<K> comparator;

  public BTNode() {
    entryList = new LinkedList<>();
    children = new LinkedList<>();
    isLeaf = true;
  }

  public BTNode<K, V> getParent() {
    return parent;
  }

  public void setParent(BTNode<K, V> parent) {
    this.parent = parent;
  }

  public boolean isLeaf() {
    return isLeaf;
  }

  public void setLeaf(boolean leaf) {
    isLeaf = leaf;
  }

  public BTNode(Comparator<K> comparator) {
    this();
    this.comparator = comparator;
  }

  public int compare(K key1, K key2) {
    return comparator == null ? ((Comparable<K>) key1).compareTo(key2) : comparator.compare(key1, key2);
  }

  /**
   * 使用二分法在 B 树的一个节点中查找关键字，代码示例可能最多是 5 阶，也就是一个节点最多 5 个关键字，看起来不需要使用二分法
   * 但是真实的 B 树关键字个数可能成千上万，不使用二分法，你的程序就谈不上及格。
   * 次数的节点内关键字的查找，使用了变种的二分法，特别针对没有找到关键字的情况，因为一个关键字在当前节点找不到，有可能在当前节点的子节点中
   * 但是在哪个子节点中那，这里就需要根据关键字定位子节点，所以还是有些特别的
   *
   * @param entry 关键字
   * @return
   */
  public SearchResult<V> search(Entry<K, V> entry) {
    int start = 0;
    int end = entryList.size() - 1;
    K targetKey = entry.getKey();
    while (start < end) {
      int mid = (start + end) / 2;
      int cr = compare(targetKey, entryList.get(mid).getKey());
      if (cr == 0) {
        return new SearchResult<V>(true, mid, entryList.get(mid).getValue());
      } else if (cr > 0) {
        start = mid + 1;
      } else {
        end = mid - 1;
      }
    }
    if (start == end) {
      int cr = compare(targetKey, entryList.get(start).getKey());
      if (cr == 0) {
        return new SearchResult<>(true, start, entryList.get(start).getValue());
      } else if (cr > 0) {
        // 在 children[start+1] 的子节点中
        return new SearchResult<>(false, start + 1, null);
      } else {
        return new SearchResult<>(false, start, null);
      }
    } else {
      return new SearchResult<>(false, start, null);
    }
  }

  public Entry<K, V> entryAt(int index) {
    return this.entryList.get(index);
  }

  public void insertEntry(Entry<K, V> entry, int index) {
    this.entryList.add(index, entry);
  }

  public boolean insertEntry(Entry<K, V> entry) {
    SearchResult<V> searchResult = search(entry);
    if (searchResult.isExist) {
      return false;
    }
    entryList.add(searchResult.index, entry);
    return true;
  }

  public Entry<K, V> removeEntry(int index) {
    return entryList.remove(index);
  }

  public V putEntry(Entry<K, V> entry) {
    SearchResult<V> searchResult = search(entry);
    if (searchResult.isExist) {
      Entry<K, V> oldEntry = entryList.get(searchResult.index);
      V val = oldEntry.getValue();
      oldEntry.setValue(entry.getValue());
      return val;
    }
    entryList.add(searchResult.index, entry);
    return null;
  }

  public int entrySize() {
    return entryList.size();
  }

  public int nodeSize() {
    return children.size();
  }

  public BTNode<K, V> childAt(int index) {
    return children.get(index);
  }

  public void insertChild(BTNode<K, V> childNode, int index) {
    children.add(index, childNode);
  }

  public BTNode<K, V> removeChild(int index) {
    return children.remove(index);
  }

  public void removeChild(BTNode childNode) {
    children.remove(childNode);
  }
}
