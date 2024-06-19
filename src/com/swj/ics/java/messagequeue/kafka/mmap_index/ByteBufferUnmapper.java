package com.swj.ics.java.messagequeue.kafka.mmap_index;

import static java.lang.invoke.MethodHandles.constant;
import static java.lang.invoke.MethodHandles.dropArguments;
import static java.lang.invoke.MethodHandles.filterReturnValue;
import static java.lang.invoke.MethodHandles.guardWithTest;
import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodType.methodType;

import com.swj.ics.java.messagequeue.kafka.Java;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2024/06/18 16:22
 */
public class ByteBufferUnmapper {
  private static final MethodHandle UNMAP;
  private static final RuntimeException UNMAP_NOT_SUPPORTED_EXCEPTION;

  static {
    MethodHandle unmap = null;
    RuntimeException exception = null;
    try {
      unmap = lookupUnmapMethodHandle();
    } catch (RuntimeException e) {
      exception = e;
    }
    if (unmap != null) {
      UNMAP = unmap;
      UNMAP_NOT_SUPPORTED_EXCEPTION = null;
    } else {
      UNMAP = null;
      UNMAP_NOT_SUPPORTED_EXCEPTION = exception;
    }
  }

  public static void unmap(String resourceDescription, ByteBuffer buffer) throws IOException {
    if (!buffer.isDirect()) {
      throw new IllegalArgumentException("Unmapped works only with direct buffers");
    }
    if (UNMAP == null) {
      throw UNMAP_NOT_SUPPORTED_EXCEPTION;
    }
    try {
      UNMAP.invokeExact(buffer);
    } catch (Throwable throwable) {
      throw new IOException("unable to unmap the byte buffer : " + resourceDescription, throwable);
    }
  }

  private static MethodHandle lookupUnmapMethodHandle() {
    MethodHandle unMappedMethodHandle = null;
    final MethodHandles.Lookup lookup = lookup();
    try {
      if (Java.IS_JAVA9_COMPATIBLE) {
        unMappedMethodHandle = unmapJava9(lookup);
      } else {
        unMappedMethodHandle = unmapJava7Or8(lookup);
      }
    } catch (ReflectiveOperationException e) {
      throw new UnsupportedOperationException("Unmapping is not supported on this platform, because internal " +
          "Java APIs are not compatible with this Kafka version", e);
    }
    return unMappedMethodHandle;
  }

  private static MethodHandle unmapJava7Or8(MethodHandles.Lookup lookup) throws ReflectiveOperationException {
    /* "Compile" a MethodHandle that is roughly equivalent to the following lambda:
     *
     * (ByteBuffer buffer) -> {
     *   sun.misc.Cleaner cleaner = ((java.nio.DirectByteBuffer) byteBuffer).cleaner();
     *   if (nonNull(cleaner))
     *     cleaner.clean();
     *   else
     *     noop(cleaner); // the noop is needed because MethodHandles#guardWithTest always needs both if and else
     * }
     */
    Class<?> directBufferClass = Class.forName("java.nio.DirectByteBuffer");
    Method m = directBufferClass.getMethod("cleaner");
    m.setAccessible(true);
    MethodHandle directBufferCleanerMethod = lookup.unreflect(m);
    Class<?> cleanerClass = directBufferCleanerMethod.type().returnType();
    MethodHandle cleanMethod = lookup.findVirtual(cleanerClass, "clean", methodType(void.class));
    MethodHandle nonNullTest = lookup.findStatic(org.apache.kafka.common.utils.ByteBufferUnmapper.class, "nonNull",
        methodType(boolean.class, Object.class)).asType(methodType(boolean.class, cleanerClass));
    MethodHandle noop = dropArguments(constant(Void.class, null).asType(methodType(void.class)), 0, cleanerClass);
    MethodHandle unmapper = filterReturnValue(directBufferCleanerMethod, guardWithTest(nonNullTest, cleanMethod, noop))
        .asType(methodType(void.class, ByteBuffer.class));
    return unmapper;
  }

  private static MethodHandle unmapJava9(MethodHandles.Lookup lookup) throws ReflectiveOperationException {
    Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
    Field f = unsafeClass.getDeclaredField("theUnsafe");
    f.setAccessible(true);
    Object theUnsafe = f.get(null);
    MethodHandle unMapper =
        lookup.findVirtual(unsafeClass, "invokeCleaner", methodType(void.class, ByteBuffer.class));
    return unMapper.bindTo(theUnsafe);
  }

  private static boolean nonNull(Object x) {
    return x != null;
  }
}
