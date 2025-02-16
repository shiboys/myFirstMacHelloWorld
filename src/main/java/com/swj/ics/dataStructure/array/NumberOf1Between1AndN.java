package com.swj.ics.dataStructure.array;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/01/03 22:57
 * 题目：输入一个整数，求 1~n 这 n 个数的十进制表示中 1 出现的次数。例如输入12，1~12 这些整数中包含 1 的数字有
 * 1、10、11、12。1 一共出现了 5 次
 * <p>
 * 解题思路：
 * 考虑将 10 进制的 n 的每一位单独拿出来讨论，每一位的值记为 weight。
 * 1) 个位
 * 以 534 为例
 * <p>
 * round   weight
 * / \    |
 * n  = 5   3   4
 * 在 从 1 增长到 n 的过程中，534 的个位从 0-9 变化了 53 次(00~52)，记为 round。在每一轮变化中，1 在个位出现一次，所以一共出现 53 次。
 * 再来看看 weight 的值。weight 值为 4，大于 0，说明第 54 轮变化是从 0-4，1 又出现了 1 次。我们记 1 出现的次数为 count。所以：
 * count = round*1 + 1 =54。
 * 如果此时 weight 为0（n=530），那说明第 54 轮到 0 就停止了，那么 count = round*1 = 53
 * <p>
 * 2) 十位
 * 对于 10 位来说，其 0~9 出现的次数与个位统计的方式是现同的,如下图
 * <p>
 * round weight
 * |    |
 * n  = 5   3   4
 * 不同点在于，从 1 到 n，十位的 weight 才会增加 1，所以，一轮十位数的 0-9 的周期内，每轮 1 都会出现 10 次。（010-019,110-119，210-219... 410-419）
 * 也即是  round * 10，再来看看 weight 的值，此时 weight  为 3，说明第 6 轮出现了 10 次的1 (510-519)。因此十位的1 的出现次数为：
 * count=round*10 + 10 = 5*10+10 = 60
 * <p>
 * 3）更高位
 * 更高位的计算方式其实是与十位的计算方式是一致的
 * <p>
 * 总结：
 * 将 n 的各个位分为两类 ：个位与其他位
 * 对个位来说：
 * 若个位大于 0 ， 1 出现的次数为 round * 1 + 1
 * 若个位等于 0 ，1 出现的次数为 round*1 + 1
 * <p>
 * 对其他位来说，记录每一位的权值为 base ，权重为 weight，该位之前的数为 former，如下图所示
 * round weight former  base=10
 * |    |      |
 * n  = 5    3      4
 * 则：
 * 若 weight = 0，则 1 出现的次数为 round * base
 * 若 weight = 1 ，则 1 出现的次数为 round * base + former + 1
 * 若 weight > 1,  则 1 出现的次数为 round * base + base
 * <p>
 * 比如 534 = (个位 1 出现的次数) + (十位 1 出现的次数) + (百位 1 出现的次数)
 * = (53*1 + 1) + (5*10 + 10) + (0*100 + 100) = 214
 * 514 = （51*1+1）+（5*10+4+1）+（0*100+100） = 207
 */
public class NumberOf1Between1AndN {
    public static void main(String[] args) {
        System.out.println(countNumbersOf1Between1AndN(534));
    }

    static int countNumbersOf1Between1AndN(int n) {
        int base = 1;
        int weight = 0;
        int count = 0;
        int round = n;
        while (round > 0) {
            weight = n % 10;
            round = round / 10;
            count += base * round;
            if(weight == 1) {
                count += (n % base) + 1;
            } else if(weight > 1) {
                count += base;
            }
            base *= 10;
        }
        return count;
    }
}
