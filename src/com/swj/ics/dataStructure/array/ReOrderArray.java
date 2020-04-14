package com.swj.ics.dataStructure.array;


import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author shiweijie
 * @date 2020/4/14 下午8:41
 * 输入一个整数数组，实现一个函数来调整该数组中数字的顺序，使得所有的奇数位于数组的前半部，所有的偶数位于数组的后半部，
 * 并保证奇数和奇数，偶数和偶数之间的相对位置不变
 */
public class ReOrderArray {
    public void reOrder1(int[] arr) {
        /**
         * 方法1：
         * 使用类似于冒泡排序的规则，如果两个相邻的元素，偶数在前，奇数在后，每次循环的时候，如果遇到前偶后奇就交换
         * 同时我们设立一个flag，用来标示数组是否已经复合要求。
         * 当在此循环的时候，如果发现没有可交换的数据，说明数组已经复合要求
         */
        if (arr.length <= 1) {
            return;
        }
        boolean swapped = false;
        for (int i = 0; i < arr.length; i++) {
            swapped = false;
            for (int j = arr.length - 1; j > i; j--) {
                //前偶后奇
                if (arr[j - 1] % 2 == 0 && arr[j] % 2 == 1) {
                    swap(arr, j, j - 1);
                    swapped = true;
                }
            }
            //如果没有交换了，说明已经拍好序了，跳出
            if (!swapped) {
                break;
            }
        }
    }

    /**
     * 方法2：使用辅助数组完成。
     * 思路：将偶数从数组中复制到一个新arrayList,并从原来的数组进行删除。
     *
     * @param arr
     */
    public void reOrder2(int[] arr) {

    }

    /**
     * 方法3：思路，起数组前后2个指针或者索引，如果前面的指针遇到偶数，则停止，后面的指针遇到奇数则停止，交换彼此。知道遇到彼此，
     * 一遍排序即可完成，但是这是不稳定的排序，有可能会改变奇数与奇数，偶数与偶数的相对顺序
     *
     * @param arr
     */
    public void reOrder3(int[] arr) {
        if (arr.length <= 1) {
            return;
        }
        int left = 0, right = arr.length - 1;
        while (left < right) {
            while (left < right && (arr[left] & 0x01) == 1) {
                left++;
            }
            while (left < right && (arr[right] & 0x01) == 0) {
                right--;
            }
            System.out.println("left = " + left + ",right =" + right);
            if (left < right) {
                swap(arr, left, right);
            }
        }
    }

    private void swap(int[] arr, int j, int i) {
        int temp = arr[j];
        arr[j] = arr[i];
        arr[i] = temp;
    }

    public static void main(String[] args) {
        int[] arr = new int[]{5, 1, 2, 3, 4, 5};
        ReOrderArray instance = new ReOrderArray();
        instance.reOrder1(arr);
        System.out.println(Arrays.stream(arr).mapToObj(String::valueOf).collect(Collectors.joining(",")));
    }

}
