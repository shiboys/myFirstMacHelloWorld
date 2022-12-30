package com.swj.ics.dataStructure.array;

import java.util.Arrays;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2022/12/30 09:25
 * 快速选择法的 top n，采用的原理为快速排序的 pivot 分治法，将 大于 pivot 的放到右边，小于 pivot 放到左边
 * 进一步缩小 pivot 的查找范围。
 */
public class TopNWithQuickSelect {
    static void swap(int[] arr, int i, int j) {
        int length = arr.length;
        assert i >= 0 && i < length;
        assert j >= 0 && j < length;
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }

    /**
     * 快速选择前 n 小
     *
     * @param sourceArr
     * @param index
     * @return
     */
    static int quickSelect(int[] sourceArr, int index) {
        int start = 0;
        int end = sourceArr.length - 1;

        while (true) {
            if (start == end) {
                return start;
            }
            int left = start + 1;
            int right = end;
            int pivot = sourceArr[start];
            // 执行每次的快速选择

            while (true) {
                while (left <= right && sourceArr[left] <= pivot) {
                    left++;
                }
                while (right > start && sourceArr[right] >= pivot) {
                    right--;
                }
                // 找到需要交换的两个元素，
                if (left < right) {
                    swap(sourceArr, left, right);
                } else { // left >= right 说明 左后指针相遇了，一遍快速选择结束
                    // 将 pivot 和 right 的位置互换，因为 此时 right 刚好是 pivot 的正确位置，而且是一个小于 pivot 的值，而 right 放到 pivot 原来的 0 位置，
                    // 也满足小于 pivot 的值都在左边
                    if (right > start) { // 如果相等，说明 pivot 在 0 的位置就是刚刚好的位置，不需要交换,否则将 pivot 放到属于他的正确位置。
                        swap(sourceArr, start, right);
                    }
                    break;
                }
            }
            // 一遍快速选择结束，判断 pivot 和 index 的先后，进一步缩小快速选择的范围，跟二分法有点像。
            if (index == right) {
                return right;
            } else if (index > right) {
                start = right + 1;
            } else {
                end = right - 1;
            }
        }
    }

    /**
     * 快速选择前 n 大。
     *
     * @param sourceArr
     * @param index
     * @return
     */
    static int quickSelectMax(int[] sourceArr, int index) {
        int start = 0;
        int end = sourceArr.length - 1;
        while (start < end) {
            int left = start + 1;
            int right = end;
            int pivot = sourceArr[start];
            // 比 pivot 大的放左边，比 pivot 小的放右边
            while (true) {
                while (left <= right && sourceArr[left] > pivot) {
                    left++;
                }
                while (right > start && sourceArr[right] < pivot) {
                    right--;
                }
                if (left < right) { // 遇到需要交换的元素
                    swap(sourceArr, left, right);
                } else { // left >= right 说明左右指针交汇，一遍快排结束
                    if (right > start) {
                        swap(sourceArr, start, right);
                    }
                    break;
                }
            }
            if (index == right) {
                return right;
            } else if (index > right) {
                start = right + 1;
            } else {
                end = right - 1;
            }
        }
        return start;
    }

    public static void main(String[] args) {
        int[] arr = new int[]{6, 67, 18, 29, 1000, 105, 222, 101, 11, 15, 22, 33, 55, 100, 99, 77, 23};
        int[] arr_copy = Arrays.copyOf(arr, arr.length);
        int topK = 10;
        int index = quickSelect(arr, topK);
        System.out.println("index is " + index);
        for (int i = 0; i < index; i++) {
            if (i == topK - 1) {
                System.out.print(arr[i]);
            } else {
                System.out.print(arr[i] + ", ");
            }
        }
        System.out.println();
        int maxIndex = quickSelectMax(arr_copy, topK);
        System.out.println("max index is " + index);
        for (int i = 0; i < maxIndex; i++) {
            if (i == topK - 1) {
                System.out.print(arr_copy[i]);
            } else {
                System.out.print(arr_copy[i] + ", ");
            }
        }
    }

}

/**
 * 总结，快选选择法跟快速排序一样，可以随机选择支点，而不是每次都使用最左边的支点位置，避免遭遇固定的最差案例
 * topK 的快速选择法的时间复杂度：
 * 最坏：O(n^2)，并非要取得最大或者最小值，但是选择的支点刚好是最大或者最小值。
 * 最好：O(n) 第一次选择的支点元素就是要找的第 k 小或者第 k 大。
 * 平均 O(n)
 * 空间复杂度 O(1)
 */
