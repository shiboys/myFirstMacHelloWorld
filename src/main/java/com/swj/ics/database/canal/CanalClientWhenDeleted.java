package com.swj.ics.database.canal;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.alibaba.otter.canal.protocol.exception.CanalClientException;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2024/11/28 16:11
 */
@Slf4j
public class CanalClientWhenDeleted {

  private static final String CONSUMER_GROUP = CanalClientWhenDeleted.class.getName();
  private static final String DEFAULT_SCHEMA = "metadata";
  private static final String DEFAULT_TOPIC = "canal";
  private static final String DELETE_EVENT_TYPE = "DELETE";
  private static final int batchSize = 1000;

  public static void main(String[] args) {
    //canalDemo();
    try {
      Class.forName(io.netty.util.internal.shaded.org.jctools.util.UnsafeRefArrayAccess.class.getName());
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  static void canalDemo() {
    /**
     * destination 是我们同步数据源的配置文件，在 canal.property 中有配置项 canal.destinations = example
     *  如果改为 canal.destinations =
     *  则表示为 取消默认的 example , 改用自动扫描，会扫描 conf 文件夹下的文件夹名(metrics 和 spring 除外), 来作为 example 实例，
     *  一般一个 example 对应一个数据库的同步
     */
    /**
     * 11111 端口，没打开，是因为我使用了 Kafka 模式，看起来 Tcp 这个模式不能用，只能改用 Kafka Consumer
     * 因为那个11111端口是给 tcp模式（netty）时候用的，你应该是用了kafka或者rocketmq吧，就不会去起这个端口了。可以看下 CanalController那一块初始化的逻辑（在构造函数里）。
     */

    /**
     * find 查找多个扩展名文件
     * find ./ -type f \( -name "*.properties" -or -name "*.yml" \)
     */

    int emptyCount = 0;

    String db = "canal", username = "datax", pwd = "123456";
    Connection mysqlConn;
    CanalConnector connector = null;
    String sql =
        "insert into sync_deleted_table(table_name,ids) values(?,?) on duplicate key update ids=concat(ids,',',?);";
    try {
      mysqlConn = DriverManager
          .getConnection("jdbc:mysql://127.0.0.1:3306/" + db + "?zeroDateTimeBehavior=CONVERT_TO_NULL", username, pwd);
      int emptyThresholdCount = 120;
      PreparedStatement ps = mysqlConn.prepareStatement(sql);
      connector = initCanalConnector();
      KafkaConsumer<String, String> consumer = getDefaultKafkaConsumer();
      long batchId = 0, recordCount = 0;
      while (emptyCount < emptyThresholdCount) {

        Message message = null;
        if (connector != null) {
          message = connector.getWithoutAck(batchSize);
          if (message == null || (batchId = message.getId()) <= 0 || message.getEntries().isEmpty()) {
            recordCount = 0;
          } else {
            recordCount = message.getEntries().size();
          }
          handleCanalEntries(message.getEntries(), ps);
        } else {
          recordCount = handleMessageFromKafka(consumer, ps);
        }

        if (recordCount == 0) {
          emptyCount++;
          Thread.sleep(1000);
        } else {
          emptyCount = 0;
        }
        if (connector != null) {
          connector.ack(batchId);
        }
      }

      System.out.println("empty too many times, exit!");
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      if (connector != null) {
        connector.disconnect();
      }
    }
  }

  private static int handleMessageFromKafka(KafkaConsumer<String, String> consumer, PreparedStatement ps) {

    ConsumerRecords<String, String> consumerRecords = consumer.poll(batchSize);
    if (consumerRecords.isEmpty()) {
      return 0;
    }
    int recordsCount = 0;
    for (ConsumerRecord<String, String> record : consumerRecords) {
      if (record.value() == null || record.value().isEmpty()) {
        continue;
      }
      recordsCount++;
      handleCanalJsonEntries(record.value(), ps);
    }
    return recordsCount;
  }


  static CanalConnector initCanalConnector() {
    try {
      CanalConnector connector =
          CanalConnectors.newSingleConnector(new InetSocketAddress("127.0.0.1", 1111), "example", "", "");
      String subscription = "metadata\\\\..*";
      connector.connect();
      // 回滚上一次未提交完的 batch
      connector.rollback();
      connector.subscribe(subscription);
      return connector;
    } catch (CanalClientException e) {
      log.error("failed to connected canal server.", e);
      return null;
    }
  }

  static KafkaConsumer<String, String> getDefaultKafkaConsumer() {
    Properties props = new Properties();
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
    props.put(ConsumerConfig.GROUP_ID_CONFIG, CONSUMER_GROUP);
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

    KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
    consumer.subscribe(Collections.singleton(DEFAULT_TOPIC));
    return consumer;
  }


  static void handleCanalEntries(List<CanalEntry.Entry> entries, PreparedStatement ps) {
    for (CanalEntry.Entry entry : entries) {
      if (entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONBEGIN ||
          entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONEND) {
        continue;
      }
      CanalEntry.RowChange rowChange = null;
      String schemaName, tableName;
      try {
        rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
        CanalEntry.EventType eventType = rowChange.getEventType();
        schemaName = entry.getHeader().getSchemaName();
        tableName = entry.getHeader().getTableName();
        if (rowChange.getIsDdl() || !schemaName.equals(DEFAULT_SCHEMA)) {
          System.out.printf("================&gt; binlog[%s:%s] , name[%s,%s] , eventType : %s%n",
              entry.getHeader().getLogfileName(), entry.getHeader().getLogfileOffset(),
              schemaName, tableName, eventType);
          log.info(rowChange.getSql());
          continue;
        }
        for (CanalEntry.RowData rowData : rowChange.getRowDatasList()) {
          // 目标数据库的删除
          if (eventType == CanalEntry.EventType.DELETE) {
            handleDeletedRecords(rowData, schemaName, tableName, ps);
          }
        }


      } catch (InvalidProtocolBufferException e) {
        throw new RuntimeException(e);
      }
    }
  }

  static void handleCanalJsonEntries(String jsonData, PreparedStatement ps) {
    JSONObject jsonObject = JSONObject.parseObject(jsonData);

    if (!DELETE_EVENT_TYPE.equalsIgnoreCase(jsonObject.getString("type"))
        || !DEFAULT_SCHEMA.equalsIgnoreCase(jsonObject.getString("database"))) {
      return;
    }
    String tableName = jsonObject.getString("table");
    JSONArray jsonArray = jsonObject.getJSONArray("data");
    List<String> ids = new ArrayList<>();
    for (int i = 0; i < jsonArray.size(); i++) {
      ids.add(jsonArray.getJSONObject(i).get("id").toString());
    }
    if (ids.isEmpty()) {
      return;
    }
    String idstr = String.join(",", ids);
    log.info("record was deleted from table {} and id is {}", tableName, idstr);

    try {
      ps.setString(1, tableName);
      ps.setString(2, idstr);
      ps.setString(3, idstr);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  static void handleDeletedRecords(CanalEntry.RowData rowData, String schemaName, String tableName,
      PreparedStatement ps) {
    List<CanalEntry.Column> columnList = rowData.getAfterColumnsList();
    tableName = (schemaName == null || schemaName.isEmpty()) ? tableName : schemaName + "." + tableName;
    try {

      for (CanalEntry.Column column : columnList) {
        // 如果是联合主键，这段代码还有问题那
        if (!column.getIsKey()) {
          continue;
        }
        log.info("record was deleted from table {} and id is {}", tableName, column.getValue());

        ps.setString(0, tableName);
        ps.setString(1, column.getValue());
        ps.setString(2, column.getValue());
        ps.executeUpdate();
        break;
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
