### 可变字节详述

 Varints 是使用一个或多个字节来序列化或多个字节来序列化整数的一种方法。数值越小，其占用的字节数就越少。
Varints 中的每个字节都有一个位于最高位的 msb 位(most significant bit) ，除最后一个字节外，其余 msb 位置设置都为 1，
最后一个字节的 msb 位为 0。这个 msb 位表示其后的字节是否和当前字节一起来表示一个整数。(0 位以后的，就不表示了，也就是 0 位以后的表示当前整数的字节结束)
除 msb 位以外，剩余的 7 位用于存储数据本身，这种表示类型又称为 Base 128。通常而言，一个字节的 8 位可以表示 256 个值，所以称为 Base 256,
而这里只能用 7 位表示，2 的 7 次方即 128。varints 中采用的是小端字节序，即最小的字节放在最前面。
举个栗子：比如说数字1，它只表示 1 个字节，所以 msb 位为 0：
0000 0001
再举个复杂的例子，比如说 300 的 varints 编码如下：
1010 1100 0000 0010
300 的二进制原本为 0000 0001 0010 1100 = 256+32+8+4，那么为什么 300 的变长表示为上面的这种形式？

首先去掉每个字节的 msb 位，表示如下：

1010 1100 0000 0010
    ->010 1100 000 0010
如前所述，varints 使用的是小端字节的布局方式，所以这里两个字节的位置需要翻转一下：

010 1100 000 0010
    ->000 0010 010 1100(翻转)
    ->000 0010 ++ 010 1100(两个字节紧挨着拼在一起)
    -> 0000 0001 0010 1100 = 256+32+8+4=300(前面补0，会导致前一个字节的0，转移到后一个字节上组成一个完整的字节，在算法上就是前一个字节左移 7 位 和有一个字节进行逻辑或 | 运算)

Varints 可以用来表示 int32,int64,unint32、unint64、sint32、sint64、bool、enum 等类型。
在实际使用过程中，如果当期那字段可以表示为负数，那么对 int32/int64 和 sint32/sint64 而言，它们在进行编码的时候存在较大的区别。比如使用 int64 表示一个负数，哪怕是 -1，其编码后的长度始终为 10 个字节，具体测试方法请参见 varintTest.sizeOfLong 方法，就如同对待一个很大的无符号长整数一样。为了使编码更加高效，Varints 使用了 ZigZag 的编码方式。

ZigZag 编码是以一种锯齿形(zig-zags)的方式来回穿梭正负数，将带符号的映射为无符号的整数，这样可以使绝对值较小的负数仍然享有较小的 Varints 编码，比如 -1 编码为 1,1 编码为 2，-2 编码为 3,2 编码为 4。如下表所示

|  原值   | 编码后的值  |
|  ----  | ----  |
| 0  | 0 |
| -1  | 1 |
| 1  | 2 |
| -2  | 3 |
| 2147483647  | 4294967294 |
| -2147483648  | 4294967295 |

