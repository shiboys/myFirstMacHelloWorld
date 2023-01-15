package com.swj.ics.dataStructure.tree.binarytree;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/01/15 11:03
 * 二叉搜索树与双向链表
 * 题目：输入一颗二叉搜索树，将该二叉搜索树转换成一个排序的双向链表。要求不能创建任何新的节点，只能调整书中的节点指针的指向。
 * 如图《bst转成排序双链表.png》
 */
public class BstConvertedToDoubleLinkedList {
  /**
   * 在二叉树中，每个节点都有两个指向子接点的指针。在双向链表中，每个节点也有两个指针，分别指向前一个节点和后一个节点。由于这两种节点的结构相似
   * 同时二叉搜素树也是一种排序的数据结构，因此，在理论上有可能实现 bst 和排序双链表的转换。在 bst 中，左子节点的值总是小于父节点的值，
   * 右子节点的值总是大于父节点的值。因此我们在 bst 转换成双链表时，原先指向左子节点的指针调整为链表中指向前一个节点的指针，由来指向右子节点的指针
   * 调整为指向后一个节点的指针，接下来我们分析下如何转换
   * 由于要求转换之后的链表是排好序的，我们可以中序遍历树中的每个节点，这是因为中序遍历算法的特点是按照虫小到大的顺序遍历二叉树的每个节点。
   * 当遍历到根节点的时候，我们把树看成 3 部分，值为 10 的节点，根节点为 6 的左子树，根节点为 14 的右子树。根据链表的定义，
   * 值为 10 的节点将和它的左子树的最大一个节点（值为 8 的节点）连机起来，同时它还将和右子树最小的节点（值为 12 的节点）链起来，如图
   * bst转成排序双链表.png 的下半部分所示
   *
   */
  public static void main(String[] args) {
    BinaryTreeNode root1 = new BinaryTreeNode(10);
    BinaryTreeNode parentNode = root1;
    BinaryTreeNode leftChild = new BinaryTreeNode(6);
    BinaryTreeNode rightChild = new BinaryTreeNode(14);
    parentNode.leftChild = leftChild;
    parentNode.rightChild = rightChild;

    parentNode = leftChild;
    leftChild = new BinaryTreeNode(4);
    parentNode.leftChild = leftChild;
    parentNode.rightChild = new BinaryTreeNode(8);

    parentNode = rightChild;
    leftChild = new BinaryTreeNode(12);
    rightChild = new BinaryTreeNode(16);
    parentNode.leftChild = leftChild;
    parentNode.rightChild = rightChild;
    TreeNodeWrap wrap = new TreeNodeWrap();
    convertTreeToDoubleLink(root1, wrap);
    BinaryTreeNode node = wrap.treeNode;
    if (node != null) {
      while (node != null) {
        System.out.print(node.value + "\t");
        node = node.leftChild;
      }
    } else {
      System.out.println("node is null");
    }
  }


  private static void convertTreeToDoubleLink(BinaryTreeNode root, TreeNodeWrap wrap) {
    if (root == null) {
      return;
    }
    if (wrap == null) {
      throw new IllegalArgumentException("illegal argument exception");
    }
    BinaryTreeNode currentNode = root;
    // 先到最左子节点进行递归
    if (currentNode.leftChild != null) {
      convertTreeToDoubleLink(currentNode.leftChild, wrap);
    }
    // 当前节点的前一个节点之前 wrap.treeNode 这个上一个节点，wrap.treeNode 首次初始为为 null，再次则会被初始为父节点，将当前节点的前一个节点指向 父节点
    currentNode.leftChild = wrap.treeNode;
    if (wrap.treeNode != null) { // 如果 wrap.treeNode 被初始化为父节点了，则 父节点双链表的后一节点为 当前节点
      wrap.treeNode.rightChild = currentNode;
    }
    // wrap.treeNode 初始为当前节点，以便于当做下一次递归的父节点
    wrap.treeNode = currentNode;
    // 递归处理 右子节点，右子节点的核心处理也是 现在上面第 68-71 行的两句 设置左右节点。
    convertTreeToDoubleLink(currentNode.rightChild, wrap);
  }

  static class TreeNodeWrap {
    BinaryTreeNode treeNode;
  }
}
