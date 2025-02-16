package com.swj.ics.dataStructure.tree.binarytree;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/01/11 22:59
 * 二叉树的镜像
 * 请完成一个函数，输入一颗二叉树，该函数输出它的镜像。
 * 镜像对很多人来说是个新概念，我们未必能一下子想出来树的镜像方法。为了能够形成直观的印象，我们可以自己画一颗二叉树
 * 然后按照镜子的经验画出它的镜像。参考《二叉树镜像.png》
 */
public class MirrorOfBinaryTree {
  /**
   * 仔细分析这两棵树的特点，看看能不能总结出求镜像的步骤。这两棵树的根节点相同，但是他们的左右子节点交换了位置
   * 因此我们可以看出先交换根节点的左右子树，再交换左右子树节点的左右子树
   * 总结下规律：前前序遍历二叉树的序列，交换左右子节点，
   * 在递归的遍历左右子节点
   */

  static void mirrorOfBinaryTree(BinaryTreeNode rootNode) {
    if (rootNode == null) {
      return;
    }
    BinaryTreeNode tempNode = rootNode.leftChild;
    rootNode.leftChild = rootNode.rightChild;
    rootNode.rightChild = tempNode;
    if (rootNode.leftChild != null) {
      mirrorOfBinaryTree(rootNode.leftChild);
    }
    if (rootNode.rightChild != null) {
      mirrorOfBinaryTree(rootNode.rightChild);
    }
  }

  public static void main(String[] args) {
    int[] preOrderArr = {8, 6, 5, 7, 10, 9, 11};
    int[] middleOrderArr = {5, 6, 7, 8, 9, 10, 11};

    BinaryTreeNode binaryTreeNode = RebuildBinaryTree.buildBinaryTree(preOrderArr, middleOrderArr);
    visitBinaryTreeMiddle(binaryTreeNode);
    System.out.println();
    mirrorOfBinaryTree(binaryTreeNode);
    visitBinaryTreeMiddle(binaryTreeNode);
  }

  static void visitBinaryTreeMiddle(BinaryTreeNode rootNode) {
    if (rootNode == null) {
      return;
    }
    visitBinaryTreeMiddle(rootNode.leftChild);
    System.out.print(rootNode.value + "\t");
    visitBinaryTreeMiddle(rootNode.rightChild);
  }
}
