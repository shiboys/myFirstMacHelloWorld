package com.swj.ics.algorithms;

/**
 * @author shiweijie
 * @date 2020/4/13 下午10:41
 * 二进制中的1的个数
 */
public class TheCountOfOneInBinaryFormat {
    /**
     * 分析：
     * 首先来分析n与n-1的差别
     * 如果n != 0,那么其二进制中至少有1个1
     * 如果n的最低位是1(奇数)，那么n-1正好把这个最低位变成0，其他不变
     * 如果n的最低位是0（偶数），那么假设从右开始数的第一个1位于m位，也即m之后全部是0，那么n-1的m位由1变成0，而m位之后的
     * 所有0都将变成1，m位之前的所有位均保持不变
     * 因此我们通过分析发现：ps:这个发现很牛逼
     * 把一个整数n减去1，再和原来的整数做与(&)运算，会把该整数最右边的1变成0，那么该整数有多少个1，就会进行多少次(&)运算。
     * 循环的次数，就是二进制中1的个数
     */
    public int numberOf1(int n) {
        int count =0;
        while (n != 0) {
            n = (n & n-1);
            count++;
        }
        return count;
    }

    public static void main(String[] args) {
        TheCountOfOneInBinaryFormat instance = new TheCountOfOneInBinaryFormat();
        System.out.println(instance.numberOf1(1));
        System.out.println(instance.numberOf1(2));
        System.out.println(instance.numberOf1(3));
        System.out.println(Integer.toString(Integer.MIN_VALUE,2));
        System.out.println(instance.numberOf1(Integer.MIN_VALUE));
        System.out.println(Integer.toString(Integer.MAX_VALUE,2));

        System.out.println(instance.numberOf1(Integer.MAX_VALUE));
    }
}

