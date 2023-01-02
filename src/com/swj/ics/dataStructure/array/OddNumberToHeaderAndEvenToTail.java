package com.swj.ics.dataStructure.array;

import java.util.Arrays;
import java.util.function.Function;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/01/02 23:25
 * 题目：输入一个整数数组，实现一个函数来调整该数组中的数字的舒心，使得所有的奇数位于数组的前半部分，所有的偶数
 * 位于数组的后半部分
 */
public class OddNumberToHeaderAndEvenToTail {
    static void adjustSeqOfOddAndEvenNum(int[] arr) {
        int start = 0;
        int end = arr.length - 1;
        while (start < end) {
            while ((arr[start] & 1) == 1) {
                start++;
            }
            while ((arr[end] & 1) == 0) {
                end--;
            }
            if (start < end) {
                swap(arr, start, end);
            }
        }
    }


    static void adjustSeqOfOddAndEvenNum2(int[] arr, Function<Integer, Boolean> startStepTail, Function<Integer, Boolean> endStepHead) {
        int start = 0;
        int end = arr.length - 1;
        while (start < end) {
            while (startStepTail.apply(arr[start])) {
                start++;
            }
            while (endStepHead.apply(arr[end])) {
                end--;
            }
            if (start < end) {
                swap(arr, start, end);
            }
        }
    }

    static void swap(int[] arr, int i, int j) {
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }

    //boolean


    public static void main(String[] args) {
        int[] arr = {2, 4, 6, 8, 10, 1, 3, 5, 7, 9, 11};
        int[] arr2 = Arrays.copyOf(arr, arr.length);
        adjustSeqOfOddAndEvenNum(arr);
        System.out.println(Arrays.toString(arr));
        // todo：改为更通用的前半部分和后半部分
        adjustSeqOfOddAndEvenNum2(arr2, i -> (i & 1) == 1, j -> (j & 1) == 0);
        System.out.println(Arrays.toString(arr2));
    }
}
