package com.swj.ics.dataStructure.array;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/01/29 23:21
 * 股票最大利润
 * 题目：假设把某股票的价格按照时间先后顺序存储在数组中，请问买卖该股票一次可能获得的最大利润是多少？
 * 例如，一只股票在某些时间节点的价格为{9,11,8,5,7,12,16,14}。如果我们能在价格为 5 的时候买入并在价格为 16 的时候卖出
 * 则能收获最大利润 11
 */
public class StockMaxProfit {
  /**
   * 分析：最大利润就是最高价和最低价的差值，因此如果在一趟循环中找出最高价和最低价就行，这个也不是流式的数据
   */
  private static int getMaxProfitValue(int[] arr) {
    if (arr == null || arr.length < 1) {
      return 0;
    }
    int max = arr[0];
    int min = arr[0];
    for (int i = 1; i < arr.length; i++) {
      if (arr[i] > max) {
        max = arr[i];
      } else if (arr[i] < min) {
        min = arr[i];
      }
    }
    return max - min;
  }

  public static void main(String[] args) {
    int[] arr = {9,11,8,5,7,12,16,14};
    System.out.println(getMaxProfitValue(arr));
    // 这个 leetcode 的原则是有问题的，必须先买入，才能再卖出，如果 7-1 = 6 ，这个的话，买入 7块钱，卖出 1 块钱，赔 6 块钱。哈哈
    // 还是 labuladong 的动态规划 股票买卖问题解决，比较牛逼。
    arr =  new int[] {7,1,5,3,4,6};
    System.out.println(getMaxProfitValue(arr));
  }
}
