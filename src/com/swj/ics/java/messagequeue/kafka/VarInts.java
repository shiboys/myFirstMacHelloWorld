package com.swj.ics.java.messagequeue.kafka;

import java.nio.ByteBuffer;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2024/06/15 11:32
 * Varints 是使用哦一个或多个字节来序列化整数的一种方法。数值越小，其占用的字节数就越少。Varints 中的每个字节都一个位于最高位的
 * msb 位(most
 * significant bit), 除最后一个字节外，其余 msb 位都设置为 1，最后一个字节的 msb 位为0。这个 msb
 * 位的表示其后的字节是否和当前字节一起来表示
 * 同一个整数(就是标识一个整数的字节结束)。除 msb 外，剩余的 7 位用于存储数据本身，这种表示类型又称为 Base
 * 128。通常而言，一个字节 8 位可以表示 256 个值
 * 所以成为 Base 256，而这里只能用 7 位表示，2^7=128。Varints 中采用的是小端字节序，即最小的字节放在最前面。
 * 举个栗子，比如数字 1 ，它只占用一个字节，所以 msb 位为 0：
 * 0000 0001
 * 再举个复杂的例子，比如说数字 300：
 * 1010 1100 0000 0010
 * 300 的二进制原本为 0000 0001 0010 1100 = 256 + 32 + 8 +4 = 300, 那么为什么 300
 * 的变成表示为上面的这种形式那？
 * 0000 0001 0010 1100
 * -> 00 000 0010 010 1100 每个字节取低 7 位
 * -> 1(msb)010 1100 0(msb)000 0010 反转并实现小端字节序列-小的字节放前面，并 msb 高7位为 1 ，最后
 * 7 位为 0。最前面的 2 个 0 表示为 0，
 * 因为没有 1 ，会被变为认为 0 字节，也就是没有字节可读被舍去，相当于前面补0，没有实际意义
 * -> 1010 1100 0000 0010 最终编码的值
 * 反编码的过程就是把这个过程倒过来。
 * 1010 1100 0000 0010
 * -> 010 1100 000 0010 （去掉每个字节的 msb 位）
 * -> 000 0010 010 1100 （翻转)
 * -> 1 0010 1100 （还原为真实的二进制)
 * -> 0000 0001 0010 1100 = 256 + 32 + 8 + 4 (格式化一下)
 * 至此，varint 的编码理论的已经描述完毕。
 */

public final class VarInts {
  /**
   * varints 编码
   *
   * @param v          被编码值
   * @param byteBuffer 编码结果写入 bytebuffer 中
   */
  public static void writeVarInts(int v, ByteBuffer byteBuffer) {
    /**
     * Varint 可以用来表示 int32, int 64,unit32,sint32 等类型。在实际使用过程中，那么对 int32/int64 和
     * sint32/sint64 而言，
     * 它们在进行编码时存在较大的区别。比如使用 int64 表示一个负数，哪怕是 -1，其编码后的长度始终是一个 10
     * 字节的数值，就如同一个很大的无符号长整数一样。
     * 可以使用辅助函数 sizeOfLong 进行测试；为了使编码更加高效，Varints 使用了 ZigZag 的编码方式
     * 有关ZigZag 的描述请参见 md 文档
     */
    // 先进行 zigzag 编码
    int encodeVal = (v << 1) ^ (v >> 31);
    // 0x0xffffff80 是低 7 位为0，其余位全部为 1。用来判断当前字节是否多于 7 位
    while ((encodeVal & 0xffffff80) != 0) {
      // 0x7f 的二进制是 111 1111，& 0x7f 表示取低 7 位的字节
      // | 0x80 是将第 8位的 msb 位置置为 1
      byteBuffer.put((byte) ((encodeVal & 0x7f) | 0x80));
      // 因为已经通过 zigzag 编码为 正整数了，这里的无符号右移 >>> ，其实跟有符号右移效果一样
      // 但是为了严谨，这里仍然采用无符号右移
      encodeVal = encodeVal >>> 7;
    }
    // 剩余字节，由于 msb 为0，则任务就是剩余 7 字节本身.
    // 同时 byteBuffer 的 put 操作，刚刚是数组操作，
    // 从 0 开始，满足了 varints 编码的小端顺序要求：最小的字节放前面, 最后 put 的是 encodeValue 原始值的最高 7 位。
    byteBuffer.put((byte) encodeVal);
  }

