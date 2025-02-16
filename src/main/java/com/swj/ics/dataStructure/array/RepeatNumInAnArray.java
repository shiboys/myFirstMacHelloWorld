package com.swj.ics.dataStructure.array;

/**
 * @author shiweijie
 * @date 2020/3/29 下午9:25
 * 数组中的重复数字
 * 在一个长度为N的数组里，所有的数字都在0~n-1之间，数组中某些数字是重复的，但不知道有几个数字是重复了，也不知道每个数字重复了几次
 * 请找出数组中的任意一个重复数字
 */
public class RepeatNumInAnArray {
    /**
     * 使用hash算法，是一个时间复杂度为O（n）和空间复杂度也是O（n）的，这里我们使用空间复杂度为O（1）的
     *
     * @param arr
     * @return
     */
    public Integer getAnyRepeatNumber(int[] arr) {
        if (arr == null || arr.length < 1) {
            return null;
        }
        for (int i = 0, len = arr.length; i < len; i++) {
            if (arr[i] < 0 || arr[i] > len - 1) {
                //不满足规则
                return null;
            }
        }
        // [2,3,4,5,6,1,6]
        int i = 0;
        for (int len = arr.length; i < len; ++i) {
            //这里虽然2 重循环，但是每个数字最多只需要交换2次就能找到它的位置，因此时间复杂度为O（n)，又因为这个交换发生在
            //数组上，因此空间复杂度为O(1)
            while (arr[i] != i) {
                //说明arr[i]位置上存储 的不是它自己的数字i而是别人的数字-arr[i]
                if (arr[i] == arr[arr[i]]) {
                    return arr[i];
                }
                //6,3,4,5,2,1,4
                /**
                 * 0,5,4,3,2,1,4,
                 * 0,5,2,3,4,1,4,
                 * 0,5,2,3,4,1,4
                 * 0,5,2,3,4,1,4
                 * 0,1,2,3,4,5,4
                 * 碉堡了。
                 */
                int temp = arr[i];
                arr[i] = arr[temp];
                arr[temp] = temp;
                //arr[arr[i]] = i;

            }
        }
        return null;
    }

    public static void main(String[] args) {
        int[] arr = new int[]{0, 3, 4, 5, 2, 1, 3};
        RepeatNumInAnArray instance = new RepeatNumInAnArray();
        Integer num = instance.getAnyRepeatNumber(arr);
        if (num != null) {
            System.out.println("repeat number is " + num);
        } else {
            System.out.println("invalid params");
        }
    }
}
