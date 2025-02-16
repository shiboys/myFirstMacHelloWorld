package com.swj.ics.algorithms.sort.heapSort;

import java.util.Random;

/**
 * author shiweijie
 * date 2019/6/10 下午7:49
 */
public class HeapSort {
    /**
     * 题目：如何在10亿大的数据中找出前1000大的数据
     * 解决：使用堆排序的方式，建立大顶堆，先对前1000的数据建立大顶堆，然后对从1001开始的数据，分别跟堆顶的数据比较，
     * 如果小于堆顶的数据，则直接舍弃，否则跟堆顶数据交换，然后至上而下重建堆。
     */

    public static void main(String[] args) {
        int count = 100;
        Random random = new Random();
        int[] arr = new int[count];
        for(int i=0;i<count;i++) {
            arr[i]=random.nextInt(100);
        }

        int heapSize =10;
        TopN top100  = new TopN();
        top100.findTopN(heapSize,arr);
        top100.printData(arr);
    }
}
