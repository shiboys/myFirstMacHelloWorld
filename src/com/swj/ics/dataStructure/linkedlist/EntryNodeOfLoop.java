package com.swj.ics.dataStructure.linkedlist;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/01/08 15:30
 * 链表中的环形部分的入口节点
 * 题目：如果一个链表中包含环，如何找出环的入口点？如下图所示，环的入口点为3
 * __________
 * |         |
 * \|/        |
 * 1->2->3->4->5->6
 * <p>
 * 解决这个问题的第一步是如何确定一个链表中包含环。根据《找到倒数第 k 个节点》的启发， 我们可以使用两个指针来解决这个问题。和前面一样，定义两个
 * 指针，同时从链表的头结点出发，一个指针一次走一步，另一个指针一次走两步。如果走的块的指针追上了走得慢的指针，那么链表就包含环；如果走的块的指针
 * 走到了链表的末尾（next 指针指向 null）都没有追上第一个指针，那么链表就不包含环。
 * 第二步就是如何找到环的入口。我们还是可以用两个指针来解决这个问题。先定义两个指针 p1 和 p2 都指向头结点。如果链表中的环有 n 个节点，则指针 p1
 * 先在链表上向前移动 n 步，然后两个指针以相同的速度向前移动。当第p1指针和 p2 指针相遇时，p2指针此时刚好指向环的入口。
 * 剩下的问题是如何得到环中的节点数目。我们在前面提到判断一个链表中是否有环时用到了快慢两个指针。如果两个指针相遇，则表明链表中存在环。
 * 两个指针相遇的节点一定在环中。可以从这个节点触发，一边走一遍进行计数，当再次回到这个节点时，就可以得到环中的节点数了。
 */
public class EntryNodeOfLoop {

  static LinkNode getMeetingNode(LinkNode head) {
    if (head == null) {
      return null;
    }
    LinkNode pSlow = head.next;
    if (pSlow == null) {
      return null;
    }
    LinkNode pFast = pSlow.next;
    while (pFast != null && pSlow != null) {
      if (pFast == pSlow) {
        return pFast;
      }
      pSlow = pSlow.next;

      pFast = pFast.next;
      if (pFast != null) {
        pFast = pFast.next;
      }
    }
    return null;
  }

  /**
   * 在找到环中任意一个节点之后，就能得出环中的节点数目，并找出环的入口节点。
   */
  static LinkNode findEntryNodeOfLoop(LinkNode head) {
    LinkNode jointNode = getMeetingNode(head);
    // 如果根本没有环形，则返回
    if (jointNode == null) {
      System.out.println("current linked list is not a loop list");
      return null;
    }
    LinkNode pNode = jointNode;
    int count = 1;
    while (pNode.next != jointNode) {
      pNode = pNode.next;
      count++;
    }
    // 此时环形链表的节点数量为 count 个
    // 先让 p1 节点走 count 步
    LinkNode p1Node = head;

    for (int i = 0; i < count; i++) {
      if (p1Node != null) {
        p1Node = p1Node.next;
      } else {
        return null;
      }
    }
    LinkNode p2Node = head;
    while (p1Node != p2Node) {
      p1Node = p1Node.next;
      p2Node = p2Node.next;
    }
    return p2Node;
  }

  public static void main(String[] args) {
    int[] arr = {1, 2, 3, 4, 5, 6};
    int entryNodeVal = 3;
    LinkNode pHead = null;
    LinkNode pNode = null;
    LinkNode entryNode = null;
    for (int i = 0; i < arr.length; i++) {
      if (i == 0) {
        pHead = pNode = new LinkNode(arr[i], null);
      } else {
        LinkNode newNode = new LinkNode(arr[i], null);
        pNode.next = newNode;
        pNode = newNode;
      }
      if (arr[i] == entryNodeVal) {
        entryNode = pNode;
      }
      if (i == arr.length - 1) {
        pNode.next = entryNode;
      }
    }
    LinkNode findEntrNode = findEntryNodeOfLoop(pHead);
    if (findEntrNode != null) {
      System.out.println("find entry node, value is " + findEntrNode.value);
    }
  }
}
