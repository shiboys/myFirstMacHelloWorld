package com.swj.ics.dataStructure.linkedlist;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/01/09 11:02
 * 输入两个链表，找出他们的第一个公共节点。
 */
public class FirstJointNodeOf2List {
  /**
   * 面试的时候，很多应聘者的第一反应就是蛮力法。在第一个链表上顺序遍历每个节点，每遍历一个节点， 就在第二个链表上顺序遍历每个节点。如果在第2个
   * 链表有一个节点和第一个节点相等，则说明两个链表在这个节点上重合，于是就找到了他们的公共节点。如果第一个链表长度为m，第二个长度为 n
   * 那么显然时间复杂度是 O(m*n）
   * 通常蛮力法不是最好的办法，我们接下来试着分析有公共节点的两个链表有哪些特点。从链表节点的定义可以看出，这两个链表都是单链表，
   * 如果两个单链表指向同一个节点，但由于单链表的特点，每个节点只有一个 next ，因此从第一个公共节点开始，之后他们所有的节点都是重合的，不可能
   * 出现分叉。因此其拓扑形状看起来像一个 Y，而不是像 X。 如下图所示
   * 1->2->3
   * \
   * 6->7
   * /
   * 4->5
   * <p>
   * 经过上面的分析，我们发现如果两个链表有公共节点，那么公共节点出现在两个链表的尾部。如果我们从两个链表的尾部开始比较，那么最后一个相同的节点
   * 就是我们要找的节点。可问题是，在单链表中，我们只能从头结点开始按顺序遍历，最后才能到达尾结点，最后到达的尾结点腰线比较，这听起来又是"后进先出"
   * 于是我们想起来要使用栈来解决这个问题：先把两个链表的节点放入两个栈中，这样两个链表的尾结点就位于两个栈的栈顶，接下来就是比较两个栈顶的几点是否相同
   * 如果相同，就把栈顶元素弹出接着比较下一个元素。知道找到最后一个相同的节点。
   * <p>
   * 在上述思路中，我们需要用到 2 个辅助栈，如果链表长度分别是 m 和 n，那么空间复杂度就是 O(m+n)，时间复杂度也是O(m+n),和蛮力法相比，
   * 时间效率提高了，相当于空间换时间。
   * 但是解决这个问题，还有另外一办法：
   * 首先遍历两个链表，得到他们的链表长度，就能知道哪个链表比较长，以及长链表比锻炼表长多少个节点。在第二次遍历的时候，在较长的链表上走若干步，
   * 接着同时在两个链表上进行遍历，找到第一个相同的节点返回即可
   */

  static LinkNode findTheJointNode(LinkNode head1, LinkNode head2) {
    if (head1 == null || head2 == null) {
      return null;
    }
    LinkNode longerLink = head1;
    LinkNode shorterLink = head2;
    int listLength1 = getLinkListLength(head1);
    int listLength2 = getLinkListLength(head2);
    int nodeDiffCount = listLength1 - listLength2;
    if (listLength1 < listLength2) {
      longerLink = head2;
      shorterLink = head1;
      nodeDiffCount = listLength2 - listLength1;
    }

    LinkNode pNode = longerLink;
    LinkNode sNode = shorterLink;
    for (int i = 0; i < nodeDiffCount; i++) {
      pNode = pNode.next;
    }

    while (pNode != null && sNode != null && pNode.value != sNode.value) {
      pNode = pNode.next;
      sNode = sNode.next;
    }
    if (pNode == null || sNode == null) {
      return null;
    }

    return pNode;
  }

  static int getLinkListLength(LinkNode head) {
    int count = 0;
    LinkNode node = head;
    while (node != null) {
      count++;
      node = node.next;
    }
    return count;
  }

  public static void main(String[] args) {
    int[] arr1 = {1, 2, 3};
    int[] arr2 = {4, 5};
    LinkNode tailNode = new LinkNode(7, null);
    LinkNode joinNode = new LinkNode(6, tailNode);
    LinkNode head1 = LinkNode.getLinkedListByArray(arr1);
    LinkNode head2 = LinkNode.getLinkedListByArray(arr2);
    LinkNode p1 = head1;
    LinkNode p2 = head2;
    while (true) {
      if (p1.next == null) {
        p1.next = joinNode;
        break;
      }
      p1 = p1.next;
    }
    while (true) {
      if (p2.next == null) {
        p2.next = joinNode;
        break;
      }
      p2 = p2.next;
    }

    LinkNode jointNode = findTheJointNode(head1, head2);
    if (joinNode != null) {
      System.out.println("jointNode is " + joinNode.value);
    } else {
      System.out.println("joint node is null");
    }
  }
}
