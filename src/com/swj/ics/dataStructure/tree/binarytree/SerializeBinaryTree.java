package com.swj.ics.dataStructure.tree.binarytree;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/01/14 16:35
 * 序列化二叉树
 * 请实现两个个函数，分别用来序列化和反序列二叉树
 * <p>
 * 通过之前的重建二叉树，我们知道，从前序遍历和中序遍历的结果中能重新构建一个二叉树。受此启发，我们可以先把一颗二叉树序列化成一个前序遍历序列
 * 和一个中序遍历序列，然后再反序列时通过这两个序列重构出原来的二叉树
 * 但是这个思路有两个缺点：一是该方法要求二叉树中不能有重复的节点；二是只有当两个序列中所有数据读出来之后才能开始反序列化。如果两个遍历数据是从
 * 一个序列流里面读出来的，那么可能需要等待较长的时间
 * 实际上二叉树的序列化是从根节点开始的，那么相应的反序列化在根节点的数值读出来的时候就可以开始了。因此，我们可以根据前序遍历出来的顺序来序列化
 * 二叉树，因为前序遍历是从根节点开始的。在遍历二叉树碰到 null 指针时，这些 null 指针可以序列化为 字符串的 null，节点之前的数值用一个特殊
 * 符号隔开，例如逗号 , 如下图所示的二叉树序列化之后如下所示
 *    1
 *   / \
 *  2   3
 * /   / \
 *4   5   6
 * <p>
 * 序列化之后的字符串为 1,2,4,null,null,null,3,5,null,null,6,null,null
 */
public class SerializeBinaryTree {

  static void serializeBinaryTreeToString(BinaryTreeNode root, List<Integer> nodeValueList) {
    BinaryTreeNode pNode = root;

    if (pNode != null) {
      nodeValueList.add(pNode.value);
    } else {
      nodeValueList.add(null);
    }
    if (pNode != null) {
      serializeBinaryTreeToString(pNode.leftChild, nodeValueList);
    }
    if (pNode != null) {
      serializeBinaryTreeToString(pNode.rightChild, nodeValueList);
    }
  }

  /**
   * 反序列化字符串为一个二叉树。
   * 1,2,4,null,null,null,3,5,null,null,6,null,null
   * 这个序列化的字符串如何进行反序列化那？第一个读取出来的数字是 1
   * @param nodeValueSerValue
   */
  static BinaryTreeNode deserializeBinaryTreeNodeFromString(String nodeValueSerValue) {
    if (nodeValueSerValue == null || nodeValueSerValue.isEmpty()) {
      throw new IllegalArgumentException("deserialized binary tree value is empty.");
    }
    String[] splitArr = nodeValueSerValue.split(",");
    if (splitArr.length < 1) {
      throw new IllegalArgumentException("deserialized binary tree value is illegal.");
    }
    Cursor cursor = new Cursor();
    Integer rootValue = getIntegerValue(splitArr, 0);
    if (rootValue == null) {
      throw new IllegalArgumentException("the root value of deserialized binary tree is illegal.");
    }
    BinaryTreeNode root = doRebuildTree(splitArr, cursor);
    if(root != null) {
      System.out.println(
          String.format("val=%s,left=%s,right=%s", root.value, root.leftChild.value, root.rightChild.value));
    } else {
      System.out.println("root is null");
    }
    return root;
  }

  static BinaryTreeNode doRebuildTree(String[] desValArr, Cursor cursor) {
    int curIndex = cursor.currentIndex++;
    if (curIndex > desValArr.length - 1) {
      return null;
    }
    BinaryTreeNode pNode = null;
    Integer val = getIntegerValue(desValArr, curIndex);

    if (val != null) {
      pNode = new BinaryTreeNode(val);
      pNode.leftChild = doRebuildTree(desValArr, cursor);
      pNode.rightChild = doRebuildTree( desValArr, cursor);
    }
    return pNode;
  }

  private static Pattern numberPatter = Pattern.compile("^[0-9]+$");

  static class Cursor {
    public int currentIndex;

  }

  private static Integer getIntegerValue(String[] deserializeValueArr, int index) {
    String val = deserializeValueArr[index];
    if (val == null || val.equals("") || val.equals("null")) {
      return null;
    } else if (!numberPatter.matcher(val).matches()) {
      System.out.println(String.format("val is not mater integer number. [val=%s]", val));
      return null;
    }
    return Integer.valueOf(val);
  }



  public static void main(String[] args) {
    BinaryTreeNode root1 = new BinaryTreeNode(1);
    BinaryTreeNode parentNode = root1;
    BinaryTreeNode leftChild = new BinaryTreeNode(2);
    BinaryTreeNode rightChild = new BinaryTreeNode(3);
    parentNode.leftChild = leftChild;
    parentNode.rightChild = rightChild;

    parentNode = leftChild;
    leftChild = new BinaryTreeNode(4);
    parentNode.leftChild = leftChild;

    parentNode = rightChild;
    leftChild = new BinaryTreeNode(5);
    rightChild = new BinaryTreeNode(6);
    parentNode.leftChild = leftChild;
    parentNode.rightChild = rightChild;
    List<Integer> nodeList = new ArrayList<>();
    serializeBinaryTreeToString(root1, nodeList);
    String serializeValue = nodeList.stream().map(String::valueOf).collect(Collectors.joining(","));
    System.out.println(serializeValue);
    deserializeBinaryTreeNodeFromString(serializeValue);

  }
}
