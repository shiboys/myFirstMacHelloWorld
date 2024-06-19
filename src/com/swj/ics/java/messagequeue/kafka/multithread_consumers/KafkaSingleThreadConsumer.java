package com.swj.ics.java.messagequeue.kafka.multithread_consumers;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.Random;

public class KafkaSingleThreadConsumer {
  private KafkaConsumer<String, String> consumer;
  private int expectedCount;
  Random random;

  public KafkaSingleThreadConsumer(String kafkaServerUrl, String topic, String groupId, int expectedCount) {
    consumer = new KafkaConsumer<String, String>(getConsumerProperties(kafkaServerUrl, groupId));
    consumer.subscribe(Collections.singletonList(topic));
    this.expectedCount = expectedCount;
    random = new Random();
  }

  private Properties getConsumerProperties(String kafkaServerUrl, String groupId) {
    Properties properties = new Properties();
    properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServerUrl);
    properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
    properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

    //单线程的打开 enable.auto.offset.commit=false;
    properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
    properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    return properties;
  }


  public void run() {
    int accumulateConsumedCount = 0;
    while (accumulateConsumedCount < expectedCount) {
      ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofSeconds(1));
      accumulateConsumedCount += consumerRecords.count();
      for (ConsumerRecord<String, String> record : consumerRecords) {
        handleRecords(record);
      }
      System.out.println(String.format("thread:%s, consumed record's count:%d", Thread.currentThread().getName(),
          accumulateConsumedCount));
    }
  }


  void handleRecords(ConsumerRecord<String, String> consumerRecord) {
    // 模拟一个 10 ms 之内的处理
    try {
      Thread.sleep(random.nextInt(10));
    } catch (InterruptedException e) {

    }
  }
}
