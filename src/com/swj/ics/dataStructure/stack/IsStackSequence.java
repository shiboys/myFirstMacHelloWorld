package com.swj.ics.dataStructure.stack;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/01/28 14:55
 * 是否是栈的压入弹出序列。
 * 题目：输入两个整数序列，第一个序列表示栈的压入顺序，请判断第二个序列是否为该栈的弹出顺序。假设压入的所有数字均不相等。例如，序列{1,2,3,4,5}
 * 是某栈的压栈序列，序列{4,5,3,2,1} 是该栈的一个弹出序列，但是 {4,3,5,1,2} 就不是该压栈的弹出序列。
 */
public class IsStackSequence {
  /**
   * 通过分析题目需求和用例分析，我们总结规律如下：
   * 如果下一个要弹出的元素，刚好是弹出序列的栈顶元素，则直接弹出；如果下一个要弹出的元素不在栈顶，则把压栈序列中尚未入栈的数字压入辅助栈，直到把
   * 下一个需要弹出的数字压入栈顶为止；如果所有的数字都压入栈后仍然没有找到下一个弹出的元素，那么该序列就可能不是一个弹出序列。
   */
  private static boolean isStackPopSequence(List<Integer> pushSeq, List<Integer> popSeq) {
    if (pushSeq == null || pushSeq.isEmpty()) {
      return false;
    }
    if (popSeq == null || popSeq.isEmpty()) {
      return false;
    }
    int pushIndex = 0;
    int popIndex = 0;
    Stack<Integer> stack = new Stack<>();
    while (pushIndex < pushSeq.size()) {
      stack.push(pushSeq.get(pushIndex));
      pushIndex++;
      while (popIndex < popSeq.size() && !stack.isEmpty() && stack.peek().equals(popSeq.get(popIndex))) {
        popIndex++;
        stack.pop();
      }
    }

    while (!stack.isEmpty() && popIndex < popSeq.size()) {
      if (!stack.peek().equals(pushSeq.get(popIndex))) {
        return false;
      }
      popIndex++;
      stack.pop();
    }

    return stack.isEmpty() && popIndex == popSeq.size();
  }

  public static void main(String[] args) {
    List<Integer> stackList = Arrays.asList(1, 2, 3, 4, 5);
    List<Integer> popList = Arrays.asList(4, 5, 3, 2, 1);
    List<Integer> popList2 = Arrays.asList(4, 3, 5, 1, 2);
    List<Integer> popList3 = Arrays.asList(4, 3, 5, 2, 1);
    System.out.println(isStackPopSequence(stackList, popList));//true
    System.out.println(isStackPopSequence(stackList, popList2));//false
    System.out.println(isStackPopSequence(stackList, popList3));//true
  }
}
