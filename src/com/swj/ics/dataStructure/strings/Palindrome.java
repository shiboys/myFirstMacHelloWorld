package com.swj.ics.dataStructure.strings;

/**
 * @author shiweijie
 * @date 2020/3/8 上午11:27
 * 回文判断。所谓回文，是指正读和反读都是一样的字符串，比如madam,我爱我，上海自来水来自海上
 * 黄河小浪底浪小河黄，前门出租车租出门前，黄山落叶松叶落山黄，山东落花生花落东山
 */
public class Palindrome {
  /**
   * 常用的2中解法：
   * 1、两头往中间扫
   * 2、中间往两头扫。
   */

  /**
   * 这种实现方式直白且效率不错，时间负载度为O(n),空间复杂度为O(1)
   *
   * @param str
   * @return
   */
  public boolean isPalindromeMethod1(String str) {
    if (str == null || str.length() <= 1) {
      return false;
    }
    int start = 0, end = str.length() - 1;
    while (start < end) {
      if (str.charAt(start) != str.charAt(end)) {
        return false;
      }
      start++;
      end--;
    }
    return true;
  }

  /**
   * 方法2,从中间向两边跑。
   *
   * @param input
   * @return
   */
  public boolean isPalindromeMethod2(String input) {
    if (input == null || input.length() <= 1) {
      return false;
    }
    int n = input.length();
    //6:110,
    //长度/2 -1
    int m = Math.max(((n >> 1) - 1), 0);
    int first = m;
    int second = n - 1 - m;
    //上海自来水水来自海上
    while (first >= 0) {
      if (input.charAt(first) != input.charAt(second)) {
        return false;
      }
      first--;
      second++;
    }
    return true;
  }

  /**
   * 判断最长回文字符串。
   *
   * @param input
   * @return
   */
  public int longestPalindrome(final String input) {
    /**
     * 以回文中心为位置，向左向右判断。
     */
    if (input == null || input.length() < 1) {
      return 0;
    }
    int max = 0;
    int tempMax = 0;
    for (int i = 0, len = input.length(); i < len; i++) {
      //获取以i 为中心的回文且长度为奇数的
      for (int j = 0; j <= i && (j + i) < len; j++) {
        if (input.charAt(i - j) == input.charAt(i + j)) {
          tempMax = j * 2 + 1;
        } else {
          break;
        }
      }
      if (max < tempMax) {
        max = tempMax;
      }
      //以i为中心，且回文长度为偶数的
      for (int j = 0; j <= i && (j + i + 1) < len; j++) {
        if (input.charAt(i - j) == input.charAt(i + j + 1)) {
          tempMax = j * 2 + 2;
        } else {
          break;
        }
      }
      if (max < tempMax) {
        max = tempMax;
      }
    }
    return max;
  }
}
