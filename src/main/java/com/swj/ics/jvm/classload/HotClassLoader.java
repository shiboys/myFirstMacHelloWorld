package com.swj.ics.jvm.classload;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/04/24 21:21
 */
public class HotClassLoader extends java.lang.ClassLoader {
  private String fileDirPath;

  public HotClassLoader(String fileClassPath) {
    this.fileDirPath = fileClassPath;
  }

  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {
    Class<?> clazz = findLoadedClass(name);
    if (clazz != null) {
      return clazz;
    }
    String userDir = fileDirPath;
    String className = "com.swj.ics.jvm.classload.HotDemoA";

    byte[] classBytes = readClassFile(userDir, className);
    if (classBytes == null) {
      System.err.println("classBytes is null");
      return null;
    }
    return defineClass(className, classBytes, 0, classBytes.length);
  }

  private static byte[] readClassFile(String customClassPath, String classFullName) {
    String classFilePath = getClassFileName(customClassPath, classFullName);
    try {
      FileInputStream inputStream = new FileInputStream(classFilePath);
      FileChannel fileChannel = inputStream.getChannel();
      ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      WritableByteChannel writeChannel = Channels.newChannel(baos);
      while (true) {
        int read = fileChannel.read(byteBuffer);
        if (read == 0 || read == -1) {
          break;
        }
        byteBuffer.flip();
        writeChannel.write(byteBuffer);
        byteBuffer.clear();
      }

      inputStream.close();
      return baos.toByteArray();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  private static String getClassFileName(String customClassPath,
      String classFullName) {
    return customClassPath + "/" + classFullName.replaceAll("\\.", "/") + ".class";
  }
}
