package com.swj.ics.dataStructure.strings;

import java.math.BigInteger;
import java.util.Random;
import java.util.regex.Pattern;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/03/01 10:37
 */
public class StringBigInteger {
  private static final Random RANDOM = new Random();

  /**
   * 将输入的 16 进制转换成 10 进制的数字
   *
   * @param hexString
   * @return
   */
  public static BigInteger convertToBigIntegerFromHex(String hexString) {
    if (hexString == null || hexString.isEmpty()) {
      return null;
    }
    if (hexString.startsWith("0x")) {
      hexString = hexString.substring(2);
    }
    BigInteger base = BigInteger.valueOf(16);
    BigInteger result = BigInteger.valueOf(0);
    int len = hexString.length();
    for (int i = 0; i < len; i++) {
      char ch = hexString.charAt(len - 1 - i);
      BigInteger currentVal = base.pow(i);
      if (ch >= 'A' && ch <= 'F') {
        currentVal = currentVal.multiply(BigInteger.valueOf((ch - 'A') + 10));
      } else {
        currentVal = currentVal.multiply(BigInteger.valueOf(ch - '0'));
      }
      result = result.add(currentVal);
    }
    return result;
  }

  public static void main(String[] args) {
    //testHexString();
    // 32 位
    int bitsLength = 32;
    String s1 = generateRandomBinaryString(bitsLength);
    String s2 = generateRandomBinaryString(bitsLength);
    Long val1 = Long.valueOf(s1, 2);
    Long val2 = Long.valueOf(s2, 2);
    System.out.println("val1=" + val1 + ", val2=" + val2);
    System.out.println(bigNumberAdd(s1, s2));

    System.out.println(bigNumberMulti(s1, s2));
  }

  private static void testHexString() {
    String hexString = "0x123456AEC";
    System.out.println(convertToBigIntegerFromHex(hexString));
    System.out.println(convertToBigIntegerFromHex("0x1A"));
    System.out.println(convertToBigIntegerFromHex("0x1AB"));
    System.out.println(convertToBigIntegerFromHex("0x1ABC"));
    System.out.println(convertToBigIntegerFromHex("0x123456AE"));
  }


  private static BigInteger bigNumberAdd(String binaryString1, String binaryString2) {
    BigInteger num1 = new BigInteger(binaryString1, 2);
    return num1.add(new BigInteger(binaryString2, 2));
  }

  private static BigInteger bigNumberMulti(String binaryString1, String binaryString2) {
    BigInteger num1 = new BigInteger(binaryString1,2);
    return num1.multiply(new BigInteger(binaryString2));
  }

  private static String generateRandomBinaryString(int bitsLength) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < bitsLength; i++) {
      sb.append(RANDOM.nextInt(2));
    }
    return sb.toString();
  }

  private static void basicCheckBigBinaryString(String binaryString1, String binaryString2) {
    if (binaryString1 == null || binaryString2 == null) {
      throw new java.lang.IllegalArgumentException("argument is null");
    }
    String regex = "^[0-1]+$";
    Pattern pattern = Pattern.compile(regex);
    if (!pattern.matcher(binaryString1).matches() || !pattern.matcher(binaryString2).matches()) {
      throw new java.lang.IllegalArgumentException("argument illegal");
    }
  }


}
