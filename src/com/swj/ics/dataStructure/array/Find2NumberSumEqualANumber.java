package com.swj.ics.dataStructure.array;

import com.swj.ics.algorithms.sort.quicksort.QuickSort;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2022/12/30 13:57
 * 寻找和为定值的两个数。输出一对或者多对。比如，输入数组[1,2,4,5,7,11,15] 求和为 15 的两个数，由于 4+11 为 15，
 * 因此可以输出 4 和 11。
 */
public class Find2NumberSumEqualANumber {
    /**
     * 解法一：散列映射法
     * 如果题目对事件复杂度要求比较严苛，可以考虑下空间换时间，也就是说给定一个数，，根据散列映射去查找另一个数是否也在数组中
     * 此种方法只需要O(1) 的时间，所以查询 n 次，总的时间复杂度为 O(n)，但前提是需要 O(n）的空间构造散列表。
     * 能否做到在时间复杂度为 O(N) 的情况下，空间复杂度为 O(1) 那。
     * <p>
     * 解法二：排序夹逼
     * 如果数组是无需的，可以考虑把数组排成有序的，数组变成有序之后，便有很多方法可以找到其中等于给定的两个数。
     * 例如，对于每个 a[i]，查找 sum-a[i] 是否也在原始序列中，每一次查询都可以使用二分法，这样对于 a[i],需要花 logn 的时间去找出对应的 sum-a[i]
     * 是否在原始序列中。因此加上预先排序的时间复杂度，总的时间复杂度为 O(nlogn + nlogn) = O(nlogn)，空间复杂度为 O(1).
     * <p>
     * 有没有一种办法，在数组有序的前提下，使用两个指针 start 和 end 分别指向数组的首尾两段，令 begin=0, end=n-1，然后 begin++,end--
     * 逐次判断 a[begin]+a[end] 是否相等于给定的 sum
     */

    static List<TwoSumItem> twoSum(int[] array, int targetSum) {
        // 先排序
        QuickSort.quickSort(array);
        int begin = 0;
        int end = array.length - 1;
        List<TwoSumItem> items = new ArrayList<>();
        while (begin < end) {
            if (array[begin] + array[end] == targetSum) {
                items.add(new TwoSumItem(begin, end));
                begin++;
                end++;
            } else if (array[begin] + array[end] > targetSum) {
                end--;
            } else {
                begin++;
            }
        }
        return items;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class TwoSumItem {
        public int f1Index;
        public int f2Index;
    }

    public static void main(String[] args) {
        int[] arr = {1, 3, 4, 5, 7, 11, 15, 8, 12, 14};
        int targetSum = 15;
        List<TwoSumItem> items = twoSum(arr, targetSum);
        if (!items.isEmpty()) {
            for (TwoSumItem item : items) {
                System.out.println(item + String.format("; f1=%s, f2=%s", arr[item.f1Index], arr[item.f2Index]));
            }
        } else {
            System.out.println("items are empty");
        }

    }
}
