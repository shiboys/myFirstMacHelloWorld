package com.swj.ics.jvm.classload;

import java.lang.reflect.Method;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/04/24 21:30
 */
public class HotClassLoop {
  public static void main(String[] args) throws Exception {
    while (true) {
      String userDir = "/Users/shiweijie/class_loader_dir/";
      HotClassLoader classLoader = new HotClassLoader(userDir);

      String className = "com.swj.ics.jvm.classload.HotDemoA";
      Class<?> loadClass = classLoader.loadClass(className);
      System.out.println(classLoader.hashCode());
      Object demo = loadClass.newInstance();
      Method method = demo.getClass().getDeclaredMethod("hot");
      method.invoke(demo,null);
      Thread.sleep(10_000);
    }
  }
}
