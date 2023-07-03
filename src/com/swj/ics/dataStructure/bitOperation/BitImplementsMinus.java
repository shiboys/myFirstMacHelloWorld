package com.swj.ics.dataStructure.bitOperation;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/03/01 17:23
 * 使用位运算来实现减法操作。
 * 要用位运算来实现四则运算，不仅仅要知道 &,|,^,~,>>,<< 怎么做，还需要先掌握几个位运算的规律：
 * 1：~n=-(n+1)或者~(n-1) = -n,比如 ~3=-4
 * 2: 获取整数 n 的二进制串中的最后一个 1：-n&n=~(n-1)&n
 * 3:去掉整数n 的二进制串中的最后一个 1：n&(n-1)
 */
public class BitImplementsMinus {
  public static void main(String[] args) {
    int i = 24;
    // 原理就是 i&(-i) = ~(i-1)&8 = ~7 = 1111 1000 & 0000 1000 =8;
    System.out.println(i & (-i));

    int a = 10;
    int b = 5;
    System.out.println(add(a, b));
    System.out.println(minus(a, b));

    b = 20;
    System.out.println(add(a, b));
    System.out.println(minus(a, b));

    System.out.println(Integer.toBinaryString(-3));
  }

  /**
   * 加法：
   * 由 a^b 可得按位相加后没有没有进位的和
   * 由 a&b 可得可以产生进位的地方；
   * 由 (a&b) << 1 得到进位后的值
   * 那么 按位相加后原位的和 + 进位的和 就是加法的和了，而 a^b +(a&b)<<1 相当于把 + 两边带入上述 3 步进行加法计算
   * 直到进位和为0说明没有了进位，则此时原位和即所求和
   */

  public static int add(int a, int b) {
    int res = a;
    int xor = a ^ b; // 获取原位和
    int forward = (a & b) << 1; //获取进位和
    if (forward != 0) { /// 若进位和不为 0，则递归求原位 + 进位和
      res = add(xor, forward);
    } else {
      res = xor; // 若进位和为 0，说明此时已经没有进位了，那么原位和就是两个数的和。
    }
    return res;
  }

  /**
   * 减法 a-b。
   * 由前面的规则可知 -b = ~(b-1)，由此可得 a-b=a+(~(b-1)) 把减法转化为加法即可
   *
   * @param a
   * @param b
   * @return
   */
  public static int minus(int a, int b) {
    return add(a, ~(b - 1));
  }


}
