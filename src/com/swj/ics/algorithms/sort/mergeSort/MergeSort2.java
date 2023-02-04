package com.swj.ics.algorithms.sort.mergeSort;

import java.util.Arrays;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/01/06 20:51
 * 归并排序 2 刷。
 * 归并排序使用了分治和递归的思想
 */
public class MergeSort2 {

  public static void main(String[] args) {
    int[] arr = {3, 4, 5, 2, 1, 7, 6, 9, 0};
    System.out.println("before sort");
    System.out.println(Arrays.toString(arr));
    // mergeSort(arr);
    mergeSort2(arr);
    System.out.println("after sort:");
    System.out.println(Arrays.toString(arr));
  }

  static void mergeSort(int[] arr) {
    int[] copyArr = new int[arr.length];
    doMergeSort(arr, copyArr, 0, arr.length - 1);
  }


  static void doMergeSort(int[] arr, int[] copyArr, int start, int end) {
    // 此处不是 while，否则就是 死循环，这里的 if 是为了出现递归出口
    if (start < end) {
      // 先分治，再合并，分治是通过递归实现
      int mid = (start + end) / 2;
      // 分治前半部分
      doMergeSort(arr, copyArr, start, mid);
      // 分治后半部分
      doMergeSort(arr, copyArr, mid + 1, end);
      // 此时的 mid = start + 1，进行归并
      // 需要归并的两部分包括 start~mid，以及 mid+1~end,
      // 归并之后的数据放入 辅助数组，
      // 最后将辅助数组写回原始数组。
      doMerge(arr, copyArr, start, mid, end);
    }
  }

  /**
   * 进行归并。归并的大体过程是是
   * 1、将分治后的两个子数组按照元素大小依次放入一个辅助数组中。
   * 2、移动两个子数组指针和辅助数组指针，
   * 3、直到所有的元素都存入辅助数组
   * 4、将辅助数组当前区间的元素，拷贝回原始数组
   *
   * @param arr     原始数组
   * @param copyArr 辅助数组
   * @param start
   * @param mid
   * @param end
   */
  static void doMerge(int[] arr, int[] copyArr, int start, int mid, int end) {
    int p1Header = start;
    int p2Header = mid + 1;
    int pos = start;
    // 注意：这里是小于等于，而不是小于，因为 p1Header 可以等于 mid，同理 p2Header 可以等于 end
    while (p1Header <= mid && p2Header <= end) {
      if (arr[p1Header] < arr[p2Header]) {
        //拷贝完成之后，需要同步向前移动，因此这里直接使用了 ++
        copyArr[pos++] = arr[p1Header++];
      } else {
        copyArr[pos++] = arr[p2Header++];
      }
    }
    // 如果两边的分治数组还有剩下未拷贝完成的元素，继续拷贝
    while (p1Header <= mid) {
      copyArr[pos++] = arr[p1Header++];
    }
    while (p2Header <= end) {
      copyArr[pos++] = arr[p2Header++];
    }
    // 至此，当前的归并已经完成，需要将归并之后的元素重新写会原始数组
    for (int i = start; i <= end; i++) {
      arr[i] = copyArr[i];
    }
  }

  /**
   * 总结：
   * 空间复杂度 O(n),因为是使用了辅助数组
   * 时间复杂度 O(nlogn),每一层归并的时间复杂度为 n
   * 分治层数为 logn+1, 归并层数为 logn+1, 分治+归并的时间复为 2*(logn+1), 也就是时间复杂度为 O(logn)
   * 因此总的时间复杂度为 O(nlogn)
   * 稳定性：
   * 稳定性跟我们的代码逻辑有关，如果两个数相等  if(arr[p1Header] < arr[p2Header])，则这个代码不会执行
   * 因此是不稳定的，如果是 小于等于，那就是稳定的(在辅助数组中的位置)
   */

  /**
   * 第二种归并排序，思想是一致的，主要差别是更接近归并的思想，不需要传入 copyArr 数组
   */
  private static void mergeSort2(int[] inputArr) {
    mergeSort2(inputArr, 0, inputArr.length - 1);
  }

  private static void mergeSort2(int[] inputArr, int start, int end) {
    if (start >= end) {
      return;
    }
    int middle = (start + end) / 2;
    // 对左边进行划分
    mergeSort2(inputArr, start, middle);
    mergeSort2(inputArr, middle + 1, end);
    doMerge2(inputArr, start, middle, end);
  }

  private static void doMerge2(int[] inputArr, int start, int middle, int end) {
    int[] tempArr = new int[end - start + 1];
    // 把归并的结果按照左右两个数组取最小的写入新的数组
    int p1 = start;
    int p2 = middle + 1;
    int newPos = 0;
    while (p1 <= middle && p2 <= end) {
      if (inputArr[p1] < inputArr[p2]) {
        tempArr[newPos++] = inputArr[p1++];
      } else {
        tempArr[newPos++] = inputArr[p2++];
      }
    }
    // 下面这 2 个 while 循环时继续处理
    while (p1 <= middle) {
      tempArr[newPos++] = inputArr[p1++];
    }
    while (p2 <= end) {
      tempArr[newPos++] = inputArr[p2++];
    }
    // 将归并的结果写回原数组
    for (int i = 0, len = newPos; i < len; i++) {
      inputArr[start + i] = tempArr[i];
    }
  }
}
