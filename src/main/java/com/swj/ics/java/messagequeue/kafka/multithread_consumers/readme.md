## Kafka 多线程 Consumer

该版本的 Kafak 多线程 Consumer，我认为是为数不多的精品多线程消费者，参考自 Apache Kafka Commiter 胡夕的博客，地址为
https://www.cnblogs.com/huxi2b/p/13668061.html
使用了很多 java 和 kafka 的异步编程技术，实现多线程地消费，即高性能，也线程安全，更是具有非常高的弹性消费能力。
使用到的技术主要有：
jdk 方面：
    线程池技术
    CompletableFuture 异步计算技术
    多线程的线程之间的边界划分的非常清楚，使我们即使是使用简单的 HashMap 也能保证线程安全
    
Kafka 方面，主要是围绕 Kafka Consumer 来进行的:
    consumer 的 offset 提交使用手动方式，按分区提交
    使用 consumer 的 pause/resume 来手动地控制消息的暂停和技术消费
    使用 ConsumerRebalanceListener 来消费组监听分区再平衡，在回收分区(revokePartition) 之前提交已消费的分区，在收到分区后，resume 暂停的分区