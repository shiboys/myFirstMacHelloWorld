package com.swj.ics.dataStructure.array;

import java.util.HashMap;
import java.util.Map;

/**
 * author shiweijie
 * date 2018/10/7 下午10:42
 * 数组中重复的数字
 * [题目]
 * 在一个长度为n的数组里的所有数字都在0到n-1的范围内。
 * 数组中某些数字是重复的，但不知道有几个数字是重复的。
 * 也不知道每个数字重复几次。请找出数组中任意一个重复的数字。
 * 例如，如果输入长度为7的数组{2,3,1,0,2,5,3}，那么对应的输出是第一个重复的数字2。
 * [解析]
 * 方法0:
 * 双重循环，外层遍历所有的数字 number，内层循环遍历数组并统计 number 出现的次数。出现重复返回即可。
 * 时间复杂度 O(n^2)
 * 方法1:
 * 用空间换时间。使用 hash 表，遍历一次数组即可完成每个数次数的统计。最后再便利一次 hash 表即可得到重复的数字。
 * 时间复杂度 O(n)，空间复杂度 O(n)。
 * 对于 c ++ 可以使用
 * 方法2：
 * 使用题目的性质进一步优化。
 * 注意题目：长度为n的数组中，所有的数字都在 0 ～ n-1 的范围内。长度为 n 的数组，则下标从 0 到 n-1。
 * 对于数组 arry, arry[i] 可以放在下标为 arry[i] 的位置，即数组中的元素值等于其在数组中的下标位置。
 * 如果这一条件得不到满足，即检查到 arry[i] != i 时，
 * 我们应该将 arry[i] 放到 arry[arry[i]]，如果 arry[arry[i]] 满足要求，此时其中一对重复的数字被找到，直接返回即可。
 * 空间复杂度-O(1)；时间复杂度-O(n)：遍历一次数组为每个数找到对应的位置，没个数最多交换 2 次可以回到其原来的位置。
 */
public class ReplicateNumber {
  public static void main(String[] args) {
/*    Integer[] array = {2, 3, 1, 0, 2, 5, 3};
    System.out.println(getReplicateNumber(array));
    System.out.println(getReplicateNumberByArray(array));
    System.out.println(getReplicateNumberBySwap(array));*/
    //System.out.println(Arrays.stream(array).map(x->x.toString()).collect(Collectors.toList()));
    int[] array2 = new int[] {2, 3, 5, 4, 1, 2, 6, 7};
    int duplicateNum = getDuplicationNumberBinarySearch(array2);
    if (duplicateNum > 0) {
      System.out.println("duplicate number is " + duplicateNum);
    } else {
      System.out.println("no duplicate number found!");
    }
  }

  //下面这2个方法的时间复杂度和空间复杂都是O(n)
  static Integer getReplicateNumber(Integer[] numArray) {
    if (numArray == null || numArray.length < 1) {
      return null;
    }
    Map<Integer, Integer> map = new HashMap<>(numArray.length);
    for (int i = 0, len = numArray.length; i < len; i++) {
      if (!map.containsKey(numArray[i])) {
        map.put(numArray[i], i);
      } else {
        return numArray[i];
      }
    }
    return null;
  }

  static Integer getReplicateNumberByArray(Integer[] numArray) {
    if (numArray == null || numArray.length < 1) {
      return null;
    }
    Integer[] targetArray = new Integer[numArray.length];
    for (int i = 0, len = numArray.length; i < len; i++) {
      //在一个长度为n的数组里的所有数字都在0到n-1的范围内
      if (targetArray[numArray[i]] != null) {
        return targetArray[numArray[i]];
      }
      targetArray[numArray[i]] = numArray[i];
    }
    return null;
  }

  //下面这个是优化后的空间复杂度O（1），时间O（n）

