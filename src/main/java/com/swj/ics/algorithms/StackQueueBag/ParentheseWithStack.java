package com.swj.ics.algorithms.StackQueueBag;

/**
 * author shiweijie
 * date 2018/5/7 上午10:21
 * 打括号中括号，圆括号测试
 */
public class ParentheseWithStack {

    public static void main(String[] args) {
        String exp1="[()]{}{[()()]()}";
        System.out.println(isValidParenthese(exp1));
        String exp2 = "[(])";
        System.out.println(isValidParenthese(exp2));
    }

    public static boolean isValidParenthese(String exp) {
        if (exp == null || exp.equals("")) {
            return false;
        }
        String[] splitArr = exp.split("");
        if (splitArr.length < 1) {
            return false;
        }
        ResizingArrayStack<String> stack = new ResizingArrayStack<>();
        String item = null;
        int i = 0;
        for (int len = splitArr.length; i < len; i++) {
            item = splitArr[i];
            if(!pushOrPopItem(item,stack)) {
                break;
            }
        }
        if (i != splitArr.length) {
            return false;
        }
        if (!stack.isEmpty()) {
            return false;
        }
        return true;
    }

    static boolean pushOrPopItem(String item, ResizingArrayStack<String> stack) {
        if (item.equals("(") || item.equals("[") || item.equals("{")) {
            stack.push(item);
        } else {
            if (stack.isEmpty()) {
                return false;
            }

            if (item.equals(")") && !stack.pop().equals("(")) {
                return false;
            } else if (item.equals("]") && !stack.pop().equals("[")) {
                return false;
            } else if (item.equals("}") && !stack.pop().equals("{")) {
                return false;
            }
        }
        return true;
    }
}
