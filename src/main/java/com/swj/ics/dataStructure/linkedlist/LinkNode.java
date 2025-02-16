package com.swj.ics.dataStructure.linkedlist;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/01/07 14:39
 */
@AllArgsConstructor
@Data
public class LinkNode {
  public int value;
  public LinkNode next;

  public static LinkNode getLinkedListByArray(int[] arr) {
    if (arr == null || arr.length < 1) {
      return null;
    }
    LinkNode pHead = null;
    LinkNode pNode = null;
    for (int i = 0; i < arr.length; i++) {
      if (i == 0) {
        pHead = pNode = new LinkNode(arr[i], null);
      } else {
        LinkNode newNode = new LinkNode(arr[i], null);
        pNode.next = newNode;
        pNode = newNode;
      }
    }
    return pHead;
  }

  public static void printThisList(LinkNode head) {
    if (head != null) {
      LinkNode pNode = head;
      while (pNode != null) {
        System.out.print(pNode.value + "\t");
        pNode = pNode.next;
      }
      System.out.println();
    } else {
      System.out.println("linked list is null");
    }
  }

}
