package com.swj.ics.jvm.classload;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/04/24 14:44
 */
public class FindClassOrder {
  public static void main(String[] args) {
    java.lang.ClassLoader classLoader = FindClassOrder.class.getClassLoader();
    String classFullName = "com.swj.ics.jvm.classload.HelloLoader";
    try {
      byte[] classBytes = readClassFile(classFullName);
      Method method_defineClass =
          java.lang.ClassLoader.class.getDeclaredMethod("defineClass", byte[].class, int.class, int.class);
      method_defineClass.setAccessible(true);
      // 通过反射的方式调用 defineClass, 让当前的 ClassLoader 预先定义 HelloLoader 这个类
      method_defineClass.invoke(classLoader, classBytes, 0, classBytes.length);
      method_defineClass.setAccessible(false);

      Object helloLoader = classLoader.getParent().loadClass(classFullName).newInstance();
      Method printMethod = helloLoader.getClass().getDeclaredMethod("print",null);
      printMethod.invoke(helloLoader,null);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  private static void defaultClassLoaderTest() {
    HelloLoader helloLoader = new HelloLoader();
    helloLoader.print();
  }

  private static byte[] readClassFile(String classFullName) {
    // String pkgPath = FindClassOrder
    String filePath =
        System.getProperty("user.dir") + "/target/classes/" + classFullName.replaceAll("\\.", "/") + ".class";
    FileInputStream inputStream = null;
    try {
      inputStream = new FileInputStream(filePath);
      FileChannel fileChannel = inputStream.getChannel();
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ByteBuffer buffer = ByteBuffer.allocate(1024);
      WritableByteChannel outChannel = Channels.newChannel(baos);
      while (true) {
        int read = fileChannel.read(buffer);
        if (read == 0 || read == -1) {
          break;
        }
        buffer.flip();
        outChannel.write(buffer);
        buffer.clear();
      }
      return baos.toByteArray();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (inputStream != null) {
        try {
          inputStream.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    return null;
  }
}
