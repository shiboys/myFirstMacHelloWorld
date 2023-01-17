package com.swj.ics.dataStructure.tree.binarytree;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/01/15 16:53
 * 二叉搜索树第 K 大子节点
 * 给定一个二叉树，请找出第 k 小的节点。如下图所示，第 三大的节点是 4；
 * 5
 * / \
 * 3   7
 * / \ / \
 * 2  4 6  8
 */
public class TopKOfBst {

  public static void main(String[] args) {
    BinaryTreeNode root1 = new BinaryTreeNode(5);
    BinaryTreeNode parentNode = root1;
    BinaryTreeNode leftChild = new BinaryTreeNode(3);
    BinaryTreeNode rightChild = new BinaryTreeNode(7);
    parentNode.leftChild = leftChild;
    parentNode.rightChild = rightChild;

    parentNode = leftChild;
    parentNode.leftChild = new BinaryTreeNode(2);
    parentNode.rightChild = new BinaryTreeNode(4);

    parentNode = rightChild;
    parentNode.leftChild = new BinaryTreeNode(6);
    parentNode.rightChild = new BinaryTreeNode(8);

    KMax kMax = new KMax();
    int k = 3;
    findKRecord(root1, kMax, k);
    if (kMax.kValue != null) {
      System.out.println(String.format("the top k small %s record's value is %s", k, kMax.kValue));
    } else {
      System.out.println("can not find k max value");
    }
    KMax kMax2 = new KMax();
    findKRecordMax(root1, kMax2, k);
    if (kMax2.kValue != null) {
      System.out.println(String.format("the top k largest %s record's value is %s", k, kMax2.kValue));
    } else {
      System.out.println("can not find k max value");
    }
  }

  // 中序遍历一遍，同时记录被遍历的次数，也就第 k 次
  static class KMax {
    public int kIndex;
    public Integer kValue;
  }

  static void findKRecord(BinaryTreeNode treeNode, KMax kmax, int k) {
    if (treeNode == null || kmax == null) {
      return;
    }
    if (treeNode.leftChild != null) {
      findKRecord(treeNode.leftChild, kmax, k);
    }

    if (kmax.kValue != null) {
      return;
    }
    if (++kmax.kIndex == k) {
      kmax.kValue = treeNode.value;
      return;
    }

    if (treeNode.rightChild != null) {
      findKRecord(treeNode.rightChild, kmax, k);
    }
  }

  static void findKRecordMax(BinaryTreeNode treeNode, KMax kmax, int k) {
    if (treeNode == null || kmax == null) {
      return;
    }
    if (treeNode.rightChild != null) {
      findKRecordMax(treeNode.rightChild, kmax, k);
    }


    if (kmax.kValue != null) {
      return;
    }
    if (++kmax.kIndex == k) {
      kmax.kValue = treeNode.value;
      return;
    }

    if (treeNode.leftChild != null) {
      findKRecordMax(treeNode.leftChild, kmax, k);
    }
  }
}
