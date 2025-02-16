package com.swj.ics.java.messagequeue.kafka;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2024/06/17 14:41
 */
@Slf4j
public final class KafkaServerUtils {

  /**
   * Attempts to move source to target atomically and fall back to a non-atomic move if it fails
   *
   * @param source
   * @param target
   * @throws java.io.IOException if both atomic and non-atomic move failed
   */
  public static void atomicMoveWithFallBack(Path source, Path target) throws IOException {
    try {
      Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
    } catch (IOException outer) {
      try {
        Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        log.debug("Non-atomic move from {} to {} succeed after atomic move failed due to {}", source, target,
            outer.getMessage());
      } catch (IOException inner) {
        inner.addSuppressed(outer);
        throw inner;
      }
    }
  }

  public static void inWriteLock(ReadWriteLock readWriteLock, Runnable runnable) {
    inLock(readWriteLock.writeLock(), runnable);
  }

  public static void inReadLock(ReadWriteLock readWriteLock, Runnable runnable) {
    inLock(readWriteLock.readLock(), runnable);
  }

  public static <T> T inWriteLock(ReadWriteLock readWriteLock, Supplier<T> supplier) {
    return inLock(readWriteLock.writeLock(), supplier);
  }

  public static <T> T inReadLock(ReadWriteLock readWriteLock, Supplier<T> supplier) {
    return inLock(readWriteLock.readLock(), supplier);
  }

  public static void inLock(Lock lock, Runnable runnable) {
    lock.lock();
    try {
      runnable.run();
    } finally {
      lock.unlock();
    }
  }

  public static <T> T inLock(Lock lock, Supplier<T> supplier) {
    lock.lock();
    try {
      return supplier.get();
    } finally {
      lock.unlock();
    }
  }


}