  /**
   * 这种方法是 通过重排这个数组来实现的。
   * 从头到尾依次扫描这个数组中的每个数字，当扫描到下标为 i 的数字时，首先会比较这个数字（用 m 表示）是不是等于 i
   * 如果是，则接着扫描下一个数字；如果不是，则再拿它和第 m 个数字进行比较。如果找到了一个重复的数字(该数字在下标为 i 和 m 的位置都出现了)。比如 第 2 位是 3 ，第 3 位也是 3
   * 如果它和 第 m 个数字不相等，就把第 i 个数字和第 m 个数字进行交换，把 m 放到属于它的位置（比如 第 2 为是 3，第 3 为是 4，则交换，
   * 交换后，第 3 为是3，第 2 为是4 ）。接下来继续重复 这个比较，交换（把第 2 为的4 和第 4 位继续比较交换），直到我们发现一个重复的数字
   * <p>
   * 以数组[2,3,1,0,2,5,3] 为例来分析找到重复数字的步骤。数组的第 0 个数字是 2，与它的下标不相同，于是将它和下标为 2 的数字 1 进行交换
   * 交换之后的数组是[1,3,2,0,2,5,3]，此时第 0 个数字 1 仍然与它的下标不相等，而第 1 个数字是 3，继续交换得到数组 [3,1,2,0,2,5,3]
   * 发现第 0 的 3 和 0 仍然不相等，则继续交换得到[0,1,2,3,2,5,3]，
   * 此时发现 第 0 位 为0，1 位为 1，2 位为 2，3位为3，第 4 位 为2 ，而 第 2 位也为 2 ，则说明 2 是重复的。
   *
   * @param numArray
   * @return
   */
  static Integer getReplicateNumberBySwap(Integer[] numArray) {
    for (int i = 0, len = numArray.length; i < len; i++) {
      if (numArray[i].equals(i)) {
        continue;
      }
      do {
        Integer ireal = numArray[i];
        if (numArray[ireal].equals(ireal)) {
          return ireal;
        } else {
          swap(numArray, i, ireal);
        }

      } while (!numArray[i].equals(i));
    }
    return null;
  }

  private static void swap(Integer[] numArray, int i, Integer ireal) {
    int temp = numArray[i];
    numArray[i] = numArray[ireal];
    numArray[ireal] = temp;
  }

  /**
   * 题目2：不修改数组找出重复的元素
   * 在一个长度为 n+1 的数组里面，所有的元素的范围都在 1~n 之间，所以数组中至少有一个数字是重复的。请找出数组中任意一个重复的数字，但不能修改
   * 输入的数组。
   * 该题目简单的话，可以建一个长度为 n+1 的数组，将原数组复制到新数组，复制方法为将原数组的 m 位的 n 值复制到新素组的 n 位，
   * 在复制的过程中，如果发现 n 位被占用，则说明 n 为重复数字
   * 接下来我们尝试避免使用 O(n) 的辅助空间。为什么数组中会有重复的数字？假如没有重复的数字，那么 1~n 的范围内只有 n 个数字，由于数组包含超过 n 个数字，
   * 那么一定包含了重复数字。
   * 我们把从 1-n 的数字从中间的数字 m 分为两部分，前面一半为 1-m，后面一半为 m+1~n。如果 1~m 的数字个数超过 m，那么这一半的区间一定包含重复的数字
   * 。我们可以继续把包含重复数字的区间一分为二，直到找到一个重复的数字。这个过程和二分法算法很类似，只是多了一个统计数字区间数目的功能
   * 我们以长度为 8 的数组 [2,3,5,4,3,2,6,7] 为例分析查找的过程，根据题目要求，这个长度为 8 的所有数字都在 1~7 的范围内。中间的数字4 把 1~7 分为两段，
   * 一段是 1-4，另一段是 5~7。接下来我们统计下1~4 这 4 个数字在数组中出现的次数，他们一共出现了 5 次，因此这个 4 个数字中一定有重复数字。
   * 实现如下。
   */

  static int getDuplicationNumberBinarySearch(int[] targetArr) {
    int start = 1;
    int end = targetArr.length - 1;
    while (start <= end) {
      int middle = (start + end) >> 1;
      // int middle = (end-start) >> 1 + start
      int count = calculateCount(start, middle, targetArr);
      if (start == end) {
        if (count > 1) {
          return targetArr[start];
        } else {
          break;
        }
      } else {
        int shouldCount = middle - start + 1;
        if (count > shouldCount) {
          end = middle;
        } else {
          // 不在 start ... middle 之间，
          start = middle + 1;
        }
      }
    }
    return -1;
  }

  private static int calculateCount(int start, int end, int[] targetArr) {
    if (targetArr == null || targetArr.length < 1) {
      return 0;
    }
    int count = 0;
    for (int i : targetArr) {
      if (targetArr[i] >= start && targetArr[i] <= end) {
        count++;
      }
    }
    return count;
  }

    /**
     * 总结：
   * 上述代码按照二分法查找的思路，如果输入长度为 n 的数组，那么函数 calculateCount 将被调用 O(logn) 次，每次调用需要时间O(n)的时间，因此
   * 总的时间复杂度为O(nlogn)，空间复杂度为 O(1)。和最前面提到的需要 O(n) 的辅助空间比，这种算法相当于以时间换空间。
   * 需要指出的是，这种算法不能保证找出的所有重复数字。例如，该算法不能找出数组[2,3,5,4,3,2,7,6] 中的重复数字 2 ，这是因为 1~2 范围内有有 1和2 两个数字，
   * 但是这个范围内的的数字 2 也是出现了 2 次，此时我们用该算法不能确定是每个数字各出现一次还是某个数字出现了两次。
   */
}
