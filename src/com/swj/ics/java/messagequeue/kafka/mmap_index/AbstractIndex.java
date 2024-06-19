package com.swj.ics.java.messagequeue.kafka.mmap_index;

import static com.swj.ics.java.messagequeue.kafka.KafkaServerUtils.inLock;

import com.swj.ics.java.messagequeue.kafka.OperationSystem;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2024/06/18 14:44
 */
@Slf4j
public abstract class AbstractIndex implements Closeable {

  protected volatile MappedByteBuffer mmap;
  protected volatile File file;
  protected final long baseOffset;
  protected final int maxFileSize;
  protected final boolean writable;
  protected final int entrySize = getEntrySize();

  private volatile long fileLength;

  private volatile int maxEntries;
  protected volatile int entries;
  protected final Lock lock = new ReentrantLock();

  // 冷热 LRU 数据的分界点，有关冷热 LRU 的原理和分析，请参考 md 文档
  protected int warmEntries = 8192 / entrySize;
  ;

  /**
   * @param file        the index segment file
   * @param baseOffset  the base offset of segment that this index is corresponding to
   * @param maxFileSize the maximum index size in bytes。默认 4M
   * @param writable    索引的读取方式，默认只读，可以以读写方式打开
   */
  public AbstractIndex(File file, long baseOffset, int maxFileSize, boolean writable) {
    this.file = file;
    this.baseOffset = baseOffset;
    this.maxFileSize = maxFileSize;
    this.writable = writable;
    this.mmap = createMmapIndexFile();

    this.maxEntries = mmap.limit() / entrySize;
    this.entries = mmap.position() / entrySize;
  }

  protected boolean isFull() {
    return entries >= maxEntries;
  }

