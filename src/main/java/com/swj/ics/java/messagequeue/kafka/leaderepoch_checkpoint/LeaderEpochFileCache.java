package com.swj.ics.java.messagequeue.kafka.leaderepoch_checkpoint;

import static com.swj.ics.java.messagequeue.kafka.KafkaServerUtils.inReadLock;
import static com.swj.ics.java.messagequeue.kafka.KafkaServerUtils.inWriteLock;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.common.TopicPartition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2024/06/17 17:26
 * Represents a cache of mapping (LeaderEpoch => StartOffset) for a particular replica
 * Leader Epoch = epoch assigned to each partition leader by controller
 * StartOffset = offset of the first message in each epoch
 */
@Slf4j
public class LeaderEpochFileCache {
  private static final int UNDEFINED_EPOCH = -1;
  private static final long UNDEFINED_EPOCH_OFFSET = -1;

  private final Supplier<Long> logEndOffsetSupplier;
  private final LeaderEpochCheckpoint checkpoint;
  private final String logPrefix;
  private final ReentrantReadWriteLock lock;
  private final TreeMap<Integer, EpochEntry> epochCache;

  public LeaderEpochFileCache(TopicPartition topicPartition, Supplier<Long> logEndOffsetSupplier,
      LeaderEpochCheckpoint checkpoint) {
    this.logPrefix = String.format("[LeaderEpochCache %s]", topicPartition);
    this.logEndOffsetSupplier = logEndOffsetSupplier;
    this.checkpoint = checkpoint;
    lock = new ReentrantReadWriteLock();
    epochCache = new TreeMap<>();

    inWriteLock(lock, () -> checkpoint.read().forEach(this::assign));
  }

  public void assign(int epoch, long startOffset) {
    EpochEntry epochEntry = new EpochEntry(epoch, startOffset);
    if (assign(epochEntry)) {
      log.debug("{} append new epoch entry {}. cache now contains {} entries", logPrefix, epochEntry,
          epochCache.size());
    }
    flush();
  }

  private boolean assign(EpochEntry entry) {
    if (entry.epoch < 0 || entry.startOffset < 0) {
      throw new IllegalArgumentException(String.format("invalid epochEntry : %s", entry));
    }
    if (!needToBeUpdated(entry)) {
      return false;
    }

    return inWriteLock(lock, () -> {
      if (!needToBeUpdated(entry)) {
        return false;
      }
      maybeTruncateNonMonotonicEntries(entry);
      epochCache.put(entry.epoch, entry);
      return true;
    });
  }

  /**
   * 将非单调递增的节点给删除
   */
  private void maybeTruncateNonMonotonicEntries(EpochEntry newEntry) {
    List<EpochEntry> removedEntries =
        removeFromEnd(entry -> entry.epoch >= newEntry.epoch || entry.startOffset >= newEntry.startOffset);
    // 如果只有一项被移除，而且被移除的 offset 不相等 说明 offset 发生了变更，记录之
    // 除此之外，都应该视为异常现象，记录日志
    if (removedEntries.size() > 1 || (!removedEntries.isEmpty()
        && removedEntries.get(0).startOffset != newEntry.startOffset)) {
      log.warn("{} new epoch entry {} caused truncation of conflicting entries {} "
          + " cache now contains {} entries ", logPrefix, newEntry, removedEntries, epochCache.size());
    }

  }

  List<EpochEntry> removeFromEnd(Predicate<EpochEntry> shouldToBeRemoved) {
    return removeWhileWatching(epochCache.descendingMap().entrySet().iterator(), shouldToBeRemoved);
  }

  List<EpochEntry> removeFromFirst(Predicate<EpochEntry> shouldToBeRemoved) {
    return removeWhileWatching(epochCache.entrySet().iterator(), shouldToBeRemoved);
  }

  List<EpochEntry> removeWhileWatching(Iterator<Map.Entry<Integer, EpochEntry>> iterator,
      Predicate<EpochEntry> shouldToBeRemoved) {
    List<EpochEntry> removedList = new ArrayList<>();
    while (iterator.hasNext()) {
      Map.Entry<Integer, EpochEntry> mapEntry = iterator.next();
      EpochEntry epochEntry = mapEntry.getValue();
      if (shouldToBeRemoved.test(epochEntry)) {
        iterator.remove();
        removedList.add(epochEntry);
      }
    }
    return removedList;
  }

  private boolean needToBeUpdated(EpochEntry newEntry) {
    Optional<EpochEntry> lastEpochEntryOption = latestEntry();
    // lastEpochEntry 初始为 null
    if (!lastEpochEntryOption.isPresent()) {
      return true;
    }
    EpochEntry lastEpochEntry = lastEpochEntryOption.get();
    // epoch 不想等或者 epoch 相等，但是最后一个 startOffset > newStartOffset，也就是说 epoch 相等，则缓存中只能减小 startOffset
    // 这是符合预期的。
    return newEntry.epoch != lastEpochEntry.epoch || lastEpochEntry.startOffset > newEntry.startOffset;
  }

  Optional<EpochEntry> latestEntry() {
    return inReadLock(lock, () -> Optional.of(epochCache.lastEntry()).map(Map.Entry::getValue));
  }

