package com.swj.ics.java.messagequeue.kafka;

import java.nio.ByteBuffer;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/11/14 21:49
 * 变长字段测试
 */
public class VarintTest {
  /**
   * 计算 long 类型在 Varints 编码后的长度
   */

  int sizeOfLongWithVarints(long v) {
    int bytes = 1;
    while ((v & 0xffffffffffffff80L) != 0) {
      // 14 个 f，1 个 f 表示 1111 这 4 个1,14*4 =56, 16 进制的 80 表示 1000 1000 这 8 个 bit ，所以总共
      // 8+56 = 64 位
      v = v >>> 7;// 注意这里 v >> 7 是不可取的，负数的左移，一定不能带符号左移，因此需要使用无符号的左移。
      bytes += 1;
    }
    return bytes;
    // 所以 -1 就返回 10 个字节，因为 -1 的补码是 1111 1111 1111 1111 1111 1111 1111 1111 1111 1111 1111 1111 1111 1111 1111 1111
    // 而 varint 7 位表示一个字节，因此 最坏的情况下 一个long 表示为 varints 的所需要的字节长度是 64/7 + 1 = 10 个字节
    // 所以 varints 需要使用 ZigZag 编码将 这种情况下的大字节占用给编码成小字节占用
  }

  void writeVarint(int value, ByteBuffer buffer) {
    // 先进行 ZigZag 编码，将 大字节占用改为锯齿状编码的小字节占用
    //如果使用 value >>> 31 就大错特错了，表示无符号右移，但是 zigzag 的实现是有符号右移，该编码的含义是将负数也给编码成正数
    int v = (value << 1) ^ (value >> 31);
    while ((v & 0xffffff80) != 0) { // v 大于 7 个字节。0xffffff80 低 7 位是 0 ，剩余位全为 1。
      byte b = (byte) ((v & 0x7f) | 0x80); // 取低 7 位的字节，异或 0x80 将 msb 变为 1 。
      buffer.put(b);
      // 因为 v 已经被 zigzag 编码为正整数了。使用 无符号右移 没问题，也进行保证
      v = v >>> 7;
    }
    // 剩余字节 put 进 byteBuffer，且 msb 位置默认为 0
    buffer.put((byte) v);
  }

  int readVarint(ByteBuffer buffer) {
    int val = 0;
    byte b = 0;
    int shift = 0;
    while (((b = buffer.get()) & 0x80) != 0) { // msb 位置为 1 ，不是最后一个 字节
      val = val | (b & 0x7f) << shift; // 取出 低 7 位，然后 左移 shift 位置，然后 和 value 进行异或，这样当前 byte 位置就能算入最高位
      shift += 7;
      if (shift > 28) { // 表示 buffer 存储的数字 > int 的最大值 28/7 =4
        throw new IllegalArgumentException("bytebuffer contains more bytes than an integer value");
      }
    }
    // 最后一个 msb 位置不是 1 的字节
    val |= (b << shift);
    // 进行 zig-zag 编码去除。正整数都 编码为偶数，负整数都编码为奇数，所以正整数的偶数 val &1 =0 ,负整数的为 1，前面有个符号也就是 -1 = 1111....1111
    val = (val >>> 1) ^ -(val & 1);
    return val;
  }

  public static void main(String[] args) {
    int i = -64;
    System.out.println(Integer.toBinaryString(i << 1));
    System.out.println(Integer.toBinaryString(i >> 31));
    System.out.println(Integer.toBinaryString(i >>> 31));
    // 输出分别如下：
    /**
     * 11111111111111111111111111111110
     * 11111111111111111111111111111111
     * 1
     * 注意区分有符号右移和无符号右移
     */
    //testVarInt();
  }

  private static void testVarInt() {
    VarintTest instance = new VarintTest();
    System.out.println(instance.sizeOfLongWithVarints(-1));
    ByteBuffer buffer = ByteBuffer.allocate(4);
    instance.writeVarint(1,buffer);
    buffer.rewind();
    System.out.println(instance.readVarint(buffer));
    buffer.clear();
    instance.writeVarint(-2,buffer);
    buffer.rewind();
    System.out.println(instance.readVarint(buffer));

    buffer.clear();
    instance.writeVarint(64,buffer);
    buffer.rewind();
    System.out.println(instance.readVarint(buffer));
  }
}