  private MappedByteBuffer createMmapIndexFile() {
    try {
      boolean newFile = this.file.createNewFile();
      String mode = writable ? "rw" : "r";
      RandomAccessFile raf = new RandomAccessFile(file, mode);

      // pre-allocate the file if necessary
      // 如果是新文件，我们要将文件的大小暂停为 10M 左右的且是 entrySize 的整数倍
      try {
        if (newFile) {
          if (this.maxFileSize < entrySize) {
            throw new IllegalArgumentException("Invalid max index size: " + maxFileSize);
          }
          raf.setLength(roundDownToExactMultiply(this.maxFileSize, entrySize));
        }

        // memory map the file
        this.fileLength = raf.length();
        FileChannel.MapMode mapMode = writable ? FileChannel.MapMode.READ_WRITE : FileChannel.MapMode.READ_ONLY;
        MappedByteBuffer index = raf.getChannel().map(mapMode, 0, fileLength);
        // set the position of index for the next entry
        if (newFile) {
          index.position(0);
        } else {
          // if it is a pre-existing file, assume it is valid and set position to last entry
          index.position(roundDownToExactMultiply(index.limit(), entrySize));
        }
        return index;
      } finally {
        raf.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * 返回一个 <= maxFileSize 的数且是 entrySize 的整数倍。
   * 其实就是 entrySize * (maxFileSize / entrySize) ，跟时间轮求 时间轮的 currentTime 异曲同工，都是 tickMs 的整数倍
   *
   * @param maxFileSize 最大文件 size
   * @param entrySize   每个 entry 的大小
   * @return
   */
  private int roundDownToExactMultiply(int maxFileSize, int entrySize) {
    return entrySize * (maxFileSize / entrySize);
  }

  abstract int getEntrySize();

  protected void safeForceUnmap() {
    if (mmap != null) {
      try {
        forceUnmap();
      } catch (IOException e) {
        log.error("Error while unmapped index {}", file, e);
      }
    }
  }

  private void forceUnmap() throws IOException {
    try {
      ByteBufferUnmapper.unmap(file.getAbsolutePath(), mmap);
    } finally {
      // 直接访问为 null 的 mmap 会导致 jvm crash， 因此我们这里置为 null
      mmap = null;
    }
  }

  /**
   * 调整 mmap 索引文件的大小。
   *
   * @param newSize 新的文件尺寸
   * @return 大小改变则任务调整成功，否则调整失败
   */
  private boolean resize(int newSize) {
    return inLock(lock, () -> {
      int newRoundSize = roundDownToExactMultiply(newSize, entrySize);
      if (newRoundSize == fileLength) {
        log.debug("Index {} was not resized because it already has size {} ", file.getAbsolutePath(), newRoundSize);
        return false;
      }
      // windows 或者 zos(ibm） 不允许修改使用 mmap 映射的文件, 因此这里先关掉 mmap ，再重新打开
      // linux / unix 之类的没有这个问题，可以直接重新打开
      if (OperationSystem.IS_WINDOWS || OperationSystem.IS_ZOS) {
        safeForceUnmap();
      }
      //RandomAccessFile raf = null;
      int position = mmap.position();
      try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
        raf.setLength(newRoundSize);
        mmap = raf.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, newRoundSize);
        fileLength = newRoundSize;
        maxEntries = mmap.limit() / entrySize;
        mmap.position(position);
        log.debug("resize index {} to new size {}, position is {} and limit is {}", file.getAbsolutePath(),
            newRoundSize, position, mmap.limit());
        return true;
      } catch (IOException e) {
        log.error("failed to resize index {} to expected size {} and current size is {}", file.getAbsolutePath(),
            newRoundSize, fileLength);
        return false;
      }
    });
  }


  void closeHandle() {
    // On JVM, a memory mapping is typically unmapped by garbage collector.
    // However, in some cases it can pause application threads(STW) for a long moment reading metadata from a physical disk.
    // To prevent this, we forcefully cleanup memory mapping within proper execution which never affects API responsiveness.
    // See https://issues.apache.org/jira/browse/KAFKA-4614 for the details.
    // mmap 的取消和删除是个比较复杂的工作，如果不及时回收内存，会导致 JVM STW 较长.
    // todo 还是要要好好读读 这篇 Kafka 的 jira，看看别人怎么发现 这个 问题的，学学线上排查
    inLock(lock, this::safeForceUnmap);
  }

  @Override
  public void close() throws IOException {
    // 先关掉文件句柄，释放 mmap 的堆外内存
    closeHandle();
    // 再进行截断，重新生成 mmap
    trimToValidSize();
  }

  /**
   * 截断文件到当前大小，mmap 文件一旦启动，就是固定大小的默认的 10M 大小文件，
   * 一旦索引需要滚动，即使索引文件大小还不到 10M，就需要进行截断。
   */
  private void trimToValidSize() {
    inLock(lock, () -> {
      resize(entries * entrySize);
    });
  }

  /**
   * 将 index 的第 n 个 slot 解析为 IndexEntry
   *
   * @param byteBuffer index 所在的 mmap
   * @param n          第 n 个 slot
   * @return
   */
  protected abstract IndexEntry parseEntry(ByteBuffer byteBuffer, int n);

  /**
   * 改进二分查找算法
   * Kafka 在写入索引文件的方式是在文件末尾追加写，而几乎所有的索引查询都集中在索引的尾部(follower 的 fetch 和已存在的 consumer 拉取数据都是在拉取最近的数据)
   * 而目前几乎所有的操作系统都是用 LRU 或类似于 LRU 的机制来管理缓存页。这么来看的话，LRU 机制非常适合于 Kafka 索引的访问场景的。
   * 有关改进的二分法，请参考 advanced_binary_search_kafka_index.md
   *
   * @return
   */
  private Pair<Integer, Integer> indexSlotRangeFor(ByteBuffer idx, long target, IndexSearchType searchEntity) {
    if (entries == 0) {
      return Pair.of(-1, -1); // 0 表示最起始的 offset
    }

    // 这里为什么要使用 entries-1 -warmEntries ，我们可以看成是索引项，或者槽 slot 的索引
    // 或者举个栗子，baseoffset =1000,也就是 0 entry 的 offset 为 1000，我们的 parseEntry 的计算公式为 baseOffset + n*entrySize
    // 这里的 n 就是索引的含义，第二个 entry 的 offset = baseOffset + 1*entrySize, 因此这里的 n 是index 的意思

    int startOfWarmEntry = Math.max(0, entries - 1 - warmEntries);
    int compareResult = compareIndexEntry(parseEntry(idx, startOfWarmEntry), target, searchEntity);
    // target 位于 warm 区域
    if (compareResult <= 0) {
      return binarySearch(startOfWarmEntry, entries - 1, idx, target, searchEntity);
    }
    compareResult = compareIndexEntry(parseEntry(idx, 0), target, searchEntity);
    // target 比最小的 offset 还要小，则没找到
    if (compareResult > 0) {
      // 遵循 <n,n+1>
      return Pair.of(-1, 0);
    } else { // 在 LRU 的 cold 区域查找
      return binarySearch(0, startOfWarmEntry, idx, target, searchEntity);
    }
  }

  private Pair<Integer, Integer> binarySearch(int start, int end, ByteBuffer idx, long target,
      IndexSearchType indexSearchType) {
    int mid = 0;
    int lo = start, hi = end;
    while (lo < hi) {
      // kafka 使用这种方式 mid = (lo + hi +1 ) >>> 1; 这种方式感觉是即使 int 溢出然后左移之后 也 OK？经过测试，我的推理是正确的，
      // 不会出现溢出，因此这里更换为 kafka 的方式
      // mid = lo + (hi - lo + 1) / 2;
      mid = (lo + hi + 1) >>> 1; // kafka 采用了稍微激进的二分法，而不是传统的 start + (end-start)/2
      int compareResult = compareIndexEntry(parseEntry(idx, mid), target, indexSearchType);
      if (compareResult > 0) {
        hi = mid - 1;
      } else if (compareResult < 0) {
        lo = mid; // 注意这里，Kafka 选择了不大于 target 的最大值，这是符合要求的
      } else {
        // 找到恰好相等的目标 offset
        return Pair.of(mid, mid);
      }
    }
    // 没找到恰好相等的目标 offset ，则返回一个小于等于 目标 offset 和一个 大于等于 目标 offset 的位置
    // lo == hi 是循环退出的标志，此时 lo 和 lo+1 是我们想要的，mid 可能等于 lo+1???
    //判断 mid+1 是否越界
    if (lo == entries - 1) {
      // right 返回 -1 表示 lo 就是最后一个 entry
      return Pair.of(lo, -1);
    }
    return Pair.of(lo, lo + 1);
  }

  private int compareIndexEntry(IndexEntry indexEntry, long target, IndexSearchType indexSearchType) {
    switch (indexSearchType) {
      case KEY:
        return Long.compare(indexEntry.indexKey(), target);
      case VALUE:
        return Long.compare(indexEntry.indexValue(), target);
      default:
        return 1;
    }
  }
}


enum IndexSearchType {
  KEY,
  VALUE
}
