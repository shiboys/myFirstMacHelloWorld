package com.swj.ics.dataStructure.tree.binarytree;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/01/10 17:07
 * 重建二叉树
 * 输入一个二叉树的前序遍历和中序遍历的结果，重建该二叉树
 * 假设输入的前序遍历和中序遍历的结果都不含重复的数字，假设输入的
 * 前序序列{1,2,4,7,3,5,6,8}和中序遍历序列{4,7,2,1,5,3,8,6}
 * 重建如下图所示的二叉树
 *      1
 *    /  \
 *  2    3
 * /     /\
 * 4     5 6
 * \    /
 * 7   8
 * 在二叉树前序遍历的中，第一个数字总是数的根节点。但是在中序遍历中，根节点的位置在序列中间，左子树的节点的值在根节点的值的前面，而右子树的节点的
 * 值在根节点的右边，因此我们需要扫描中序遍历的结果，才能正确地找到左右子树。
 * 前序遍历
 * 根节点---左子树----右子树
 * 1,      2,4,7,   3,5,6,8
 * 中序遍历
 * 左子树---根节点---右子树
 * 4,7,2,  1,      5,3,8,6
 * 既然我们可以分别找到了左右子树的前序遍历和中序遍历结果，我们可以用同样的方法分别构建左右子树。
 */

public class RebuildBinaryTree {

  public static void main(String[] args) {
    int[] preOrderArr={1,2,4,7,3,5,6,8};
    int[] middleOrderArr={4,7,2,1,5,3,8,6};
    //visitPreOrder();
    BinaryTreeNode rootNode = buildBinaryTree(preOrderArr, middleOrderArr);
    visitPreOrder(rootNode);
  }
  static void visitPreOrder(BinaryTreeNode treeNode) {
    if (treeNode == null) {
      return;
    }
    System.out.print(treeNode.value + "\t");
    visitPreOrder(treeNode.leftChild);
    visitPreOrder(treeNode.rightChild);
  }

  static BinaryTreeNode buildBinaryTree(int[] preOrderArr, int[] middleOrderArr) {
    if (preOrderArr == null || preOrderArr.length < 1) {
      throw new IllegalArgumentException("pre order Array is empty");
    }
    if (middleOrderArr == null || middleOrderArr.length < 1) {
      throw new IllegalArgumentException("middle order Array is empty");
    }

    return doBuildBinaryTree(preOrderArr, 0, preOrderArr.length - 1, middleOrderArr, 0, middleOrderArr.length - 1);
  }

  static BinaryTreeNode doBuildBinaryTree(int[] preOrderArr, int preStartIndex, int preEndIndex, int[] middleOrderArr,
      int midStartIndex, int midEndIndex) {
    if (preStartIndex > preEndIndex || midStartIndex > midEndIndex) {
      return null;
    }
    int rootVal = preOrderArr[preStartIndex];
    BinaryTreeNode rootNode = new BinaryTreeNode(rootVal);
    if (preStartIndex == preEndIndex) {
      if (midStartIndex == midEndIndex && preOrderArr[preStartIndex] == middleOrderArr[midStartIndex]) {
        return rootNode;
      } else {
        throw new IllegalArgumentException("invalid input");
      }
    }
    int rootIndex = -1;
    for (int i = midStartIndex; i <= midEndIndex; i++) {
      if (middleOrderArr[i] == rootVal) {
        rootIndex = i;
        break;
      }
    }
    if (rootIndex == -1) {
      throw new IllegalArgumentException("invalid root value of middle order tree");
    }
    int leftLength = rootIndex - midStartIndex;
    int leftPreIndexEnd = preStartIndex + leftLength;
    //构建左子树
    rootNode.leftChild = doBuildBinaryTree(preOrderArr, preStartIndex + 1, leftPreIndexEnd, middleOrderArr,
        midStartIndex, rootIndex - 1);
    rootNode.rightChild = doBuildBinaryTree(preOrderArr, leftPreIndexEnd + 1,
        preEndIndex, middleOrderArr, rootIndex + 1, midEndIndex);
    return rootNode;
  }
}
