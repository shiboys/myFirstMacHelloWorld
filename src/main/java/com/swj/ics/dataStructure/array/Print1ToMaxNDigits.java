package com.swj.ics.dataStructure.array;

import java.util.Arrays;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/01/02 15:09
 * 题目：输入数字 n，按照顺序打印从 1 到 n 位十进制数。比如输入3，则打印出 1，2，3 一直到最大的 3 位数 999
 */
public class Print1ToMaxNDigits {

    public static void main(String[] args) {
        // print1ToMaxNDigits(2);
        print1ToMaxNDigits2(2);
    }

    // 从 1 到 n 的循环，这里就不再演示了。复杂度为 O(10^n)，这里我们使用 字符串模拟数字自增以及进位的方式
    // 实现复杂度为 10*O(n) = O(n) 的复杂度解法

    static void print1ToMaxNDigits(int n) {
        if (n < 1) {
            System.out.println(String.format("n is illegal. [n=%d]", n));
            return;
        }
        char[] numbers = new char[n + 1];
        Arrays.fill(numbers, 0, n, '0');
        numbers[n] = '\0'; // ascii 为 0
        while (increment(numbers)) {
            printNumber(numbers);
        }
    }

    /**
     * 实现字符串模拟数字的递增，
     *
     * @param numbers 每次调用，在原 numbers 的基础上用字符数组模拟实现 + 1。numbers数组的每个成员 必须都初始化为字符 '0'
     * @return 如果自增不成功-溢出则返回 false，否则返回 true。
     */
    static boolean increment(char[] numbers) {
        boolean isOverflow = false;
        int numberCounter = 0;// 数字自增器
        int nTakeOver = 0; // 进位数，比如 9 +1 进 1 位。
        //从 数字的低位，但是数组的高位开始遍历字符数组。数组长度最低为2 ，多出的一位用来存储溢出/进位的数据
        // 最后是结束符，其他位置是数字的字符表现形式
        for (int i = numbers.length - 2; i >= 0; i--) {
            numberCounter = numbers[i] - '0' + nTakeOver;
            if (i == numbers.length - 2) {
                numberCounter++; // 默认个位数递增
            }
            if (numberCounter >= 10) {
                if (i == 0) { // i ==0 说明遍历到最高位，此时最高位还需要进位，则说明超过最大的10进制的 n 个数
                    isOverflow = true;
                } else { // 需要进位
                    numberCounter -= 10;
                    numbers[i] = (char) (numberCounter + (int) '0');
                    nTakeOver = 1;
                    // 注意这里因为进位了，需要计算上一位，必须再次进入循环，因此这里必须不能 break;
                }
            } else {
                // 默认情况下，只进行一次个位的 ++ 计算就行
                numbers[i] = (char) (numberCounter + (int) '0');
                break;
            }
        }
        return !isOverflow;
    }

    static void printNumber(char[] numbers) {
        if (numbers == null || numbers.length == 0) {
            System.out.println("numbers is empty");
            return;
        }
        boolean isFirstNoneZero = true;
        for (int i = 0, len = numbers.length; i < len; i++) {
            if (numbers[i] != '0' && isFirstNoneZero) {
                isFirstNoneZero = false;
            }
            if (!isFirstNoneZero) {
                System.out.print(numbers[i]);
            }
        }
        System.out.println();
    }

    // 方法2 递归，方法3 手抄一遍

    /**
     * 上述思路虽然比较直观，但是由于模拟了整数的加法，代码有点长。要在面试的几十分钟里面完成，不是一件容易的事。
     * 接下来我们换一种思路来考虑这个问题。如果我们在数字前面补 0，就会发现 n 位所有十进制其实就是 n 个从 0 到 9 的全排列。也就是说，我们把
     * 数字的每一位都从 0 到 9 排列一遍，就得到了所有的 10 进制数。只是在打印的时候，排在前面 0 就不打印出来。
     * 全排列用 递归 很容易表达，数字的每一位都可能是 0-9 中的一个数，然后设置下一位。递归结束的条件是我们已经设置了数字的最后一位。
     */

    static void print1ToMaxNDigits2(int n) {
        if (n < 1) {
            System.out.println("n is illegal. n=" + n);
            return;
        }
        char[] numbers = new char[n + 1];
        Arrays.fill(numbers, 0, n, '0');
        numbers[n] = '\0';
        for (int i = 0; i < 10; i++) {
            numbers[0] = (char) (i + '0');
            print1ToMaxNDigitsRecursively(numbers, n, 0);
        }
    }

    private static void print1ToMaxNDigitsRecursively(char[] numbers, int length, int index) {
        if (index == length - 1) {
            // numbers 各个位置都已经被赋值了
            printNumber(numbers);
            return;
        }
        // 打印 0-9 的数字
        for (int i = 0; i < 10; i++) {
            // 设置 10 位数，
            numbers[index + 1] = (char) (i + '0');
            // 递归设置 百位数，千位数，万位数 ...
            print1ToMaxNDigitsRecursively(numbers,length,index + 1);
        }
    }
}
