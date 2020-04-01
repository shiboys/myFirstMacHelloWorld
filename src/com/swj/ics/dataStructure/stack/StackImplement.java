package com.swj.ics.dataStructure.stack;

import java.util.ArrayList;
import lombok.Data;

/**
 * @author shiweijie
 * @date 2020/3/31 上午8:58
 */

class StackInArray {
    private int top;
    private int maxSize;
    private int[] elements;

    public StackInArray(int size) {
        maxSize = size;
        elements = new int[size];
        top = -1;
    }

    public boolean isEmpty() {
        return top == -1;
    }

    public boolean isFull() {
        return top == maxSize - 1;
    }

    public int push(int element) {
        if (!isFull()) {
            int oldValue = Integer.MIN_VALUE;
            if (top >= 0) {
                oldValue = elements[top];
            }
            top++;
            elements[top] = element;
            return oldValue;
        } else {
            resize(maxSize * 2);
            return push(element);
        }
    }

    public int pop() {
        if (!isEmpty()) {
            if (top < maxSize / 4) {
                //缩容
                resize(maxSize / 2);
            }
            return elements[top--];
        } else {
            System.out.println("the stack is empty");
            return -1;
        }
    }

    private void resize(int newSize) {
        int[] transferArray = new int[newSize];
        int copyLength = Math.min(maxSize, newSize);
        System.arraycopy(elements, 0, transferArray, 0, copyLength);
        elements = transferArray;
        maxSize = newSize;
    }

}

class StackInArrayList {
    ArrayList<Integer> stackList;

    public StackInArrayList() {
        stackList = new ArrayList<>();
    }

    private boolean isEmpty() {
        return stackList.isEmpty();
    }

    public Integer pop() {
        if (!isEmpty()) {
            Integer val = stackList.get(stackList.size() - 1);
            stackList.remove(stackList.size() - 1);
            return val;
        } else {
            System.out.println("the stack is empty!");
            return null;
        }
    }

    public void push(Integer val) {
        stackList.add(val);
    }
}

@Data
class StackNode {
    private Integer data;
    private StackNode next;

    public StackNode(Integer data) {
        this.data = data;
    }
}

class LinkedNodeStack {
    private StackNode head;
    private int size = 0;

    public boolean isEmpty() {
        return size == 0;
    }

    public void push(Integer x) {
        StackNode node = new StackNode(x);
        if (size == 0) {
            head = node;
        } else {
            StackNode temp = head;
            node.setNext(temp);
            head = node;
        }
        size++;
    }

    public Integer pop() {
        if (size == 0) {
            System.out.println("the stack is empty");
            return null;
        }
        StackNode temp = head;
        Integer val = temp.getData();
        head = head.getNext();
        temp = null;
        size--;
        return val;
    }
}

public class StackImplement {
    public static void main(String[] args) {
        //arrayStackTest();
        LinkedNodeStack stack = new LinkedNodeStack();
        for (int i = 1; i <= 10; i++) {
            stack.push(i);
        }
        for (int i = 1; i <= 11; i++) {
            System.out.print(stack.pop() + "\t");
        }
    }

    private static void arrayStackTest() {
        System.out.println("3.16.6".compareTo("3.16.7"));
        //System.out.println(Integer.valueOf(0).compareTo(null));
        StackInArray stack = new StackInArray(5);
        for (int i = 1; i <= 10; i++) {
            stack.push(i);
        }
        for (int i = 1; i <= 10; i++) {
            System.out.print(stack.pop() + "\t");
        }
        System.out.println();

        StackInArrayList stack2 = new StackInArrayList();

        for (int i = 11; i <= 20; i++) {
            stack2.push(i);
        }
        for (int i = 11; i <= 20; i++) {
            System.out.print(stack2.pop() + "\t");
        }

        System.out.println();
    }
}
