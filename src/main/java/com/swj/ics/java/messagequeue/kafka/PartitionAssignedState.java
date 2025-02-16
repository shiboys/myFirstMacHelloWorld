package com.swj.ics.java.messagequeue.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.kafka.common.Node;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/11/18 17:16
 * Kafka 消费组消费情况对象封装
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PartitionAssignedState {
  private String group;
  private String topic;
  private int partition;
  private long current_offset;
  private long log_end_offset;
  private long lag;
  private String consumerId;
  private String clientHost;
  private String clientId;
  // 协调器
  private Node coordinator;
}
