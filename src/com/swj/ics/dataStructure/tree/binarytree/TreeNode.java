package com.swj.ics.dataStructure.tree.binarytree;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/01/10 21:42
 */
public class TreeNode<T> {
  public T value;
  public TreeNode<T> leftChild;
  public TreeNode<T> rightChild;
  public TreeNode<T> parent;

  public TreeNode() {

  }

  public TreeNode(T val) {
    this.value = val;
  }

  @Override
  public String toString() {
    return "TreeNode{" +
        "value=" + value +
        '}';
  }
}
