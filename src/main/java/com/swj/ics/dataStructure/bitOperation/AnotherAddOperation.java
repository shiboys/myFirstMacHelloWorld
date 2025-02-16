package com.swj.ics.dataStructure.bitOperation;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/01/29 23:22
 * 不用加减乘除做加法
 */
public class AnotherAddOperation {
  /**
   * 求两个数之和，不能用四则运算，那还能用什么？对数字做运算，除了四则运算之外，就只剩下位运算了。位运算是针对二进制的
   * 例如求 5+17=22 这个为例
   * 5 的二进制是 101，17 的二进制是10001，我们还是试着把计算分为三步：第一步个位相加但是不进位，得到的结果为 10100（最后一位两个数都是 1
   * 相加的结果是二进制的 10，这一步不计算进位，因此得到的结果仍然是0）
   * 第二步记下进位，在这个例子中只在最后一位相加的时候产生了一个进位，结果是二进制的 10。
   * 第三步，把前两步的结果相加，得到结果是 10110，转换成 10 进制刚好是 22。
   * <p>
   * 接下来我们试着把 二进制的加法用位运算来替代。第一步不考虑进位对每一位相加。0+0，,1+1 的结果都是 0；0+1 和 1+0 的结果都是1. 我们注意到
   * 这和异或运算的结果是一样的，对异或而言，0和0，1和1 的异或结果为0，而 0和1、1和0 的结果为1.
   * 接着考虑第二步进位，对0+0，,0+1 和 1+0 而言，都不会产生进位，只有 1+1 时，会产生一个进位。此时我们可以想象成两个数先做 位与运算 ，然后再
   * 向左移动 1 位, 来模拟进位。与运算的特点是，两个数都是 1 的时候，得到的结果才是 1，其余为0.
   * 第三步相加的过程依然是重复前面两步（第一步产生的结果作为第一个数，第二步产生的结果作为第二个数继续进行异或运算和 位与运算），直到不产生进位为止。
   */

  private static int getTheSumOf2NumberByBit(int num1, int num2) {
    int sum = 0;
    int carry = 0;
    do {
      // 使用异或运算 ^ 来模拟相加但是不产生进位
      sum = num1 ^ num2;
      // 模拟进位
      carry = (num1 & num2) << 1;

      num1 = sum;
      num2 = carry;
    } while (num2 != 0);
    return num1;
  }

  public static void main(String[] args) {
    System.out.println(getTheSumOf2NumberByBit(5,17));
    System.out.println(getTheSumOf2NumberByBit(5,27));
    System.out.println(getTheSumOf2NumberByBit(5,37));
    System.out.println(getTheSumOf2NumberByBit(555,337));
    System.out.println(getTheSumOf2NumberByBit(555,-337));
  }
}
