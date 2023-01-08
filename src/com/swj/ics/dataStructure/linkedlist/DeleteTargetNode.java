package com.swj.ics.dataStructure.linkedlist;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/01/07 15:36
 * 题目1：在 O(1) 时间内删除链表节点
 * 给定单链表的头指针和一个节点指针，定义一个函数在 O(1) 时间内删除该节点。
 */
public class DeleteTargetNode {
    /**
     * 在单向链表中删除一个节点，常规的做法无疑是从链表的头结点开始。顺序遍历查找要删除的节点，并在链表中删除该节点。
     * 从头到位开始遍历一个链表，用 pNode 记录当前节点，用 prev 节点记录当前节点的前一个节点，如果 pNode 等于目标节点
     * 把 prev 的 next 指针执行 pNode 的下一个节点，并将当前节点删除。这样的思路肯定需要顺序查找了，事件复杂度自然奴就是 O(n)了
     * 那是不是一定要找到被删除节点的前一个节点那？答案是否定的。
     * 我们可以很方便地得到要被删除节点的下一个节点。如果我们把下一个节点的内容复制到要删除节点上覆盖原有的内容，再把下一个节点删除，
     * 那是不是就等于把当前需要的节点删除了？相当于京广线上我们要删除保定车站，O(1) 的时间复杂度，我们可以将高碑店站搬到保定站，再把老的高碑店站
     * 拆除，这样就相当于把保定站删除了。
     * 上述思路还一个问题：如果要删除的节点位于链表的尾部，那么它没有下一个节点，怎么办？我们仍然从链表的头结点开始，顺序遍历得到该节点的前序节点，
     * 并完成删除操作
     * 最后还需要注意，如果链表只有一个节点，而我们又要删除链表的头结点（也是尾结点），那么，此时我们在删除节点之后，还需要把链表的头结点设置为 null
     * 有了上述思路，我们就可以开始动手写代码。
     */

    static void deleteTargetNode(LinkedNode<String> head, LinkedNode<String> targetNode) {
        if (head == null || targetNode == null) {
            throw new IllegalArgumentException("head and target node can not be null");
        }

        // 要删除的节点不是尾结点
        if (targetNode.next != null) {
            LinkedNode<String> nextNode = targetNode.next;
            targetNode.value = nextNode.value;
            targetNode.next = nextNode.next;
        } else if (head == targetNode) { // 要删除的节点为头结点 链表只有一个节点
            head = null;
            targetNode = null;
        } else { // 要删除的节点是尾结点
            LinkNode prevNode = null;
            LinkedNode<String> pNode = head;
            while (pNode != null && pNode.next != targetNode) {
                pNode = pNode.next;
            }
            pNode.next = null;
            pNode = null;
        }
    }

    static void printLinkedList(LinkedNode<String> head) {
        if (head == null) {
            System.out.println("head is null");
        }
        LinkedNode<String> pNode = head;
        while (pNode != null) {
            System.out.print(pNode.value + "\t");
            pNode = pNode.next;
        }
        System.out.println();
    }

    static void printLinkList(LinkNode head) {
        if (head == null) {
            System.out.println("head is null");
        }
        LinkNode pNode = head;
        while (pNode != null) {
            System.out.print(pNode.value + "\t");
            pNode = pNode.next;
        }
        System.out.println();
    }

    public static void main(String[] args) {
        String[] arr = {"许昌", "郑州", "安阳", "邯郸", "石家庄", "保定", "高碑店", "北京"};
        LinkedNode<String> head = null;
        LinkedNode<String> prevNode = null;
        LinkedNode<String> targetNode = null;
        //String targetCity = "保定";
        String targetCity = "北京";
        for (int i = 0; i < arr.length; i++) {
            LinkedNode<String> node = new LinkedNode<>(arr[i], null);
            if (head == null) {
                head = node;
                prevNode = node;
            } else {
                prevNode.next = node;
                prevNode = node;
            }
            if (targetCity.equals(arr[i])) {
                targetNode = node;
            }
        }
        System.out.println("original linked list:");
        printLinkedList(head);
        deleteTargetNode(head, targetNode);
        System.out.println(String.format("after delete target node %s :", targetCity));
        printLinkedList(head);

        int[] intArr = {1, 2, 3, 3, 4, 4, 5};
        LinkNode headNode = null;
        LinkNode prevLinkNode = null;
        for (int i = 0; i < intArr.length; i++) {
            LinkNode node = new LinkNode(intArr[i], null);
            if (headNode == null) {
                headNode = node;
                prevLinkNode = node;
            } else {
                prevLinkNode.next = node;
                prevLinkNode = node;
            }
        }
        System.out.println("before delete duplicate linked list:");
        printLinkList(headNode);
        deleteReplicateLinkNode(headNode);
        System.out.println("after delete duplicate node:");
        printLinkList(headNode);
    }

    /**
     * 我们来分析下时间复杂度：
     * 对于 n-1 个非尾结点而言，我们可通过 O(1) 的时间把下一个节点的内容复制到要被删除的节点，并删除下一个节点；
     * 对于尾结点而言，由于仍然需要顺序查找，时间复杂度是O(n)。因此，总的平均时间复杂度是 ((n-1)*O(1)+O(n))/n = 2*O(1) = O(1)
     * 结果符合 O(1)
     * 总结：
     * 该数据结构题目主要考察一下几点：
     * 1、考察应聘者对链表的编程能力。
     * 2、考察恒品这的创新思维能力。这道题要求应聘者打破常规的思维模式。当我们想删除一个节点时，并不以一定要删除这个节点本身。可以把下一个节点的
     * 内容复制出来来覆盖被删除节点的内容，然后把下一个节点删除。这种思路不是很容易想到的。
     * 3、考察应聘者思维的全面性。即使应聘者想到删除下一个节点的方法，也未必能通过这轮面试。应聘者需要全面考虑被删除的节点位于链表尾部和链表只有
     * 一个节点的情况。
     */
    /**
     * 题目2：删除链表中的重复节点
     * 在一个排序链表中，如果删除重复节点？例如：1->2->3->3->4->4->5，重复节点被删除之后，变成如下所示：
     * 1->2->5
     * 我们从头遍历整个链表。如果当前节点的值与下一个节点的值相同，那么它们就是重复的节点，都可以被删除。我了保证删除之后的链表仍然是相连的，
     * 我们要把当前节点的前一个节点 prevNode 和比当前节点的值大的节点相连。我们要确保prevNode 始终与下一个没有重复的节点连在一起。
     */

    static void deleteReplicateLinkNode(LinkNode head) {
        if (head == null) {
            return;
        }

        LinkNode pNode = head;
        LinkNode preNode = null;
        while (pNode != null) {
            LinkNode pNext = pNode.next;
            boolean duplicated = false;
            if (pNext != null && pNode.value == pNext.value) {
                duplicated = true;
            }
            if (!duplicated) {
                preNode = pNode;
                pNode = pNext;
            } else { // 出现重复元素
                LinkNode pDupNode = pNode;
                while (pDupNode != null && pDupNode.value == pNode.value) {
              /*      设置 pDupNode*/
                    pNext = pDupNode.next;
                    pDupNode = null;
                    pDupNode = pNext;
                }
                if (preNode == null) {// 尚未初始化 preNode，删除的是头结点
                    head = pNext;
                } else {
                    preNode.next = pNext;
                }
                pNode = pNext;
            }
        }
    }
}
