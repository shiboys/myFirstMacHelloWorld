package com.swj.ics.dataStructure.array;

import com.swj.ics.dataStructure.heap.BinaryHeap;
import com.swj.ics.dataStructure.heap.BinaryHeapMin;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/01/05 13:57
 * 数据流中的中位数：
 * 需求：如果得到一个数据流中的中位数？如果送数据流中读取奇数个数值，那么中位数就是所有数值之后位于中间的数值。
 * 如果从数据流中独处偶数个数值，那么中位数就是所有数值排序后的中间两个数的平均值。
 * 这里的数据流指的是输入并非是一个固定数组大小的集合，有点像 arraylist ，可以不停地 feed 数据，然后这个算法要
 * 在每次 feed 之后，快速高效地选择出中位数
 * 下面列出不同的数据结构来实现插入和寻找中位数的复杂度：
 * 数据结构         插入时间复杂度         得到中位数的时间复杂度
 * 没有排序的数组      O(1)                   O(n)
 * 排序的数组        O(n)                    O(1)
 * 排序的链表        O(n)                    O(1)
 * 二叉搜索树       平均O(logn)，最差O(n)      O(1)
 * AVL 树            O(logn)                O(1)
 * 最大堆和最小堆      O(logn)                O(1)
 * <p>
 * 经过以上比对，我们可以考虑使用最大堆和最小堆来实现这个需求。如果使用最大堆和最小堆，其中有一些细节需要考虑
 * 首先要保证数据平均分配到两个堆中，因此两个堆中的数据数目之差不能超过1。为了实现平均分配，可以在数据总数目是偶数时插把新数据插入最小堆，否则
 * 插入最大堆。
 * 其次还要保证最小堆中的所有数据都要大于最大堆中的数据。当数据的总数是偶数时，按照前面的分配原则会把新的数据插入最小堆。如果此时这个数据比最大堆
 * 中的一些数据还要小，那该怎么办？
 * 可以先把这个新的数据插入最大堆，接着把最大堆中的最大的数字取出来插入最小堆。由于最终插入最小堆的数字是原来最大堆中的最大的数字，这么久保证了
 * 最小堆中所有的水镇都大于最大堆中的数字。
 * 当需要把一个数据插入最大堆，情况跟前面所述类似。
 */
public class StreamMedium {


    public static void main(String[] args) {
        int[] arr1 = {1, 3, 5, 7, 9, 11, 13, 2, 4};
        int[] arr2 = {2, 4, 6, 8, 10, 12, 9, 11};
        getStreamMediumNumbers(arr1);
        getStreamMediumNumbers(arr2);
    }

    static void getStreamMediumNumbers(int arr[]) {
        BinaryHeapUtil util = new BinaryHeapUtil();
        for (int i = 0; i < arr.length; i++) {
            insert(arr[i], util);
        }
        Integer medium = getMedium(util);
        if (medium != null) {
            System.out.println("medium is " + medium);
        } else {
            System.out.println("medium is null");
        }
    }

    static void insert(int val, BinaryHeapUtil util) {
        // 偶数
        if (((util.maxHeap.getSize() + util.minHeap.getSize()) & 1) == 0) {
            Integer maxOfMaxHeap = util.maxHeap.getMax();
            if (maxOfMaxHeap != null && maxOfMaxHeap > val) {
                //先加后删，因为 val 符合堆的定义
                util.maxHeap.insert(val);
                util.maxHeap.delMax();
                val = maxOfMaxHeap;
            }
            util.minHeap.insert(val);
        } else { // 奇数的情况下，插入大堆中
            Integer minOfMinHeap = util.minHeap.getMin();
            if (minOfMinHeap != null && minOfMinHeap < val) {
                //先加后删，因为 val 符合堆的定义
                util.minHeap.insert(val);
                util.minHeap.delMin();
                val = minOfMinHeap;
            }
            util.maxHeap.insert(val);
        }
    }

    static Integer getMedium(BinaryHeapUtil util) {
        if (util.minHeap.isEmpty()) {
            return null;
        }
        if (((util.minHeap.getSize() + util.maxHeap.getSize()) & 1) == 0) {
            return (util.minHeap.getMin() + util.maxHeap.getMax()) / 2;
        } else {
            return util.minHeap.getMin();
        }
    }


    static class BinaryHeapUtil {
        public BinaryHeap<Integer> maxHeap = new BinaryHeap<>(16);
        public BinaryHeapMin<Integer> minHeap = new BinaryHeapMin<>(16);
    }
}
