package com.swj.ics.dataStructure.linkedlist;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/01/08 16:45
 * 反转链表。
 * 题目：定义一个函数，输入一个链表的头节点，翻转该链表比不过输出翻转链表后的头结点
 * 解决与链表相关的问题总是有大量的指针操作，而指针操作的代码总是容易出错。很多面试官喜欢出与链表相关的问题，就是想通过指针操作来考察应聘者的编码
 * 功底。为了避免出错，我们最好先进行全面分析，在实际的软件开发周期中，设计的时间通常不会比编码的时间短。在面试的时候我们不要着急于写代码，而是
 * 一开始仔细分析和设计，这样才能写出鲁棒性很高的代码，而不是很快写出一段漏洞百出的代码。
 * 为了正确地翻转一个链表，我们需要调整链表中的指针的方向。如图所示
 * .............h->i->j........
 * h,i,j 是链表中的三个节点，通过观察我们发现，由于 i 节点的 next 指针指向 j，在调整之后就指向 h 节点，此时链表在 i 节点出是断开的，为了避免
 * 出现链表断开的情况，我们需要使用一个指针来记录 j 节点
 * 也就是说，畜类需要知道 i 节点本身，还需要知道 i 节点的前一个节点 h，因为我们需要把 i 节点的指针指向 h ，
 * 同时我们还需要事先保存 i 节点之前的 next 节点 j，以防止链表断开。因此我们需要定义 3 个节点指针。
 */
public class ReverseLinkedList {
    static LinkNode reverseLinkedList(LinkNode head) {
        if (head == null) {
            return null;
        }
        LinkNode pNode = head;
        LinkNode prevNode = null;
        LinkNode nextNode;
        LinkNode reversedHead = null;
        while (pNode != null) {
            nextNode = pNode.next;
            pNode.next = prevNode;
            prevNode = pNode;
            if (nextNode == null) {
                reversedHead = pNode;
            }
            pNode = nextNode;
        }
        return reversedHead;
    }

    /**
     * 使用递归的方式来翻转链表
     * @param head 链表头部
     * @return 翻转之后的链表徒步
     */
    static LinkNode reverseLinkedListRecursively(LinkNode head) {
        if (head == null) {
            return null;
        }
        LinkNode pNode = head;
        LinkNode nextNode = pNode.next;
        LinkNode reversedHead = null;
        if (nextNode != null && nextNode.next != null) {
            reversedHead = reverseLinkedListRecursively(nextNode);
        } else {
            reversedHead = pNode.next;
        }
        nextNode.next = pNode;
        pNode.next = null;
        return reversedHead;
    }

    //static LinkNode reversedHead;

    public static void main(String[] args) {
        int[] arr = {1, 2, 3, 4, 5, 6};
        LinkNode head = LinkNode.getLinkedListByArray(arr);
        LinkNode.printThisList(head);
        /*LinkNode reverseHead = reverseLinkedList(head);
        LinkNode.printThisList(reverseHead);*/
        LinkNode reversedHead = reverseLinkedListRecursively(head);
        LinkNode.printThisList(reversedHead);
    }
}
