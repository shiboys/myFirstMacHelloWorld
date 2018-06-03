package com.swj.ics.algorithms.StackQueueBag;

/**
 * author shiweijie
 * date 2018/5/6 下午5:26
 * 使用栈来计算表达式的值
 */
public class EvaluateWithStack {

    public static void main(String[] args) {
        String exp1 = "( 1 + ( ( 2 + 3 ) * ( 4 * 5 ) ) )";
        Double result = evaluate(exp1);
        System.out.println(result);

        exp1 = "2 + 3 + 4 * 5 + 6";
        result = evaluate(exp1);
        System.out.println(result);
    }

    public static Double evaluate(String exp) {
        if (exp == null || exp =="") {
            return null;
        }
        String[] splitArr = exp.split(" ");
        ResizingArrayStack<String> ops = new ResizingArrayStack<>();
        ResizingArrayStack<Double> numStack = new ResizingArrayStack<>();
        for(String item : splitArr) {
            if(item.equals("(")) {

            } else if(item.equals("+") || item.equals("-")
                    || item.equals("*") || item.equals("/")
                    || item.equals("sqrt")) {
                ops.push(item);
            }
            else if(item.equals(")")) {
             String op = ops.pop();
             Double num1 = numStack.pop();
             Double num2 = numStack.pop();
             Double result = null;
             if(op.equals("+")) {
                 result = num1 + num2;
             } else if(op.equals("-")) {
                 result = num2 - num1;
             } else if (op.equals("*")) {
                 result = num1 * num2;
             } else if(op.equals("/")) {
                 result = num2 / num1;
             } else if(op.equals("sqrt")) {
                 numStack.push(num2);
                 result = Math.sqrt(num1);
             }

             if(result != null) {
                 numStack.push(result);
             }
            } else {
                numStack.push(Double.parseDouble(item));
            }

        }

        while (!ops.isEmpty()) {
            String op = ops.pop();
            Double num1 = numStack.pop();
            Double num2 = numStack.pop();
            Double result = null;
            if(op.equals("+")) {
                result = num1 + num2;
            } else if(op.equals("-")) {
                result = num2 - num1;
            } else if (op.equals("*")) {
                result = num1 * num2;
            } else if(op.equals("/")) {
                result = num2 / num1;
            } else if(op.equals("sqrt")) {
                numStack.push(num2);
                result = Math.sqrt(num1);
            }

            if(result != null) {
                numStack.push(result);
            }
        }

        if(!numStack.isEmpty()) {
            return numStack.pop();
        } else {
            return null;
        }
    }


}
