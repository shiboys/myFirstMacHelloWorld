package com.swj.ics.dataStructure.strings;

/**
 * @author shiweijie
 * @date 2020/3/8 上午9:56
 */
public class StringAlgorithm {

    /**
     * 空字符串替换，请实现一个函数，将里面的空格字符串都替换我%20，例如 We Are Happy->We%20Are%20Happy
     * 方法1：暴力替换
     * 从后往前遍历字符串，每找到一个空格，需要将' '后的字符串以此后移2位，预留出%20的空间
     */
    public String gedReplacedSpaceString(String str) {
        if (str == null || str.length() < 1) {
            return str;
        }
        int spaceCount = 0;
        int length = str.length();
        char[] charArr = str.toCharArray();;
        for (char ch : charArr) {
            if (ch == ' ') {
                spaceCount++;
            }
        }
        int newLength = length + spaceCount * 2;
        //这是2个数组法，是因为JAVA的字符串不可变性
        char[] newArr = new char[newLength];
        for(int i = length -1,j = newLength -1;i>=0 && j>= 0;) {
            if(charArr[i] == ' ') {
                newArr[j--]='0';
                newArr[j--]='2';
                newArr[j--] = '%';
                i--;
            } else {
                newArr[j--] =  charArr[i--];
            }
        }
        return new String(newArr);
    }
}
