package com.swj.ics.dataStructure.tree.binarytree;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/01/11 20:45
 * 树的子结构
 * 输入一个二叉树A和B，判断 B 是不是A 的子结构。
 * 如下图所示，由于 A 中有一部分的树结构和 B 是一样的，因此 B 是 A的子结构。
 * A 二叉树如下
 *        8
 *      / \
 *     8   7
 *    / \
 *  9   2
 * / \
 *4   7
 * <p>
 * B 二叉树
 *  8
 * / \
 * 9   2
 */
public class IsSubTree {

  public static void main(String[] args) {
    /*int[] preOrderArr = {8, 8, 9, 4, 7, 2, 7};
    int[] middleOrderArr = {4, 9, 7, 8, 2, 8, 7};
    int[] subPreOrderArr = {8, 9, 2};
    int[] subMiddleOrderArr = {9, 8, 2};*/
    BinaryTreeNode root1 = new BinaryTreeNode(8);
    BinaryTreeNode parentNode = root1;
    BinaryTreeNode leftChild = new BinaryTreeNode(8);
    BinaryTreeNode rightChild = new BinaryTreeNode(7);
    parentNode.leftChild=leftChild;
    parentNode.rightChild =rightChild;

    parentNode = leftChild;
    leftChild = new BinaryTreeNode(9);
    rightChild = new BinaryTreeNode(2);
    parentNode.leftChild=leftChild;
    parentNode.rightChild =rightChild;

    parentNode = leftChild;
    leftChild = new BinaryTreeNode(4);
    rightChild = new BinaryTreeNode(7);
    parentNode.leftChild=leftChild;
    parentNode.rightChild =rightChild;

    BinaryTreeNode root2 = new BinaryTreeNode(8);
    BinaryTreeNode leftChild2 = new BinaryTreeNode(9);
    BinaryTreeNode rightChild2 = new BinaryTreeNode(2);
    root2.leftChild=leftChild2;
    root2.rightChild = rightChild2;

   /* BinaryTreeNode root1 = RebuildBinaryTree.buildBinaryTree(preOrderArr, middleOrderArr);
    BinaryTreeNode root2 = RebuildBinaryTree.buildBinaryTree(subPreOrderArr, subMiddleOrderArr);
    */
    // 重建二叉树不适合有重复元素的
    System.out.println(isSubTree(root1, root2));
  }

  /**
   * 和链表相比，树中的指针操作更多也更复杂，因此与树相关的问题通常会比链表要难。如果向加大面试难度，则树的题目是很多面试官的选择。
   * 面对大量的指针操作，我们要更加小心，否则一不留神就会在代码中留下隐患。
   * 要查找 A 中是否 B 子树，我们需要分成两步：第一步，在树 A 中找到和树 B 的根节点的值一样的节点 R；
   * 第二步，判断树 A 中以 R 为根节点的子树是不是包含和树 B 一样的结构
   */
  static boolean isSubTree(BinaryTreeNode root1, BinaryTreeNode root2) {
    if (root1 == null || root2 == null) {
      return false;
    }
    boolean result = false;
    if (root1.value == root2.value) {
      result = doesHaveSameChildren(root1, root2);
    }
    if (!result) { // 根节点不同，则递归左子节点跟 root2 比较
      result = isSubTree(root1.leftChild, root2);
    }
    if (!result) { // 左子节点不包含子树，则递归右子节点跟 root2 比较
      result = isSubTree(root1.rightChild, root2);
    }
    return result;
  }

  private static boolean doesHaveSameChildren(BinaryTreeNode root1, BinaryTreeNode root2) {
    if (root2 == null) {
      return true;
    }
    if (root1 == null) {
      return false;
    }
    if (root1.value != root2.value) {
      return false;
    }
    return doesHaveSameChildren(root1.leftChild, root2.leftChild) &&
        doesHaveSameChildren(root1.rightChild, root2.rightChild);
  }
}
