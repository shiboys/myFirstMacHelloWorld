package com.swj.ics.dataStructure.strings;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/01/29 16:27
 * 最长不含重复字符的子字符串
 * 题目：请从字符串中找个出一个最长的不包含重复字符的子字符串，计算该最长字符串的长度。假设字符串中只包含 'a'~'z' 的字符。例如，在字符串
 * "arabcacfr" 中，最长的不包含重复的字符串是 "acfr" 和 "rabc" ，他们的长度相等，都等于 4
 */
public class LongestSubstringWithoutDuplication {
  /**
   * 题目分析：
   * 我们不难找出字符串中所有的子字符串，然后就可以判断每个字符串中是否包含重复的字符。这种蛮力法唯一的缺点是效率。一个长度为 n 的字符串包含有
   * O(n^2) 个子字符串，我们需要 O(n) 的时间判断一个字符串中是否包含重复的字符，因此该解法的总的时间复杂度为O(n^3)。
   * 接下来我们使用动态规划法来提高效率，首先定义函数 f(i) 表示以第 i 个字符结尾的不包含重复字符的最长长度。我们从左到右逐一扫描字符串中的每个字符。
   * 当我们计算以第 i 个字符为结尾的不包含重复字符的字符串的最长长度 f(i) 时，我们已经知道了 f(i-1) 了。
   * 如果第 i 个字符之前没有出现过，那么 f(i)=f(i-1)+1。例如 f(0)=1,f(1)=f(0)+1=2
   * 如果第 i 个字符之前已经出现过，那么情况就有些复杂，需要分情况来讨论。
   * 我们将第 i 个字符和上次出现它的位置之间的举例，记为 d，接着分两种情况进行分析：
   * 1、d 小于等于 f(i-1) ，这意味着第 i 个字符出现在 f(i-1) 对应的最长的无重复的子字符串中，因此 f(i)=d。比如 "ara",index =2 的a 的位置的
   * 最大长度 f(2)=2
   * 2、第二种情况是 d 大于 f(i-1) ，此时第 i 个字符出现在 f(i-1）对应的最长子字符串之前，因此仍有 f(i)=f(i-1)+1。
   * 比如 最后一个字母 r 结构的最长不含重复字符的子字符串，即求 f(8)。以它之前的一个字符 f 结尾的最长不含重复字符的子字符串为 acf。因此
   * f(7)=3。我们注意到最后一个字符 r 在之前的子字符 "arabcacfr" 中出现过，这说明上一个字符 r 不在 f(7) 对应的最长不包含重复字符的子字符串
   * 中，上一次出现的索引下标为1，因此两次出现的距离为 d 等于 7，大于 f(7)。此时把 r 拼接到 acf 之后不会出现重复字符。因此
   * f(8)=f(7)+1, 即 f(8)=4
   */
  static int getLongestSubstringWithoutDup(String inputStr) {
    if (inputStr == null || inputStr.isEmpty()) {
      return -1;
    }
    // 为了记录inputString 各个字符的位置，并且简化到数组这里增加一个辅助数组，数组下标为 0-25 代表 a-z 的 26 个字符，数组的值为各个字符的
    // 位置，这里允许替换，因为用到数组的地方是当前字符的上一个位置。这里完美的使用一维数组实现了 hash 的功能：记录各个字母的位置顺序，并且 O(1) 的
    // 时间找到每个字母的位置。也是用空间换时间，
    int[] positionArr = new int[26];
    char[] inputCharArr = inputStr.toCharArray();
    int currLength = 0;
    int maxLength = 0;
    for (int i = 0, len = positionArr.length; i < len; i++) {
      positionArr[i] = -1;
    }

    for (int i = 0; i < inputCharArr.length; i++) {
      int currCharPosition = positionArr[inputCharArr[i] - 'a'];
      if (currCharPosition < 0 // 没有找到重复的字母时
          // 第二种情况: d 大于 f(i-1)
          || i - currCharPosition > currLength) {
        currLength++;
      } else { // 有重复的
        if (currLength >= maxLength) {
          maxLength = currLength;
        }
        //重置 curLength 为 两重复元素位置之间的差值, 以便重置 currLength 的值用来重新统计
        currLength = i - currCharPosition;
      }
      positionArr[inputCharArr[i] - 'a'] = i;
    }
    return maxLength;
  }

  public static void main(String[] args) {
    String inputStr = "arabcacfr";
    System.out.println(getLongestSubstringWithoutDup(inputStr));
  }
  // todo：打印最长的无重复的子字符串
}
