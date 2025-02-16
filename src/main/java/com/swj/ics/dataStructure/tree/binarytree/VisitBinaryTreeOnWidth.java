package com.swj.ics.dataStructure.tree.binarytree;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Stack;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/01/12 12:02
 * 从上到下打印二叉树
 * 从上到下打印出一个二叉树的每个节点，同一层的节点按照从左至右的顺序打印。
 * 如下图所示的的二叉树
 * 8
 * / \
 * 6   10
 * /\   /\
 * 5 7  9 11
 * 打印顺序为 8,6,10,5,7,9,11
 * 这道题其实是考察对树的遍历，只是这种遍历不是我们熟悉的前序、中序和后序。由于我们不太熟悉这种按层遍历的方法，可能一下子也想不清楚遍历的过程。
 * 那么面试的时候怎么办？那么我们不妨先分析下这个二叉树的广度优先遍历过程
 * 每次打印一个节点的时候，如果该节点有子节点，应该把子节点放入一个集合中，而且这个集合还能很方便的取出/移除它的子节点。那么我们选用集合就能满足
 */
public class VisitBinaryTreeOnWidth {
  public static void main(String[] args) {
    // visitTreeOnWithDemo();
    printBinaryTreeLikeZ(buildBinaryTreeOfZ());
  }

  private static void visitTreeOnWithDemo() {
    BinaryTreeNode root1 = new BinaryTreeNode(8);
    BinaryTreeNode parentNode = root1;
    BinaryTreeNode leftChild = new BinaryTreeNode(6);
    BinaryTreeNode rightChild = new BinaryTreeNode(10);
    parentNode.leftChild = leftChild;
    parentNode.rightChild = rightChild;

    parentNode = leftChild;
    /*leftChild = new BinaryTreeNode(5);
    rightChild = new BinaryTreeNode(7);*/
    parentNode.leftChild = new BinaryTreeNode(5);
    parentNode.rightChild = new BinaryTreeNode(7);

    parentNode = rightChild;
  /*  leftChild = new BinaryTreeNode(4);
    rightChild = new BinaryTreeNode(7);*/
    parentNode.leftChild = new BinaryTreeNode(9);
    parentNode.rightChild = new BinaryTreeNode(11);
    printBinaryTreeOnWidth(root1);
    System.out.println();
    printBinaryTreeOnWidthByLevel(root1);
  }

  static void printBinaryTreeOnWidth(BinaryTreeNode root) {
    if (root == null) {
      return;
    }
    Queue<BinaryTreeNode> queue = new ArrayDeque<>();
    queue.add(root);
    while (!queue.isEmpty()) {
      BinaryTreeNode node = queue.remove();
      if (node != null) {
        System.out.print(node.value + "\t");
      }
      if (node.leftChild != null) {
        queue.add(node.leftChild);
      }
      if (node.rightChild != null) {
        queue.add(node.rightChild);
      }
    }
  }

  /**
   * 举一反三：不管是广度优先遍历一幅图还是一棵树，都要用到队列。首先把起始节点（对树而言是根节点）放入队列。杰西莱每次从队列中的头部取出一个节点
   * 遍历这个节点之后把它能访问到的节点(对树而言就是左右子节点）都依次放入队列。重复这个遍历过程，直到队列中的节点全部被遍历为止。
   * 题目2：从上到下按层打印二叉树，每一层打印顺序是从左到右，每一层打印一行。如下而插入的打印结果
   *  8
   * / \
   * 6   10
   * /\   /\
   * 5 7  9 11
   * 打印结果为
   * 8
   * 6,10
   * 5,7,9,11
   */

