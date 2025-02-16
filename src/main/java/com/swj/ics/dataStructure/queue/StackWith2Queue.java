package com.swj.ics.dataStructure.queue;


import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/01/28 21:30
 * 两个队列实现一个栈，二刷
 * 使用两个不同的队列来实现。画出图，就会很容易理解。请参考图 《两个队列实现一个栈.png》
 */
public class StackWith2Queue<T> {
  private Queue<T> queue1 = new ArrayDeque<>();
  private Queue<T> queue2 = new ArrayDeque<>();

  public void push(T item) {
    if (item == null) {
      return;
    }
    // 那个队列不空则用那个队列
    if (queue1.isEmpty() && queue2.isEmpty()) {
      queue1.add(item);
    } else if (!queue1.isEmpty()) {
      queue1.add(item);
    } else {
      queue2.add(item);
    }
  }

  public T pop() {

    Queue<T> fullQueue;
    Queue<T> emptyQueue;
    if (queue1.isEmpty()) {
      emptyQueue = queue1;
      fullQueue = queue2;
    } else {
      emptyQueue = queue2;
      fullQueue = queue1;
    }
    while (fullQueue.size() > 1) {
      emptyQueue.add(fullQueue.poll());
    }
    if (fullQueue.isEmpty()) {
      throw new IllegalArgumentException("stack is emtpy");
    }
    return fullQueue.poll();
  }

  public boolean isEmtpy() {
    return queue1.isEmpty() && queue2.isEmpty();
  }



  public static void main(String[] args) {
    StackWith2Queue<Integer> stack = new StackWith2Queue<>();
    for(int i : Arrays.asList(1,2,3,4,5,6,7,8,9,10)) {
      stack.push(i);
    }
    while (!stack.isEmtpy()) {
      System.out.print(stack.pop() + "\t");
    }
    System.out.println();


    StackWith2Queue<Character> stack2 = new StackWith2Queue<>();
    for(char i : Arrays.asList('a','b','c')) {
      stack2.push(i);
    }
    System.out.println(stack2.pop() + "\t");// 弹出 c
    System.out.println(stack2.pop() + "\t");// 弹出b
    stack2.push('d'); // 压入 d
    System.out.println(stack2.pop() + "\t");// 弹出 d
    System.out.println();
  }
}
