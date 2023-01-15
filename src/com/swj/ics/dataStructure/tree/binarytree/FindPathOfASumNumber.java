package com.swj.ics.dataStructure.tree.binarytree;

import java.util.Stack;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/01/12 23:30
 * 二叉树和为某一值的路径
 * 输入一棵二叉树和一个整数，打印出二叉树中节点值的和为输入整数的所有路径。从树的根节点一直往下直到叶子节点所经过的节点形成一条路径。
 */
public class FindPathOfASumNumber {
  /**
   * 这里先给出一个示例二叉树，如下所示
   *      10
   *     / \
   *    5  12
   *   /\
   *  4 7
   * 二叉树中有两条和为 22 的路径，一条路径是 10->5->7，另外一条是 10->22。
   * 一般的数据结构和算法都没有介绍树的路径，因此对大多数应聘者而言，这是一个新概念，也很难一下子想象出完整的解题路径。这时候我们可以试着从一两个
   * 具体的例子入手，寻找规律。
   * 从根节点出发，我们想到了树的前序遍历。首先我们遍历 10->5->4 发现节点的路径和不为 22，接着遍历下一个节点
   * 在遍历下一个节点之前，我们需要先从节点 4 回到节点 5，再去遍历节点 5 的右子节点 7.值得注意的是，当回到节点 5 的时候，由于节点 4 已经不再是
   * 前往节点 7 的路径上，因此我们需要把节点 4 从路径中删除。接下来访问节点 7 的时候，再把该节点添加到路径中。此时路径中的三个节点 10,5,7
   * 之和刚好是 22，是一条符合要求的路径。
   * 我们最后要遍历的节点是 12。在遍历这个节点之前，需要先经过节点 5 回到节点 10。同样，每次当从子节点回到父节点的时候，我们都需要在路径上删除
   * 子节点。最后再从节点 10 达到节点 12 的时候，路径上的这 2 个节点之后也是 22.
   * 分析完前面的具体例子，我们就找到了一些规律，当前序遍历的方式访问到某一个节点的时候，我们把该节点添加到路径上，并累加该节点的值。
   * 如果该节点为叶节点，并且路径中的节点值之和刚好为目标和
   * 经过前面的分析，我们找到一个一些规律。当前序遍历的方式遍历到某一个节点的时候，我们把该节点添加到路径上，并累加该节点的值。如果该节点为叶节点
   * 并且路径中节点值的和刚好等于输入的整数，则当前路径满足，把当前路径打印出来。如果当前节点不是叶子节点，则继续访问它的子节点。当子节点访问结束后
   * 递归函数将自动返回到它的父节点。因此在函数退出之前要在路径上删除当前节点并减去当前节点的值，以确保返回父节点时，路径刚好是从根节点到父节点
   * 我们不难看出保存路径的数据结构实际上是一个栈，因为路径要与递归调用状态一致（也就是说，递归到哪个节点，路径和就是从根节点到当前递归节点的和）
   * 而递归调用就的本质就是一个压栈和出栈的过程。
   */

  public static void main(String[] args) {
    BinaryTreeNode root1 = new BinaryTreeNode(10);
    BinaryTreeNode parentNode = root1;
    BinaryTreeNode leftChild = new BinaryTreeNode(5);
    BinaryTreeNode rightChild = new BinaryTreeNode(12);
    parentNode.leftChild = leftChild;
    parentNode.rightChild = rightChild;

    parentNode = leftChild;
    leftChild = new BinaryTreeNode(4);
    rightChild = new BinaryTreeNode(7);
    parentNode.leftChild = leftChild;
    parentNode.rightChild = rightChild;
    int targetSum = 22;
    findPathOfTargetSumNumber(root1, targetSum);
  }
  static void findPathOfTargetSumNumber(BinaryTreeNode root, int sum) {
    if (root == null) {
      throw new IllegalArgumentException("root is null");
    }
    int currSum = 0;
    Stack<BinaryTreeNode> nodeStack = new Stack<>();
    printPathOfTargetSumNumber(root, sum, currSum, nodeStack);
  }

  private static void printPathOfTargetSumNumber(BinaryTreeNode node, int targetSum, int currSum,
      Stack<BinaryTreeNode> nodeStack) {
    nodeStack.push(node);
    currSum += node.value;
    if (isLeafNode(node) && currSum == targetSum) { // 到达叶子节点
      printPathFromStack(nodeStack);
    } else { // 非叶子节点，继续递归遍历
      if (node.leftChild != null) {
        printPathOfTargetSumNumber(node.leftChild, targetSum, currSum, nodeStack);
      }
      if (node.rightChild != null) {
        printPathOfTargetSumNumber(node.rightChild, targetSum, currSum, nodeStack);
      }
    }
    // 把当前节点从栈中弹出，表示当前节点已经不是当前路径的一部分了。
    nodeStack.pop();
  }

  private static boolean isLeafNode(BinaryTreeNode node) {
    return node != null && node.leftChild == null && node.rightChild == null;
  }

  private static void printPathFromStack(Stack<BinaryTreeNode> nodeStack) {
    if (nodeStack != null && !nodeStack.isEmpty()) {
      for (BinaryTreeNode node : nodeStack) {
        System.out.print(node.value + "\t");
      }
      System.out.println();
    } else {
      System.out.println("node stack is empty!");
    }
  }

}
