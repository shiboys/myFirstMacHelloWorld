package com.swj.ics.dataStructure.array;

import java.util.Arrays;
import java.util.Random;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/01/03 14:06
 * 题目：数组中有一个数字出现的次数超过数组长度的一半，请找出这个数字。例如，输入一个长度为 9 的数组 {1,2,3,2,2,2,5,4,2}。
 * 由于数字 2 在数组中出现了 5 次，超过数组长度的一半，因此输出 2
 */
public class MoreThanHalfNumber {
    /**
     * 解析：题目给出的数组没说是排好序的，因此我们需要先给它排序。排序的时间复杂度是 O(nlogn)。最直观的算法通常不是面试官满意的算法
     * 下面我们来尝试找到更快的算法
     * 我们回归到题目本身仔细分析，就会发现前面的思路并没有考虑到数组的特性：数组中有一个数字出现的次数搞过来数组长度的一半。如果把这个数组排序
     * 则排序后的位于中间位置的数字一定就是那个出现次数超过数组一半的数字。也就是说，这个数字就是统计学上的中位数，即长度为 n 的数组中的第 n/2
     * 大的数字。我们有成熟的时间复杂度为 O(n) 的算法得到数组中任意第 k 大的数字
     * 解法一，根据 partition 的分治法的时间复杂度为 O(n) 的算法。
     * 这种算法受快速排序的启发。在随机快速排序中，我们先在数组中随机选取一个数字，然后调整数组中数字的顺序，使得比选中数字小的都在左边，
     * 比选中数字大的都在右边。如果这个数字下标刚好是 n/2，那么这个数字就是数组的中位数；如果它的下标大于 n/2，那么中位数就应该位于它的左边，
     * 我们可以在数组的左半部分继续查找，否则就在有半部分继续查找。
     * 这里所说的 时间复杂度为 O(n) 并非只是第一次全快速选择的排序的O(n)，还要加上后面的半递归，这个半递归总的时间复杂度应该不超过 一个 n
     * 因此这里总的时间复杂度为 O(2n) ，也就是时间复杂度为 O（n）
     */
    static final Random random = new Random();

    static int getMoreThanHalfNumber(int[] arr) {
        if (!checkInvalidArray(arr)) {
            return 0;
        }
        int length = arr.length;
        int start = 0;
        int end = length - 1;
        int index = partition(arr, start, end, length);
        int middle = length >> 1;
        while (index != middle) {
            if (index > middle) {
                end = index - 1;
            } else {
                start = index + 1;
            }
            index = partition(arr, start, end, length);
        }
        int result = arr[middle];
        if (!checkMoreThanHalf(arr, result)) {
            return 0;
        }
        return result;
    }

    private static boolean checkMoreThanHalf(int[] arr, int result) {
        return Arrays.stream(arr).filter(x -> x == result).count() * 2 > arr.length;
    }

    private static boolean checkInvalidArray(int[] arr) {
        return arr != null && arr.length > 0;
    }

    /**
     * @param arr
     * @param start
     * @param end
     * @param length 数组的原始长度
     * @return
     */
    static int partition(int[] arr, int start, int end, int length) {
        //if(!checkValidArray)
        int index = getRandomIndex(start, end);
        int pivot = arr[index];
        // 仍然是从 0 开始的选取 start
        if (index != start) {
            swap(arr, start, index);
        }
        int left = start;
        int right = end;
        while (left < right) {
            while (left < right && arr[right] >= pivot) {
                right--;
            }
            while (left < right && arr[left] <= pivot) {
                left++;
            }
            if (left < right) {
                swap(arr, left, right);
            }
        }
        if (left != start) {
            swap(arr, start, left);
        }

        return left;
    }

    static int getRandomIndex(int begin, int end) {
        if (end == begin) {
            return begin;
        }

        return begin + random.nextInt(end - begin + 1);
    }

    static void swap(int[] arr, int i, int j) {
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }

    public static void main(String[] args) {
        int[] arr = {1, 2, 3, 2, 2, 2, 5, 4, 2};
        //System.out.println("中位数是 " + getMoreThanHalfNumber(arr));
        System.out.println("中位数是 " + moreThanHalfNumByCounting(arr));

        int[] arr2 = {1, 2, 3, 4, 5, 6, 7, 8};
        //System.out.println("中位数是 " + getMoreThanHalfNumber(arr2));
        System.out.println("中位数是 " + moreThanHalfNumByCounting(arr2));

        int[] arr3 = {5, 2, 5, 4, 5, 6, 5, 8, 5};
        //System.out.println("中位数是 " + getMoreThanHalfNumber(arr3));
        System.out.println("中位数是 " + moreThanHalfNumByCounting(arr3));
    }

    /**
     * 解法2：根据数组特点找出时间复杂度为 O（n）的算法。
     * 接下来，我们从另外一个角度来解决这个问题。数组中有一个数字出现的次数超过数组长度的一半，也就说它出现的次数是比其他说有数字出现的次数和还要
     * 多。因此，我们可以在遍历数组的试试保存两个值：一个是数组中的一个数字，另一个是次数。当我们遍历到下一个数字的时候，进入该下一个数字和我们之前
     * 保存的数字相同，则次数 +1，否则 次数 -1。如果次数为 0，那么我们需要保存下一个数字，并把次数设置为 1。由于我们要找的数字出现的此时比其他所有
     * 数字出现的次数之和还要多，那么要找的数字肯定是最后一次把次数设置为 1 时对应的数字。
     */

    static int moreThanHalfNumByCounting(int[] arr) {
        if (!checkInvalidArray(arr)) {
            return 0;
        }
        int result = arr[0];
        int times = 1;
        for (int i = 1; i < arr.length; i++) {
            if (times == 0) { // 重新初始化
                result = arr[i];
                times = 1;
            } else if (result == arr[i]) {
                times++;
            } else {
                times--;
            }
        }
        if (!checkMoreThanHalf(arr, result)) {
            return 0;
        }
        return result;
    }
}
