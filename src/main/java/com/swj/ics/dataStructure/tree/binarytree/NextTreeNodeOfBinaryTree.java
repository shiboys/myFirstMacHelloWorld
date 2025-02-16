package com.swj.ics.dataStructure.tree.binarytree;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/01/10 21:41
 * 二叉树的下一个节点
 * 给定一颗二叉树和其中一个节点，如何找出中序遍历的下一个节点？树中的节点除了有两个左右子节点指针外，还有一个指向父节点的指针。
 */
public class NextTreeNodeOfBinaryTree {
  /**
   * 中序遍历下一个节点。如下图所示，中序遍历的结果是{d,b,h,e,i,a,f,c,g}
   *       a
   *     /   \
   *    b     c
   *   /\    /\
   *  d  e  f g
   *    /\
   *   h  i
   */

  /**
   * 如果一个节点有右子树，那么它的下一个节点就是它的右子树的最左子节点。例如 {b,h} ，{a,f}
   * 如果一个节点没有右子树，如果节点是其父节点的左子树，那么它的下一个节点就是其父节点。比如 {d,b},{f,c}
   * 如果一个节点既没有右子树，且它还是其父节点的右子节点，我们需要沿着其父节点的指针一直向上遍历，直到找到一个父节点是其父父节点的左子节点，
   * 那么这个父父节点就是当前节点的下一个节点，比如{i,a}
   */
  public static void main(String[] args) {
    testNextChild();
  }

  static void testNextChild() {
    TreeNode<Character> h = new TreeNode<>('h');
    TreeNode<Character> i = new TreeNode<>('i');
    TreeNode<Character> e = new TreeNode<>('e');
    e.leftChild = h;
    e.rightChild = i;
    TreeNode<Character> d = new TreeNode<>('d');
    TreeNode<Character> b = new TreeNode<>('b');
    b.leftChild = d;
    b.rightChild = e;
    TreeNode<Character> a = new TreeNode<>('a');

    TreeNode<Character> f = new TreeNode<>('f');
    TreeNode<Character> g = new TreeNode<>('g');
    TreeNode<Character> c = new TreeNode<>('c');
    c.leftChild = f;
    c.rightChild = g;
    a.leftChild = b;
    a.rightChild = c;

    h.parent = i.parent = e;
    d.parent = e.parent = b;
    b.parent = c.parent = a;
    f.parent = g.parent = c;

    //d,b,h,e,i,a,f,c,g
    System.out.println(findNextTreeNode(d));
    System.out.println(findNextTreeNode(b));
    System.out.println(findNextTreeNode(h));
    System.out.println(findNextTreeNode(e));
    System.out.println(findNextTreeNode(i));
    System.out.println(findNextTreeNode(a));
    System.out.println(findNextTreeNode(f));
    System.out.println(findNextTreeNode(c));
    System.out.println(findNextTreeNode(g));
  }

  static TreeNode<Character> findNextTreeNode(TreeNode<Character> sourceNode) {
    if (sourceNode == null) {
      return null;
    }
    if (sourceNode.rightChild != null) {
      if (sourceNode.rightChild.leftChild != null) {
        return sourceNode.rightChild.leftChild;
      }
      return sourceNode.rightChild;
    } else {
      TreeNode<Character> parent = sourceNode.parent;
      if (parent != null && sourceNode == parent.leftChild) {
        return parent;
      } else {
        while (parent != null) {
          TreeNode<Character> treeNode = parent;
          parent = treeNode.parent;
          if (parent != null && treeNode == parent.leftChild) {
            return parent;
          }
        }
      }
    }
    return null;
  }
}
