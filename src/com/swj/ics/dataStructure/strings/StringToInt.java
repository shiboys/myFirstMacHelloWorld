package com.swj.ics.dataStructure.strings;

/**
 * @author shiweijie
 * @date 2020/3/8 上午9:57
 */
public class StringToInt {

    /**
     * 将String 转换为int类型。有如下几个步骤
     * 1、判断正负
     * 2、判断非整数
     * 3、"321"转为321可以看作是3*100+2*10+1，等于是每次循环时，都对前几个数字*10再累加
     * 4、判断是否超出了int的最大整数范围
     *
     * @param str
     */
    public int stringToInt(String str) {
        if (str == null || str.length() < 1) {
            return 0;
        }
        String s = str.trim();
        int length = s.length();
        boolean negtive = false;
        int radis = 10;
        int i = 0, result = 0;
        char firstChar = str.charAt(0);
        if (firstChar < '0') {
            if (firstChar == '-' || firstChar == '+') {
                i++;
                if (firstChar == '-') {
                    negtive = true;
                }
            } else {
                throw new RuntimeException("invalid digital string for " + str);
            }
        }
        int topNum = Integer.MAX_VALUE / 10;

        while (i < length) {
            //非数字的字符，返回-1
            int digit = Character.digit(s.charAt(i), radis);
            if (digit < 0) {
                throw new RuntimeException("invalid digital string : " + str);
            }
            if (!negtive && (result > topNum ||
                    (result == topNum && digit > Integer.MAX_VALUE % 10))) {
                //result > Integer.max_val
                return Integer.MAX_VALUE;
            } else if (negtive && (result > topNum ||
                    (result == topNum && digit > Integer.MAX_VALUE % 10 + 1))) {
                return Integer.MIN_VALUE;
            }
            result = result * 10 + digit;
            i++;
        }
        return negtive ? -1 * result : result;
    }

    public static void main(String[] args) {

        int min_val = Integer.MIN_VALUE / 10;
        int val = -214748365;
        System.out.println("min-val=" + min_val);
        System.out.println("val < min_val ? " + (val < min_val));
        System.out.println(Character.digit('A', 10));
        System.out.println(Character.digit('a', 10));
        System.out.println(Character.digit('3', 10));
        String str1 = "123", str0 = "-123", str2 = "2147483649", str3 = "21474836490", str4 = "-2147483649", str5 = "-21474836499";
        String strFull = "1234567890";
        System.out.println("Integer.MAX_VALUE=" + Integer.MAX_VALUE + ",Integer.MIN_VALUE=" + Integer.MIN_VALUE);
        StringToInt instance = new StringToInt();
        System.out.println("str=" + str0 + ",strToInt =" + instance.stringToInt(str0));
        System.out.println("str=" + str1 + ",strToInt =" + instance.stringToInt(str1));
        System.out.println("str=" + str2 + ",strToInt =" + instance.stringToInt(str2));
        System.out.println("str=" + str3 + ",strToInt =" + instance.stringToInt(str3));
        System.out.println("str=" + str4 + ",strToInt =" + instance.stringToInt(str4));
        System.out.println("str=" + str5 + ",strToInt =" + instance.stringToInt(str5));
        System.out.println("str=" + Integer.MAX_VALUE + ",strToInt =" + instance.stringToInt(String.valueOf(Integer.MAX_VALUE)));
        System.out.println("str=" + Integer.MIN_VALUE + ",strToInt =" + instance.stringToInt(String.valueOf(Integer.MIN_VALUE)));

        System.out.println("str=" + strFull + ",strToInt =" + instance.stringToInt(strFull));
        System.out.println("str=" + strFull+"0" + ",strToInt =" + instance.stringToInt(strFull+"0"));

    }
}