  Optional<EpochEntry> earliestEntry() {
    return inReadLock(lock, () -> Optional.of(epochCache.firstEntry()).map(Map.Entry::getValue));
  }

  boolean nonEmpty() {
    return inReadLock(lock, () -> !epochCache.isEmpty());
  }

  void flush() {
    checkpoint.write(epochCache.values());
  }

  Optional<Integer> latestEpoch() {
    return latestEntry().map(entry -> entry.epoch);
  }

  /**
   * 根据 requestEpoch 返回等于当前 epoch 的 leo 值，
   * 主要通过获取缓存中比 requestEpoch 第一个大的 Epoch 的 startOffset ，可以作为 requestEpoch 的 leo 值返回
   *
   * @param requestEpoch 请求的分区 leader epoch 值
   * @return 返回 >= requestEpoch 的 leo 值。
   */
  public Pair<Integer, Long> endOffsetFor(int requestEpoch) {
    Pair<Integer, Long> result = null;
    if (requestEpoch == UNDEFINED_EPOCH) {
      return Pair.of(UNDEFINED_EPOCH, UNDEFINED_EPOCH_OFFSET);
    } else if (latestEpoch().orElse(UNDEFINED_EPOCH).equals(requestEpoch)) { // 请求的最新的 epoch 的 leo 值
      // 最新的 leo 值 只存在于 leader 或者 follower 的内存中，所有返回外部函数的调用
      result = Pair.of(requestEpoch, this.logEndOffsetSupplier.get());
    } else {
      Map.Entry<Integer, EpochEntry> highEpochEntry = epochCache.higherEntry(requestEpoch);
      if (highEpochEntry == null) { // 比缓存中最大的 epoch 还要大，已经超过记录的范围
        result = Pair.of(UNDEFINED_EPOCH, UNDEFINED_EPOCH_OFFSET);
      } else {
        Map.Entry<Integer, EpochEntry> floorEntry = epochCache.floorEntry(requestEpoch);
        if (floorEntry == null) { // 比最小的 epoch 还要小，此时 highEpoch 就是缓存中的第一个 EpochEntry
          result = Pair.of(requestEpoch, highEpochEntry.getValue().startOffset);
        } else { // 处在 floorEntry 和 highEpochEntry 中间，是闭区间关系
          // 注意我们返回的是 leo 值，就是要比 请求的 epoch 大 1 的那个 epoch 的那个 startOffset，就可以作为 leo 值返回
          result = Pair.of(floorEntry.getKey(), highEpochEntry.getValue().startOffset);
        }
      }
    }
    log.debug("{} processed end offset request for epoch {} and returning epoch {} with end offset {} from epoch cache"
        + " of size {}", logPrefix, requestEpoch, result.getLeft(), result.getRight(), epochCache.size());
    return result;
  }

  /**
   * remove all epoch entries from store with start offset greater or equal to the request end offset
   *
   * @param requestEndOffset
   */
  public void truncateFromEnd(long requestEndOffset) {
    if (requestEndOffset < 0) {
      return;
    }
    inWriteLock(lock, () -> {
      if (latestEntry().filter(entry -> entry.startOffset >= requestEndOffset).isPresent()) {
        // 调用 removeFromEnd ,忽略返回的被删除的项
        List<EpochEntry> removedEntries = removeFromEnd(entry -> entry.startOffset >= requestEndOffset);
        //调用 flush 将 cache 中的内容，全部写入文件
        flush();
        log.debug("{} clear entries {} from epoch cache after truncating to end offset {}, leaving {} entries "
            + "in the epoch cache", logPrefix, removedEntries, requestEndOffset, epochCache.size());
      }
    });
  }

  /**
   * 从头开始将 startOffset 小于 requestStartOffset 的截断，截断到 <= startOffset 的最大的那个 startOffset，
   * 该 offset 需要保留，然后持久化文件
   *
   * @param requestStartOffset 请求的 startOffset
   */
  public void truncateFromStart(long requestStartOffset) {
    if (requestStartOffset < 0) {
      return;
    }
    inWriteLock(lock, () -> {
      List<EpochEntry> removedEntries = removeFromFirst(entry -> entry.startOffset <= requestStartOffset);
      if (removedEntries.isEmpty()) {
        return;
      }
      EpochEntry lastEpochEntry = removedEntries.get(removedEntries.size() - 1);
      EpochEntry updatedEpochEntry = new EpochEntry(lastEpochEntry.epoch, requestStartOffset);
      epochCache.put(updatedEpochEntry.epoch, updatedEpochEntry);

      flush();

      log.debug("{} cleared epoch entries {} and rewrote first entry {} after truncating to first start offset to {} "
              + ",leaving size {} of epoch entries in the cache",
          logPrefix, removedEntries, updatedEpochEntry, requestStartOffset, epochCache.size());
    });
  }

  public void clearAndFlush() {
    inWriteLock(lock, () -> {
      epochCache.clear();
      flush();
    });
  }

  public void clear() {
    inWriteLock(lock, epochCache::clear);
  }

  public Collection<EpochEntry> epochEntries() {
    return epochCache.values();
  }
}


@AllArgsConstructor
class EpochEntry {
  public int epoch;
  public long startOffset;

  @Override
  public String toString() {
    return "EpochEntry{" +
        "epoch=" + epoch +
        ", startOffset=" + startOffset +
        '}';
  }
}