  static void printBinaryTreeOnWidthByLevel(BinaryTreeNode root) {
    if (root == null) {
      return;
    }
    Queue<BinaryTreeNode> queue = new ArrayDeque<>();
    queue.add(root);
    int levelCount = 1;
    int nextLevelCount = 0;
    while (!queue.isEmpty()) {
      BinaryTreeNode node = queue.remove();
      if (node != null) {
        System.out.print(node.value + "\t");
      }
      if (node.leftChild != null) {
        queue.add(node.leftChild);
        nextLevelCount++;
      }
      if (node.rightChild != null) {
        queue.add(node.rightChild);
        nextLevelCount++;
      }
      levelCount--;
      if (levelCount == 0) {
        System.out.println();
        levelCount = nextLevelCount;
        nextLevelCount = 0;
      }
    }
  }

  /**
   * 题目三：之字形打印二叉树
   * 请实现一个函数按照之字形打印二叉树。即第一行按照从左至右的顺序打印，第二行按照从右至左的顺序打印，第三行再按照从左至右的顺序打印，其他行
   * 以此类推。如图《之字形打印二叉树.png》
   * 打印结果如下：
   * 1
   * 3，2
   * 4，5，6，7
   * 15，14，13，12，11，10，9，8
   * 之字形打印二叉树需要两个栈，我们在打印某一层节点的时候，把相应的节点保存在另外一个栈中。
   * 如果当前打印的是奇数(第一层，第三层），则先保存左子节点，再保存右子节点到第一个栈里；
   * 如果当前打印的是偶数层（第二层，第四层）则先保存右子节点再保存左子节点到第二个栈里
   * 为什么需要两个栈，因为同一层的节点需要放到一个栈里，而遍历的时候，同时需要把下一层的节点放到另外一个栈中
   */

  static void printBinaryTreeLikeZ(BinaryTreeNode root) {
    if (root == null) {
      return;
    }

    Stack<BinaryTreeNode> current = new Stack<>();
    Stack<BinaryTreeNode> next = new Stack<>();
    current.push(root);
    int currentLevel = 1;
    while (!current.isEmpty()) {
      BinaryTreeNode node = current.pop();
      if (node == null) {
        continue;
      }
      System.out.print(node.value + "\t");
      if ((currentLevel & 1) == 1) {
        if (node.leftChild != null) {
          next.push(node.rightChild);
        }
        if (node.rightChild != null) {
          next.push(node.leftChild);
        }
      } else {
        if (node.rightChild != null) {
          next.push(node.rightChild);
        }
        if (node.leftChild != null) {
          next.push(node.leftChild);
        }
      }

      if (current.isEmpty()) {
        Stack<BinaryTreeNode> temp = current;
        current = next;
        next = temp;
        next.clear();
        currentLevel++;
        System.out.println();
      }
    }
  }

  static BinaryTreeNode buildBinaryTreeOfZ() {
    BinaryTreeNode root1 = new BinaryTreeNode(1);
    BinaryTreeNode parentNode = root1;
    BinaryTreeNode leftChild = new BinaryTreeNode(2);
    BinaryTreeNode rightChild = new BinaryTreeNode(3);
    parentNode.leftChild = leftChild;
    parentNode.rightChild = rightChild;

    parentNode = leftChild;
    leftChild = new BinaryTreeNode(4);
    rightChild = new BinaryTreeNode(5);
    parentNode.leftChild = leftChild;
    parentNode.rightChild = rightChild;

    leftChild.leftChild = new BinaryTreeNode(8);
    leftChild.rightChild = new BinaryTreeNode(9);

    rightChild.leftChild = new BinaryTreeNode(10);
    rightChild.rightChild = new BinaryTreeNode(11);


    parentNode = root1.rightChild;
    leftChild = new BinaryTreeNode(6);
    rightChild = new BinaryTreeNode(7);
    parentNode.leftChild = leftChild;
    parentNode.rightChild = rightChild;

    leftChild.leftChild = new BinaryTreeNode(12);
    leftChild.rightChild = new BinaryTreeNode(13);

    rightChild.leftChild = new BinaryTreeNode(14);
    rightChild.rightChild = new BinaryTreeNode(15);

    return root1;
  }
}
