package com.swj.ics.java.messagequeue.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.Random;
import java.util.concurrent.Future;
import java.util.zip.CRC32;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/11/14 19:21
 */
public class MultiThreadConsumerTest {
  private static final int RECORDS_COUNT = 50_000;
  private static final int STEP_COUNT = 50;
  //private static final int EXPECTED_TEST_COUNT = 45_000;
  private static final int EXPECTED_TEST_COUNT = 100_000;

  public static void main(String[] args) {
    //String topic = "shiboys3";// 3 个分区
    String topic = "kafka_clcn_100";// 100 个分区
    String brokerUrl = "localhost:9092";
    //testProducer(topic, brokerUrl);
    testSingleThreadConsumer(topic, brokerUrl);
    //testMultiThread(topic, brokerUrl);
    /**
     * 结论，3个分区的topic经过测试发现，单线程耗时 250，多线程耗时 90 seconds
     * 100 个分区的 topic 多线程 80 个线程 耗时 5 秒钟
     * multi thread consumer consumed 45000 records with topic of 3 partition takes 5 seconds
     */
  }

  private static void testMultiThread(String topic, String brokerUrl) {
    String groupId = "multi-thread-consumer-group";
    KafkaMultiThreadConsumer multiThreadConsumer =
        new KafkaMultiThreadConsumer(topic, groupId + 1, brokerUrl, EXPECTED_TEST_COUNT);
    long now = System.currentTimeMillis();
    multiThreadConsumer.run();
    long elaspedSeconds = (System.currentTimeMillis() - now) / 1000;
    System.out.println(
        String.format("multi thread consumer consumed %d records with topic of 3 partition takes %d seconds",
            EXPECTED_TEST_COUNT, elaspedSeconds));
  }

  private static void testSingleThreadConsumer(String topic, String producerUrl) {
    String groupId = "single-thread-consumer-group";
    KafkaSingleThreadConsumer singleThreadConsumer =
        new KafkaSingleThreadConsumer(producerUrl, topic, groupId, EXPECTED_TEST_COUNT);
    long now = System.currentTimeMillis();
    singleThreadConsumer.run();
    long elaspedSeconds = (System.currentTimeMillis() - now) / 1000;
    System.out.println(
        String.format("single thread consumer consumed %d records with topic of 3 partition takes %d seconds",
            EXPECTED_TEST_COUNT, elaspedSeconds));
  }

  private static void testProducer(String topic, String producerUrl) {
    int start = 1504;
    testProduceRecord(topic, producerUrl, start);
    System.out.println("done");
  }

  static void testProduceRecord(String topic, String producerUrl, int start) {
    KafkaProducer<String, String> producer = new KafkaProducer<>(getProducerProperties(producerUrl));
    CRC32 crc32 = new CRC32();
    Random random = new Random();

    for (int i = start; i <= start + RECORDS_COUNT; i++) {
      if (i % STEP_COUNT == 0) {
        Future<RecordMetadata> future =
            producer.send(new ProducerRecord<>(topic, String.valueOf(i), getCrc32Val(i, crc32)));
        try {
          RecordMetadata recordMetadata = future.get();
          System.out.println("send count : " + i + " and offset = " + recordMetadata.offset());
          Thread.sleep(random.nextInt(10));
        } catch (Exception e) {
          e.printStackTrace();
        }
      } else {
        producer.send(new ProducerRecord<>(topic, String.valueOf(i), getCrc32Val(i, crc32)));
      }
    }
  }

  static String getCrc32Val(int val, CRC32 crc32) {
    crc32.update(String.valueOf(val).getBytes());
    return Long.toHexString(crc32.getValue());
  }

  static Properties getProducerProperties(String producerUrl) {
    Properties props = new Properties();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, producerUrl);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props.put(ProducerConfig.ACKS_CONFIG, "-1");
    return props;
  }
}
