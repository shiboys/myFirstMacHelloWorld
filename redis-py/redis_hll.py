import redis

HLL_KEY = "hll_python_key"
client = redis.StrictRedis()

'''
为啥 hyperloglog 的命令相关的都是以 pf 开头的，因为 该算法是一个叫  Philippe Flajolet 的教授发明，主要用于不精确统计，
但也是不是非常不精确。
下面这个方法 演示了 1...1000 的过程中，hyperloglog 第一次统计失误的位置
打印结果为：hyperloglog diff from realcount pos is 135
这说明了到了 135 位的时候，hll 出现了统计错误
'''


def report_first_diff_pos():
    for i in range(1000):
        client.execute_command("pfadd", HLL_KEY, i)
        pf_count = client.execute_command("pfcount", HLL_KEY)
        if (pf_count != (i+1)):
            print("hyperloglog diff from realcount pos is %d" %
                  (i+1), " count is %d" % pf_count)
            break


'''
下面这个例子，是说 hll 在add 10000 个数字的时候，hll 的 count 是多少，可以估算统计的误差率
打印结果如下：
max number is 10000 , and pf_count is 10069
'''


def calc_stat_error_count_test():
    max_num = 10000
    client.delete(HLL_KEY)
    for i in range(max_num):
        client.execute_command("pfadd", HLL_KEY, "user%d" % i)

    pf_count = client.execute_command("pfcount", HLL_KEY)
    print("max number is %d" % max_num, ", and pf_count is %d" % pf_count)


if __name__ == "__main__":
    calc_stat_error_count_test()
