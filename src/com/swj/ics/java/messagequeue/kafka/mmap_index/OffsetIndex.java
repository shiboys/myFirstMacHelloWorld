package com.swj.ics.java.messagequeue.kafka.mmap_index;

import static com.swj.ics.java.messagequeue.kafka.KafkaServerUtils.inLock;

import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.ByteBuffer;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2024/06/18 19:09
 */
@Slf4j
public class OffsetIndex extends AbstractIndex {


  private long lastOffset = lastEntry().offset;

  /**
   * @param file        the index segment file
   * @param baseOffset  the base offset of segment that this index is corresponding to
   * @param maxFileSize the maximum index size in bytes。默认 4M
   * @param writable    索引的读取方式，默认只读，可以以读写方式打开
   */
  public OffsetIndex(File file, long baseOffset, int maxFileSize, boolean writable) {
    super(file, baseOffset, maxFileSize, writable);
  }

  @Override
  int getEntrySize() {
    return 8;
  }

  OffsetPosition lastEntry() {
    return inLock(lock, () -> {
      if (entries > 0) {
        return parseEntry(mmap, entries - 1);
      }
      return new OffsetPosition(baseOffset, 0);
    });
  }

  @Override
  protected OffsetPosition parseEntry(ByteBuffer byteBuffer, int n) {
    return new OffsetPosition(baseOffset + relativeOffset(byteBuffer, n), physical(byteBuffer, n));
  }

  /**
   * 读取 index 的第 n 个 entry 的 offset
   *
   * @param buffer
   * @param n
   * @return
   */
  private int relativeOffset(ByteBuffer buffer, int n) {
    return buffer.getInt(n * entrySize);
  }

  private int physical(ByteBuffer buffer, int n) {
    return buffer.getInt(n * entrySize + 4);
  }

  public void append(long offset, int position) {
    inLock(lock, () -> {
      Preconditions.checkArgument(!isFull(),
          String.format("Attempt to append a new entry to full index (size=%d).", entries));
      if (entries == 0 || offset > lastOffset) {
        mmap.putInt(toRelativeOffset(offset));
        mmap.putInt(position);
        entries++;
        lastOffset = offset;
        Preconditions.checkArgument(entries * entrySize == mmap.position(), String.format("%d entries but file "
            + "position in index is %d", entries, mmap.position()));
      } else {
        throw new InvalidOffsetException(String.format("failed to appended an offset (%d) with position to index (%s)"
            + " whose last offset (%d) is larger than it", offset, file.getAbsolutePath(), lastOffset));
      }
    });
  }

  private int toRelativeOffset(long offset) {
    return (int) (offset - baseOffset);
  }

}


class OffsetPosition implements IndexEntry {

  public final long offset;
  public final int position;

  OffsetPosition(long offset, int position) {
    this.offset = offset;
    this.position = position;
  }

  @Override
  public long indexKey() {
    return offset;
  }

  @Override
  public long indexValue() {
    return position;
  }


}
