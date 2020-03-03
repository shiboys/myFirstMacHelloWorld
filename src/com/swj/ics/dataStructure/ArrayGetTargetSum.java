package com.swj.ics.dataStructure;

/**
 * Created by swj on 2020/2/23.
 * 给定一个整数数组和一个整数，返回两个数组的索引，这两个索引指向的数字之和等于这个指定的整数。
 * 需要最优算法，分析算法的复杂度
 */
public class ArrayGetTargetSum {
    
    public int[] getTargetSumIndex(int[] arr,int target) {
        if(arr==null || arr.length < 1) {
            return null;
        }
        int i=0;
        int j = arr.length -1;
        while (i != j) {
            if (arr[i] + arr[j] == target) {
                return new int[] {i,j};
            } else if(arr[i] + arr[j] < target) {
                i++;
            } else if(arr[i]+ arr[j] > target) {
                j--;
            }
        }
        return null;
    }
    
}
