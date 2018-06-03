package com.swj.ics.algorithms.StackQueueBag;

/**
 * author shiweijie
 * date 2018/5/7 下午1:35
 * 二进制八进制16进制实现
 */
public class BinarySystemWithStack {

    private static ResizingArrayStack<String> stack = null;
    /**
     * 根据指定的数字和进制，输出相应进制的字符串表现形式
     *
     * @param num    指定的数字
     * @param jinzhi 相应的进制
     * @return 相应转化结果的字符串表示
     */
    static String getTargetJinZhi(int num, int jinzhi) {
        stack = new ResizingArrayStack<>();
        if (jinzhi == 2 || jinzhi == 8) { //如果是二进制或者8进制的
            while (num > 0) {
                stack.push(num % jinzhi + "");
                num = num / jinzhi;
            }
        } else if (jinzhi == 16) {
            while (num > 0) {
                int modNum = num % 16;
                if(modNum >= 10) {
                    pushHexNumToStack(stack,modNum);
                } else {
                    stack.push(modNum+"");
                }
                num /= 16;
            }
        }

        if(!stack.isEmpty()) {
            StringBuilder stringBuilder = new StringBuilder(stack.size() * 2);
            for (String s : stack) {
                stringBuilder.append(s);
            }
            return stringBuilder.toString();
        }
        else {
            return "";
        }
    }


    static void pushHexNumToStack(ResizingArrayStack stack, int modNum) {
        //A-65 ,10-16进制的A，range is 55
        char ch = (char) (modNum + 55);
        stack.push(String.valueOf(ch));
    }

    public static void main(String[] args) {
        int num = 255;
        int jinzhi = 2;
        System.out.println(getTargetJinZhi(num,jinzhi));
        jinzhi = 8;
        System.out.println(getTargetJinZhi(num,jinzhi));
        jinzhi = 16;
        System.out.println(getTargetJinZhi(num,jinzhi));

       /* String testStringIter ="abcdefg";
        for (char ch : testStringIter.toCharArray()) {

        }*/

    }
}
