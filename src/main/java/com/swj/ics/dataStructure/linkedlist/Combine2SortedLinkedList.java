package com.swj.ics.dataStructure.linkedlist;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/01/08 22:06
 * 合并两个排序的链表
 * 输入两个递增排序的链表，合并这两个链表并使新链表中的节点仍然是递增排序的。如下图所示
 * 链表1 和链表2 经过合并后升序链表如链表 3 所示。
 * 链表1：1->3->5->7
 * 链表2: 2->4->6->8
 * 链表3：1->2->3->4->5->6->7->8
 * <p>
 * 这是一道经常被各个公司采用的面试题。在面试过程中，我们发现应聘者最容易犯两个错误
 * 一、写代码前没有想清楚合并的过程，最终合并出来的链表要么中间断开了，要嘛没有做到递增排序
 * 二、代码在鲁棒性方面存在问题，程序一旦有了特殊的输入（如空链表）就会崩溃
 * 接下来我们进行分析：
 * 链表1 的头结点的值小于链表 2 的头结点，因此链表 1 的头结点将是合并后的链表的头结点
 * 我们继续合并两个链表剩下的节点。此时链表 2 的头结点的值小于链表 1 ，因此链表2 的头结点将会是合并的链表的下一个节点
 */
public class Combine2SortedLinkedList {
  static LinkNode getMerged2SortedLinkedList(LinkNode p1Head, LinkNode p2Head) {
    if (p1Head == null) {
      return p2Head;
    }
    if (p2Head == null) {
      return p1Head;
    }
    LinkNode mergedHead;
    if (p1Head.value < p2Head.value) {
      mergedHead = p1Head;
      mergedHead.next = getMerged2SortedLinkedList(p1Head.next, p2Head);
    } else {
      mergedHead = p2Head;
      mergedHead.next = getMerged2SortedLinkedList(p1Head, p2Head.next);
    }
    return mergedHead;
  }

  public static void main(String[] args) {
    int[] arr1 = {1, 3, 5, 7};
    int[] arr2 = {2, 4, 6, 8};

    LinkNode pHead1 = LinkNode.getLinkedListByArray(arr1);
    LinkNode pHead2 = LinkNode.getLinkedListByArray(arr2);
    LinkNode mergedHead2 = getMerged2SortedLinkedList(pHead1, pHead2);
    if (mergedHead2 != null) {
      LinkNode.printThisList(mergedHead2);
    } else {
      System.out.println("merged head is null");
    }
  }
}
