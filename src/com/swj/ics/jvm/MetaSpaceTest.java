package com.swj.ics.jvm;

import net.sf.cglib.beans.BeanGenerator;

import java.util.HashMap;
import java.util.Map;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/03/31 16:52
 * 元空间 OOM 溢出测试
 */
public class MetaSpaceTest {
  //CglibBean


  static class CglibBean {
    public CglibBean(String className, Map<String, Object> propMap) {
      BeanGenerator beanGenerator = new BeanGenerator();
      beanGenerator.setNamingPolicy((s, s1, o, predicate) -> className);
      if (propMap != null && !propMap.isEmpty()) {
        for (Map.Entry<String, Object> entry : propMap.entrySet()) {
          beanGenerator.addProperty(entry.getKey(), (Class) entry.getValue());
        }
      }
      beanGenerator.create();
    }
  }

  public static void main(String[] args) {
    int i = 0;
    try {
      for (i = 0; i < 10_000; i++) {
        CglibBean cglibBean = new CglibBean("com.swj.ics.jvm" + i, new HashMap<>());
      }
    } catch (Exception e) {
      System.out.println("total create count: " + i);
    }
  }
}
