package com.swj.ics.algorithms.sort.basicSort;

/**
 * author shiweijie
 * date 2018/4/1 下午6:32
 * 选择排序，中心思想：
 * 将 一个集合里面的最小的元素放在第一个，第二小的放在第二个，。。。第N小的放在第N个。
 * 第一遍寻找最小的放在第一位，第二遍寻找最小的放在第二位
 */
public class selectSort {

    public static void main(String[] args) {

        int[] arr= {6,1,2,7,13,4,55,10,8};

        //selectSort(arr);
        bubbleSort(arr);
        for(int i=0,len = arr.length;i < len;i ++) {
            System.out.print(arr[i]+",");
        }

        System.out.println();

        int num = 2;
        int index = BinarySearch.searchNum(arr,num);

        if (index >=0 ) {
            System.out.println(num +" 的index 是 " + index);
        } else {
            System.out.println("arr 不包含 " + num);
        }

        num = 10;
        index = BinarySearch.searchNum(arr,num);

        if (index >=0 ) {
            System.out.println(num +" 的index 是 " + index);
        } else {
            System.out.println("arr 不包含 " + num);
        }
    }


    static void selectSort(int[] arr) {

        if (arr == null || arr.length < 1) {
            return ;
        }

        for(int i=0;i<arr.length - 1 ;i++) {
            int minIndex = i;
            for (int j=i+1;j<arr.length;j++) {
                if(arr[j] < arr[minIndex]) {
                    minIndex = j;
                }
            }
            if (minIndex == i) {
                continue;
            }
            swap(arr,i,minIndex);
        }

    }

    static void swap(int[] arr,int xIndex,int yIndex) {
        int temp = arr[xIndex];
        arr[xIndex] = arr[yIndex];
        arr[yIndex] = temp;
    }


    /**
     * 冒泡排序，又叫交换排序，核心思想：
     * 每次冒泡，相邻的彼此进行交换，如果前面大于后面，则交换，这样第一趟排序下来，就可以确定第一个最大的排到后面
     * 以此类推。因此后面的n-i序列是排好的。
     * @param arr
     */
    static void bubbleSort(int[] arr) {
        if (arr == null || arr.length < 1) {
            return ;
        }

        for(int i=0;i< arr.length - 1;i++) {
            for(int j=0;j < arr.length - 1 - i; j++) {
                if (arr[j] > arr[j+1]) {
                    swap(arr,j,j+1);
                }
            }
        }
    }
}
