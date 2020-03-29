package com.swj.ics.dataStructure.array;

/**
 * @author shiweijie
 * @date 2020/3/25 下午11:35
 * 升降序列，查找 求先递增后递减数组最大值的下标
 */
public class AscendingDescendingArray {

    public int getMaxNumByLoopN(int[] arr) {
        if (arr == null || arr.length < 1) {
            return -1;
        }
        if (arr.length == 1) {
            return 0;
        }
        //单调递减
        if (arr[0] > arr[1]) {
            return 0;
        }
        int lastIndex = arr.length - 1;
        //单调递增
        if (arr[lastIndex] > arr[lastIndex - 1]) {
            return lastIndex;
        }
        for (int i = 0; i < lastIndex; i++) {
            if (arr[i + 1] < arr[i]) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 数组单调递增. 因为题目要求数组"先递增后递减", 如果是"单调递增"的话, 那么最后一个元素,
     * 肯定大于倒数第二个元素. 2, 数组单调递减. 同第一种情况的分析, "单调递减"的该数组, 肯定满足条件a[0]>a[1]
     * 第一种方法是O(n)的方法
     */

    /**
     * 第二种方法是使用二分查找的方式。对于有序数组，二分查找是最容易想到的
     */
    public int getMaxNumIndexByBinarySearch(int[] arr) {
        if (arr == null || arr.length < 1) {
            return -1;
        }
        if (arr.length == 1) {
            return 0;
        }
        //单调递减
        if (arr[0] > arr[1]) {
            return 0;
        }
        int lastIndex = arr.length - 1;
        //单调递增
        if (arr[lastIndex] > arr[lastIndex - 1]) {
            return lastIndex;
        }
        int i = 0,j = arr.length -1;
        int mid =0;
        while (i < j) {
            mid = i + (j-i) /2;
            if(arr[mid] > arr[mid-1] && arr[mid] > arr[mid +1] ) {
                return mid;
            } else if(arr[mid] > arr[mid -1]) {
                //处于上坡阶段
                i = mid + 1;
            } else if(arr[mid] > arr[mid + 1]) {
                //处于下坡阶段
                j = mid -1;
            }
        }
        return -1;
    }

    public static void main(String[] args) {
        AscendingDescendingArray instance = new AscendingDescendingArray();
        int[] arr = new int[] {-9833421,-123876,-5201,-398,0, 12,888,212398,19823123,98712,9831,245,22, 3};
        int index = instance.getMaxNumIndexByBinarySearch(arr);
        System.out.println("index = "+index+",max num = " + arr[index]);
    }
}
