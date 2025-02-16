package com.swj.ics.dataStructure.queue;

import java.util.Stack;

/**
 * @author shiweijie
 * @date 2020/4/13 下午8:01
 * 2个队列实现一个栈。
 * 思路：入队时，将队列押入s1，
 * 出队时：判断S2是否为空，如不为空，则直接弹出栈顶元素，若为空，则将s1的元素逐个"倒入"s2,把最后一个元素弹出并出队列。
 * 这个思路，可以避免反复"倒"栈，仅在需要的时候，才"倒"一次。但在实际面试的时候，很少人有人想到
 */
public class QueueWithTwoStack {
    private Stack<Integer> stackIn = new Stack<>();
    private Stack<Integer> stackOut = new Stack<>();

    private boolean empty() {
        return stackIn.isEmpty() && stackOut.isEmpty();
    }

    public void push(Integer i) {
        stackIn.push(i);
    }

    public Integer pop() {
        if (empty()) {
            System.out.println("the queue is empty");
            return -1;
        }
        if (stackOut.isEmpty()) {
            while (!stackIn.isEmpty()) {
                Integer num = stackIn.pop();
                stackOut.push(num);
                System.out.println("stackIn 倒入 stackOut " + num);
            }
        }
        return stackOut.pop();
    }

    public static void main(String[] args) {
        QueueWithTwoStack instance = new QueueWithTwoStack();
        for (int i = 1; i <= 5; i++) {
            instance.push(i);
        }
        System.out.println("先出2个元素");
        System.out.print(instance.pop()+"\t");
        System.out.print(instance.pop()+"\t");
        for (int i = 6; i <= 10; i++) {
            instance.push(i);
        }
        System.out.println("\n再出其他元素");
        while (!instance.empty()) {
            System.out.print(instance.pop()+"\t");
        }
        System.out.println("\n");
    }
}
