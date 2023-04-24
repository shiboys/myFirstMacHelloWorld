package com.swj.ics.jvm.classload;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

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
