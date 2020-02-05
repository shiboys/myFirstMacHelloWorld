package com.swj.ics.dataStructure;

/**
 * @author shiweijie
 * date 2020/2/3 下午6:12
 */

/**
 * 单链表的反转。
 */
public class ReversedSingleList {

    public static void reverse(Node head) {
        if (head == null || null == head.next) {
            System.out.println("single list is empty");
            return;
        }

        //先声明3个节点，前中后
        Node pre = null;
        Node currNode = head.next;
        Node next = null;

        while (currNode != null) {
            if (currNode.next == null) {//跑到最后一个节点了
                currNode.next = pre;
                break;
            }
            next = currNode.next;
            //把当前节点的指针反方向指向前一个节点。
            currNode.next = pre;
            //移动上一个节点到当前节点
            pre = currNode;
            //移动当前指针到下一个节点
            currNode = next;
        }

        head.next = currNode;

        Node temp = head.next;
        while (temp != null) {
            System.out.print(temp.data + "\t");
            temp = temp.next;
        }

    }

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
        for (int i = 0; i < 10; i++) {
            Node node = new Node(i + 1);
            currentNode.next = node;
            currentNode = node;
        }

        System.out.println("before reverse");
        Node temp = headNode.next;
        while (temp != null) {
            System.out.print(temp.data + "\t");
            temp = temp.next;
        }
        System.out.println("\n after reverse");
        reverse(headNode);

    }
}
