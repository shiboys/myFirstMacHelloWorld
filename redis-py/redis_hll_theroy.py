import redis
import math
import random

'''
reids hyperloglog 原理说明。请参考 hyperloglog.md
'''
# 计算低位是 0 bit 的个数


def get_zero_bit_count_of_low_pos(input_num):
    for i in range(32):
        # 右移 i 位 然后再左移 i 位，如果 不等于原来的值，则说明遇到了bit 为 1 的，则退出循环
        if input_num >> i << i != input_num:
            # 注意，这里 0 的个数是 i-1，i 位置的 bit 值为 1，因此 0 的个数就是 i-1
            return i-1 

    return 31


class BitKeeper:

    def __init__(self):
        self.max_bits = 0

    def calc_max_bits_with_random(self):
        rnd_num = random.randint(0, 2**32-1)
        zero_bits_count = get_zero_bit_count_of_low_pos(rnd_num)
        if (zero_bits_count > self.max_bits):
            self.max_bits = zero_bits_count

    def calc_max_bits_with_given_random(self, rnd_num):
        zero_bits_count = get_zero_bit_count_of_low_pos(rnd_num)
        if (zero_bits_count > self.max_bits):
            self.max_bits = zero_bits_count


'''
通过该简单实验类，来测试元素大小的对数 log2 和元素的低 0 位个数之和，来显示规律
'''


class ExperimentSimple:
    def __init__(self, n):
        self.n = n
        self.bitKeeper = BitKeeper()

    def doExperiment(self):
        for i in range(self.n):
            # 扔 n 次 random 遍硬币(一旦遇到 正面则停止,就是一旦遇到 bit 为 1 的就停止一样)，来统计这其中最大的一次的反面个数
            self.bitKeeper.calc_max_bits_with_random()

    def report_detail(self):
        print(self.n, "%0.2f" % math.log(self.n, 2), self.bitKeeper.max_bits)


'''
打印结果如下：
34500 15.07 17
34600 15.08 21
34700 15.08 16
34800 15.09 15
34900 15.09 18
35000 15.10 16
35100 15.10 21
35200 15.10 16
35300 15.11 15
35400 15.11 16
35500 15.12 16
35600 15.12 15
35700 15.12 14
35800 15.13 15
35900 15.13 18
36000 15.14 17
36100 15.14 16
可以看出 元素的 maxbits 和 对数确实有一定的相关性，但是单个 bitKeeper 的相关性不大，
接下来我们试着做多个 bitKeeper 的计算，然后再使用调和平均数，来测试更精确的预估
'''


def doExperimentSimple():
    for i in range(1000, 100000, 100):
        exp = ExperimentSimple(i)
        exp.doExperiment()
        exp.report_detail()


class ExperimentMultiKeepers:
    def __init__(self, n, k=1024):
        self.n = n
        self.keeper_size = k
        self.keepers = [BitKeeper() for i in range(k)]

    def do_calculdate_max_bits_for_keeper(self):
        max_int_value = 1<<32 - 1
        # 这里仍然是计算一遍 n 次的硬币个数，扔的次数少了，计算回不准确
        for i in range(self.n):
            rand = random.randint(0, max_int_value)
            # 取高 16 位作为 Keeper 的索引，这样 高 16 位相同的数都会使用同一个 keeper
            keeper_idx = ((rand & 0xffff0000) >> 16) % self.keeper_size
            keeper = self.keepers[keeper_idx]
            keeper.calc_max_bits_with_given_random(rand)

    def do_estimate_for_all_keepers(self):
        inversed_keeper_sum_bits = 0.0
        # 取得 倒数 之和，利用该和求出调和平均数
        for keeper in self.keepers:
            inversed_keeper_sum_bits += 1.0/float(keeper.max_bits)

        # 取得平均零位数
        inversed_keeper_avg_bits = float(self.keeper_size) / inversed_keeper_sum_bits

        # 根据桶的数量,对估值进行放大
        return 2**inversed_keeper_avg_bits * self.keeper_size


'''
打印结果如下：
100000 94349.78
200000 201643.57
300000 300091.03
400000 414963.29
500000 502767.18
600000 610725.28
700000 718021.44
800000 807060.83
900000 888653.37
这说明说用大的标本量来计算调和平均之后的 bits 位数，然后再进行还原，就会发现，这个数据跟原始数据很接近,
'''
def do_experiment_with_multi_keeper():
    for i in range(100_000, 1_000_000, 100_000):
        multiKeepers = ExperimentMultiKeepers(i)
        
        multiKeepers.do_calculdate_max_bits_for_keeper()
        estimate_result = multiKeepers.do_estimate_for_all_keepers()
        print(i, "%.2f" %estimate_result , "%.2f" % (abs(i-estimate_result)/i))


if __name__ == "__main__":
    # doExperimentSimple()
    do_experiment_with_multi_keeper()
