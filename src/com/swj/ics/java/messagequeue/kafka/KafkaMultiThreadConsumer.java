package com.swj.ics.java.messagequeue.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Kafka 的多个线程消费客户端
 */
public class KafkaMultiThreadConsumer {
  /**
   * 核心思路是一个 Consumer 主线程，将拉取到的消息，按分区分发给所有线程池中的业务处理线程
   * 然后业务处理线程在处理完业务之后，将 offset 通过 computableFuture 返回
   * consumer 主线程负责同步提交 offset 至 kafka broker
   * 并同时监听 PartitionReBalance, 在分区被回收之前，提交分区，
   * 根据上述思路，主要分为一下几个类：
   * 1、ConsumerWorker 业务处理类，主要负责处理某个分区的 records
   * 2、MultiThreadConsumer ，Kafka Consumer 主类，负责拉取数据，然后根据分区分发给基于线程池的 worker 类来处， 最后收集完成的 offset ，统一进行提交
   * 3、CustomReBalanceListener 自定义的分区重分配监听程序，监听分区重分配
   * 4、Test 测试类，主要测试 单线程和多线程的对比
   */

  // 提交间隔为 3s
  private static final int DEFAULT_SUBMIT_INTERVAL = 3000;

  private KafkaConsumer<String, String> consumer;
  Map<TopicPartition, Long> totalPartitionRecordsCount;
  Map<TopicPartition, OffsetAndMetadata> commitOffsets;
  Map<TopicPartition, ConsumerWorker> busyingWorkers;
  long lastCommitTime;
  long expectedCount;

  ExecutorService threadPool;

  public KafkaMultiThreadConsumer(String topic, String groupId, String brokerUrl, long expectedCount) {
    totalPartitionRecordsCount = new HashMap<>();
    commitOffsets = new HashMap<>();
    busyingWorkers = new HashMap<>();
    this.expectedCount = expectedCount;
    this.lastCommitTime = System.currentTimeMillis();
    int threadCount = 10 * Runtime.getRuntime().availableProcessors();
    System.out.println("thread count is " + threadCount);

    consumer = new KafkaConsumer<>(getConsumerProperties(groupId, brokerUrl));
    consumer.subscribe(Collections.singletonList(topic),
        new PartitionRebalanceListener(consumer, busyingWorkers, commitOffsets));

    threadPool = new ThreadPoolExecutor(threadCount, threadCount, 10, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1000),
        r -> {
          Thread t = new Thread(r);
          t.setDaemon(true);
          return t;
        }, new ThreadPoolExecutor.CallerRunsPolicy());
  }

  private Properties getConsumerProperties(String groupId, String brokerUrl) {
    Properties properties = new Properties();
    properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrl);
    properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    //
    properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
    properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

    return properties;
  }

  public void run() {
    // 多线程消费的主函数，主要分为一下几个步骤：
    // 1、拉取消费消息，distribute 分发消息给多线程和线程池，并 pause consumer
    //2、 consumer主线程获取各个 worker 的 offset,并生成当前已经消费的消息个数，并 resume consumer
    //3、提交 offset 至 kafka broker
    ConsumerRecords<String, String> consumerRecords = null;
    try {
      while (true) {
        consumerRecords = consumer.poll(Duration.ofSeconds(1));
        distributeRecords(consumerRecords);
        checkBusyingWorkers();
        submitOffsets();
        if (totalPartitionRecordsCount.values().stream().mapToLong(x -> x).sum() >= expectedCount) {
          break;
        }
      }
    } finally {
      consumer.close();
    }
  }


  private void distributeRecords(ConsumerRecords<String, String> consumerRecords) {
    consumerRecords.partitions().forEach(tp -> {
      List<ConsumerRecord<String, String>> tpConsumerRecords = consumerRecords.records(tp);
      final ConsumerWorker worker = new ConsumerWorker(tpConsumerRecords);
      CompletableFuture.supplyAsync(worker::run, threadPool);
      busyingWorkers.put(tp, worker);
    });
    consumer.pause(consumerRecords.partitions());
  }

  /**
   * 收集 所有的 工作线程生成的 offset，并生成当前已经消费的消息个数, 最后将相关 partition 进行 resume
   */
  private void checkBusyingWorkers() {
    Set<TopicPartition> finishedTps = new HashSet<>();
    busyingWorkers.forEach((tp, work) -> {
      // 只有计算完成的分区，才会重新被 resume 开始重新消费
      if (work.isCompleted()) {
        finishedTps.add(tp);
      }
      // 计算总的消息个数。 这里计算的 totalOffset 默认都是从 0 开始，否则计算错误
      long offset = work.getLatestOffset();
      totalPartitionRecordsCount.put(tp, offset);
      // offset 可能小于 0
      if (offset > 0) {
        commitOffsets.put(tp, new OffsetAndMetadata(offset));
      }
    });
    // 从 busying workers 里面移除 已经完成业务逻辑并生成 offset 的工作线程
    finishedTps.forEach(busyingWorkers::remove);
    // 已完成的分区重新开始消费
    consumer.resume(finishedTps);
  }

  // 手动提交位移
  private void submitOffsets() {
    // 手动提交这里有个小优化， 为了防止频繁提交，需要设置一个 提交的 Interval。
    long currentTime = System.currentTimeMillis();
    if (currentTime - lastCommitTime >= DEFAULT_SUBMIT_INTERVAL && !commitOffsets.isEmpty()) {
      consumer.commitSync(commitOffsets);
      commitOffsets.clear();
      lastCommitTime = currentTime;
    }
  }

}
