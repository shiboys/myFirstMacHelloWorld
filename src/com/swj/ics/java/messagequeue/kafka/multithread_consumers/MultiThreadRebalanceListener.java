package com.swj.ics.java.messagequeue.kafka.multithread_consumers;

import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2024/06/16 14:50
 * 消费者再平衡的监听器
 * 主要处理的是在分区被回收之前，将处理该分区的 worker close ，并将 woker 产生的 offset 进行提交
 * 在再平衡分配后，则 resume 相关暂停的分区
 */
public class MultiThreadRebalanceListener implements ConsumerRebalanceListener {

  private final KafkaConsumer<String, String> consumer;

  private final Map<TopicPartition, OffsetAndMetadata> committingOffsets;

  private final Map<TopicPartition, ConsumerWorker> busyingWorkers;

  public MultiThreadRebalanceListener(
      KafkaConsumer<String, String> consumer,
      Map<TopicPartition, OffsetAndMetadata> committingOffsets,
      Map<TopicPartition, ConsumerWorker> busyingWorkers) {
    this.consumer = consumer;
    this.committingOffsets = committingOffsets;
    this.busyingWorkers = busyingWorkers;
  }



  @Override
  public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
    Map<TopicPartition, ConsumerWorker> tobeDeletedTpWorkers = new HashMap<>();

    // 将要被回收的分区的相关 worker 暂停
    for (TopicPartition tp : partitions) {
      ConsumerWorker worker = busyingWorkers.get(tp);
      if (worker != null) {
        worker.close();
        tobeDeletedTpWorkers.put(tp, worker);
      }
    }

    tobeDeletedTpWorkers.forEach((tp, worker) -> {
      // 每个 worker 等待 1s 的关闭时间
      long offset = worker.awaitForCompletion(1, TimeUnit.SECONDS);
      if (offset > 0) {
        committingOffsets.put(tp, new OffsetAndMetadata(offset));
      }
    });

    // 将 已经完成 offset 的 worker 移出容器，以便被回收
    tobeDeletedTpWorkers.forEach(busyingWorkers::remove);
    //提交正常结束任务的 offset
    if (!committingOffsets.isEmpty()) {
      consumer.commitSync(committingOffsets);
      committingOffsets.clear();
      System.out.println(String.format("committing offset on partition revoked done! tps and offsets: %s", committingOffsets));
    }
  }

  @Override
  public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
    // 恢复相关主题的分配，因为在 handle 的时候暂停，然后分区重新分配之后，继续恢复是一个无害的操作
    //目标分区如果在再平衡之前归当前消费者所有，再平衡之后，不归当前消费者所消费，那么也无妨
    // 当前消费者在再平衡之后可能还会处理不属于它的分区的消息，这是因为 resume 会导致 kafkaConsumer 中缓存的消息继续被当前消费者消费，
    //但是之后就不会被当前消费者获取到不属于它的分区了，因为重分配之后，它不再拥有该分区的消费权利。
    consumer.resume(partitions);
  }
}
