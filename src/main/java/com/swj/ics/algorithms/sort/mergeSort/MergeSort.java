package com.swj.ics.algorithms.sort.mergeSort;

/**
 * author shiweijie
 * date 2018/3/26 下午11:265
 * 所谓归并排序就是将带排序的数分成两半后排好序，然后再将两个排好序的序列合并成一个有序序列。
 */
public class MergeSort {

    public static void main(String[] args) {
        int[] arr= {6,1,2,7,3,4,5,10,8};

        mergeSort(arr);
        for(int i=0,len = arr.length;i < len;i ++) {
            System.out.print(arr[i]+",");
        }
    }

    //对于合并，其实就是不断从两个有序数组中比较小的那个放在一个辅助数组中，直到两个有序数组的元素全部取完。
    //归并排序以 O(NlogN)最坏情况运行，而使用的比较次数几乎是最优的，它是递归算法的一个好的示例。

    static void mergeSort(int[] arr) {

        int[] tempArr = new int[arr.length];
        mergeSortInternal(arr,tempArr,0,arr.length -1);

    }

    static void mergeSortInternal(int[] arr,int[] tempArr,int left,int right) {
        //left == right 的时候，数组只有一个元素，就递归到数组只有一个元素，递归终止
        if (left < right) {
            //将数组一分为二
            int middle = left + (right - left)/2;
            //将左边的数组分治为最少的一个元素，然后排序
            mergeSortInternal(arr,tempArr,left,middle);
            //对右边的子数组进行排序
            mergeSortInternal(arr,tempArr,middle+1,right);
            //合并两个有序数组 // left + 1 >= right
            merge(arr,tempArr,left,middle,right);
        }
    }

    /**
     * 将arr[left..middle] 和 arr[middle+1...right]两个有序数组合并为一个有序数组
     * @param arr
     * @param tempArr
     * @param left
     * @param middle
     * @param right
     */
    static void merge(int[] arr,int[] tempArr,int left,int middle,int right) {
        int i = left,j = middle + 1;
        //先通过比较，将两个有序数组合并成一个有序数组，结果暂放到temp数组中。
        for(int k = left;k <= right; k++) {
            //如果 arr[left..middle]中的元素已经取完，也即是 i> middle,则直接copy右边数组的元素到临时数组。同时右边数组也同理。
            if (i > middle) {
                tempArr[k] = arr[j++];
            } else if (j > right) {
                tempArr[k] = arr[i++];
            } else if (arr[i] < arr[j]) { //左右两边均为达到临界位置
                tempArr[k] = arr[i++];
            } else {
                tempArr[k] = arr[j++];
            }
        }

        //合并完两边之后，需要把排好序的数组复制到原数组中
        for(int k = left; k <= right;k++) {
            arr[k] = tempArr[k];
        }
    }
}
