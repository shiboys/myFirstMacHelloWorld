package com.swj.ics.algorithms.fibonacci;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2022/12/27 17:38
 * 斐波那契数列的实现，分别为递归法，循环法
 * 以及 蛙跳台阶的斐波那契应用
 */
public class Fibonacci2 {

  static int fibRecursive(int n) {
    if (n <= 0) {
      return 0;
    } else if (n == 1) {
      return 1;
    }
    return fibRecursive(n - 1) + fibRecursive(n - 2);
  }

  static int fibWithLoop(int n) {
    int[] initArr = new int[] {0, 1};
    if (n < 0) {
      return 0;
    }
    if (n < 2) {
      return initArr[n];
    }
    int firstNum = 0;
    int secondNum = 1;
    int total = 0;
    for (int i = 2; i <= n; i++) {
      total = firstNum + secondNum;
      firstNum = secondNum;
      secondNum = total;
    }
    return total;
  }

  public static void main(String[] args) {
    int n = 5;
    System.out.println("fib with recursive of " + n + " is " + fibRecursive(n));
    System.out.println("fib with loop of " + n + " is " + fibWithLoop(n));
    System.out.println("fibFromJump with loop of " + n + " is " + fibFrogJumpWithLoop(n));
  }
  /**
   * 蛙跳台阶问题：
   * 一只青蛙一次可以跳上 1 级台阶，也可以跳上 2 级台阶。求该青蛙跳上一个 n 级的台阶总共有几种跳法
   *
   * 首先我们考虑最简单的情况，如果只有 1 个台阶，那么显然只有 1 中跳法。如果有 2 个台阶，就有两种跳法
   * 一种是分两次跳，每次跳 1 阶，另一种就是一次跳 2 阶
   * 接着我们再讨论一般的情况，我们把 n 阶台阶的跳法看成是 n 的函数，记为 f(n)。当 n>2 时，第一次跳的时候，有 2 中选择，一是第一次跳 1 阶，
   * 这中跳法数量等于后面的 n-1 阶的跳法数量，即为 f(n-1); 二是第一次跳 2 阶，这种跳法的数量等于后面剩下的 n-2 阶的跳法数量。
   * 因此 n 阶跳法的数量就是 f(n-1)+f(n-2), 斐波那契刚好可以满足
   */

  static int fibFrogJumpWithLoop(int n) {
    int[] initArr = new int[] {0, 1, 2};
    if (n < 0) {
      return 0;
    }
    if (n <= 2) {
      return initArr[n];
    }
    int firstNum = 1;
    int secondNum = 2;
    int total = 0;
    for (int i = 3; i <= n; i++) {
      total = firstNum + secondNum;
      firstNum = secondNum;
      secondNum = total;
    }
    return total;
  }
}
