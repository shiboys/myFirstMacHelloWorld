package com.swj.ics.dataStructure.tree;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/01/31 22:03
 * 树的最小公共节点
 * 题目：输入两棵树，求他们的公共节点
 */
public class TheSmallestCommonNodeOfATree {
  /**
   * 题目分析：
   * 这类概括性的题目，一般比价发散，需要问清楚面试官的各种细节：
   * 1、这个树是不是二叉树？是又怎么样，不是又怎么样
   * 如果是二叉树，且是二叉搜索树，那么是可以找到公共节点的
   * 那假设说是二叉搜索树，是怎么个查找法？
   * 二叉搜索树是排过序的，位于左子树的节点都比父节点小，而位于右子树的节点都比父节点大，我们只需要从树的根节点开始跟里那个输入节点进行比较。
   * 如果当前节点的值比两个输入节点的值都大，那么最低的公共节点一定在当前节点的左子树上，于是下一步遍历当前节点的左子节点。
   * 如果当前节点的值小于两个输入值的任何一个值，那么最低公共节点一定在当前节点的右子树中，于是下一步遍历当前节点的右子树。
   * 这样就能在书中从上到下找到第一个在两个节点之间的输入值
   * 2、如果这棵树不是二叉搜索树，甚至连二叉树都不是，只是普通的树，又该怎么办呢？
   * 解答：
   * 如果书中的每个节点（根节点除外）都有一个指向父节点的指针，那么这个问题可以转换成求两个链表的第一个公共节点。
   * 画图参考 《带父节点指针的树.png》
   * 比如输入的两个节点分别是 F 和 H，那么 F 在链表 F->D->B->A 上，而 H 在链表 H->E->B->A 上
   * 这两个链表的第一个交汇点是B，那么B就是他们的最低公共祖先
   * 求两个链表的公共节点这个，在 FirstJointNodeOf2List 这个类中有详细的描述，主要步骤为：
   * 2.1 分别求出两个链表的长度 l 和 s
   * 2.2 使用 l-s 得出两个链表长度之差 d，然后让长的链表先走 d 步之后，两个链表同一个步长走，那么如果它们的下一个节点相等，
   * 则认为下一个节点就是他们的第一个节点
   * <p>
   * 3、如果这个树没有指向父节点的指针，只是普通的树，那要怎么计算出两个输入节点的最低公共节点那？
   * 继续参考图 《带父节点指针的树.png》 不考虑指向父节点的指针
   * 我们用两个链表分别保存从根节点到输入节点的路径，然后把问题转化为两个链表的公共节点
   * 分析步骤：
   * 3.1 我们首先得到一条从根节点到树中某一条叶节点的路径，这就要求在遍历的时候有一个辅助内存来保存遍历路径。比如说我们用前序遍历的方法得到从
   * 根节点到 H 节点的路径是这样的：
   * 3.1.1 遍历到 A，把 A 放到路径中，路径中只有一个节点 A；
   * 3.1.2 遍历到B，把 B 放到路径中，此时的路径为 A->B
   * 3.1.3 遍历到 D，把 D 放到路径中，此时的路径为 A->B->D
   * 3.1.4 遍历到 F，把 F 放到路径中，此时的路径为 A->B->D->F
   * 3.1.5 此时 F 是叶子结点，并且路径上不包含 H 节点，因此这里把 F 从路径中删除，变成 A->B->D
   * 3.1.6 此时遍历 G 节点，和 F 节点一样，这条路径不能到达 F 节点，遍历完 G 节点后，路径仍然是 A->B->D
   * 3.1.7 由于 D 节点的所有子节点都已经遍历完毕，不可能达到 H 节点，因此 D 不在从 A 到 H 的路径中，把 D 从路径中删除变成 A->B
   * 3.1.8 遍历 E 节点，把 E 节点放到路径中，此时路径编程 A->B->E。
   * 3.1.9 遍历 H ，已经到达目标节点，A->B->E 就是从根节点到达 H 的必经之路
   * <p>
   * 3.2 我们可以同样的方式得到从根节点到达 F 必须经过的路径是 A->B->D。接着我们求出两条路径的最后一个公共节点，也就是 B。
   * B 这个节点就是 F 和 H 的最低公共节点。
   * 这总思路的时间和攻坚效率如何？
   * 为了得到从根节点到输入的连个节点的两条路径，需要遍历两次树。每遍历一次的时间复杂度是 O(n)。得到两条路径的长度的最差情况是 O(n) 此时二叉树
   * 退化为链表，而通常情况下两条路径的长度为 O(logn)
   */

