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
BLOOM_FILTER_TEST_KEY="bloomfilter_py"


CHARS = "".join([chr(ord('a') + i) for i in range(26)])


def test_redis_bloom_filter_common():
    client.delete(BLOOM_FILTER_TEST_KEY)
    for i in range(10000):
        client.execute_command("bf.add",BLOOM_FILTER_TEST_KEY,"user%d"%i)
        ret=client.execute_command("bf.exists",BLOOM_FILTER_TEST_KEY,"user%d"%i)
        if ret==0:
            print(i)
            break
    print("all this test key in range user[0,10000} exists in bloom filter.")


def random_string(char_length):
    chars = []
    for i in range(char_length):
        char_idx = random.randint(0, len(CHARS))
        chars.append(CHARS[char_idx])
    return "".join(chars)

if __name__=="__main__":
    test_redis_bloom_filter_common()
