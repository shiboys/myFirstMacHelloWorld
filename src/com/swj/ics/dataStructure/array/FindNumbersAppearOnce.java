package com.swj.ics.dataStructure.array;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/01/06 09:21
 * 题目1：题目中只出现一次的两个数
 * 一个整形数组里除了 2 个数字外，其他数字都出现了 2 次。请写程序找出这两个只出现一次的数字。
 * 要求时间复杂度是 O(n)，空间复杂度是O(1)
 * 个人心得：我刚看到这个题目的时候，想到的还是用 hash 表来存储出现次数，但是使用hash 表的话，空间复杂度就是 O(n)，不符合题目要求
 * O(n) 的复杂度，那就是一遍 for 循环呀？一遍 for 循环就能记住那个数字出现了1次？那必须得有个地方能存储某个数字及其出现的次数，
 * 闲了半天是在想不起来。没办法，继续看书，参考作者的思路，发现作者说使用异或位运算，同一个数字跟自己进行异或运算结果为 0 ，
 * 异或运算我知道，但是异或运算跟出现次数结合起来，卧槽，打死我也想不起来，主要还是工作中尼玛几乎没有用过异或运算。
 * 但是还有一个问题尚未解决，对一个数组的只有 2 个元素出现了 2次，其他元素都只出现 1 次的数组每一个元素进行异或运算，最终结果只能是一个元素，怎么
 * 才能把这 2 个元素给筛选出来呀？
 * 继续往下看作者的思路：
 * 我们从头到尾一次异或数组中的每一个数字，那么最终得到的结果是两个只出现了一次的数字的抑或结果，因为其他数字都只出现了 2 次，在异或运算中都抵消了。
 * 由于这两个数字都不一样，那么抑或的结果肯定不为 0，也就是说，在这个结果的二进制表示中肯定至少有一位是 1 。我们在结果数字中寻找第一个为 1 的位的位置
 * 记为 n 位。现在我们以第 n 位是不是 1 的准则将原数组的元素分成两个虚拟自数组。第一个虚拟自数组的第 n 位都是1，第二个虚拟子数组的第 n 位都是0
 * 由于我们分组的标准是第 n 位是是否是 1，那么出现两次的数字肯定会被分配到同一个虚拟子数组中，每个子数组都只包含了出现一次的数字，而其他数字都出现了 2 次。
 * 我们已经知道如如何在一个数组中找出只出现 1 次的数字，因此，到目前位为止，题目1 的整体思路已经梳理完毕。
 */
public class FindNumbersAppearOnce {

    public static void main(String[] args) {
        /*int a = 3;
        int b = 13;
        swapWithoutTemp(a, b);*/
        int[] arr = {2, 4, 3, 6, 3, 2, 5, 5};
        int[] arr2 = {3, 4, 5, 6, 7, 8, 9, 9, 8, 7, 6, 5, 4, 3, 2, 20};
        AppearOnceNumber appearOnceNumber = doFindNumbersAppearOnce(arr);
        System.out.println(appearOnceNumber);
        appearOnceNumber = doFindNumbersAppearOnce(arr2);
        System.out.println(appearOnceNumber);
        int[] arr3 = {1, 1, 1, 5130, 4, 4, 4, 7, 7, 7, 10, 10, 10};
        System.out.println(findNumberAppearingOnceWithDuplicate3Numbers(arr3));
        /**
         * FindNumbersAppearOnce.AppearOnceNumber(number1=4, number2=6)
         * FindNumbersAppearOnce.AppearOnceNumber(number1=20, number2=2)
         */
    }

    static AppearOnceNumber doFindNumbersAppearOnce(int[] arr) {
        int xorVal = 0;
        for (int val : arr) {
            xorVal ^= val;
        }
        if (xorVal == 0) {
            System.out.println("arr is illegal for the requirement of only 2 numbers appear once");
            return null;
        }
        int firstBit1Index = findFirstBit1Index(xorVal);
        int num1 = 0, num2 = 0;
        for (int val : arr) {
            if (isFirst1IndexNumber(firstBit1Index, val)) {
                num1 ^= val;
            } else {
                num2 ^= val;
            }
        }
        return new AppearOnceNumber(num1, num2);
    }

    private static boolean isFirst1IndexNumber(int first1BitIndex, int val) {
        return ((val >> first1BitIndex) & 1) == 0;
    }

    private static int findFirstBit1Index(int num) {
        int index = 0;
        while ((num & 1) == 0 && index < Integer.SIZE) {
            num = num >> 1;
            index++;
        }
        return index;
    }

    static void swapWithoutTemp(int a, int b) {
        a = a ^ b;
        b = a ^ b;
        a = a ^ b;
        System.out.println("a=" + a + ",b=" + b);
    }

    @AllArgsConstructor
    @Data
    static class AppearOnceNumber {
        public int number1;
        public int number2;
    }

    /**
     * 题目2：数组中唯一只出现 1 次的数字。
     * 在一个数组中除了一个数字只出现一次之外，其他数字都出现了 3 次。请找出那个只出现一次的数字。
     * 在前面的介绍中，对所有的数组元素进行一遍异或操作可以获取只出现一次的元素。
     * 但是这个思路不能解决这里的问题，因为 3 个相同的数字的异或结果还是该数字。尽管这里我们不能应用异或运算，但是我们还是可以使用位运算的思路
     * 如果一个数字出现三次，那么它的二进制表示的每一位（0 or 1）也出现 3次。如果把所有出现 3 次的数字的二进制表示的每一位都分别加起来，那么
     * 每一位的和都能被 3 整除。
     * 我们把数组中所有的数字的二进制表示的每一位都叫加起来。如果某一位的和能被 3 整除（0+0+0+0 或者 1+1+1+0或者 6+0）,那么那个只出现一次的数字二进制表示中的对应的那一位
     * 是 0，否则就是 1。[1+1+1+1 %3 =1 或者 6+1 %3]
     */

    static int findNumberAppearingOnceWithDuplicate3Numbers(int[] arr) {
        if (arr == null || arr.length < 1) {
            throw new IllegalArgumentException("arr is empty");
        }
        /**
         * 1、把数组中所有数字的二进制数据之和放入到一个 32 长度的整形数组中。
         * 2、利用该 32 长度的整形数组，将改数字还原出来
         */
        int[] bitNumberArr = new int[32];
        for (int val : arr) {
            int bitMask = 1;
            // 使用数组模拟一个数字，数组高的index 存储数字低位数
            for (int j = 31; j >= 0; j--) {
                if ((val & bitMask) != 0) {
                    bitNumberArr[j]++;
                }
                bitMask = bitMask << 1;
            }
        }
        // 将目标数字还原
        int result = 0;
        for (int i = 0; i < 32; i++) {
            // 先左移位，再加 0 加 1
            result = result << 1;
            if (bitNumberArr[i] % 3 == 1) {
                result++;
            }
            // result += bitNumberArr[i] % 3;
        }
        return result;
    }

}
