package com.swj.ics.java.messagequeue.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.List;
import java.util.Optional;
import java.util.Properties;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/11/18 17:15
 */
public class KafkaGroupUtil {
  public static Properties getConsumerProperties(String groupId, String brokerUrl) {
    Properties props = new Properties();
    props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrl);
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

    return props;
  }

  /**
   * 打印消费情况的最终结果。
   *
   * @param pasList
   */
  public static void printPasList(List<PartitionAssignedState> pasList) {
    //topic,partition,currentOffset,leo,lag,consumer_id,client_host, client_id;
    String format = "%-20s %-10s %-15s %-15s %-10s %-50s %-20s %s";
    System.out.println(
        String.format(format, "TOPIC", "PARTITION", "CURRENT-OFF-SET", "LOG-END-OFFSET", "LAG", "CONSUMER_ID", "HOST",
            "CLIENT_ID"));

    if (pasList == null || pasList.isEmpty()) {
      return;
    }
    for (PartitionAssignedState pas : pasList) {
      System.out.println(String.format(format,
          pas.getTopic(), pas.getPartition(),
          pas.getCurrent_offset(), pas.getLog_end_offset(), pas.getLag(),
          Optional.ofNullable(pas.getConsumerId()).orElse("-"),
          Optional.ofNullable(pas.getClientHost()).orElse("-"),
          Optional.ofNullable(pas.getClientId()).orElse("-")
      ));
    }
  }
}
