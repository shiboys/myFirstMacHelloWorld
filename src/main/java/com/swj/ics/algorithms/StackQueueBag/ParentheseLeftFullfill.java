package com.swj.ics.algorithms.StackQueueBag;

/**
 * author shiweijie
 * date 2018/5/8 上午11:41
 * 使用栈补全在有做括号的情况下，补全有括号
 * 例如给定输入：1+2)*3-4)*5-6)))
 * 输出为((1+2)*((3-4)*(5-6)))
 * 中间是有空格的，否则就没办法区分出10以及2位以上的数的
 */
public class ParentheseLeftFullfill {

    static ResizingArrayStack<String> ops = new ResizingArrayStack<String>();
    static ResizingArrayStack<String> nums = new ResizingArrayStack<>();

    static String correctExpression(String exp) {
        if (exp == null || exp.equals("")) {
            return "";
        }
        try {
            String[] splitArr = exp.split(" ");
            for (String item : splitArr) {
                if (item.equals("+") || item.equals("-")
                        || item.equals("*") || item.equals("/")) {
                    ops.push(item);
                } else if (!item.equals(")")) {
                    nums.push(item);
                } else if (item.equals(")")) {
                    String left = "( ";
                    String num1 = nums.pop() + " ";
                    String num2 = nums.pop() + " ";
                    String op = ops.pop() + " ";
                    nums.push(left + num2 + op + num1 + item + " ");
                }
            }
            if (!nums.isEmpty()) {
                return nums.pop();
            } else {
                return "";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        String exp = "1 + 2 ) * 3 - 4 ) * 5 - 6 ) ) )";
        System.out.println(correctExpression(exp));
        exp = "1 + 2 ) * 3 - 4 ) * 5 - 6 ) ) ) )";
        System.out.println(correctExpression(exp));
    }
}
