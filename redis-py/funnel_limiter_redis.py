import redis
import time
import random

# 使用漏斗的限流原理


class FunnelRateLimiter:
    def __init__(self, capacity, leak_rate):
        self.capacity = capacity  # 容量
        self.leak_rate = leak_rate  # 漏斗的流速
        self.left_cap = capacity  # 漏斗的剩余容量默认等于容量，也就是漏斗默认是空的
        self.last_mark_time = time.time()

    def flow_in_funnel_with_fixed_rate(self):
        delta_time = time.time() - self.last_mark_time
        delta_cap = delta_time * self.leak_rate
        if (delta_cap < 1):
            return
        # 恒定速率流动，则剩余容量增大
        self.left_cap += delta_cap
        # 记录更新时间
        self.last_mark_time = time.time()
        # 边界判断
        if (self.left_cap > self.capacity):
            self.left_cap = self.capacity

    # 往流通里面灌水，模拟流量
    def watering_to_funnel(self, watering_rate):
        # 每次灌水之前，先调用 flow ，来模拟漏斗的流动
        self.flow_in_funnel_with_fixed_rate()
        if self.left_cap >= watering_rate:
            self.left_cap -= watering_rate  # 模拟注水请求，剩余容量减少
        print("left capacity %d" % self.left_cap)
        if int(self.left_cap) <= 0:
            return False
        else:
            return True


funnels = {}  # 记录所有 funnel


def is_action_allowed(action_key, user_id, leak_rate, funnel_capacity):
    action_key = "%s:%s" % (user_id, action_key)
    funnel = funnels.get(action_key)
    if funnel is None:
        funnel = FunnelRateLimiter(funnel_capacity, leak_rate)
        funnels[action_key] = funnel
    return funnel.watering_to_funnel(1)


for i in range(50):
    time.sleep(random.random())
    print(is_action_allowed('reply', 'peter', 0.5, 15))
