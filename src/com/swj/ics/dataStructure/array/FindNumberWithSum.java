package com.swj.ics.dataStructure.array;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/01/06 13:58
 * 题目一：和为 s 的两个数
 * 输入一个递增排序的数组和一个数字 s，在数组中找到两个数，使得他们的和正好是 s。如果有各个数字的和等于 s，则输出
 * 任何一对即可。
 * 题目二：和为 s 的连续整数序列
 * 输入一个整数 s， 打印出所有和为 s 的连续正数序列（至少包含两个数）。例如，输入 15，由于 1+2+3+4+5=4+5+6=7+8=15，所以打印出 3 个连续的
 * 序列 1~5，4~6和7~8
 */
public class FindNumberWithSum {
    /**
     * 仔细观察题目一的需求和条件，任意两个数和 为 s，递增排序，我们可以联想到 左右两个指针，向中间靠拢
     * 如果前后的和 大于 s，则需要减小，也就是 tail--，反之 则 head++
     */

    public static void main(String[] args) {
        int[] arr = {1, 3, 5, 7, 9, 11};
        PairSumEqualANumber targetPair = getTargetPair(arr, 20);
        if (targetPair != null) {
            System.out.println(targetPair);
        } else {
            System.out.println("not found");
        }
        int[] arr2 = {1, 2, 3, 4, 5, 6, 7, 8};
        int sum = 15;
        List<SubSequenceNumber> numberList = getSeqNumbersWhenSumEqualsTargetNumber(arr2, sum);
        if (numberList != null) {
            numberList.forEach(System.out::println);
        } else {
            System.out.println("numberList is empty");
        }
    }

    static PairSumEqualANumber getTargetPair(int[] arr, int sum) {
        if (arr == null || arr.length < 2) {
            return null;
        }
        int head = 0;
        int tail = arr.length - 1;
        while (head < tail) {
            int curSum = arr[head] + arr[tail];
            if (curSum == sum) {
                return new PairSumEqualANumber(arr[head], arr[tail]);
            } else if (curSum > sum) {
                tail--;
            } else {
                head++;
            }
        }
        return null;
    }

    /**
     * @param arr 源数组
     * @param sum 和为目标数字的 s
     * @return 和为 s 的一系列子序列。
     * 为了实现找到所有的子序列，先从最小的数字第0 位和第 1 位开始。分别使用 small 和 big 两个变量分别初始为 1 和 2.
     * 如果从small 到 big 的序列和如果大于 s，则可以去掉序列中较小的数 sum-small，同时 small 向前推进 small ++
     * 如果从 small 到 big 的序列和如果小于 s，则可以增加较大的数，也就是 big++，sum+=big
     * 因为这个序列要至少包含 2 个数字，我们一直增加 small 到 (1+s)/2 为止。
     */
    static List<SubSequenceNumber> getSeqNumbersWhenSumEqualsTargetNumber(int[] arr, int sum) {
        if (sum < 3) {
            return null;
        }
        int small = 1;
        int big = 2;
        int curSum = big + small;
        int middle = (sum + 1) / 2;
        List<SubSequenceNumber> numberList = new ArrayList<>();
        while (small < middle) {
            if (curSum == sum) {
                numberList.add(new SubSequenceNumber(small, big));
            }
            while (curSum > sum) {
                curSum -= small;
                small++;
                if (curSum == sum) {
                    numberList.add(new SubSequenceNumber(small, big));
                }
            }
            big++;
            curSum += big;
        }
        return numberList;
    }

    @Data
    @AllArgsConstructor
    static class PairSumEqualANumber {
        public int number1;
        public int number2;
    }

    @Data
    @AllArgsConstructor
    static class SubSequenceNumber {
        public int small;
        public int big;

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(16);
            for (int i = small; i <= big; i++) {
                sb.append(i);
                if (i != big) {
                    sb.append(",");
                }
            }
            return sb.toString();
        }
    }
}
