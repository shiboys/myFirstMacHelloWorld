package com.swj.ics.dataStructure.bitmap;

import java.util.Random;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/02/13 10:36
 * 实现字节数组
 * 题目：40亿个未排序的 unsigned int 的整数，然后再给一个数，如何快速判断这这个数在哪 40 一个数中。
 */
public class BitArray {
  /**
   * 参考 jdk 的 bitset 实现
   */
  private static final int DEFAULT_SHIFT_STEPS = 6;
  private static final int DEFAULT_MASK = 63;
  private long size;
  private long[] dataArr;

  public BitArray(long length) {
    // todo 判断不能大于 Integer.MAX
    if ((length - 1) >> 6 + 1 > Integer.MAX_VALUE) {
      long maxLength = (long) Integer.MAX_VALUE << 6 + 1;
      throw new IllegalArgumentException("the length of bit array is too large, max size is " + maxLength);
    } else if (length < 1) {
      throw new IllegalArgumentException("the length of bit array can not be less than 1");
    }
    this.size = length;
    dataArr = new long[(int) ((length - 1) >> DEFAULT_SHIFT_STEPS) + 1];
  }

  public boolean getBit(int dataIndex) {
    int recordIndex = dataIndex >> DEFAULT_SHIFT_STEPS;
    if (recordIndex >= dataArr.length) {
      throw new IllegalArgumentException(
          "data index should less than bit array length of " + (dataArr.length << DEFAULT_SHIFT_STEPS));
    }
    int recordPos = dataIndex & DEFAULT_MASK;
    // jdk 的做法是 data = data | 1 << dataIndex, 这里的 << 是有符号的，对于特别大的数，目前还没有查出来那里来折回
    long data = dataArr[recordIndex];
    return (data & (1 << recordPos)) != 0;
  }

  public void setBit(int dataIndex, boolean set) {
    int recordIndex = dataIndex >> DEFAULT_SHIFT_STEPS;
    if (recordIndex >= dataArr.length) {
      throw new IllegalArgumentException(
          "data index should less than bit array length of " + (dataArr.length << DEFAULT_SHIFT_STEPS));
    }
    if (set) {
      int recordPos = dataIndex & DEFAULT_MASK;
      // jdk 的做法是 data = data | 1 << dataIndex, 这里的 << 是有符号的，对于特别大的数，目前还没有查出来那里来折回
      long data = dataArr[recordIndex];
      // 我这里的做法是参照 stackoverflow 的做法
      // https://stackoverflow.com/questions/15736626/java-how-to-create-and-manipulate-a-bit-array-with-length-of-10-million-bits
      // 这里我想了下，跟 jdk 的是一致的。为啥说那，是因为 只是 data 是数组的 recordIndex 部分，它的二进制长度最多是 64 位的
      // 因此这里对这部分数据进行 逻辑或操作 能将 这个最高的 64 位 recordPos 这这个位置的 bit 设置为 1 ，但是其他低位部分的 64 位数字没有改变
      data = data | (1 << recordPos);
      dataArr[recordIndex] = data;
    } else {
      clearBit(dataIndex);
    }
  }

  private void clearBit(int dataIndex) {
    int recordIndex = dataIndex >> DEFAULT_SHIFT_STEPS;
    int recordPos = dataIndex & DEFAULT_MASK;
    // jdk 的做法是 data = data | 1 << dataIndex, 这里的 << 是有符号的，对于特别大的数，目前还没有查出来那里来折回
    long data = dataArr[recordIndex];
    // 取反并进行 逻辑或操作，将指定的bit位置置为 0 ，其他位置不变
    data = data & ~(1 << recordPos);
    dataArr[recordIndex] = data;
  }

  public static void main(String[] args) {
    // BitSet bitSet = new BitSet();
    BitArray bitSet = new BitArray(Integer.MAX_VALUE);
    int maxBound = 1 << 20;
    int length = 20;
    Random random = new Random();
    int[] arr = new int[length];
    for (int i = 0; i < length; i++) {
      arr[i] = random.nextInt(maxBound);
      if ((i & 1) == 0) {
        bitSet.setBit(arr[i], true);
      }
    }

    for (int i = 0; i < length; i++) {
      if (bitSet.getBit(arr[i])) {
        System.out.println("bitmap hit: " + arr[i]);
      } else {
        System.out.println("not hit:" + arr[i]);
      }
    }
  }
}
