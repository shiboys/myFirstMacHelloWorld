package com.swj.ics.java.messagequeue.kafka;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.ConsumerGroupDescription;
import org.apache.kafka.clients.admin.DescribeConsumerGroupsResult;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.ListConsumerGroupOffsetsResult;
import org.apache.kafka.clients.admin.MemberDescription;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/11/18 17:15
 */
public class KafkaConsumerGroupService {
  private String brokerUrl;
  private KafkaConsumer<String, String> kafkaConsumer;
  private AdminClient adminClient;

  public KafkaConsumerGroupService(String bootstrapServer) {
    this.brokerUrl = bootstrapServer;
  }

  // 初始化
  public void init(String groupId) {
    Properties properties = new Properties();
    properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, this.brokerUrl);
    adminClient = KafkaAdminClient.create(properties);
    kafkaConsumer = new KafkaConsumer<>(KafkaGroupUtil.getConsumerProperties(groupId, brokerUrl));
  }

  public void close() {
    if (kafkaConsumer != null) {
      kafkaConsumer.close();
    }
    if (adminClient != null) {
      adminClient.close();
    }
  }

  public List<PartitionAssignedState> listAllGroupConsumerInfos(String groupId) {
    // 初始化 kafkaConsumer
    init(groupId);
    List<PartitionAssignedState> pasList = new ArrayList<>();
    // 有消费者的消费的这些分区
    Set<TopicPartition> assignedPartitionsWithConsumer = new HashSet<>();

    // 从 admin client 中获取 consumer group 的元数据信息，包括并不限于 topic partition
    // 从 admin client 中获取 currentOffset, 当前消费到哪里了。

    DescribeConsumerGroupsResult describeConsumerGroupsResult =
        adminClient.describeConsumerGroups(Collections.singleton(groupId));

    try {
      ConsumerGroupDescription consumerGroupDescription = describeConsumerGroupsResult.all().get().get(groupId);
      Collection<MemberDescription> members = consumerGroupDescription.members();
      // 通过 fetchOffsetRequest 请求获取消费位移
      ListConsumerGroupOffsetsResult listConsumerGroupOffsetsResult = adminClient.listConsumerGroupOffsets(groupId);
      Map<TopicPartition, OffsetAndMetadata> offsets =
          listConsumerGroupOffsetsResult.partitionsToOffsetAndMetadata().get();
      if (members != null && members.size() > 0) {
        // 获取有消费者列表的请
        String groupStatus = consumerGroupDescription.state().toString();
        if (groupStatus != null && groupStatus.equals("Stable")) {
          pasList = getPasListWithConsumer(consumerGroupDescription, offsets, assignedPartitionsWithConsumer, groupId);
        }
      }
      // 无消费者列表的当前消费组的消费情况
      List<PartitionAssignedState> pasWithoutConsumerList =
          getPasListWithoutConsumer(consumerGroupDescription, offsets, assignedPartitionsWithConsumer, groupId);
      if (pasWithoutConsumerList != null) {
        pasList.addAll(pasWithoutConsumerList);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return pasList;
  }

  /**
   * @param description                    消费组描述信息，可以从描述信息中获取 members 消费者信息，也可以获取组协调器的相关信息，一个Node 节点
   * @param offsets                        消费组中消费者当前已经消费的位移 offset 信息
   * @param assignedPartitionsWithConsumer 有消费者信息的这部分分区。在获取无消费组的位移情况时，需要排除这部分信息
   * @param groupId                        组Id
   * @return
   */
  private List<PartitionAssignedState> getPasListWithoutConsumer(ConsumerGroupDescription description,
      Map<TopicPartition, OffsetAndMetadata> offsets, Set<TopicPartition> assignedPartitionsWithConsumer,
      String groupId) {
    return offsets.keySet().stream()
        .filter(tp -> !assignedPartitionsWithConsumer.contains(tp))
        .map(tp -> {
          PartitionAssignedState pas = new PartitionAssignedState();
          long currentOffset = offsets.get(tp).offset();
          long leoOffset = kafkaConsumer.endOffsets(Collections.singleton(tp)).get(tp);
          pas.setGroup(groupId);
          pas.setTopic(tp.topic());
          pas.setCoordinator(description.coordinator());
          pas.setCurrent_offset(currentOffset);
          pas.setLog_end_offset(leoOffset);
          pas.setLag(getLag(leoOffset, currentOffset));
          return pas;
        }).collect(Collectors.toList());
  }

  private long getLag(long logSize, long offset) {
    return logSize < offset ? 0 : logSize - offset;
  }

  /**
   * @param description                    消费组描述信息，可以从描述信息中获取 members 消费者信息，也可以获取组协调器的相关信息，一个Node 节点
   * @param offsets                        消费组中消费者当前已经消费的位移 offset 信息
   * @param assignedPartitionsWithConsumer 有消费者信息的这部分分区
   * @param groupId                        组Id
   * @return
   */
  private List<PartitionAssignedState> getPasListWithConsumer(ConsumerGroupDescription description,
      Map<TopicPartition, OffsetAndMetadata> offsets, Set<TopicPartition> assignedPartitionsWithConsumer,
      String groupId) {
    Collection<MemberDescription> members = description.members();
    List<PartitionAssignedState> pasListWithConsumer = new ArrayList<>();
    for (MemberDescription member : members) {
      if (member.assignment() == null) {
        continue;
      }
      Set<TopicPartition> memberTps = member.assignment().topicPartitions();
      if (memberTps == null || memberTps.isEmpty()) {
        pasListWithConsumer.add(getPathWithoutTopicPartition(description, member, groupId));
      } else {
        // 通过 consumer 来获取 leo 的信息
        Map<TopicPartition, Long> leoMap = kafkaConsumer.endOffsets(memberTps);
        // 将这些带有 消费者信息的集合加入 assigned 集合中
        assignedPartitionsWithConsumer.addAll(memberTps);
        // 生成 PartitionAssignedState pasList
        List<PartitionAssignedState> tempList = memberTps.stream()
            .sorted(Comparator.comparing(TopicPartition::partition))
            .map(tp -> getPasWithConsumer(tp, offsets, leoMap, member, description, groupId))
            .collect(Collectors.toList());
        pasListWithConsumer.addAll(tempList);
      }
    }
    return pasListWithConsumer;
  }

  private PartitionAssignedState getPasWithConsumer(TopicPartition tp, Map<TopicPartition, OffsetAndMetadata> offsets,
      Map<TopicPartition, Long> leoMap, MemberDescription member, ConsumerGroupDescription description,
      String groupId) {
    long offset = offsets.get(tp).offset();
    long leoOffset = leoMap.get(tp);
    return PartitionAssignedState.builder()
        .group(groupId)
        .coordinator(description.coordinator())
        .clientId(member.clientId())
        .consumerId(member.consumerId())
        .clientHost(member.host())
        .current_offset(offset)
        .log_end_offset(leoOffset)
        .lag(getLag(leoOffset, offset))
        .topic(tp.topic())
        .build();
  }

  private PartitionAssignedState getPathWithoutTopicPartition(ConsumerGroupDescription description,
      MemberDescription member, String groupId) {
    return PartitionAssignedState.builder()
        .clientHost(member.host())
        .consumerId(member.consumerId())
        .clientId(member.clientId())
        .coordinator(description.coordinator())
        .group(groupId)
        .build();
  }

  public static void main(String[] args) {
    String groupId = "single-thread-consumer-group";
    String brokerUrl = "localhost:9092";
    KafkaConsumerGroupService service = new KafkaConsumerGroupService(brokerUrl);
    List<PartitionAssignedState> partitionAssignedStates = service.listAllGroupConsumerInfos(groupId);
    if (partitionAssignedStates != null) {
      KafkaGroupUtil.printPasList(partitionAssignedStates);
    }
    service.close();
  }

}