  public static int readVarint(ByteBuffer byteBuffer) {
    if (byteBuffer == null || !byteBuffer.hasRemaining()) {
      throw new IllegalArgumentException("byteBuffer is empty");
    }
    byte b;
    // 位移多少
    int shfit = 0;
    int val = 0;
    // 循环判断 msb 为是否为 1，如果不为 1 说明读取 int 的字节没有结束
    while (((b = byteBuffer.get()) & 0x80) != 0) {
      // 取 b 的低 7 位的字节为有效字节, 然后左移 shift 位，跟 val 进行逻辑或运算，使得 左移后的值在 val 的高位
      // 从而还原原始数据，因为 varints 是小端字节。
      val = val | ((b & 0x7f) << shfit);
      // 每次左移 7 位
      shfit += 7;
      if (shfit > 28) { // byteBuffer 存储的 varints 类型的值最多只能 5 字节，而最高的 (32-28) 为 4 位是无法通过 while 循环的条件的
        // 如果能够通过，就会出现 shift > 28 的情况，就不是一个有效的 int 值了，因此这里抛出异常
        throw new IllegalArgumentException("bytebuffer contains more bytes than an integer value");
      }
    }
    // 追加有 剩余的 4 位或者其他不够 32 位的字节剩余字节
    val = val | (b << shfit);
    // val 被还原成 zigzag 编码后的数字，现在要将 zigzag 的编码继续还原
    // val 如果是偶数，(val & 1) == 0 ,则原始值是正数 直接 >> 1 表示除以2 ，然后 ^0 = 原始值
    // val 是奇数，(val & 1) == 1，-(val & 1) == -1, -1 的补码为 1111 1111 1111 1111 1111 1111 1111 1111, 因此 任何数 ^ (-1) 都是它对应的补码负数
    // 也即是是 63 ^ -1 == -64 == ~63
    val = (val >> 1) ^ -(val & 1);
    return val;
  }

  public static void main(String[] args) {

    // 经测试不会出现溢出
    System.out.println((Integer.MAX_VALUE + 10 +1) >>>1);
    // 经测试，会出现溢出，溢出结果为负数。
    System.out.println((Integer.MAX_VALUE + 10 +1));

    ByteBuffer buffer = ByteBuffer.allocate(10);
    testWriteVarints(-1, 1, buffer);

    testWriteVarints(1, 2, buffer);
    testWriteVarints(63, 126, buffer);
    testWriteVarints(-64, 127, buffer);
    testWriteVarints(64, 128, buffer);

  }

  static void testWriteVarints(int originalVal, int expectedVal, ByteBuffer buffer) {
    writeVarInts(originalVal, buffer);
    buffer.flip();
    int encodeVal = 0;
    if (buffer.limit() == 0) {
      System.out.println("buffer is empty");
      return;
    } else if (buffer.limit() < 2) {
      encodeVal = buffer.get();
    } else if (buffer.limit() < 4) {
      int len = 4;
      ByteBuffer tmpByteBuf = ByteBuffer.allocate(len);
      int bufferLimit = buffer.limit();
      // 直接使用 byteBuffer.getShort() 由于 msb 为 1 ，short 将其当为 负值的标识，因此 getShort() 返回的是负值
      // 这里使用 高位补 0 的方式，将其正确还原为 int 类型。方法有两种：1、使用 ByteBuffer 的复制功能，2、使用字节数组，字节数组调试更为友好，
      // 然后再转化为 byteBuffer, 最终使用 byteBuffer 的 getInt() 功能读取出一个完整的 int 值。

      //方法1： 先把不足的高位补 0;
      for (int i = 0; i < len - bufferLimit; i++) {
        tmpByteBuf.put((byte) 0);
      }
      //再把 将 buffer 的全部 limit 数量的字节数据复制到 tmpByteBuf 中
      tmpByteBuf.put(buffer);
//      方法2 为使用字节数组的方式，中心思想也是先用补0，剩下的填充，不过比较复杂，没有方法1 直接
//      int len = 4;
//      byte[] bytes = new byte[len];
//      int i = 0;
//      int bufferLimit = buffer.limit();
//      while (buffer.hasRemaining()) {
//        bytes[len - bufferLimit + i] = buffer.get();
//        i++;
//      }
//      // 前面前面补 0
//      for (i = 0; i < bytes.length - bufferLimit; i++) {
//        bytes[i] = (byte) 0;
//      }
//      ByteBuffer tmpByteBuf = ByteBuffer.wrap(bytes);
      /*while (tmpByteBuf.position() < 4) {
        tmpByteBuf.put(0, (byte) 0);
      }*/
      tmpByteBuf.flip();
      encodeVal = tmpByteBuf.getInt();
      //encodeVal = buffer.getShort();
    } else {
      encodeVal = buffer.getInt();
    }

    System.out.println("varint of (" + originalVal + ") equals " + expectedVal + " is " + (encodeVal == expectedVal ?
        "true"
        :
        "false, and encodeValue = " + encodeVal + ", and the binary value of encodeValue is " + Integer.toBinaryString(
            encodeVal)));

    if (encodeVal != expectedVal) {
      ByteBuffer duplicate = buffer.duplicate();
      duplicate.rewind();
      System.out.println("original value of " + encodeVal + " is " + readVarint(duplicate));
    }
    buffer.clear();
  }

  /**
   * 获取 经过 varint 编码之后 val 的字节数
   *
   * @param val
   */
  public static int sizeOfVarints(int val) {
    /**
     * 跟 writeVarints 方法的逻辑非常类似，只是稍微有一点不同
     */
    // 先进行 zigzag 编码。
    val = (val << 1) ^ (val >> 31);
    int bytes = 1;
    while ((val & 0xffffff80) != 0) {// val 高于 7 位
      bytes += 1;
      val = val >> 7;
    }
    // 不高于 7 位的默认已经添加了，就是说最后的低 7 位最先在 bytes 出吃货的时候被计算了。
    return bytes;
  }
}
