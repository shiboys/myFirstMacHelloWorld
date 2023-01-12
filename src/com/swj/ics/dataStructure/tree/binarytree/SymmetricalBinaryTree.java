package com.swj.ics.dataStructure.tree.binarytree;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/01/11 23:35
 * 对称的二叉树
 * 请实现一个函数，用来判断一颗二叉树是不是对称的。如果一颗二叉树和它的镜像一样，那么它是对称的。
 * 如 《对称二叉树.png》所示，第一棵树是对称的，另外两棵不是。
 * 通常我们有 3 种不同的二叉树遍历法，即前序，中序，后序。在这 3 种遍历算法中，都是先遍历左子节点，再遍历右子节点。
 * 我们是否可以定义一种遍历算法，先遍历右子节点再遍历左子节点。比如我们针对前序遍历的一种对称遍历算法，先遍历父节点，再遍历右子节点，左后遍历左子节点。
 * 比如图一所示，前序遍历的结果为：{8,6,5,7,6,7,5}，而前序遍历的对称遍历方法遍历结果如下：{8,6,5,7,6,7,5},可以看出和前序遍历的结果完全一样。
 * 而图中第二棵树的 前序遍历的结果为：{8,6,5,7,9,7,5}，而前序遍历的对称遍历方法遍历结果如下：{8,9,5,7,6,7,5}，这显然不符合对称二叉树
 * 第三棵树有些特别，它的所有值都是一样的，它的前序是{7,7,7,7,7,7} 前序的对称遍历也是 {7,7,7,7,7,7}，可以是显然第三棵树是不对称的，怎样才能正确地
 * 判断这种类型的二叉树那？我们可以借助遍历时经常被忽略的 null 节点，如果把遍历时遇到的 null 节点也考虑进去，就可以辨别出是否是真正的对称二叉树
 * <p>
 * 加上 null 的判断之后，第三个二叉树的前序遍历结果为：7,7,7,null,null,7,null,null,7,7,null,null,null
 * 第三个二叉树的前序遍历的对称遍历结果为：7,7,null,7,null,null,7,7,null,null,7,null,null 这样以来，第三课二叉树就可以认为不对称。
 */
public class SymmetricalBinaryTree {

  public static void main(String[] args) {
    //testDemo1();
    BinaryTreeNode root1 = new BinaryTreeNode(7);
    BinaryTreeNode parentNode = root1;
    BinaryTreeNode leftChild = new BinaryTreeNode(7);
    BinaryTreeNode rightChild = new BinaryTreeNode(7);
    parentNode.leftChild=leftChild;
    parentNode.rightChild =rightChild;

    parentNode = leftChild;
    parentNode.leftChild=new BinaryTreeNode(7);
    parentNode.rightChild =new BinaryTreeNode(7);

    parentNode = rightChild;
    parentNode.leftChild=new BinaryTreeNode(7);


    System.out.println(isSymmetrical(root1));
  }

  private static void testDemo1() {
    BinaryTreeNode root1 = new BinaryTreeNode(8);
    BinaryTreeNode parentNode = root1;
    BinaryTreeNode leftChild = new BinaryTreeNode(6);
    BinaryTreeNode rightChild = new BinaryTreeNode(6);
    parentNode.leftChild=leftChild;
    parentNode.rightChild =rightChild;

    parentNode = leftChild;
    /*leftChild = new BinaryTreeNode(5);
    rightChild = new BinaryTreeNode(7);*/
    parentNode.leftChild=new BinaryTreeNode(5);
    parentNode.rightChild =new BinaryTreeNode(7);

    parentNode = rightChild;
  /*  leftChild = new BinaryTreeNode(4);
    rightChild = new BinaryTreeNode(7);*/
    parentNode.leftChild=new BinaryTreeNode(7);
    parentNode.rightChild =new BinaryTreeNode(5);


    System.out.println(isSymmetrical(root1));
  }

  static boolean isSymmetrical(BinaryTreeNode root) {
    return isSymmetrical(root, root);
  }

  static boolean isSymmetrical(BinaryTreeNode root1, BinaryTreeNode root2) {
    if (root1 == null && root2 == null) {
      return true;
    }
    if (root1 == null || root2 == null) {
      return false;
    }
    if (root1.value != root2.value) {
      return false;
    }
    return isSymmetrical(root1.leftChild, root2.rightChild) && isSymmetrical(root1.rightChild, root2.leftChild);
  }
}
