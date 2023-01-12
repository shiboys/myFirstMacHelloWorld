package com.swj.ics.dataStructure.tree.binarytree;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/01/12 22:21
 * 二叉搜索树的后续遍历
 * 输入一个数组，判断该数组是不是某个二叉搜素树的后续遍历结果。如果是则返回 true，否则返回 false
 * 假设输入的数组的任意两个数字都互不相同。例如，输入数组 {5,7,6,9,11,10,8} ，则返回 true
 * 如果输入的数组是 {7,4,6,5}, 由于没有哪个 BST 的后续遍历结果是这个序列，因此返回 false
 */
public class PostOrderVisitOfBST {

  /**
   * 以数组 {5,7,6,9,11,10,8} 为例，后续遍历的结果序列的最后一个数字 8 就是根节点的值
   * 在这个数组中，前 3 个数字 {5,7,6} 都比 8 小，是值为 8 的根节点的左子节点
   * {9,11,10} 都比 8 大 是值为 8 的根节点的右子节点
   * 接下来我们分析 {5,7,6} ,发现最后一个数字 6 是左子节点的根节点。数字 5 比 6 小，因此代表左节点，7比 6 大因此代表右节点
   * {9,11,10} 也是同理，因此我们可以看出这就是一个递归。
   */

  public static void main(String[] args) {
    int[] arr = {5, 7, 6, 9, 11, 10, 8};
    int[] arr2 = {7, 4, 6, 5};
    System.out.println(isPostOrderVisitOfBST(arr, 0, arr.length));
    System.out.println(isPostOrderVisitOfBST(arr2, 0, arr2.length));
  }

  static boolean isPostOrderVisitOfBST(int[] bstArr, int startIndex, int length) {
    if (bstArr == null || bstArr.length < 1) {
      return false;
    }
    // 只有一个元素的时候返回 true，否则就是死循环，栈溢出了
    if (startIndex == length - 1) {
      return true;
    }
    int rootValue = bstArr[length - 1];
    int left = startIndex;
    for (; left < length; left++) {
      if (bstArr[left] > rootValue) {
        break;
      }
    }
    int right = left;
    for (; right < length; right++) {
      if (bstArr[right] < rootValue) {
        return false;
      }
    }

    boolean isPostOrderLeft = true; // 到这里，如果没有左子树，则认为左子树符合后序遍历
    if (left > startIndex) { // 有左子树，则遍历左子树
      isPostOrderLeft = isPostOrderVisitOfBST(bstArr, startIndex, left);
    }
    boolean isPostOrderRight = true; // 同理，如果没有右子树，则认为右子树符合后序遍历
    if (right > left) { // 有右子树，遍历右子树
      isPostOrderRight = isPostOrderVisitOfBST(bstArr, left, length - left - 1);
    }
    return isPostOrderLeft && isPostOrderRight;
  }

}
