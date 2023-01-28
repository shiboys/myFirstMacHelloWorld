package com.swj.ics.dataStructure.stack;

import java.util.Stack;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/01/28 13:40
 * 用 2 个栈实现一个队列
 * 题目：用两个栈实现一个队列。咧咧的声明如下：请实现它的两个函数 appendTail 和 deleteHead，分别完成在队列尾部插入节点和在队列头部删除节点
 * 的功能
 */
public class QueueImplWith2Stack {
  private Stack<Integer> stack1 = new Stack<>();
  private Stack<Integer> stack2 = new Stack<>();

  public void appendTail(int val) {
    stack1.push(val);
  }

  public int removeHead() {
    while (!stack1.isEmpty()) {
      stack2.push(stack1.pop());
    }
    if (stack2.isEmpty()) {
      throw new IllegalArgumentException("queue is empty");
    }
    return stack2.pop();
  }

  public boolean isEmpty() {
    return stack1.isEmpty() && stack2.isEmpty();
  }

  public static void main(String[] args) {
    QueueImplWith2Stack instance = new QueueImplWith2Stack();
    int[] arr = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
    for(int val : arr) {
      instance.appendTail(val);
    }
    while (!instance.isEmpty()) {
      System.out.print(instance.removeHead() + "\t");
    }
    System.out.println();
  }
}
