package com.swj.ics.dataStructure.array;

import com.swj.ics.algorithms.sort.quicksort.QuickSort;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/01/29 22:26
 * 扑克牌中的顺子
 * 题目：从扑克牌中随机抽取 5 张牌，判断你是不是一个顺子，即这 5 张牌是不是连续的。2-10本身为数字，A 为 1，J为 11
 * Q 为 12，K 为13，而大小王可以看成是任意数字
 */
public class IsContinuous {
  /**
   * 分析：
   * 我们需要把扑克牌的需求背景抽象成计算机语言。不难想象，我们可以把 5 张牌看成是 5 个数字组成的数组。
   * 大小王是特殊的数字，我们不妨把他们都定义为成 0，这样就能和其他扑克牌分别开来
   * 接下来我们分析下怎么判断 5 个数字是不是连续的的，最直观的办法就是把数组排序。值得注意的是，由于 0 可以被当成任意数字，我们可以用 0 去补满
   * 数组中的空缺。如果排序之后数组是不连续的，即相邻的两个数字之间相隔若干个数，我们可以使用 0 去补缺这两个数字之间的空缺，条件是 0 的数量是足够的
   * 举个栗子，数组排序之后是 {0,1,3,4,5} , 在 1 和 3 之间空缺了个 2，刚好我们有一个 0，也就是我们可以把它当成 2 去填补空缺。
   * 于是我们要做的就是 3 件事：
   * 首先把数组排序
   * 其次统计数组中 0 的个数
   * 最后统计排序后数字之间的空缺数值个数总和，如果空缺的个数总和小于等于 0 的总个数，则表示这个牌是一个顺子
   * 另外还有一种边界情况，就是相邻的连个数字相等，这就是我们打牌常说的对子，那么这 5 张牌就肯定不是顺子了
   */
  private static boolean isContinuous(int[] numbers) {
    if (numbers == null || numbers.length != 5) {
      throw new IllegalArgumentException(" array's size must be 5");
    }
    //1 、先排序
    QuickSort.quickSort(numbers);
    //2、统计数组中 0 的个数
    int countOfZero = 0;
    int length = numbers.length;
    for (int i = 0; i < length; i++) {
      if (numbers[i] == 0) {
        countOfZero++;
      }
    }
    if (countOfZero > 2) {
      throw new IllegalArgumentException("the count zero value in numbers array exceed 2");
    }
    int numberSpan = 0;
    //3、统计间隙情况
    for (int i = countOfZero; i < length-1; i++) {
      // 出现了对子
      if (numbers[i + 1] == numbers[i]) {
        return false;
      }
      numberSpan += (numbers[i+1] - numbers[i] - 1);
    }
    return numberSpan <= countOfZero;
  }

  public static void main(String[] args) {
    int[] arr1 = {3, 1, 2, 5, 4};
    int[] arr2 = {0, 5, 4, 3, 1};
    // 对子
    int[] arr3 = {5, 5, 4, 3, 1};
    // 非顺子
    int[] arr4 = {5, 6, 4, 3, 1};

    System.out.println(isContinuous(arr1));
    System.out.println(isContinuous(arr2));
    System.out.println(isContinuous(arr3));
    System.out.println(isContinuous(arr4));
  }
}
