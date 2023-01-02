package com.swj.ics.algorithms;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/01/02 13:54
 * 数值的整数次方：
 * 题目：实现函数 double power(double base, int exponent), 求 base 的 exponent 次方。
 * 不得使用库函数，同时不需要考虑大数据的问题。
 */
public class PowerOfANumber {
    // for(;;){sum*=n;} 如果就这样交卷，基本是没有深入思考的或者没技术含量的不配称为面试题

    static double getPowerOfNumber(double base, int exponent) {
        if (base == 0) {
            return 0;
        }
        if (exponent == 0) {
            return 1;
        }
        if (exponent < 0) {
            int absExponent = Math.abs(exponent);
            return 1 / getUnsignedPowerOfNumber(base, absExponent);
        } else {
            return getUnsignedPowerOfNumber(base, exponent);
        }
    }

    static double getPowerOfNumberRecursively(double base, int exponent) {
        if (base == 0) {
            return 0;
        }
        if (exponent == 0) {
            return 1;
        }
        if (exponent < 0) {
            int absExponent = Math.abs(exponent);
            return 1 / getUnsignedPowerOfNumberRecursively(base, absExponent);
        } else {
            return getUnsignedPowerOfNumberRecursively(base, exponent);
        }
    }

    /**
     * 这里使用事件复杂度更小的更为激进的算法和公式来实现
     */

    static double getUnsignedPowerOfNumber(double base, int exponent) {
        double powerNum = 1.0;
        for (int i = 1; i <= exponent; i++) {
            powerNum = powerNum * base;
        }
        return powerNum;
    }

    public static void main(String[] args) {
        double base = 2;
        int exponent = 3;
        System.out.println(getPowerOfNumber(base, exponent));
        System.out.println(getPowerOfNumber(base, -1 * exponent));
        System.out.println(getPowerOfNumber(-1 * base, exponent));
        System.out.println(getPowerOfNumber(-1 * base, -1 * exponent));

        exponent = 10;
        System.out.println(getPowerOfNumberRecursively(base, exponent));
        System.out.println(getPowerOfNumberRecursively(base, exponent + 1));
    }

    /**
     * 上述解法全面但是并不高效，接下来我们尝试一个高效的解法。
     * 先来看一个公式：
     * <p>
     * a^n = a^n/2 * a^n/2 (n 为偶数时）
     * a^n = a^(n-1)/2 * a^(n-1)/2 * a (n 为奇数时）
     * 在看到这个这个公式的时候，我们就很容易想起来通过递归来实现，而且是 O(logn) 的时间复杂度
     * 接下来我们来实现这个公式
     */
    static double getUnsignedPowerOfNumberRecursively(double base, int exponent) {
        if (base == 0) {
            return 0;
        }
        if (exponent == 0) {
            return 1;
        }
        else if (exponent == 1) {
            return base;
        }
        double result = getUnsignedPowerOfNumberRecursively(base, exponent >> 1);
        result *= result;
        if ((exponent & 1) == 1) { // 奇数
            result *= base;
        }
        return result;
    }
}
