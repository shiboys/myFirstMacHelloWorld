package com.swj.ics.dataStructure.linkedlist;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/01/09 20:26
 */
@AllArgsConstructor
@NoArgsConstructor
public class ComplexLinkNode<T extends Comparable<T>> {
    public T value;
    public ComplexLinkNode<T> next;
    public ComplexLinkNode<T> sibling;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (value != null) {
            sb.append(String.format("value=%s, ", value));
        }
        if (sibling != null) {
            sb.append(String.format("sibling value = %s, ", sibling.value));
        }
        return sb.length() > 1 ? sb.substring(0, sb.length() - 2) : "null";
    }

    static String getSiblingStringValue(ComplexLinkNode sibling) {
        return sibling != null ? sibling.value.toString() : "null";
    }

    public static void printThisList(ComplexLinkNode head) {
        if (head != null) {
            ComplexLinkNode pNode = head;
            while (pNode != null) {
                System.out.print(String.format("value=%s,sibling=%s\t", pNode.value, getSiblingStringValue(pNode.sibling)));
                pNode = pNode.next;
            }
            System.out.println();
        } else {
            System.out.println("linked list is null");
        }
    }
}
