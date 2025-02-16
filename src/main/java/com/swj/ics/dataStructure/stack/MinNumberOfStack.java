package com.swj.ics.dataStructure.stack;

import java.util.Arrays;
import java.util.Stack;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/01/28 14:32
 * 包含 min 函数的栈
 * 题目：定义栈的数据结构，请在类型中实现一个能够得到栈的最小元素的 min 函数，调用 min、push 以及 pop 的时间复杂度都是O(1)
 */
public class MinNumberOfStack<T extends Comparable<T>> {
  private Stack<T> dataStack = new Stack<>();
  private Stack<T> minStack = new Stack<>();

  public void push(T item) {
    if (item == null) {
      return;
    }
    dataStack.push(item);
    if (minStack.isEmpty() || item.compareTo(minStack.peek()) < 0) {
      minStack.push(item);
    } else {
      minStack.push(minStack.peek());
    }
  }

  public T pop() {
    assert !dataStack.isEmpty();
    minStack.pop();
    return dataStack.pop();
  }

  public T min() {
    assert !minStack.isEmpty();
    return minStack.peek();
  }

  public static void main(String[] args) {
    MinNumberOfStack<Integer> stack = new MinNumberOfStack<>();
    for (int val : (Arrays.asList(3, 4, 2, 1))) {
      stack.push(val);
      System.out.println(String.format("min value is %s", stack.min()));
    }
    stack.pop();
    System.out.println(String.format("min value is %s", stack.min()));
    stack.pop();
    System.out.println(String.format("min value is %s", stack.min()));
    stack.push(0);
    System.out.println(String.format("min value is %s", stack.min()));
  }
}
