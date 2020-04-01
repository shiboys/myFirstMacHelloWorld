package com.swj.ics.dataStructure.stack;

/**
 * @author shiweijie
 * @date 2020/3/31 上午8:58
 */

class StackInArray {
    private int top = -1;
    private int maxSize;
    private int[] elements;

    public StackInArray(int size) {
        maxSize = size;
        elements = new int[size];
        top = 0;
    }

    public boolean isEmpty() {
        return top == -1;
    }

    public boolean isFull() {
        return top == maxSize - 1;
    }

    public int push(int element) {
        if (!isFull()) {
            int oldValue = elements[top];
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

public class StackImplement {
    public static void main(String[] args) {
        System.out.println("3.16.6".compareTo("3.16.7"));
        //System.out.println(Integer.valueOf(0).compareTo(null));
        StackInArray stack = new StackInArray(10);
        for (int i = 1; i <= 10; i++) {
            stack.push(i);
        }
        for (int i = 1; i <= 10; i++) {
            System.out.print(stack.pop() + "\t");
        }
        System.out.println();
    }
}
