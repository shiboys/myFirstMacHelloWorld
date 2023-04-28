package com.swj.ics.jvm.classload;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/04/24 20:37
 * 相同的类，不同的类加载器，来验证不能相互转化
 */
public class SameClassDiffClassLoader {
  public static void main(String[] args) {
    // 第一步，将指定的类加载到当前的类加载器中
    String classPath = System.getProperty("user.dir") + "/target/classes";
    String classFullName = "com.swj.ics.jvm.classload.HelloLoader";
    byte[] fileBytes = readClassFile(classPath, classFullName);
    if (fileBytes == null || fileBytes.length < 1) {
      System.err.println("fileBytes is emtpy.");
      return;
    }
    java.lang.ClassLoader cl = SameClassDiffClassLoader.class.getClassLoader();
    try {
      Method methodDefineClass =
          java.lang.ClassLoader.class.getDeclaredMethod("defineClass", byte[].class, int.class, int.class);
      methodDefineClass.setAccessible(true);
      methodDefineClass.invoke(cl, fileBytes, 0, fileBytes.length);
      methodDefineClass.setAccessible(false);
      // 上述代码已经把 应用程序的 类加载器进行了加载

      // 使用 启动类加载器进行加载 启动类版本的  HelloLoader, 并进行强制转换成应用类的 HelloLoader
      HelloLoader helloLoader = (HelloLoader) cl.getParent().loadClass(classFullName).newInstance();
      System.out.println(helloLoader.getClass().getClassLoader());
      helloLoader.print();

    } catch (Exception e) {
      e.printStackTrace();
    }
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
