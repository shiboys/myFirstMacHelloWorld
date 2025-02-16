package com.swj.ics.dataStructure;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import com.swj.ics.algorithms.StackQueueBag.ResizingArrayStack;

/**
 * author shiweijie
 * date 2018/4/22 下午7:07
 */
public class StackUsage {

    public static final String OPRATORS = "+-*/()";

    public static void main(String[] args) {
        String expression = "12*(3+4)-6+8/2";
        String result = getPostFixExp(expression);
        // String result = String.join(",", splitStringWithOperator(expression));
        //System.out.println(result);
        System.out.println(evaluateThePosExp(result));
    }

    /**
     * 栈的使用，示例1，中缀表达式转后缀表达式
     */
    static Stack<String> operatorStatck = new Stack<>();

    public static String getPostFixExp(String originalExp) {
        if (originalExp == null) {
            return originalExp;
        }

        //String[] numbers = originalExp.split("[\\+\\-\\*\\/\\(\\)]+");

        List<String> numbers = splitStringWithOperator(originalExp);

        int pos = 0;
        StringBuilder stringBuilder = new StringBuilder(100);
        for (String ch : numbers) {
            pos = getPriorityByOperator(ch);
            if (pos > 0) { //加减乘除的4个符号。
                if (operatorStatck.empty()) {
                    operatorStatck.push(ch);
                } else {
                    String op = operatorStatck.peek();
                    if (op == "(") { //如果当前栈顶是（ 则，当前操作符直接入栈
                        operatorStatck.push(ch);
                    } else {
                        while (!operatorStatck.empty()
                                && !op.equals("(")
                                && pos <= getPriorityByOperator(op)) { //如果当前运算符的优先级小于等于栈顶元素的优先级，则弹出栈顶元素
                            operatorStatck.pop();
                            stringBuilder.append(op + " ");
                            if (!operatorStatck.isEmpty()) {
                                op = operatorStatck.peek();
                            }

                        }
                        //把当前操作符压入栈顶
                        operatorStatck.push(ch);
                    }
                }

            } else if ("(".equals(ch)) { //如果遇到（则直接入栈，
                operatorStatck.push(ch);
            } else if (")".equals(ch)) {
                String op = operatorStatck.peek();
                while (!operatorStatck.empty() && !op.equals("(")) { //如果当前运算符的优先级小于等于栈顶元素的优先级，则弹出栈顶元素
                    operatorStatck.pop();
                    stringBuilder.append(op + " ");
                    if (!operatorStatck.isEmpty()) {
                        op = operatorStatck.peek();
                    }
                }
                if (!operatorStatck.isEmpty()) {//弹出 左括号（
                    op = operatorStatck.pop();
                }
            } else {
                stringBuilder.append(ch + " ");
            }
        }

        while (!operatorStatck.isEmpty()) {
            stringBuilder.append(operatorStatck.pop() + " ");
        }

        return stringBuilder.toString();
    }

    static int getPriorityByOperator(String ope) {
        int result = 0;
        switch (ope) {
            case "+":
            case "-":
                result = 1;
                break;
            case "*":
            case "/":
                result = 2;
                break;

        }
        return result;
    }


    public static List<String> splitStringWithOperator(String exp) {
        if (exp == null || exp.equals("")) {
            return null;
        }
        int i = 0;
        int length = exp.length();
        List<String> itemList = new ArrayList<>();
        while (i < length) {
            int j = i;
            while (j < length && !isOperator(exp.charAt(j))) {
                //ch =
                j++;
            }

            if (j <= length) {
                if (i != j) {//取数字子字符串
                    itemList.add(exp.substring(i, j));
                }
                if (j < length) { //取符号
                    itemList.add(String.valueOf(exp.charAt(j)));
                }
            }

            i = j + 1;

        }
        return itemList;
    }

    private static boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/' || c == ')' || c == '(';
    }


    public static double evaluateThePosExp(String postFix) {
        String[] expArr = postFix.split(" ");
        if (expArr == null || expArr.length < 1) {
            return 0;
        }
        ResizingArrayStack<String> stack = new ResizingArrayStack();
        Double result = null;
        for (String item : expArr) {
            int priority = getPriorityByOperator(item);
            if (priority > 0 && stack.size() >= 2) {
                double num2 = Double.parseDouble(stack.pop());
                double num1 = Double.parseDouble(stack.pop());
                result = getCalResult(num1,num2,item);
                //重新压入栈
                if(result != null) {
                    stack.push(String.valueOf(result));
                }
            } else {
                stack.push(item);
            }
        }

        if(!stack.isEmpty()) {
            return Double.parseDouble(stack.pop());
        }

        return 0;

    }

    private static Double getCalResult(double num1, double num2, String op) {

        Double result = null;
        if (op.equals("+")) {
            result = num1 + num2;
        } else if (op.equals("-")) {
            result = num1 - num2;
        } else if (op.equals("*")) {
            result = num1 * num2;
        } else if (op.equals("/")) {
            result = num1 / num2;
        } else if (op.equals("sqrt")) {
            result = Math.sqrt(num1);
        }
        return result;
    }
}
