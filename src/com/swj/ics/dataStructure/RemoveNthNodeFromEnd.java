package com.swj.ics.dataStructure;

/**
 * Created by swj on 2020/2/23.
 * 给定一个单链表，删除链表的倒数第N个节点，并且返回链表的头结点。要求只允许对链表进行一次遍历。
 */

/**
 * 示例：给定一个链表1->2->3->4->5，和n=2，当删除了倒数第2个元素之后，链表变为1->2->3->5，给定的n保证是有效的
 */

/**
 * 解决思路：
 * 我们可以使用2个指针而不是一个指针。第一个指针从列表的开头向前移动n+1步，而第二个指针从列表的开头出发。
 * 现在，这2个指针中间被n个节点分开。我们通过同时移动这2个指针来保持这个固定的间隔，知道第一个指针到达最后一个节点。
 * 此时第二个指针将指向倒数第n个节点的前一个节点。我们只需要重新连接第二个指针的next到该指针的下下个节点即可。
 */
public class RemoveNthNodeFromEnd {

    public Node removeNthNodeFromEnd(Node head,int n) {
        if(head == null) {
            return null;
        }
        Node dummy = new Node(0);
        dummy.next = head;
        Node first = dummy;
        Node second = dummy;
        for(int i=1;i<=n+1;i++) {
            first = first.next;
        }
        //开始移动第一个指针和第二个指针。
        while(first != null) {
            first = first.next;
            second = second.next;
        }
        //次数。second就是倒数第n+1个节点
        second.next=second.next.next;
        return dummy.next;
    }

    /**
     * 时间复杂度为O(n)
     * 空间复杂度，O(1),只是用了常量基本的额外空间
     */

    public static class Node {
        private int data;
        private Node next;

        public Node(int data) {
            this.data = data;
        }
    }

    public static void main(String[] args) {
        Node currentNode = new Node(0);
        Node headNode = currentNode;
        for (int i = 1; i <= 10; i++) {
            Node node = new Node(i);
            currentNode.next = node;
            currentNode = node;
        }
        RemoveNthNodeFromEnd instance = new RemoveNthNodeFromEnd();
        headNode = instance.removeNthNodeFromEnd(headNode,2);
        currentNode = headNode;
        while (currentNode.next != null) {
            System.out.print(currentNode.next.data+"\t,");
            currentNode = currentNode.next;
        }
        System.out.println("\n done !");
        
    }
}
