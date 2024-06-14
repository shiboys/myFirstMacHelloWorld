package com.swj.ics.jvm.classload;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/04/23 19:03
 */
public class ClassLoad {
  static void printStingClassLoad() throws Exception {
    Class clazz = Class.forName("java.lang.String");
    Method[] methods = clazz.getDeclaredMethods();
    StringBuilder printInfo = null;
    for (Method method : methods) {
      printInfo = new StringBuilder();
      printInfo.append(Modifier.toString(method.getModifiers()) + " ");
      printInfo.append(method.getReturnType().getSimpleName() +" ");
      printInfo.append(method.getName());
      printInfo.append("(");
      Class<?>[] parameterTypes = method.getParameterTypes();
      if (parameterTypes.length < 1) {
        printInfo.append(')');
      }
      for (int i = 0, len = parameterTypes.length; i < len; i++) {
        char end = (i == len - 1) ? ')' : ',';
        printInfo.append(parameterTypes[i].getSimpleName() + end);
      }
      System.out.println(printInfo.toString());

      ReentrantLock lock = new ReentrantLock();
      Condition cd =  lock.newCondition();
      System.out.println("直接调用 cd.await");
      cd.await(1, TimeUnit.SECONDS);
      System.out.println("调用结束");
    }
  }

  public static void main(String[] args) {
    try {
      printStingClassLoad();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
