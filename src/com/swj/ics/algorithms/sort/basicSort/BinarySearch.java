package com.swj.ics.algorithms.sort.basicSort;

import java.util.Arrays;

/**
 * author shiweijie
 * date 2018/4/1 下午10:00
 */
public class BinarySearch {

    public static int searchNum(int[] arr,int num) {
        int left = 0,right = arr.length - 1;

        while (left <= right) {
            int middle = left + (right -left) /2;
            if (num == arr[middle]) {
                return middle;
            } else if (arr[middle] > num) {
                right = middle - 1;
            } else {
                left = middle + 1;
            }
        }
        return -1;
    }

    /**
     * 二分查找递增递减的最大值
     * @param arr
     * @param num
     * @return
     */

    public static int searchIncreaseDecreaseNum(int[] arr,int num) {
        int begin = 0,end= arr.length - 1,middle = begin + (end - begin) / 2;
        while (middle >0 && middle < arr.length - 1) { //确保 middle处于 arr之间
            if(arr[middle] > arr[middle - 1] && arr[middle] > arr[middle + 1]) {
                return middle;
            } else if (arr[middle] < arr[middle + 1]) {
                begin = middle + 1;
                middle = begin + (end - begin) / 2;
            } else {
                end = middle - 1;
                middle = begin + (end - begin) / 2;
            }
        }
        return -1;
    }

    public static int binarySearchRecv(int[] arr,int key) {
        if (arr == null || arr.length < 1) {
            return -1;
        } else if (arr.length == 1) {
            return 0;
        }

        int middle = binarySearchRecv(arr,key,0,arr.length - 1);
        if (arr[middle] == key) {
            return middle;
        }
        return -1;
    }

    static int binarySearchRecv(int[] arr,int key,int low,int high) {
        if (low >= high) {
            return low;
        }
        int middle = low + (high - low) / 2;
        if (key > arr[middle]) {
            low = middle + 1;
        } else {
            high = middle - 1;
        }
        return binarySearchRecv(arr,key,low,high);
    }

    /**
     * 递归打印 奇妙数字1
     */
    static void printRecursiveDemo() {
        int[] arr = new int[10];
        for(int i = 0; i < 10; i++) {
            arr[i] = 9 - i;
        }
        for(int i = 0;i < 10;i++ ) {
            arr[i] = arr[arr[i]];
        }
        for(int i = 0; i < arr.length ; i++) {
            System.out.print(arr[i]+" ");
        }
        System.out.println();
    }

    static String exR1(int n) {
        if (n <= 0 ) {
            return "";
        }
        return exR1(n - 3) + n + exR1(n - 2) + n;
    }

    static int fibLoop(int n) {
        int sum = 0;
        if (n  <= 0) {
            sum = 0;
        } if(n  == 1) {
            sum = 1;
        }
        int first = 0;
        int second = 1;
        int temp = 0;
        for(int i = 2;i < n;i++) {
            temp = second;
            second = first + second;
            first = temp;
        }
        return second;
    }

    static String mystery(String s) {
        if (s == null) {
            return "";
        }
        int len = s.length();
        if (len <= 1 ) {
            return s;
        }
        String a = s.substring(0,len / 2);
        String b = s.substring(len / 2 ,len);
        return mystery(b) + mystery(a);
    }

    public static void main(String[] args) {
        /*int[] arr = {10,12,8,2,3,5,13,6};
        Arrays.sort(arr);
        for(int i = 0; i < arr.length ; i++) {
            System.out.print(arr[i]+" ");
        }
        System.out.println();

        int key = 5;
        int pos = binarySearchRecv(arr,key);
        System.out.println(" key = " + key +",pos is " + pos);*/

        printRecursiveDemo();

        //System.out.println(exR1(6));
        // 0 1 1 2 3 5 8 13
        //System.out.println(fibLoop(8));
       // System.out.println(mystery("hello,swj"));
    }

}
