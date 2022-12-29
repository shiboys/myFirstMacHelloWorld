fib斐波那契数列
提起fib，我们很容易想到fib的递归调用fib(n) = $;
int fibonacci(int n) {
if(n<=1) {
    return 1;
} else {
    return fibonacci(n-1) + fibonacci(n-2);
}
}

但是很抱歉，stackOverFlow了。事实上，用递归的方式计算fib的时间复杂度是以n的指数的方式递增的。您不妨试试fib的第100项。

