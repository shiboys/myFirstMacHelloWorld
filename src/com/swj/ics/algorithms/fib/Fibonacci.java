package com.swj.ics.algorithms.fib;

import org.apache.logging.log4j.core.util.JsonUtils;

/**
 * @author shiweijie
 * @date 2020/3/27 上午9:36
 */
public class Fibonacci {
    /**
     * fib斐波那契数列
     * 提起fib，我们很容易想到fib的递归调用fib(n) = $;
     * int fibonacci(int n) {
     * if(n<=1) {
     * return 1;
     * } else {
     * return fibonacci(n-1) + fibonacci(n-2);
     * }
     * }
     * <p>
     * 但是很抱歉，stackOverFlow了。事实上，用递归的方式计算fib的时间复杂度是以n的指数的方式递增的。您不妨试试fib的第100项。
     */

    //因此这里我们使用普通的方法，将递推公式进行展开
    public int fib1(int n) {
        if (n <= 1) {
            return n;
        }
        int one = 0;
        int two = 1;
        int res = 0;
        for (int i = 2; i <= n; i++) {
            res = one + two;
            one = two;
            two = res;

        }
        return res;
    }

    /**
     * 上述方法并非最快的，下面介绍一种时间复杂度为O(Logn)的方法。
     * @param args
     */
    /**
     * 在介绍这种方法之前，先介绍一个数学公式：
     * <p>
     * {f(n), f(n-1), f(n-1), f(n-2)} ={1, 1, 1,0}n-1
     * <p>
     * (注：{f(n+1), f(n), f(n), f(n-1)}表示一个矩阵。在矩阵中第一行第一列是f(n+1)，第一行第二列是f(n)，第二行第一列是f(n)，第二行第二列是f(n-1)。)
     * <p>
     * 有了这个公式，要求得f(n)，我们只需要求得矩阵{1, 1, 1,0}的n-1次方，因为矩阵{1, 1, 1,0}的n-1次方的结果的第一行第一列就是f(n)。
     * 这个数学公式用数学归纳法不难证明。感兴趣的朋友不妨自己证明一下。
     *
     * @param n
     */
    private static int pre;
    private static int post;


    public int fibLogN(int n) {
        /**
         * 每次Fibonacci调用完成之后，
         * pre 存储的是fib(N)的结果
         * post 存储的是fib（n-1）的结果
         */
        if (n <= 2) {
            if (n == 0) {
                return 0;
            }
            pre = 1;
            post = 1;
            return pre;
        }
        /**
         * n为奇数时，则做减1操作，生成fib(n-1)时的pre and post。函数回调时再生成fib(n)的pre和post
         * pre : f(n) = f(n-1)+f(n-2)
         * post: f(n-1) = f(n) - f(n-2)
         *
         */
        if (n % 2 == 1) {
            fibLogN(n - 1);
            pre = pre + post;
            post = pre - post;
            return pre;
        }
        //N为偶数时 不断的除以2
        /**
         * 设 n=2k
         * f(2k)= f(k)*f(k+1) + f(k)*f(k-1)
         *      = f(k)*(f(k)+f(k-1)) + f(k)*f(k-1)
         *      = f(k)^2 + 2*f(k)*f(k-1)
         *
         * f(2k-1) = f(k)*f(k) +f(k-1)*f(k-1)
         *          =f(k)^2+f(k-1)^2
         */
        fibLogN(n / 2);
        int temp = pre;
        pre = pre * pre + 2 * pre * post;
        post = temp * temp + post * post;
        return pre;
    }


    /**
     * 一个青蛙一次可。以跳跃1个台阶，也可以一次跳跃2个台阶，请问对于n个台阶，
     * 分析：
     * 每次只能跳1级或者2级，那么跳N个台阶，可以这么理解：
     * 先跳1级，然后剩余的是跳一个$N-1级的台阶，
     * 先跳2级，然后跳剩余的是跳一个N-2级的台阶
     *那么它的地推公式为
     * 当n=0时，$=0,
     * 当你=1时,$=1,
     * 当n=2，$=2
     * 当n=n时，$=f(n-1)+f(n-2)
     */
    public int fib_jum_floor(int n) {
        if(n<=2) {
            return n;
        }
        int one =1 ;
        int two =2;
        int res = 0;
        for (int i = 3; i <= n; i++) {
            res = one + two;
            one = two;
            two = res;
        }
        return res;
    }

    public static void main(String[] args) {
        Fibonacci instance = new Fibonacci();
        for (int i = 1; i <= 10; i++) {
            System.out.print(instance.fib1(i) + ",");
        }
        System.out.println();

        for (int i = 1; i <= 10; i++) {
            System.out.print(instance.fibLogN(i) + ",");
        }
        System.out.println();
    }

}
