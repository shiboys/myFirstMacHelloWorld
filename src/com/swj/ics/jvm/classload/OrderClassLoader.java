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
 * @since 2023/04/24 18:42
 */
public class OrderClassLoader extends java.lang.ClassLoader {

  private String userDirPath = null;

  public OrderClassLoader(String userDirPath) {
    this.userDirPath = userDirPath;
  }

  // 改变类加载器的默认加载行为：先让双亲去加载，
  @Override
  protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
    //先尝试当前类加载，然后再由父加载器加载
    Class<?> clazz = findClass(name);
    if (clazz == null) { // 如果当前类的类加载器没有找到，则使用父类的加载器去查找
      System.out.println("I can't load the class :" + name);
      return super.loadClass(name, resolve);
    }
    return clazz;
  }

  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {
    Class<?> loadedClass = this.findLoadedClass(name);
    if (loadedClass != null) {
      return loadedClass;
    }
    String classFileName = getClassFileName(name);
    byte[] classBytes = readFileContent(classFileName);
    if(classBytes== null || classBytes.length < 1) {
      return null;
    }
    return defineClass(name, classBytes, 0, classBytes.length);
  }

  byte[] readFileContent(String fileName) {
    try {
      FileInputStream inputStream = new FileInputStream(fileName);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      FileChannel fileChannel = inputStream.getChannel();
      ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
      WritableByteChannel writableChannel = Channels.newChannel(baos);
      while (true) {
        int read = fileChannel.read(byteBuffer);
        if (read == 0 || read == -1 || !byteBuffer.hasRemaining()) {
          break;
        }
        byteBuffer.flip();
        writableChannel.write(byteBuffer);
        byteBuffer.clear();
      }

      inputStream.close();
      return baos.toByteArray();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;

  }

  private String getClassFileName(String classFullName) {
    return userDirPath +"/"+ classFullName.replaceAll("\\.", "/") + ".class";
  }
}
