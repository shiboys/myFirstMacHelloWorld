package com.swj.ics.dataStructure.linkedlist;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/01/08 14:38
 * 链表倒数第 k 个节点
 * 题目：输入一个链表，输出该链表中倒数第 k 个节点。
 * 为了符合大多数人的习惯，本体从 1 开始计数，即链表的尾结点是倒数第 1 个节点。例如：一个链表有 6 个节点，从头开始，他们的值一次为1、2、3、4、5
 * 、6.这个链表倒数第 3 个节点是值为 4 的节点。
 * 为了得到倒数第 k 个节点，很自然的想法是先走到链表的尾端，再从尾端回溯 k 步。可是我们从链表的定义可以看出，本题中的链表是单向链表，单向链表的节点
 * 只有从前往后的指针而没有从后往前的指针。因此这种思路行不通
 * 既然不能从尾结点开始遍历整个链表，我们还是把思路回到头结点上来。假设真个链表有 n 个节点，那么倒数第 k 个节点就是从头结点开始的第 n-k+1 个节点
 * 如果我们能够得到链表中节点个数 n，那么只需从头结点开始往后走 n-k+1 步就可以了。如果得到节点n？这个不难，只需要从头开始遍历一遍链表，使用计数器
 * 就可以获取。
 * 也就是说我们需要遍历两边链表，第一遍统计出链表中节点的个数，第二次就能找到倒数第 k 个节点。但是题目的要求显然不满足
 * 那我们就需要找到一种只需遍历一次就能找到倒数第 k 个节点，我们可以定义两个指针，第一个指针从来链表的头指针开始遍历向前走 k-1 步，第二个指针
 * 保持不动；从第 k 步开始，第二个指针也开始从链表的头指针开始遍历。由于两个指针的距离保持在 k-1 。当第一个真真到达链表的尾结点时，第二个指针
 * 正好指向倒数第 k 个节点。
 */
public class KthNodeOfLinkedListDescending {

    static LinkNode findKthNodeFromTail(LinkNode head, int k) {
        /**
         * 该方法的鲁棒性注意点：
         * 1、输入的 head 为空指针，如果不加判断，代码会访问空指针指向的内存区域，会导致程序崩溃
         * 2、输入的以 head 为头结点的链表节点的节点数少于 k。由于在 for 循环中会走 k-1 步，仍然会由于空指针而造成程序崩溃
         * 3、输入的参数 k 为0。如果 k 是个无符号的整数，那么在 for 循环中，k-1得到的不是 -1 而是 OXFFFFFFFF。因此 for 循环的次数可能会远超出我们的想象。
         */
        if (head == null || k < 0) {
            return null;
        }
        LinkNode pNode = head;
        LinkNode p2Node;
        for (int i = 0; i < k - 1; i++) {
            if (pNode.next == null) {
                return null;
            }
            pNode = pNode.next;
        }

        p2Node = head;
        while (pNode.next != null) { // 这里一定要注意，此时 pNode 和 p2Node 已经开始步调一致，应该是一直走到链表的最后一个节点，
            // 此时 p2Node 刚好是 倒数第 k个元素
            pNode = pNode.next;
            p2Node = p2Node.next;
        }
        return p2Node;
    }

    public static void main(String[] args) {
        int[] arr = {1, 2, 3, 4, 5, 6};
        LinkNode headNode = LinkNode.getLinkedListByArray(arr);
        int k = 3;
        LinkNode targetNode = findKthNodeFromTail(headNode, k);
        if (targetNode != null) {
            System.out.println(targetNode);
        } else {
            System.out.println(String.format("can not find the target k node. [k=%s]", k));
        }
    }
}
