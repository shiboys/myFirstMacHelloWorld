import redis
import random

HOST = "10.211.55.6"
PORT = 6379


class RedisClient:
    def __init__(self):
        self.client = redis.StrictRedis(host=HOST, port=PORT)

    def getRedisClient(self):
        return self.client


redisClient = RedisClient()
client = redisClient.getRedisClient()
BLOOM_FILTER_TEST_KEY = "bloomfilter_py"


CHARS = "".join([chr(ord('a') + i) for i in range(26)])


def random_string(char_length):
    chars = []
    for i in range(char_length):
        # random.randint(start,end) 这个方法的两边边界都包括
        char_idx = random.randint(0, len(CHARS)-1)
        chars.append(CHARS[char_idx])

    return "".join(chars)


def test_redis_bloom_filter_common():
    client.delete(BLOOM_FILTER_TEST_KEY)
    for i in range(10000):
        client.execute_command("bf.add", BLOOM_FILTER_TEST_KEY, "user%d" % i)
        ret = client.execute_command(
            "bf.exists", BLOOM_FILTER_TEST_KEY, "user%d" % i)
        if ret == 0:
            print(i)
            break
    print("all this test key in range user[0,10000} exists in bloom filter.")


"""
布隆过滤器，不识别情况测试。
打印结果如下，说明到 306 的时候，布隆过滤器出现了误判。
the bloomfilter misjudge the number at 306
"""


def bloom_filter_not_exists():
    misjudge = False
    client.delete(BLOOM_FILTER_TEST_KEY)
    for i in range(10000):
        client.execute_command("bf.add", BLOOM_FILTER_TEST_KEY, "user%d" % i)
        ret = client.execute_command(
            "bf.exists", BLOOM_FILTER_TEST_KEY, "user%d" % (i+1))
        if ret == 1:
            print("the bloomfilter misjudge the number at %d " % i)
            misjudge = True
            break
    if misjudge == False:
        print("bloomfilter should not run here")


'''
测试 bloomfilter 的误判率，使用两个完全不存在重复元素的集合，一个 测试组，一个实验组，两组元素的数量完全相同，将全部的测试组的随机字符串灌入布隆过滤器，然后使用全部训练组判断字符串是否存在布隆过滤器中，如果存在，则计数+1，最终打印误判的累计个数和布隆过滤器中的总字符串个数
打印结果如下：
total users :  100000
misjudge count is 525 , total count is 50000, 误判率为 1% 左右
有没有更低的误判率那？当然有了，下面一个方法就是带有参数定制的bloomfilter
'''


def test_misjudge_rate_of_bloom_filter():
    client.delete(BLOOM_FILTER_TEST_KEY)
    rnd_str_len = 64
    misjudge_count = 0
    users = list(set([random_string(rnd_str_len) for i in range(100000)]))
    print("total users : ", len(users))
    # 这里有个坑，如果不使用 int 函数，则会报异常，说索引的起始或者结束不是整数？坑爹呀，int/2 不还是int 吗？python 这么智能吗？
    mid_idx = int(len(users)/2)
    test_users = users[:mid_idx]
    train_users = users[mid_idx:]
    for user in test_users:
        client.execute_command("bf.add", BLOOM_FILTER_TEST_KEY, user)
    print("all tested user are loaded in bloomfilter")
    for train_user in train_users:
        ret = client.execute_command(
            "bf.exists", BLOOM_FILTER_TEST_KEY, train_user)
        if ret == 1:
            misjudge_count += 1

    print("misjudge count is %d" % misjudge_count,
          ", total count is %d" % len(train_users))


'''
使用 redis bloom filter 的 reserve 方法来预定下参数。
如果 key 已经存在，bf.reserve 会报错，bf.reserve 有 3 个参数，分别是 key，error_rate 和 initial_size。错误率越大，需要的空间越大。initial_size 参数表示预计放入的元素数量，当实际数量超出这个数值时，误判率会上升。
所以需要提前设置一个较大的数值避免超出导致误判率升高。如果不使用 bf.reserve，默认的 error_rate 为 0.01，默认的 initial_size 为 100.
打印结果如下：
total user's count 100000
all tested user are all loaded in bloom filter
total users 50000 misjudge count 27
参数设置为 errorRate=0.001, initialCount=50000
刚开始参数设置为 0.01， reids 抛出异常，说是容量不够
'''


def test_bloom_filter_with_reserve_config():
    user_count = 100000
    rnd_string_len = 64
    users = list(set([random_string(rnd_string_len)
                 for i in range(user_count)]))
    mid_idx = 0
    print("total user's count %d" % len(users))
    mid_idx = int(len(users)/2)
    test_users = users[:mid_idx]
    trail_users = users[mid_idx:]
    client.delete(BLOOM_FILTER_TEST_KEY)
    client.execute_command(
        "bf.reserve", BLOOM_FILTER_TEST_KEY, 0.001, int(user_count/2))
    for user in test_users:
        client.execute_command("bf.add", BLOOM_FILTER_TEST_KEY, user)
    print("all tested user are all loaded in bloom filter")
    mis_judge_count = 0
    for user in trail_users:
        ret = client.execute_command("bf.exists", BLOOM_FILTER_TEST_KEY, user)
        if ret == 1:
            mis_judge_count+=1
    print("total users %d" % len(trail_users),
          "misjudge count %d" % mis_judge_count)


if __name__ == "__main__":
    # test_redis_bloom_filter_common()
    # bloom_filter_not_exists()
    #test_misjudge_rate_of_bloom_filter()
    test_bloom_filter_with_reserve_config()
    # for i in range(10):
    #     print(random_string(64))

'''
布隆过滤器的其他应用
在爬虫系统中，我们需要对 URL 进行去重，已经爬过的网页可以不用再爬了。但是 URL 太多了，几千万几个亿，如果用一个集合装下这些 url 地址是非常浪费空间的。这时候就可以考虑使用布隆过滤器了。它可以大幅降低去重存储消耗，只不过也会使得爬虫系统错过少量的页面。

布隆过滤器在 NoSql 数据库领域使用非常广泛，我们平时使用到的 HBase、Cassandra 还有 LevelDB，RocksDB 内部都有布隆过滤器结构，布隆过滤器可以显著降低数据库的 IO 请求数量。当我们查询某个 row 时，可以先通过内存中的布隆过滤器过滤掉大量不存在的 row 请求，然后再去磁盘进行查询。

垃圾又向的的垃圾邮件过滤功能也普遍用到了布隆过滤器，因为用这个过滤器，所以平时也会遇到某些正常的邮件被放进入了垃圾邮件目录中，这个就是误判所致，概率很低。
'''