import random
import redis
import time

# 使用 滑动窗口 来实现的简单限流

client = redis.StrictRedis()


class SimpleSildeWindow:

    def __init__(self, time_window_second, window_ele_size):
        self.time_window_s = time_window_second
        self.window_el_size = window_ele_size

    '''
    >>> print(time.time())
1688617951.5554152
>>> print(int(round(time.time() * 1000)))
1688617968581
>>> print(round(time.time() * 1000))
1688617977175
>>> print(time.time() * 1000)
1688617989804.8608
python 的毫秒是通过以上方法获取的
    '''
    # 该方法的缺点之一是如果当前用户的流量巨大，会瞬间造成大量的无用的元素存在 redis 里面，因为首先调用了 zadd

    def is_action_allowed(self, user_id, action):
        window_limit_key = "hist:%s:%s" % (user_id, action)
        # 毫秒作为单位
        now_ts = int(time.time() * 1000)
        item_count = 0
        with client.pipeline() as pipe:
            # 记录行为
            pipe.zadd(window_limit_key, {now_ts: now_ts})
            # 淘汰滑动窗口之外的元素
            pipe.zremrangebyscore(window_limit_key, 0,
                                  now_ts - (self.time_window_s*1000))
            # 刚从开始这个命令调用错误，使用了 scard ，尴尬
            pipe.zcard(window_limit_key)  # 获取当前窗口剩余的元素的数量
            # 防止冷 key  的存在，并且让 key 的过期时长 + 1秒
            pipe.expire(window_limit_key, self.time_window_s + 1)
            # 执行命令
            _, _, item_count, _ = pipe.execute()

        print("item count", item_count)
        return item_count <= self.window_el_size

# def put_ele_to_slide_window()


if __name__ == "__main__":
    slideWindow = SimpleSildeWindow(5, 5)
    for i in range(20):
        time.sleep(200*random.random()/100.0)
        allowed = slideWindow.is_action_allowed("peter", "reply")
        print(allowed)
