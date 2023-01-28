package com.swj.ics.dataStructure.queue;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/01/28 15:42
 * 滑动窗口最大值：使用队列求解
 * 给定一个数组和一个滑动窗口大小，请找出所有滑动窗口里的最大值。
 * 例如，如果输入数组{2,3,4,2,6,2,5,1} 以及滑动窗口的大小3，那么一共存在 6 个滑动窗口，它们的最大值分别为 {4,4,6,6,6,5}
 */
public class MaxValueInWindow {
  /**
   * 题目分析：
   * 如果采用暴力解法, 那么这个问题似乎不难解决；可以扫描每个滑动窗口的所有数字并找出其中的最大值。如果滑动窗口的大小为 k，则需要 O(k) 的时间
   * 才能找到滑动窗口里面的最大值。对于长度为 n 的输入数组，这种算法的总的时间复杂度为 O(nk)
   * 实际上，一个滑动窗口可以看成是一个队列。当窗口滑动时，处于窗口的第一个数字被删除，同时在窗口的末尾添加一个新的数字，这符合队列"先进先出"的特性。
   * 如果能从队列中找到最大数，这个问题也就解决了。
   * 之前我们实现了 使用栈来在 O(1) 时间内来获取最小值，同样可以使用 2 个栈来实现队列，并且存储队列的最小值，只是这样一来，一个面试题编程
   * 3 个面试题了，规定时间内几乎不可能完成
   * 下面我们换一种思路，我们并不把滑动窗口的每个值都存入队列，而是只把有可能成为滑动窗口的最大值存入一个双端的队列 deque。
   * 以数组 {2,3,4,2,6,2,5,1} 来分析：
   * 参考步骤 《maxValueInQueue.jpg》
   * 第 7 个数字是5。此时队列中已经有两个数字 6和2，2小于5，因此2不可能是一个滑动窗口的最大值，可以把它从队列的尾部删除。删除数字 2 之后，
   * 再把数字 5 存入队列。
   * 最后一个数字是 1，把 1 存入队列尾部。注意位于队列头部的 6 是数字的第 5 个元素，此时的滑动窗口已经不包含该数字了，因此需要把数字 6 从队列中
   * 删除。那么如何指定滑动窗口是否包含一个数字？应在在队列列存储数字在数组中的下标，而不是数值。当一个数字的下标与当前昂处理的数字的下标之差大于等于
   * 滑动窗口大小时，这个数字已经从窗口中滑出，可以从队列头部删除.
   * 返回的结果应该是 {4,4,6,6,6,5}
   */

  static List<Integer> getMaxInWindow(List<Integer> numbers, int windowSize) {
    assert numbers.size() >= windowSize && windowSize >= 1;
    Deque<Integer> indexQueue = new ArrayDeque<>();
    // 初始化队列，队列里面存储的是元素的下标
    for (int i = 0; i < windowSize; i++) {
      // 如果当前元素大于队列中的最后的元素，则将队列最后的元素不断地移除，加入当前元素
      while (!indexQueue.isEmpty() && numbers.get(i) > numbers.get(indexQueue.peekLast())) {
        indexQueue.pollLast();
      }
      indexQueue.addLast(i);
    }
    List<Integer> maxInWindow = new ArrayList<>();
    for (int i = windowSize; i < numbers.size(); i++) {
      int maxQueueValue = numbers.get(indexQueue.getFirst());
      maxInWindow.add(maxQueueValue);
      while (!indexQueue.isEmpty() && numbers.get(i) > numbers.get(indexQueue.getLast())) {
        // 将所有小于当前值的元素清除出当前队列
        indexQueue.pollLast();
      }
      // 如果当前的滑动窗口已经不包含 队列的第一个索引，则将第一个元素 pop
      if (!indexQueue.isEmpty() && (i - indexQueue.getFirst()) >= windowSize) {
        indexQueue.pollFirst();
      }
      indexQueue.addLast(i);
    }
    // 最后一次的 addLast 或者 如果 windowSize == numbers.size() ，则也需要再做最后一次队列统计
    int maxQueueValue = numbers.get(indexQueue.getFirst());
    maxInWindow.add(maxQueueValue);
    return maxInWindow;
  }

  public static void main(String[] args) {
    List<Integer> list = Arrays.asList(2,3,4,2,6,2,5,1);
    System.out.println(getMaxInWindow(list,3));
  }
}
