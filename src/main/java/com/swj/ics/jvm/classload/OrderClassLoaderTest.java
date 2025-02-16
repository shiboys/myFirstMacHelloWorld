package com.swj.ics.jvm.classload;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/04/24 19:11
 */
public class OrderClassLoaderTest {
  public static void main(String[] args) throws ClassNotFoundException {
    String classPath = "/Users/shiweijie/class_loader_dir";
    OrderClassLoader orderClassLoader = new OrderClassLoader(classPath);
    String className = "com.swj.ics.jvm.classload.HelloLoader";
    Class<?> clazz = orderClassLoader.loadClass(className);
    java.lang.ClassLoader clazzLoader;
    System.out.println((clazzLoader = clazz.getClassLoader()));

    System.out.println("==== Class Loader Tree ====");
    while (clazzLoader != null) {
      System.out.println(clazzLoader);
      clazzLoader = clazzLoader.getParent();
    }
  }
}
