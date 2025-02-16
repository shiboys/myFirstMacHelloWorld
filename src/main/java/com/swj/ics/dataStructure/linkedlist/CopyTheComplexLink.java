package com.swj.ics.dataStructure.linkedlist;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/01/09 18:21
 * 复杂链表的复制
 * 题目：请实现函数 ComplexListNodeClone(LinkNode head) 复制一个复杂链表。
 * 在复杂链表中，每个节点除了有一个 next 指针，还有一个指向链表中任意节点的 sibling 指针。
 */
public class CopyTheComplexLink {
  /**
   * 听到这个题之后，很多应聘者的第一反应是先把复制过程分成两步：
   * 第一步：复制原始链表上的每个节点，并用 next 连接起来。
   * 第二步是，复制每个节点上的 sibling 指针。
   * 假设原始链表上的某个节点 n 指向节点 S，由于 S 节点可能在 N 的前面也可能在 N 的后面，所以要定位 S 的位置需要从原始链表的头结点位置开始
   * 找。如果从头结点开始沿着 next 经过 s 步找到 S 节点。那么在复制链表上节点 N' 的 sibling 节点 S' 也需要沿着头结经过 s 步。用这种办法
   * 可以赋值链表上的每个节点的 sibling 指针。
   * 对于一个包含 n 个节点的链表，由于定位每个节点的 sibling 都需要从链表头结点开始结果O(n)步才能找到，因此这种方法的总的时间复杂度为O(N^2)
   * 由于上述方法的时间主要花在定位节点的 sibling 上面，我们试着在这方面进行优化，。我们还是分为两步，第一步仍然是复制原始链表上的每个节点N
   * 为 N'，然后把创建出来的 N' 节点使用 next 连接起来，同时我们把 <N,N'> 的配对信息放入一个哈希表中；第二步还是设置复制链表上每个节点的
   * sibling 。如果在原始链表上的节点N的 sibling 指向节点 S，那么在复制链表中，对应的 N' 应该执行 S'。由于有了哈希表，我们可以使用 O(1)
   * 的时间根据 S 找到 S'
   * 第二种方法相当于空间换时间。对于有 n 个节点的链表，我们需要一个大小为 O(n) 的哈希表，也就是说我们以 O(n) 的空间消耗把时间复杂度从
   * O(N^2) 降低到 O(n)
   * 接下来我们再换一种思路，在不用辅助空间的情况下实现O(n)的时间效率。第三种方法的第一步仍然是根据就原始链表中的每个节点 N 创建 N'，这一次
   * 我们把 N' 连接在 N 的后面。链表经过这一步之后的结构如图 复杂链表的复制.png 所示
   * 第二步设置复制出来的节点的 sibling。假设原始链表上 N 的 sibling 指向 S，那么对应的 N' 的 sibling 是 S'，同样 S' 是 S 的 next 指针
   * 指向。
   * 第三步：把这个长链表拆分成 2 个链表：把奇数位置的节点用 next 链起来就是原始链表，把偶数位置用 next 链起来就是复制出来的链表。
   */

  public static void main(String[] args) {
    ComplexLinkNode<String> e = new ComplexLinkNode<String>("E", null, null);
    ComplexLinkNode<String> d = new ComplexLinkNode<String>("D", e, null);
    ComplexLinkNode<String> c = new ComplexLinkNode<String>("C", d, null);
    ComplexLinkNode<String> b = new ComplexLinkNode<String>("B", c, null);
    ComplexLinkNode<String> a = new ComplexLinkNode<String>("A", b, null);
    a.sibling = c;
    b.sibling = e;
    d.sibling = b;
    ComplexLinkNode.printThisList(a);

    ComplexLinkNode<String> cloneHead = cloneComplexLinkList(a);
    if (cloneHead != null) {
      ComplexLinkNode.printThisList(cloneHead);
    } else {
      System.out.println("cloned linked list is null");
    }
  }

  static ComplexLinkNode<String> cloneComplexLinkList(ComplexLinkNode<String> head) {
    if (head == null) {
      return null;
    }
    initCloneNodeList(head);
    initSiblingOfClonedNode(head);
    return splitTheCompositeLinkedList(head);
  }

  static void initCloneNodeList(ComplexLinkNode<String> head) {
    if (head == null) {
      return;
    }
    ComplexLinkNode<String> pNode = head;
    while (pNode != null) {
      ComplexLinkNode<String> nextNode = pNode.next;
      pNode.next = new ComplexLinkNode<>(pNode.value, nextNode, null);
      pNode = nextNode;
    }
  }

  static void initSiblingOfClonedNode(ComplexLinkNode<String> head) {
    if (head == null) {
      return;
    }
    ComplexLinkNode<String> pNode = head;
    while (pNode != null && pNode.next != null) {
      ComplexLinkNode<String> cloneNode = pNode.next;
      if (pNode.sibling == null) {
        pNode = cloneNode.next;
        continue;
      }
      ComplexLinkNode<String> siblingNode = pNode.sibling;
      cloneNode.sibling = siblingNode.next;
      pNode = cloneNode.next;
    }
  }

  static ComplexLinkNode<String> splitTheCompositeLinkedList(ComplexLinkNode<String> head) {
    ComplexLinkNode<String> pNode = head;
    ComplexLinkNode<String> cloneHead = pNode.next;
    ComplexLinkNode<String> cloneNode = cloneHead;
    while (pNode != null && cloneNode != null) {
      pNode.next = cloneNode.next;
      pNode = pNode.next;
      if (pNode != null) {
        cloneNode.next = pNode.next;
        cloneNode = cloneNode.next;
      }
    }
    return cloneHead;
  }
}
