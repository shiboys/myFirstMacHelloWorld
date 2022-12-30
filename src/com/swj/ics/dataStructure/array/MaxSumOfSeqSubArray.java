package com.swj.ics.dataStructure.array;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2022/12/30 17:49
 * 最大连续子数组之和。
 * 给定一个数组，数组里面可能有正数、负数和零。数组中连续一个或者多个正数组成一个子数组，每个子数组都有一个和
 * 求所有子数组的和为最大的值。
 * 例如，如果输入的子数组为 {1,-2,3,10,-4,7,2,-5},和的最大子数组为 {3,10,-4,7,2},那么该输出为子数组的和为 18
 */
public class MaxSumOfSeqSubArray {

    /**
     * 第一种解法，蛮力枚举。求一个数组的最大子数组和，最直观，最野蛮的方法便是，用 3 个for 循环三层遍历，求出每个子数组的和，最终求出这些数组中
     * 最大的一个值。
     * 此方法的时间复杂度为 O(n^3)
     */
    static SumAndSubArray maxSumOfSubArray(int[] array) {
        if (array == null || array.length < 1) {
            return null;
        }
        int maxSum = array[0];
        SumAndSubArray subArray = new SumAndSubArray(maxSum, 0, 0);
        for (int i = 0; i < array.length; i++) {
            for (int j = i; j < array.length; j++) {
                int currSum = 0;
                for (int k = i; k <= j; k++) {
                    currSum += array[k];
                    if (currSum > maxSum) {
                        maxSum = currSum;
                        subArray.setMaxSum(maxSum);
                        subArray.startIndex = i;
                        subArray.endIndex = k;
                    }
                }
            }
        }
        return subArray;
    }

    /**
     * 解法2：动态规划法。事实上，可以令 currSum 是以当前元素结尾的最大连续子数组的和。maxSum 为全局最大子数组和，
     * 当从前往后扫描时，对第 j 个元素有两种选择，要嘛加入当前的子数组，要嘛作为一个新数组的起始元素。如果 currSum > 0，则令 currSum + a[j]
     * 如果 currSum<0，则 currSum 被置为当前元素，即 currSum=a[j]。
     * 公式表述就是：currSum=max{0,currSum{j-1}+a[j]}，如果 currSum>maxSum,则更新 maxSum=currSum,否则 currSum 保持不变，不更新。
     * 举例来说，如果输入的是  {1,-2,3,10,-4,7,2,-5},那么 currSum 和 maxSum 的值变化如下：
     * currSum:0->1->-1->3->13->9->16->18->13
     * maxSum: 0->1->1->3->13->13->16->18->18
     */

    static SumAndSubArray getMaxSumByDynamicCalculating(int[] arr) {
        int curSum = 0;
        int maxSum = arr[0];
        //int startIndex=0;
        SumAndSubArray subArray = new SumAndSubArray(maxSum, 0, 0);
        for (int i = 0; i < arr.length; i++) {
            // 这段代码真的是玄之又玄，妙之又妙。
            if (curSum >= 0) {
                curSum += arr[i];
            } else {
                curSum = arr[i];
                subArray.startIndex = i;
            }

            if (curSum > maxSum) {
                maxSum = curSum;
                subArray.setMaxSum(maxSum);
                subArray.endIndex = i;
            }

        }
        return subArray;
    }

    public static void main(String[] args) {
        int[] arr = {1, -2, 3, 10, -4, 7, 2, -5};
        SumAndSubArray subArray = maxSumOfSubArray(arr);
        System.out.println(subArray);
        subArray = getMaxSumByDynamicCalculating(arr);
        System.out.println(subArray);
        /**
         * MaxSumOfSeqSubArray.SumAndSubArray(maxSum=18, startIndex=2, endIndex=6)
         * MaxSumOfSeqSubArray.SumAndSubArray(maxSum=18, startIndex=2, endIndex=6)
         */
    }

    @AllArgsConstructor
    @Data
    @NoArgsConstructor
    static class SumAndSubArray {
        public int maxSum;
        public int startIndex;
        public int endIndex;
    }
}