  @AllArgsConstructor
  @Data
  private static class TreeNode {
    public String value;
    public List<TreeNode> nodeList;
  }

  /**
   * 根据输入节点获取从根节点到输入节点的路径
   *
   * @param inputNode 输入节点
   * @param rootNode  根节点或者子节点
   * @param nodeList  辅助链表
   * @return 是否找到 input 节点
   */
  static boolean getNodePathByInputNode(TreeNode inputNode, TreeNode rootNode, Deque<TreeNode> nodeList) {
    // 加入路径的是 rootNode ，如果写成 inputNode ，就打错特错了
    nodeList.addLast(rootNode);
    if (inputNode == rootNode) {
      return true;
    }
    boolean matched = false;
    if (rootNode.nodeList != null) {
      for (TreeNode treeNode : rootNode.nodeList) {
        boolean foundTargetNode = getNodePathByInputNode(inputNode, treeNode, nodeList);
        if (!foundTargetNode) { // 不符合路径的节点进行弹出
          nodeList.pollLast();
        } else {
          matched = true;
        }
      }
    }
    return matched;
  }

  static TreeNode getFirstShareNode(TreeNode inputNode1, TreeNode inputNode2, TreeNode rootNode) {
    if (inputNode1 == null || inputNode2 == null || rootNode == null) {
      return null;
    }
    LinkedList<TreeNode> inputNodeList1 = new LinkedList<>();
    LinkedList<TreeNode> inputNodeList2 = new LinkedList<>();
    getNodePathByInputNode(inputNode1, rootNode, inputNodeList1);
    getNodePathByInputNode(inputNode2, rootNode, inputNodeList2);

    if (inputNodeList1.isEmpty() || inputNodeList2.isEmpty()) {
      return null;
    }

    int size = Math.min(inputNodeList1.size(), inputNodeList2.size());
    TreeNode shareNode1 = null, shareNode2;
    boolean found = false;
    for (int i = 0; i < size; i++) {
      shareNode1 = inputNodeList1.get(i);
      shareNode2 = inputNodeList2.get(i);
      if (shareNode1 == shareNode2 &&
          ((i == size - 1)
              || (inputNodeList1.get(i + 1) == null && inputNodeList2.get(i + 1) == null)
              || inputNodeList1.get(i + 1) != inputNodeList2.get(i + 1))) {
        found = true;
        break;
      }
    }
    if (found) {
      return shareNode1;
    }
    return null;
  }

  public static void main(String[] args) {
    TreeNode a = new TreeNode("A", null);
    TreeNode b = new TreeNode("B", null);
    TreeNode c = new TreeNode("C", null);
    a.nodeList = Arrays.asList(b, c);
    TreeNode d = new TreeNode("D", null);
    TreeNode e = new TreeNode("E", null);
    b.nodeList = Arrays.asList(d, e);
    TreeNode f = new TreeNode("F", null);
    TreeNode g = new TreeNode("G", null);
    d.nodeList = Arrays.asList(f, g);

    TreeNode h = new TreeNode("H", null);
    TreeNode i = new TreeNode("I", null);
    TreeNode j = new TreeNode("G", null);
    e.nodeList = Arrays.asList(h, i, j);

    TreeNode firstShareNode = getFirstShareNode(h, f, a);
    System.out.println(firstShareNode == null ? "null" : firstShareNode.value);
  }


}
