package com.swj.ics.dataStructure.bitmap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/02/12 21:30
 * 2.5 亿个整数中找出不重复的整数，注：内存不足以容纳这 2.5 亿个整数
 */
public class BitArrayOf2BitMap {
  /**
   * 解法1：采用 2-Bitmap（每个数分配2bit，00表示不存在，01表示出现一次，10表示出现多次，11无意义）进行，共需要内存 2^32 * 2
   * bit = 1GB
   * 内存，还可以接受。然后扫描这 2.5 亿个整数，查看 bitmap 中相应的应位，如果是 00 则变 01，如果是 01 则变 10, 10 保持不变。
   * 扫描完成后，查看 bitmap，把对应位是 01 的输出整数即可。
   */
  /**
   * 1 byte = 8 bit, 可以分为 4 个数字，1000 bytes 可以覆盖 4000 个数字，因此我们设置输出的数字个数不大于 4000
   * 题目要求是 2.5 亿个数字，那我们其实应该准备 2.5亿/4 长度的 byte，才能覆盖所有无符号的整数，次数用较小长度的数据
   * 才测试
   * flags 的初始化值都为0
   * flag[0] 例如 1000 0100 表示 3 出现过 2 次，2和0都没有出现过，1 出现过一次
   */
  static byte[] flags = new byte[1000];

  /**
   * 输入数字索引，返回数字出现的次数。比如上面提到的二进制数字flags[0]=1000 0100,那么应该返回二进制的 10 ，也就是10进制的2
   *
   * @param numberIndex bit array 的索引位
   * @return
   */
  static int getVal(int numberIndex) {
    assert numberIndex > 0;
    int recordIndex = numberIndex / 4;
    int recordPos = numberIndex % 4;
    // (3<<2*recordPos) = 11000000，flags[0]=1000 0100，那么 1000 0100 & 11000000
    // 的目的就是为了取这个数的 第 pos*2+2 位，
    // 也就是 第7-8 位，然后再 左移 pos*2=6位之后，得到原始二进制的值 10
    int val = (flags[recordIndex] & (3 << 2 * recordPos)) >> 2 * recordPos;
    return val;
  }

  static void setValue(int numberIndex) {
    assert numberIndex > 0;

    int val = getVal(numberIndex);
    // 10 或者 11 表示出现多次，可以忽略
    if (val >= 2) {
      return;
    }
    int recordIndex = numberIndex / 4;
    int recordPos = numberIndex % 4;
    // 数组中原始的数，比如 1000 0100
    int num = flags[recordIndex];
    // 比如 numberIndex = 2 ，此时 recordIndex=0,recordPos=2;
    // 3<< 2*recordPos 就是 00110000, 那么 ~(3<< 2*recordPos) 取反就是 11001111，即让 2bit
    // 位为0，其他位为 1
    // flag[recordIndex] & ~(3<< 2*recordPos) 的效果就是将 pos 的 2bit 位 为0
    // flag[recordIndex] & ~(3<< 2*recordPos) | (val+1) << 2*recordPos 就是让 2bit
    // 位得到正确的值
    num = (num & ~(3 << 2 * recordPos)) | (val + 1) << (2 * recordPos);
    flags[recordIndex] = (byte) num;
  }

  public static void main(String[] args) {
    int numMax = 2000;
    Random random = new Random();
    Map<Integer, Integer> map = new HashMap<>();
    for (int i = 0; i < flags.length * 4; i++) {
      int num = random.nextInt(numMax);
      map.put(num, map.getOrDefault(num, 0) + 1);
      setValue(num);
      System.out.print(num + " ");
    }
    System.out.println("");
    System.out.println("----------------");
    List<Integer> bitMapArray = new ArrayList<>();
    List<Integer> mapArray = new ArrayList<>();
    for (int i = 0; i < flags.length * 4; i++) {
      if (getVal(i) == 1) {
        bitMapArray.add(i);
      }
      if (map.getOrDefault(i, 0) == 1) {
        mapArray.add(i);
      }
    }
    System.out.println("只出现过一次的数据:");
    System.out.println(bitMapArray);
    System.out.println(bitMapArray.equals(mapArray));
  }

}
