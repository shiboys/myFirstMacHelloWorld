package com.swj.ics.dataStructure.linkedlist;

import java.util.Stack;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/01/07 14:38
 * 逆序输出链表，也就是从尾到头打印链表。
 * 看到这道题之后，很多人的第一反应是从头到尾输出会比较直接，于是我们很自然的想把链表中的节点的指针反转过来，
 * 改变链表的方向，然后就可以从尾到头输出了。但是该方法改变原来的链表结构，是否允许在打印链表的时候修改链表结构，
 * 这取决于题目的要求。通常来说，一个只读操作，我们不希望打印时修改内容。
 * 接下来我们想到解决这个问题肯定要遍历链表。遍历的顺序是从头到尾，可输出的顺序是从尾到头。也就是说，第一个遍历的节点最后一个输出
 * 这是典型的「后进先出」，我们可以使用栈来实现这种顺序。每经过一个节点时，我们把该节点放到一个栈中。当遍历完整个链表后，再从栈顶开始逐个输出节点
 * 的值，此时节点的顺序已经反转过来了。
 */
public class ReversePrintLinkedList {
    public static void main(String[] args) {
        int[] arr = {1, 2, 3, 4, 5, 6};
        LinkNode pHead = null;
        LinkNode pNode = null, prevNode;
        for (int i = 0; i < arr.length; i++) {
            if (i == 0) {
                pHead = pNode = new LinkNode(arr[i], null);
            } else {
                LinkNode newNode = new LinkNode(arr[i], null);
                pNode.next = newNode;
                pNode = newNode;
            }
        }
        // printListNodeReversingIteratively(pHead);
        printListNodeReversingRecursively(pHead);
    }

    static void printListNodeReversingIteratively(LinkNode head) {
        if (head == null) {
            return;
        }
        LinkNode pNode = head;
        Stack<LinkNode> nodeStack = new Stack<>();
        while (pNode != null) {
            nodeStack.push(pNode);
            pNode = pNode.next;
        }
        while (!nodeStack.isEmpty()) {
            System.out.print(nodeStack.pop().value + "\t");
        }
        System.out.println();
    }

    /**
     * 我们既然想到用栈来实现这个函数，而递归的本质就是一个栈结构，于是很自然的就想到了用递归来实现。要实现反过来输出链表，我们每访问到一个节点
     * 的时候，先递归输出它后面的接地那，在输出该节点自身，这样就链表的输出结果就反过来了
     */

    static void printListNodeReversingRecursively(LinkNode node) {
        if (node == null) {
            return;
        }
        if (node.next != null) {
            printListNodeReversingRecursively(node.next);
        }
        System.out.print(node.value + "\t");
    }
    /**
     * 上面的基于递归的代码看起来简单，但是有一个问题：当链表非常长的时候，就会导致函数调用的层级很深，从而有可能函数调用栈溢出。
     * 显然用栈基于循环实现的代码鲁棒性要好一些。
     */
}
