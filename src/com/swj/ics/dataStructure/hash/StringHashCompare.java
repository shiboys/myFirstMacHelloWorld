package com.swj.ics.dataStructure.hash;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/02/14 17:17
 * 字符串的比较
 */
public class StringHashCompare {

  private static final int DEFAULT_TABLE_SIZE = 10007;


  public static void main(String[] args) {
    testHashFunction();
    /**
     * 结果如下：
     * 使用 jdk 的hash 函数冲突情况：
     * 使用 hash function1 的冲突情况：
     * key:433,values:[that, used]
     * key:327,values:[for, may]
     * key:207,values:[an, if]
     * key:221,values:[it, no, on]
     * 使用 horner 法则的 hash 冲突情况:
     *
     */
  }

  private static int hashFunction1(String inputVal1, int tableSize) {
    char[] splitArr = inputVal1.toCharArray();
    int hash = 0;
    for (char ch : splitArr) {
      hash += ch;
    }
    return hash % tableSize;
  }

  /**
   * horner 法则，像极了 java string 自带的 hashcode
   *
   * @param inputVal  hash 字符串
   * @param tableSize 数组长度
   * @return
   */
  private static int hornerHashFunction(String inputVal, int tableSize) {
    int hash = 0;
    for (int i = 0, len = inputVal.length(); i < len; i++) {
      hash = hash * 37 + inputVal.charAt(i);
    }

    hash = hash % tableSize;

    if (hash < 0) {
      hash += tableSize;
    }
    return hash;
  }


  public static void testHashFunction() {

    // key 存储 hash 值，value 存储冲突的字符串 value，使用 jdk 自带的 hash  函数
    Map<Integer, TreeSet<String>> hashWithNativeMap = new HashMap<>();
    // 使用 function1 的 hash 函数
    Map<Integer, TreeSet<String>> hashWithFunction1Map = new HashMap<>();

    // 使用 Horner 法则的 哈希函数
    Map<Integer, TreeSet<String>> hashWithHornerMap = new HashMap<>();



    String inputVal = "Whenever it is invoked on the same object more than once during\r\n"
        + " * an execution of a Java application, the {@code hashCode} method\r\n"
        + " * must consistently return the same integer, provided no information\r\n"
        + " * used in {@code equals} comparisons on the object is modified.\r\n"
        + " * This integer need not remain consistent from one execution of an\r\n"
        + " * application to another execution of the same application.\r\n"
        + " * <li>If two objects are equal according to the {@code equals(Object)}\r\n"
        + " * method, then calling the {@code hashCode} method on each of\r\n"
        + " * the two objects must produce the same integer result.\r\n"
        + " * <li>It is <em>not</em> required that if two objects are unequal\r\n"
        + " * according to the {@link java.lang.Object#equals(java.lang.Object)}\r\n"
        + " * method, then calling the {@code hashCode} method on each of the\r\n"
        + " * two objects must produce distinct integer results. However, the\r\n"
        + " * programmer should be aware that producing distinct integer results\r\n"
        + " * for unequal objects may improve the performan";

    String[] splitArr = inputVal.split("\\s+");

    for (String s : splitArr) {
      saveHashAndValue(hashWithNativeMap, s.hashCode(), s);
      saveHashAndValue(hashWithFunction1Map, hashFunction1(s, DEFAULT_TABLE_SIZE), s);
      saveHashAndValue(hashWithHornerMap, hornerHashFunction(s, DEFAULT_TABLE_SIZE), s);
    }
    System.out.println("使用 jdk 的hash 函数冲突情况：");
    showIfHashCollision(hashWithNativeMap);
    System.out.println("使用 hash function1 的冲突情况：");
    showIfHashCollision(hashWithFunction1Map);
    System.out.println("使用 horner 法则的 hash 冲突情况:");
    showIfHashCollision(hashWithHornerMap);
  }

  private static void saveHashAndValue(Map<Integer, TreeSet<String>> treeSetMap, int hashCode, String val) {
    if (treeSetMap.containsKey(hashCode)) {
      treeSetMap.get(hashCode).add(val);
    } else {
      TreeSet<String> treeSet = new TreeSet<>();
      treeSet.add(val);
      treeSetMap.put(hashCode, treeSet);
    }
  }

  private static void showIfHashCollision(Map<Integer, TreeSet<String>> treeSetMap) {
    for (Map.Entry<Integer, TreeSet<String>> entry : treeSetMap.entrySet()) {
      if (entry.getValue().size() > 1) {
        System.out.println("key:" + entry.getKey() + ",values:" + entry.getValue());
      }
    }
  }

  class TransactionWithHash {
    private String who;
    private Date when;
    private double amount;

    @Override
    public boolean equals(Object obj) {
      return super.equals(obj);
    }

    @Override
    public int hashCode() {
      int hash = 17;
      hash = hash * 31 + who.hashCode();
      hash = hash * 31 + when.hashCode();
      hash = hash * 31 + ((Double) amount).hashCode();
      return hash;
    }
  }
}
