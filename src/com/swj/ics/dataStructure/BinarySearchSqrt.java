package com.swj.ics.dataStructure;

/**
 * @author shiweijie
 * date 2020/2/4 上午12:11
 */

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * 已知sqrt(2)约等于1.414,要求不用数学库，求sqrt(2)精确到小数点后10位
 * 本算法题面试的基础知识点有
 * 1、基础算法的灵活运用
 * 2、退出条件的设计
 * 思路：使用二分法在1.4和1.5之间查找到一个数，使这个数的平方小于2。
 * 退出条件：前后2次差值的绝对值小于等于 0.0000000001 即可
 */
public class BinarySearchSqrt {

    //小数点后9个0一个1
    private static final double EPSILON = 0.0000000001;

    private static double sqrt2() {
        double high = 1.5;
        double low = 1.4;
        double mid = (high + low) / 2;
        while (high - low > EPSILON) {
            if (mid * mid > 2) {
                high = mid;
            } else {
                low = mid;
            }
            mid = (low + high) / 2;
        }
        return mid;
    }

    public static void main(String[] args) {
        NumberFormat numberFormat = new DecimalFormat("0.0000000000");
        System.out.println(numberFormat.format(sqrt2()));
        //System.out.println(sqrt2());
        System.out.println(Math.sqrt(2));
    }
}
