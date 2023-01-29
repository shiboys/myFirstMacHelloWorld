package com.swj.ics.dataStructure.strings;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/01/29 15:19
 * 数字翻译成字符串。
 * 题目：给定一个数字，我们按照如下的规则给它翻译成字符串：0翻译成 a，1翻译成b。。。。11 翻译成 l，25 翻译成 z。
 * 一个数字可能有多重翻译。例如，12258 有 5 中不同的翻译，分别是 bccti, bwfi,bczi, mcfi 和 mzi。请编写一个函数，用来计算一个数字有多少种
 * 不同的翻译方法。
 */
public class TranslateNumberToString {
  /**
   * 分析：
   * 我们以 12258 为例分析如何从数字的第一位开始一步步计算不同的翻译方法的数目。我们有两种不同的选择来翻译第一个数字 1.第一种选择是数字 1 单独
   * 翻译为 b，后面剩下数字 2258; 第二种选择是 1 和紧挨着的 2 一起翻译成 m，后面剩下的数字 258。
   * 当最开始的一个或者连个数字被翻译成一个字符之后，我们接着翻译后面剩下的数字。显然，我们可以写一个递归函数来计算翻译的条目。
   * 我们定义函数 f(i) 表示从第 i 为数字开始的不同翻译数目，那么 f(i)=f(i+1)+g(i,i+1)*f(i+2)。当第 i 位和第 i+1 位两位数字拼接起来的数字
   * 在 10-25范围内时，函数 g(i,i+1)=1 ,否则为 0。
   * <p>
   * 尽管我们可以采用递归的思想来分析这个问题，但是由于存在重复的子问题，递归并不是解决这个问题的最佳方法。还是以 12258 为例。翻译 12258 可以
   * 分解成两个子问题，翻译 1 和 2258 ，以及翻译 12 和 258.接下来我们翻译第一个子问题中剩下的 2258，同样也可以分解成两个子问题：翻译2 和翻译
   * 258。以及翻译 22 和 58。注意到子问题翻译 258 重复出现了。
   * <p>
   * 递归最大的问题开始自上而下解决问题（从高位到低位）。我们也可以以最小的子问题开始自下而上解决问题，这样就可以消除重复的子问题。也就是说，我们从
   * 数字的末尾开始，然后从右到左比不过计算不同的翻译数目。
   */

  private static int getTranslatedStringCountByNumber(String number) {
    int length = number.length();
    int[] arr = new int[length];

    int count;
    // 从后往前开始递推
    for (int i = length - 1; i >= 0; i--) {
      if (i == length - 1) {
        count = 1;
        arr[i] = count;
        continue;
      }
      // 先计算出 f(i+1）
      count = arr[i + 1];
      int firstNumber = number.charAt(i) - '0';
      int secondNumber = number.charAt(i + 1) - '0';
      int translated = firstNumber * 10 + secondNumber;
      if (translated >= 10 && translated <= 25) {
        // f(i)=f(i+1)+g(i,i+1)*f(i+2) 不过要判断边界问题
        if (i < length - 2) {
          count += arr[i + 2];
        } else {
          count += 1;
        }
      }
      arr[i] = count;
    }
    System.out.println(Arrays.toString(arr));

    return arr[0];
  }

  public static int getTranslatedStringCountByNumber(int number) {
    return getTranslatedStringCountByNumber(Integer.toString(number));
  }

  public static void main(String[] args) {
    System.out.println(getTranslatedStringCountByNumber(12258));
    System.out.println(getTranslatedStringCountByNumber(12758));
    System.out.println(getTranslatedStringCountByNumber(32258));
  }
}
