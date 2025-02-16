package com.swj.ics.dataStructure.tree.binarytree;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/01/15 16:54
 * 二叉树的深度
 * 输入一个二叉树的根节点，求该二叉树的深度。从根节点到叶节点一次经过的节点（含根、叶节点）形成一条路径，最长的路径为树的深度
 */
public class DepthOfBinarySearchTree {


  public static void main(String[] args) {
    BinaryTreeNode root1 = new BinaryTreeNode(1);
    BinaryTreeNode parentNode = root1;
    BinaryTreeNode leftChild = new BinaryTreeNode(2);
    BinaryTreeNode rightChild = new BinaryTreeNode(3);
    parentNode.leftChild = leftChild;
    parentNode.rightChild = rightChild;
    rightChild.rightChild = new BinaryTreeNode(6);

    parentNode = leftChild;
    leftChild = new BinaryTreeNode(4);
    rightChild = new BinaryTreeNode(5);
    parentNode.leftChild = leftChild;
    parentNode.rightChild = rightChild;
    rightChild.leftChild = new BinaryTreeNode(7);

    TreeDepth treeDepth = new TreeDepth();
    getTheMaxDepthOfTree(root1,0,treeDepth);
    System.out.println("depth of tree is " + treeDepth.depth);
  }
  static void getTheMaxDepthOfTree(BinaryTreeNode treeNode, int currLevel, TreeDepth treeDepth) {
    if (treeDepth == null) {
      return;
    }
    currLevel++;

    if (isLeafNode(treeNode) && currLevel > treeDepth.depth) {
      treeDepth.depth = currLevel;
    } else {
      if (treeNode.leftChild != null) {
        getTheMaxDepthOfTree(treeNode.leftChild, currLevel, treeDepth);
      }
      if (treeNode.rightChild != null) {
        getTheMaxDepthOfTree(treeNode.rightChild, currLevel, treeDepth);
      }
    }
  }

  private static boolean isLeafNode(BinaryTreeNode node) {
    return node != null && node.leftChild == null && node.rightChild == null;
  }

  static class TreeDepth {
    public int depth;
  }
  /**
   * 总结，这个获取二叉树的层数的方法，也是参照获取二叉树的路径和的方式一样采用的前序遍历，
   * 每遍历一层，curLevel ++ ，在递归退出的时候，形参 curLevel 又会变回上次递归调用的值，因此这里仍然不需要 --，
   * 然后里面每次的 ++ 判断，如果到达叶子结点，且大于当前最大层数，则最大层数更新为 curLevel。
   */

}

