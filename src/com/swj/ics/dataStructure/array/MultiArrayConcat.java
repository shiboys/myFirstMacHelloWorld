package com.swj.ics.dataStructure.array;

import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Random;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/03/01 14:43
 * 多个数组合并
 * 提供方法，直接调用，支持任意个数组的合并成一个数组，并且完成排序，每个数组元素个数不定
 * 需要提供两个方法，分别做到时间复杂度最低、空间复杂度最低。并说明两个方法的时间复杂度和空间复杂度
 */
public class MultiArrayConcat {
  /**
   * 这道题目的解法我查了下资料，我总结的两个方法如下：
   * 1、先合并，再排序
   * 2、先排序，后将排序的 2 个数组进行合并
   *
   * 其实在开发的过程中，使用最多的还是 List.addAll()
   */

  /**
   * 先合并再排序
   *
   * @param arrayFirst 第一个数组
   * @param otherArray 其他数据
   * @param <T>        泛型参数
   * @return 合并且排序后的数据
   */
  public static <T extends Comparable<T>> T[] mergeThenSort(T[] arrayFirst, T[]... otherArray) {
    int totalLength = arrayFirst.length;
    for (T[] array : otherArray) {
      totalLength += array.length;
    }
    T[] resultArray = Arrays.copyOf(arrayFirst, totalLength);
    int offset = arrayFirst.length;
    for (T[] array : otherArray) {
      System.arraycopy(array, 0, resultArray, offset, array.length);
      offset += array.length;
    }
    Arrays.sort(resultArray);
    return resultArray;
  }

  /**
   * 先排序，后合并。如果是排好序的，后面这种有优势，采用的是归并的方式
   *
   * @param arrayFirst
   * @param otherArray
   * @param <T>
   * @return
   */
  public static <T extends Comparable<T>> T[] sortThenMerge(T[] arrayFirst, T[]... otherArray) {
    int totalLength = arrayFirst.length;
    Arrays.sort(arrayFirst);
    for (T[] array : otherArray) {
      totalLength += array.length;
      Arrays.sort(array);
    }
    T[][] multiArray = (T[][]) java.lang.reflect.Array.newInstance(arrayFirst.getClass(),
        otherArray.length + 1);
    multiArray[0] = arrayFirst;
    for (int i = 0; i < otherArray.length; i++) {
      multiArray[i + 1] = otherArray[i];
    }
    MergeSortMultiArray<T> mergeSortMultiArray = new MergeSortMultiArray<>(multiArray);


    T[] resultArray = (T[]) java.lang.reflect.Array.newInstance(arrayFirst.getClass().getComponentType(),
        totalLength);
    for (int i = 0; i < totalLength; i++) {
      T minVal = mergeSortMultiArray.getMinVal();
      if (minVal != null) {
        resultArray[i] = minVal;
      }
    }

    return resultArray;
  }

  private static class MergeSortMultiArray<T extends Comparable<T>> {

    private class Node<T extends Comparable<T>> implements Comparable<Node<T>> {
      public int rowIndex;
      public int colIndex;
      public T value;

      public Node(int rowIndex, int colIndex, T value) {
        this.rowIndex = rowIndex;
        this.colIndex = colIndex;
        this.value = value;
      }

      @Override
      public int compareTo(Node<T> other) {
        return value.compareTo(other.value);
      }
    }


    private T[][] multiArray;

    private PriorityQueue<Node<T>> priorityQueue;

    public MergeSortMultiArray(T[][] multiArray) {
      this.multiArray = multiArray;
      this.priorityQueue = new PriorityQueue<>(multiArray.length);
      for (int i = 0; i < multiArray.length; i++) {
        priorityQueue.offer(new Node<>(i, 0, multiArray[i][0]));
      }
    }

    public T getMinVal() {
      Node<T> minNode = priorityQueue.poll();
      if (minNode == null) {
        return null;
      }
      T minValue = minNode.value;
      minNode = getNextValue(minNode);
      if (minNode != null) {
        priorityQueue.offer(minNode);
      }
      return minValue;
    }

    public Node<T> getNextValue(Node<T> oldValue) {
      if (multiArray[oldValue.rowIndex] != null &&
          oldValue.colIndex == multiArray[oldValue.rowIndex].length - 1) {
        multiArray[oldValue.rowIndex] = null;
        return null;
      }
      // 取下一位
      oldValue.colIndex++;
      oldValue.value = multiArray[oldValue.rowIndex][oldValue.colIndex];
      return oldValue;
    }
  }

  public static void main(String[] args) {
    int upBound = 10000;
    Random random = new Random();
    int rows = 5;
    int maxCols = 15;
    Integer[][] arrays = new Integer[rows][];
    for (int i = 0; i < rows; i++) {
      int cols = random.nextInt(maxCols);
      while (cols < 1) {
        cols = random.nextInt(maxCols);
      }
      arrays[i] = new Integer[cols];
      for (int j = 0; j < cols; j++) {
        arrays[i][j] = random.nextInt(upBound);
      }
    }
    for (int i = 0; i < arrays.length; i++) {
      System.out.println(Arrays.toString(arrays[i]));
    }

    System.out.println("after sort:");

    Integer[] sortedArray = sortThenMerge(arrays[0], arrays[1], arrays[2], arrays[3], arrays[4]);

    System.out.println(Arrays.toString(sortedArray));
  }
}
