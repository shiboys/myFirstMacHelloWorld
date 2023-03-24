package com.swj.ics.algorithms.sort.quicksort;

/**
 * author shiweijie
 * date 2018/3/26 下午5:28
 */
public class QuickSort {

  public static void main(String[] args) {
    /*
     * 100的二进制为 64+32+4 = 1100100 % 32 = 4 == 1100100 & 11111
     * */

    int[] arr = {6, 1, 2, 7, 3, 4, 5, 10, 8};
    quickSort(arr);
    System.out.println();
    for (int i = 0, len = arr.length; i < len; i++) {
      System.out.print(arr[i] + ",");
    }

/*
        int[] arr2 = {3, 1, 2, 7, 3, 4, 5, 10, 8};
        quickSort(arr);
        for (int i = 0, len = arr.length; i < len; i++) {
            System.out.print(arr[i] + ",");
        }
        System.out.println();
        int[] arr3 = {1, 2, 7, 3, 4, 5, 10, 8, 6};
        quickSort(arr);
        for (int i = 0, len = arr.length; i < len; i++) {
            System.out.print(arr[i] + ",");
        }*/
    System.out.println();

  }

  public static void quickSort(int[] arr) {
    //quickSortRecur(arr, 0, arr.length - 1);
    quickSortRecursively(arr, 0, arr.length - 1);
  }

  static void quickSortRecur(int[] arr, int left, int right) {
    //快速排序，核心思想，设定一个令牌，比如第一个为令牌，第一趟比较，然后先从最右边向左比较，遇到比令牌小的元素停住
    //然后 最左边的 i 指针开始向右移动，遇到一个比令牌大的元素，停住。
    //此时如果 i < j 则交换 i ,j ，i 和 j 继续相向而行，直到i和j相遇
    //第一趟比较排序结束以后，需要在i和j相遇的地方 跟令牌交换，交换完成之后，这才满足小于令牌的都在令牌的左边，大于令牌的都在令牌的右边

    //然后是分治递归，将 i 左边的和右边的分别递归进行快速排序，递归的出口是 left >= right

    if (left >= right) {
      return;
    }

    int pivotValue = arr[left];
    int i = left, j = right, t;
    while (i < j) {
      while (arr[j] >= pivotValue && i < j) { //找到大于令牌的数值，然后停下
        j--;
      }
      while (arr[i] <= pivotValue && i < j) {
        i++;
      }
      if (i < j) { // i 和 j 相遇之前，都找到了相应的位置，则交换i和j
        t = arr[i];
        arr[i] = arr[j];
        arr[j] = t;
      }
    }
    //此时 i == j ,交换i位置和令牌的位置
    // 最好加一个判断
    if (left != i) { // 说明确实发生了指针位移
      arr[left] = arr[i];
      arr[i] = pivotValue;
    }

    quickSortRecur(arr, left, i - 1);
    quickSortRecur(arr, i + 1, right);
  }

  /**
   * 快速选择一遍，任何递归start...pivot-1 和 pivot +1...end
   *
   * @param arr   数组
   * @param start 开始
   * @param end   结束位置
   */
  static void quickSortRecursively(int[] arr, int start, int end) {
    if (start >= end) {
      return;
    }
    int pivot = arr[start];

    int left = start;
    int right = end;
    while (left < right) {
      //需要先从右边向左开始 -- 相等的时候，也要走一步，
      while (right > left && arr[right] >= pivot) {
        right--;
      }

      // 相等的时候也要走一步，否则 left = start 跟 start 比较就不会变化
      while (left < right && arr[left] <= pivot) {
        left++;
      }

      // 找到 left 和 right 相汇的地方
      if (left < right) {
        swap(arr, left, right);
      }
    }
    // left 和 right 交汇之后，将 pivot 放到  right/left 位置
    if (right > start) {
      swap(arr, start, right);
    }

    // 接下来递归 start ... right-1 和 right+1...start 之间的元素
    quickSortRecursively(arr, start, right - 1);
    quickSortRecursively(arr, right + 1, end);
  }

  static void swap(int[] arr, int i, int j) {
    int temp = arr[i];
    arr[i] = arr[j];
    arr[j] = temp;
  }
}

