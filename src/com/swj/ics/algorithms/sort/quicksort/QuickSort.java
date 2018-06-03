package com.swj.ics.algorithms.sort.quicksort;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * author shiweijie
 * date 2018/3/26 下午5:28
 */
public class QuickSort {

    public static void main(String[] args) {
        /*
        * 100的二进制为 64+32+4 = 1100100 % 32 = 4 == 1100100 & 11111
        * */

        int[] arr= {6,1,2,7,3,4,5,10,8};
        quickSort(arr);
        System.out.println();
        for(int i=0,len = arr.length;i < len;i ++) {
            System.out.print(arr[i]+",");
        }


        int[] arr2 = {3,1,2,7,3,4,5,10,8};
        quickSort(arr);
        for(int i=0,len = arr.length;i < len;i ++) {
            System.out.print(arr[i]+",");
        }
        System.out.println();
        int[] arr3 = {1,2,7,3,4,5,10,8,6};
        quickSort(arr);
        for(int i=0,len = arr.length;i < len;i ++) {
            System.out.print(arr[i]+",");
        }
        System.out.println();



    }

    static void quickSort(int[] arr) {
        quickSortRecr(arr,0,arr.length -1);

    }

    static void quickSortRecr(int[] arr,int left,int right) {
        //快速排序，核心思想，设定一个令牌，比如第一个为令牌，第一趟比较，然后先从最右边向左比较，遇到比令牌小的元素停住
        //然后 最左边的 i 指针开始向右移动，遇到一个比令牌大的元素，停住。
        //此时如果 i < j 则交换 i ,j ，i 和 j 继续相向而行，直到i和j相遇
        //第一趟比较排序结束以后，需要在i和j相遇的地方 跟令牌交换，交换完成之后，这才满足小于令牌的都在令牌的左边，大于令牌的都在令牌的右边

        //然后是分治递归，将 i 左边的和右边的分别递归进行快速排序，递归的出口是 left >= right

        if (left >= right) {
            return ;
        }

        int temp = arr[left];
        int i = left,j = right,t;
        while (i < j) {
            while (arr[j] >= temp && i < j) { //找到大于令牌的数值，然后停下
                j --;
            }
            while(arr[i] <= temp && i < j) {
                i ++;
            }
            if (i < j) { // i 和 j 相遇之前，都找到了相应的位置，则交换i和j
                t = arr[i];
                arr[i] = arr[j];
                arr[j] = t;
            }

        }
        //此时 i == j ,交换i位置和令牌的位置
        arr[left] = arr[i];
        arr[i] = temp;

        quickSortRecr(arr,left,i-1);
        quickSortRecr(arr,i+1,right);
    }




    /*
    * if (left >= right) {
            return ;
        }
        int temp = arr[left]; //设置哨兵
        int i = left,j = right,t;
        while(i<j) {
            while(arr[j] >= temp && i < j) {//j先走 ,找到一个比令牌小的元素，停住
                j--;
            }
            while(arr[i] <= temp && i < j) {
                i++;
            }
            //经过j--和i++，此时，arr[j] <= temp & arr[i] >= temp,交换 i j
            if (i < j) {
                t = arr[i];
                arr[i] = arr[j];
                arr[j] = t;
            }

        }
        //经过第一轮的compare and swap 之后 此时 i == j . i和j 走到一起。将 arr[i] 和令牌交换数据
        arr[left] = arr[i];
        arr[i] = temp;

        //令牌左边和右边分别递归

        quickSortRecr(arr,left,i -1);
        quickSortRecr(arr,i+1,right);
    * */
}

