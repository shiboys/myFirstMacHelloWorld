package com.swj.ics.java.redission;

import org.redisson.Redisson;
import org.redisson.api.PendingResult;
import org.redisson.api.RStream;
import org.redisson.api.RedissonClient;
import org.redisson.api.StreamMessageId;
import org.redisson.api.stream.StreamAddArgs;
import org.redisson.api.stream.StreamReadGroupArgs;
import org.redisson.config.Config;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/07/09 18:12
 * 使用 redission 来调用 redis 的消息队列功能
 * 实现 redis 的消息队列的演示功能
 */
public class RedisStreamTest {

  private static final String STREAM_ID = "myJavaStream#1";

  private static RedissonClient getRedissonClient(String ip, int port) {
    Config config = new Config();
    config.useSingleServer().setAddress(ip + ":" + port);
    return Redisson.create(config);
  }

  private static void testPublishMsgToStream(RedissonClient redissonClient) {
    RStream<Object, Object> stream = redissonClient.getStream(STREAM_ID);
/*    stream.add("speed", 19);
    stream.add("velocity", "39%");
    stream.add("temperature", "10C");
    // add 方法被 deprecated
    */

    stream.add(StreamAddArgs.entries("speed", 19, "volocity", "39%", "temperature", "39C"));
    System.out.println("done");
  }

  public static void consumerMessageFromRedisStream(RedissonClient redissonClient) throws InterruptedException {
    RStream<Object, Object> stream = redissonClient.getStream(STREAM_ID);

    String groupId = "sensors-data";
    PendingResult pendingInfo = stream.getPendingInfo(groupId);

    if (pendingInfo == null || pendingInfo.getConsumerNames() == null) {
      stream.createGroup(groupId, StreamMessageId.ALL);
    }

    boolean consumed = false;
    while (!consumed) {
      Map<StreamMessageId, Map<Object, Object>> messageIdMap =
          stream.readGroup("sensors-data", "consumer1", StreamMessageId.NEVER_DELIVERED);
      if (messageIdMap == null) {
        System.out.println("messageIdMap is null");
        TimeUnit.SECONDS.sleep(1);
        continue;
      }
      for (Map.Entry<StreamMessageId, Map<Object, Object>> entry : messageIdMap.entrySet()) {
        Map<Object, Object> msg = entry.getValue();
        System.out.println(msg);
        stream.ack("sensors-data", entry.getKey());
        consumed = true;
      }
    }
  }

  public static void main(String[] args) throws InterruptedException {
    String ip = "10.211.55.6";
    int port = 6379;
    RedissonClient redissonClient = getRedissonClient("redis://" + ip, port);
    consumerMessageFromRedisStream(redissonClient);
    //testPublishMsgToStream(redissonClient);
  }

}
