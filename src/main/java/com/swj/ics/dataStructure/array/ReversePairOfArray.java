package com.swj.ics.dataStructure.array;

import java.util.Arrays;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/01/06 14:37
 * 数组中的逆序对。
 * 题目：在数组中的两个数字，如果前面一个数字大于后面的数字，则这两个数字组成一个逆序对。
 * 输入一个数组，求出这个数组中的逆序对的总数。
 * 例如 在数组 {7,5,6,4} 中一共存在 5 对逆序对。分别是(7,6)、(7,4)、(7,5)、(6,4)、(5,4)
 * 看到这个题目，我们的第一反应是顺序扫描整个数组。没扫描到一个数字，逐个比较该数字和它后面的数字的大小。
 * 如果后面的数字比它小，则这两个数字就组成一个逆序对。假设数组中含有 n 个数字。由于每个数字都需要和O(n)进行比较，因此这种算法的复杂度为O(n^2)
 * 我们可以尝试寻找更快的算法。
 * 我们还是以数组 {7,5,6,4} 为例来分析统计逆序对的过程。没扫描到一个数字的时候，我们不能拿它和后面的每个数字进行比较，否则时间复杂度就是O(n^2)
 * 我们可以考虑比较两个相邻的数字。
 * 1、7和5分为一组     6和4分为 1 组。
 * 2、7和5继续分解成两个长度为 1 的子数组
 * 3、把长度为1的子数组进行合并，排序，并统计逆序对
 * 4、把长度为 2 的子数组何必并，排序，并统计逆序对
 * 经过前面的套路，我们可以总结出统计逆序对的过程：先把数组分割成子数组，统计出子数组内部的逆序对的数目，然后在统计出两个相邻的子数组之间的逆序对数目。
 * 在统计逆序对的过程中，还需要对数组进行排序。如果我们对排序算法很熟悉，那么我们不难发现这个排序的过程实际上就是归并排序
 */
public class ReversePairOfArray {

    public static void main(String[] args) {
        int[] arr = {7, 5, 6, 4};
        System.out.println(calculateInversePair(arr));
    }

    static int calculateInversePair(int[] arr) {
        int[] copyArr = new int[arr.length];
        int result = getInversePairCount(arr, copyArr, 0, arr.length - 1);
        System.out.println(Arrays.toString(copyArr));
        return result;
    }

    static int getInversePairCount(int[] arr, int[] copyArr, int start, int end) {
        if (start == end) { // 递归出口
            copyArr[start] = arr[start];
            return 0;
        }
        int mid = (start + end) / 2;
        // 统计左子数组内部的逆序对数。注意 start 有可能等于 end ，因此 left 有可能等于 0
        int left = getInversePairCount(arr, copyArr, start, mid);
        // 统计右子数组内部的逆序对，注意，start=mid，那么 mid 和 end 之间应该至少有2个元素，也就是 mid 和 end
        int right = getInversePairCount(arr, copyArr, mid + 1, end);
        // 开始计算逆序对,这里应为需要统计逆序对，也就是左边的子数组大于右边的子数组，因此这里的统计从尾向头部递减
        int p1Tail = mid;
        int p2Tail = end;
        int copyIndex = end;
        int inverseCount = 0;
        while (start <= p1Tail && mid + 1 <= p2Tail) {
            if (arr[p1Tail] > arr[p2Tail]) { // 只有2个元素的子数组情况下计算逆序对，和 子数组之间的计算逆序对
                inverseCount += (p2Tail - mid);// mid+1 到 p2Tail 之间的所有元素相对 p1Tail 来说都是逆序对，数组之间也是同理
                copyArr[copyIndex--] = arr[p2Tail--];
            } else {
                copyArr[copyIndex--] = arr[p1Tail--];
            }
        }
        while (start <= p1Tail) {
            copyArr[copyIndex--] = arr[p1Tail--];
        }
        while (mid + 1 <= p2Tail) {
            copyArr[copyIndex--] = arr[p2Tail--];
        }
        // inverseCount 子数组之间的逆序对，left，左子数组内部的逆序对，right 右子数组内部的逆序对
        return inverseCount + left + right;
        // 经过运行，发现 copyArr 的排序是逆序的。不过也好更改，将copyIndex 从 start开始，由 -- 改为 ++
    }
}
