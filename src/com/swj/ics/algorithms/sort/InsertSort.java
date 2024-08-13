package com.swj.ics.algorithms.sort;

/**
 * Created by swj on 2018/3/27.
 * 插入排序：假设数据列(D0,D1,D2...Dn)中的数据(Di)之前的数据列(D0~Di)是已经排序的部分
 * 那么把Di按照大小关系升序或者降序插入到已排序的数列之中，以此完成插入的方法就是插入排序
 * 插入排序的算法核心就是不断的处把数据插入到已经排序的数据列中的恰当位置。
 * 排序过程中的数组分成 已排序(前半部分)和待排序。在开始之前，已排序的只包括数组的起始元素，
 * 而待排序的包括第2个以及其以后的所有元素
 * 
 */
public class InsertSort {

    public static void main(String[] args) {
        int[] arr ={ 9,8,5,6,2,7,1,3};
        insertSort(arr);
        for(int i=0,len = arr.length;i < len;i++) {
            System.out.print(arr[i]+",");
        }
        System.out.println();
    }

    static void insertSort(int[] arr) {
        if(arr == null || arr.length <= 1) {
            return ;
        }
        //第一个为已经排序的，从第二个开始，已经将后面的元素插入到前面已经排序好的
        for (int i=1; i < arr.length;i++) {
            insertToRightPosition(arr,i);
        }
    }
    /**
     * 
     * @param arr 当前数组，可能已部分排序，可能还没排序
     * @param i 需要被排序的位置，待排序的元素是从原始数组的第二个开始，所以这里的i应该是>=1的
     */
    static void insertToRightPosition(int[] arr,int i) {
        int j = i -1; //j初始化为 待排序的前面的一个元素
        int inserted = arr[i];//待排序的数据
        //如果 当前已经排好序的数值有大于当前待排序的，将已经排好序的元素全部向后移动一位，
        for( ;j>=0 && arr[j] > inserted; j--) {
            arr[j+1] = arr[j];            
        }
        //此时的j为小于 insert的元素或者 j =-1 表示到达了首位
        //则j + 1 为insert需要插入的元素
        arr[j+1] = inserted;
    }
}
