package com.swj.ics.algorithms;

import java.text.DecimalFormat;
import java.util.Arrays;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/01/30 21:26
 * n 个骰子的点数
 * 题目：把 n 个骰子扔在地上，所有的骰子朝上的以免的点数之和为 s。输入n，打印出 s 的所有可能的值出现的概率。
 */
public class NofDice {
  /**
   * 题目分析：
   * 玩过麻将的人都知道，骰子一共有 6 面，每个面上都有一个点数，对应 1-6 之间的一个数字。所以 n 个骰子点数和最小为 n，最大值为 6n。另外，根据
   * 排列组合的只是，我们还知道骰子的所有组合排列数为 6^n。要解决这个问题，我们需要先统计出每个点数出现的次数，然后再把每个点数出现的次数初一 6^n
   * 就能求出每个点数出现的概率。
   */
  /**
   * 解法1：基于递归求骰子点数，优点是逻辑容易理解，缺点是时间效率不高。
   * 现在我们考虑如何统计每个点数出现的次数。如果想求出 n 个骰子的点数和，可以先把 n 个骰子分为两堆，第一堆只有 1 个。另一堆有 n-1 个。
   * 单独的那一个有可能出现 1-6 的点数。我们需要计算 1-6 的每一种点数 和 剩下的 n-1 个骰子来计算点数和。接下来把剩下的 n-1 个骰子仍然分成两堆
   * 第一堆只有 1 个；第二堆有 n-2 个。我们把上一轮那个单独的骰子的点数 跟 这一轮单独的骰子的点数相加，再跟剩下的 n-2 个骰子来计算点数和。
   * 分析到这里，我们不难发现这是一个递归的思路，递归结束的条件就是最后只剩下一个骰子。
   * 我们可以定义一个长度为 6n - n +1 的数组，(最大长度-最小长度+1 可以刚刚存储 5n 的骰子所有点数和)，将和为 s 的点数出现的次数保存到数组的第
   * s-n 个元素里。基于这种思路，我们可以写出如下代码
   */

  // 骰子的最大点数是 6
  private static final int MAX_DICE_POINT = 6;
  private static final DecimalFormat DEFAULT_FORMAT = new DecimalFormat("#.######");

  static void calculateProbability(int nDice) {
    if (nDice < 1) {
      throw new IllegalArgumentException("number of nDice must be greater than 0");
    }
    int[] probabilityArr = new int[MAX_DICE_POINT * nDice - nDice + 1];
    doCalculateDiceSum(nDice, probabilityArr);
    double total = Math.pow(MAX_DICE_POINT, nDice);
    for (int i = nDice, max = MAX_DICE_POINT * nDice; i <= max; i++) {
      System.out.print(String.format("%s:%s\t", i, DEFAULT_FORMAT.format(probabilityArr[i - nDice] / total)));
      if (i > nDice && (i - nDice) % 20 == 0) {
        System.out.println();
      }
    }
  }

  private static void doCalculateDiceSum(int nDice, int[] probabilityArr) {
    // 把骰子的每个面都遍历下
    for (int i = 1; i <= MAX_DICE_POINT; i++) {
      doCalculateDiceSum(nDice, nDice, i, probabilityArr);
    }
  }

  /**
   * 计算骰子点数和为 s 的次数
   *
   * @param originalDiceCount 骰子原本的个数 n
   * @param currentDiceCount  骰子本次循环/递归的当前个数，用于判断递归或者循环退出
   * @param sum               累计的骰子点数和 s
   * @param probabilityArr    存储 和为 s 的出现次数数组容器
   */
  private static void doCalculateDiceSum(int originalDiceCount, int currentDiceCount, int sum, int[] probabilityArr) {
    if (currentDiceCount == 1) {
      probabilityArr[sum - originalDiceCount]++;
    } else {//计算 n-1 个骰子的出现次数
      for (int i = 1; i <= MAX_DICE_POINT; i++) {
        doCalculateDiceSum(originalDiceCount, currentDiceCount - 1, sum + i, probabilityArr);
      }
    }
  }

  public static void main(String[] args) {
    int[] arr = new int[2];
    System.out.println(Arrays.toString(arr));
    int nDice = 6;
    // calculateProbability(nDice);
   /* double i = 0.0012002743484224967;
    DecimalFormat format = new DecimalFormat("#.######");
    System.out.println(format.format(i));*/
    printProbability(nDice);
  }

  /**
   * 解法二：基于循环求骰子点数，时间性能好
   * 我们可以换一种思路来解决这个问题。我们主要考虑用两个数组来存储骰子点数的每一个总数出现的次数。在一轮循环中，第一个数组的第 n 个数字表示骰子
   * 和为 n 的出现次数。在下一轮循环中，我们加上一个新的骰子，此时和为 n 的骰子的出现总次数应该等于上一轮骰子中和为 n-1，n-2，n-3，n-4，n-5，
   * n-6 的次数的总和，所以我们把另一个数组的第 n 个数字设为前一个数组对应的第 n-1，n-2，n-3，n-4，n-5，n-6 个数字之和。基于这种思路，我们
   * 来尝试写下代码。也是一个左手倒右手的算法，很厉害
   */

  private static void printProbability(int nDice) {
    if (nDice < 1) {
      return;
    }
    int arrayLength = nDice * MAX_DICE_POINT;
    int[][] probabilityArr = new int[2][arrayLength + 1];
    int flag = 0;

    // 初始化第一个空白数组，每个点数出现的次数为 1
    for (int x = 1; x <= MAX_DICE_POINT; x++) {
      probabilityArr[flag][x] = 1;
    }

    // 从第二个骰子开始算起
    for (int k = 2; k <= nDice; k++) {
      // 每次都要初始化第二个数组的 0 到 k 之间的数据为0，因为 0 到 k(不包含 k) 之间的 的数据本轮出现的几率为0
      for (int j = 0; j < k; j++) {
        probabilityArr[1 - flag][j] = 0;
      }
      // 设置本轮有出现记录的数据。 k 到 nk 之间的数据，比如 2-12，3-18，4-24
      for (int x = k; x <= MAX_DICE_POINT * k; x++) {
        // probabilityArr[1 - flag][x] = 0;
        int sum = 0;
        for (int i = 1; i <= x && i <= MAX_DICE_POINT; i++) {
          // f(n)=f(n-1)+f(n-2)+f(n-3)+f(n-4)+f(n-5)+f(n-6)
          // 利用对方数组的数据进行求值，然后设置己方数组元素
          sum += probabilityArr[flag][x - i];
        }
        probabilityArr[1 - flag][x] = sum;
      }
      // 轮到下一个骰子，需要转换
      flag = 1 - flag;
    }

    double total = Math.pow(MAX_DICE_POINT, nDice);
    for (int i = nDice; i <= nDice * MAX_DICE_POINT; i++) {
      // probabilityArr的二维数组中 n 存的是 n 大小的出现次数
      System.out.print(String.format("%s:%s\t", i, DEFAULT_FORMAT.format(probabilityArr[flag][i] / total)));
      if (i > nDice && (i - nDice) % 20 == 0) {
        System.out.println();
      }
    }
  }

}
