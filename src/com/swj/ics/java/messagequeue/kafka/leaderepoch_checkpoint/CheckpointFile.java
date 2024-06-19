package com.swj.ics.java.messagequeue.kafka.leaderepoch_checkpoint;

import com.swj.ics.java.messagequeue.kafka.KafkaServerUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.helpers.MessageFormatter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2024/06/17 14:43
 */
@Slf4j
public class CheckpointFile<T> {
  private final File file;
  private final CheckpointFileFormatter<T> formatter;
  private final int version;

  private final Path path;
  private final Path tmpPath;
  private final Object lock = new Object();

  public CheckpointFile(File file,
      CheckpointFileFormatter<T> formatter, int version) {
    this.file = file;
    this.formatter = formatter;
    this.version = version;

    this.path = file.toPath().toAbsolutePath();
    this.tmpPath = Paths.get(path.toString() + ".tmp");
    try {
      Files.createFile(file.toPath()); // create the file if it does'nt exists
    } catch (IOException e) {
      // ignore IO Exception
    }
  }

  public void write(Collection<T> items) {
    if (items == null || items.isEmpty()) {
      return;
    }
    synchronized (lock) {
      try {
        FileOutputStream outputStream = new FileOutputStream(tmpPath.toFile());
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
        // write to the tmp file and then swap with existing file
        // 先写版本号，再写 size，然后写入每个 entry
        try {
          writer.append(String.valueOf(version));
          writer.newLine();

          writer.append(String.valueOf(items.size()));
          writer.newLine();

          for (T item : items) {
            writer.append(formatter.toLine(item));
            writer.newLine();
          }
          // sync the file
          writer.flush();
          outputStream.getFD().sync();
        } finally {
          writer.close();
        }

        // finally swap the existing file atomically
        KafkaServerUtils.atomicMoveWithFallBack(tmpPath, path);
      } catch (IOException e) {
        String error = MessageFormatter.format("Error while writing to checkpoint file {}",
            file.getAbsolutePath()).getMessage();
        log.error(error, e);
        throw new RuntimeException(error);
      }
    }
  }

  public Collection<T> read() {
    // read 方法的主要逻辑 为啥要单独放在一个类里面，是因为 read 比 write 复杂
    synchronized (lock) {
      try {
        BufferedReader reader = Files.newBufferedReader(this.path);
        CheckpointFileReadBuffer<T> readBuffer =
            new CheckpointFileReadBuffer<>(reader, file.getAbsolutePath(), this.version, this.formatter);
        try {
          return readBuffer.read();
        } finally {
          reader.close();
        }
      } catch (IOException e) {
        String errorMsg = MessageFormatter.format("Error has occurred while reading checkpoint file {}",
            file.getAbsolutePath()).getMessage();
        log.error(errorMsg, e);
        throw new RuntimeException(errorMsg);
      }
    }
  }
}


@Slf4j
class CheckpointFileReadBuffer<T> {
  private final BufferedReader bufferedReader;
  private final String filePath;
  private final int version;
  private final CheckpointFileFormatter<T> formatter;

  CheckpointFileReadBuffer(BufferedReader bufferedReader, String filePath, int version,
      CheckpointFileFormatter<T> formatter) {
    this.bufferedReader = bufferedReader;
    this.filePath = filePath;
    this.version = version;
    this.formatter = formatter;
  }

  public Collection<T> read() {
    if (bufferedReader == null) {
      throw new IllegalArgumentException("reader is null");
    }
    try {
      String line = bufferedReader.readLine();
      try {
        // 版本读取失败
        int lineVersion = Integer.parseInt(line);
        if (lineVersion != this.version) {
          throw new IOException(
              String.format("Unrecognized version of the checkpoint file %s : %s", filePath, lineVersion));
        }
      } catch (NumberFormatException e) {
        throw malformedException(line);
      }
      line = bufferedReader.readLine();
      if (line == null || line.isEmpty()) {
        return Collections.emptyList();
      }
      int expectedSize = Integer.parseInt(line);
      List<T> entries = new ArrayList<>(expectedSize);
      line = bufferedReader.readLine();
      while (line != null) {
        T item = this.formatter.fromLine(line);
        if (item != null) {
          entries.add(item);
        } else {
          throw malformedException(line);
        }
        line = bufferedReader.readLine();
      }

      // 元素 size 验证
      if (expectedSize != entries.size()) {
        throw new IOException(String.format("Expected %d entries in checkpoint file (%s), but only found %d",
            expectedSize, filePath, entries.size()));
      }

      return entries;
    } catch (IOException e) {
      log.error("failed to read file {}", filePath);
      return Collections.emptyList();
    }
  }

  private IOException malformedException(String line) {
    return new IOException(String.format("Malformed line of '%s' in checkpoint file %s", line, filePath));
  }


}



interface CheckpointFileFormatter<T> {
  String toLine(T entity);

  T fromLine(String line);
}
