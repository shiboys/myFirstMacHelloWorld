package com.swj.ics.java.messagequeue.kafka;

import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/11/14 18:35
 */

public class PartitionRebalanceListener implements ConsumerRebalanceListener {

  KafkaConsumer<String, String> consumer;
  Map<TopicPartition, ConsumerWorker> busyingWorkers;
  Map<TopicPartition, OffsetAndMetadata> offsets;

  public PartitionRebalanceListener(KafkaConsumer<String, String> consumer,
      Map<TopicPartition, ConsumerWorker> busyingWorkers, Map<TopicPartition, OffsetAndMetadata> offsets) {
    this.consumer = consumer;
    this.busyingWorkers = busyingWorkers;
    this.offsets = offsets;
  }


  @Override
  public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
    /**
     * 处理方式是将 partitions 中所有的 tp 在 busyingWorkers 中对应的 worker 摘除，
     * 同时将 worker 关闭，并将 对 tp 对应的 offset 进行提交
     */
    // 将未提交的 offset 进行提交
    // 然后对所有的 worker 进行关闭
    for (TopicPartition tp : partitions) {
      ConsumerWorker worker = busyingWorkers.remove(tp);
      if (worker != null) {
        worker.close();
        long offset = worker.waitForCompletion();
        if (offset > 0) {
          offsets.put(tp, new OffsetAndMetadata(offset));
        }
      }
    }

    Map<TopicPartition,OffsetAndMetadata> appliedOffsets = new HashMap<>();
    partitions.forEach(tp->{
      // 只从 offsets 里面删除当前 partitions 参数的分区，因为 offsets 可能存在其他 topic 的分区
      OffsetAndMetadata offset = offsets.remove(tp);
      if(offset != null) {
        appliedOffsets.put(tp,offset);
      }
    });

    try {
      consumer.commitSync(appliedOffsets);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
    // 重新对这些 partitions 进行恢复消费，resume 没有副作用。
    consumer.resume(partitions);
  }
}
