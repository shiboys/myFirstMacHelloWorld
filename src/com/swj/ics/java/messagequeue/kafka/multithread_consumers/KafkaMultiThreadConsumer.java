package com.swj.ics.java.messagequeue.kafka.multithread_consumers;

import lombok.extern.slf4j.Slf4j;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2024/06/16 10:43
 * KafkaMultiThreadConsumer 的重写
 * 这是 Kafka Consumer 多线程消费的入口类，这个类的主要处理逻辑是创建 Kafka Consumer 客户端、订阅相关主题进行消费，循环执行消费和手动提交位移：
 * 循环执行的主要逻辑如下：
 * 1、将消息按照分区分发给每个新生成的 Worker 任务，使用线程池来运行这些 Worker 任务；并暂停该分区的消费。这里面就有一个比较诡异的逻辑：
 * 既然分区的消息都交给 worker 了，为啥还要暂停分区消费？这里我们还是要好好理清 worker 任务的工作内容和暂停的原理：
 * worker 任务的主要工作内容是处理已经拉取到某个分区的消息，就是这批消息已经返回给消费者的业务逻辑了，而我们暂停的只是后来要继续消费的消息
 * 这里也突出了 worker 的生命周期，它只是处理这一批消息，处理完就需要被移出容器，进而被 GC 掉，因此 它不涉及主动去拉取分区消息的情况，这也就保证了
 * 消息不会被错乱消费。另外，暂停的原理我们通过源码指定，只是在 KafkaConsumer 客户端做了拦截，被暂停的消息保留在 KafkaConsumer 的缓存中，并未吐出
 * 给消费者的处理逻辑，因此这里的暂停只是 KafkaConsumer 暂时获取不到该分区的消息了，等调用 resume 之后，就可以继续获取，这样就从另外一方面保证了
 * worker 只会处理一个主题分区一个批次的消息，处理完不会有第二个相同主题分区批次的消息交给它处理，这样它的计算 latestOffset 的 completableFuture 和 awaitForComplete()
 * 就能及时的返回。
 * 2、对包含上述提交 worker 任务的容器进行检查，对已完成消费位移获取的 TP 进行 resume 操作，记录其 <TP,offset>  以便下一步的位移提交操作
 * 同时将已完成的 worker 移除该容器，以便进行 GC 回收掉该 worker。
 * 3、根据位移提交间隔，提交消费位移
 * 4、统计消费的数据量，使用 AtomicLong 来跟踪每个 消息被处理的情况，每个消息被处理之后，计数 +1，最终统计消费的消息计数，当计数累计到 expectedCount
 * 之后，当前 循环结束
 */
@Slf4j
public class KafkaMultiThreadConsumer {

  private static final long DEFAULT_COMMIT_INTERVAL = 3000;

  private final Executor threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 5,
      r -> {
        Thread thread = new Thread(r);
        thread.setDaemon(true);
        return thread;
      });

  private Map<TopicPartition, ConsumerWorker> busyingWorkers = new HashMap<>();

  private final KafkaConsumer<String, String> consumer;
  private final AtomicLong consumedMessageCounter = new AtomicLong();
  private final long expectedRecordsCount;
  private long lastCommitTimeMs;
  private Map<TopicPartition, OffsetAndMetadata> offsetsCommittedMap = new HashMap<>();



  public KafkaMultiThreadConsumer(String groupId, String topic, String brokerUrls, long expectedRecordsCount) {
    Properties props = new Properties();
    props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrls);
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "FALSE");
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

    this.consumer = new KafkaConsumer<>(props);

    this.expectedRecordsCount = expectedRecordsCount;

    consumer.subscribe(Collections.singletonList(topic),
        new MultiThreadRebalanceListener(consumer, offsetsCommittedMap, busyingWorkers));
  }

  public void run() {
    while (true) {
      try {
        ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofSeconds(1));
        distributeRecords(consumerRecords);
        watchBusyingWorkers();
        commitOffsets();
        if (consumedMessageCounter.get() >= expectedRecordsCount) {
          System.out.println(String.format("handled the expected records count (%d)", consumedMessageCounter.get()));
          break;
        }
      } catch (Exception e) {
        // 拉取消息的过程中可能出现异常，比如网络暂时中断
        log.error("failed to poll messages from kafka.", e);
      } finally {
        // 最佳实践建议，这里的 consumer 不进行关闭，或者统计异常次数大于阈值进行关闭
        // 这里 demo ，目前省略这个逻辑
        consumer.close();
      }
    }
  }

  /**
   * 提交已完成消费的位移。
   * 统计第二步中已经完成的业务处理的 消息的 offset,将 它们提取出来，进行提交
   */
  private void commitOffsets() {
    // 注意我们提交 offset 的时候，有个提交 interval，在 interval 之后再进行提交，防止提交过于频繁
    long nowMs = System.currentTimeMillis();
    if (nowMs - lastCommitTimeMs > DEFAULT_COMMIT_INTERVAL) {
      if (!offsetsCommittedMap.isEmpty()) {
        lastCommitTimeMs = nowMs;
        consumer.commitSync(offsetsCommittedMap);
        offsetsCommittedMap.clear();
      }
    }
  }

  /**
   * 观察 Worker Consumer 的工作进度。对于已完成的消费记录，将其分区和offset 记录下来，供下一步手动提交位移使用
   */
  private void watchBusyingWorkers() {
    Set<TopicPartition> completedPartitions = new HashSet<>();
    busyingWorkers.forEach((tp, worker) -> {
      // worker 消费完成
      if (worker.isFinished()) {
        completedPartitions.add(tp);
      }
      if (worker.getLastedOffset() > 0) { // worker 进行了业务处理，有消费位移了
        this.offsetsCommittedMap.put(tp, new OffsetAndMetadata(worker.getLastedOffset()));
      }
    });

    // 对已完成消费的分区，worker 的使命也完成了，此时需要从 busyingWorkers 容器中移除，让 GC 及时回收该 worker
    completedPartitions.forEach(busyingWorkers::remove);
    // 对已完成的分区，让 KafkaConsumer 恢复继续拉取相关分区的消息
    consumer.resume(completedPartitions);
  }

  /**
   * 分发消息到一个新的 Worker 任务对象上，使得 woker 的主要工作就是消费完这批消息
   * 并暂停这些分区的消费
   *
   * @param consumerRecords
   */
  private void distributeRecords(ConsumerRecords<String, String> consumerRecords) {
    if (consumerRecords.isEmpty()) {
      return;
    }
    Set<TopicPartition> needTobePausedTps = new HashSet<>();
    for (TopicPartition tp : consumerRecords.partitions()) {
      needTobePausedTps.add(tp);
      List<ConsumerRecord<String, String>> onePartitionRecords = consumerRecords.records(tp);
      ConsumerWorker consumerWorker = new ConsumerWorker(onePartitionRecords, consumedMessageCounter);
      // 将该对象在 busyingWorkers 容器中也引用起来
      busyingWorkers.put(tp, consumerWorker);
      // 将 worker 对象提交到线程池运行
      CompletableFuture.supplyAsync(consumerWorker::run, threadPool);
    }
    // 暂停这些分区的消费
    consumer.pause(needTobePausedTps);
  }
}
