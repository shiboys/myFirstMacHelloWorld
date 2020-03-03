package com.swj.ics.dataStructure;

/**
 * Created by swj on 2020/3/3.
 * 题目：在一个二维数组中，每一行都是按照从左到右递增的顺序排序，每一列都是按照从上到写的顺序递增排序，
 * 请完成一个函数，输入这样一个二维数组和一个整数，判断数组中是否包含该整数。
 * 例如下面二维数组，每行每列都递增，如果在这个数组中查找数字6，则返回true，如果查找数字5，则返回false
 * <p>
 * 1    2   8   9
 * 2    4   9   12
 * 4    7   10  13
 * 6    8   11  15
 */
public class YoungSearch {
    /**
     * 解题思路：
     * 1、分治法，将二维数组分成4个矩形。
     * 2、二分法，从二维数组的右上角开始，如果目标数字比其小，则左移，否则下移。
     * 整个过程如下：9->8->2->4>7->4->6
     */
    private static boolean findElementInArr(int[][] arr, int val) {
        if (arr == null || arr.length < 1 || arr[0].length < 1) {
            return false;
        }
        int startY = arr[0].length - 1;
        int i = 0, j = startY;
        while (i < arr.length && j >= 0) {
            if (arr[i][j] == val) {
                System.out.println("found " + val+", index is " + i +"," + j);
                return true;
            } else if (arr[i][j] > val) {
                j--;
            } else if (arr[i][j] < val) {
                i++;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        
        int[][] arr = new int[][] {
                new int[]{1,2 , 8 ,  9},
                new int[]{2  ,  4  , 9  , 12},
                new int[]{4  ,  7  , 10  ,13},
                new int[]{6  ,  8  , 11 , 15}
        };
        System.out.println(findElementInArr(arr,5));
        System.out.println(findElementInArr(arr,6));
    }
}
