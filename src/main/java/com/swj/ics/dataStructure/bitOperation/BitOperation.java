package com.swj.ics.dataStructure.bitOperation;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/03/30 15:49
 * 常用位运算
 */
public class BitOperation {
  private static final int MASK = 0x80000000;// 该数 = 1 << 31

  /**
   * 获取整数 i 的 二进制表示。正数返回原码形式，负数返回补码形式。对正数来说，原码就是补码。
   * 跟 jdk 的 Integer.toBinaryString(int i) 不太一样，jdk 这个方法对于小这个数，前面不会补零
   *
   * @param input
   * @return
   */
  public static String getIntegerBinaryString(int input) {
    int length = Integer.SIZE;
    StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      // 当 i =0 时， mask >>> 0 = 2^31 ,也就是高位的第 32 bit 位为1，其余为 0，跟 1010（10进制的 10）逻辑与结果为32 个bit都是 0，
      // 然后再右移 31 位，仍然是 0.
      // 当 i = 28 时， mask >>> 28 = 1000，然后 逻辑与 & 1010 = 1000,然后再 >>> 3 = 1
      int t = (input & MASK >>> i) >>> (length - 1 - i);
      sb.append(t);
    }
    return sb.toString();
  }

  public static void main(String[] args) {
    System.out.println(getIntegerBinaryString(10));
    System.out.println(getIntegerBinaryString(-10));
  }
}
