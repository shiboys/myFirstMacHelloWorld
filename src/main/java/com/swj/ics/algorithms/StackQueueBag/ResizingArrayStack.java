package com.swj.ics.algorithms.StackQueueBag;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * author shiweijie
 * date 2018/5/5 下午2:51
 * 能动态扩容的基于数组的栈
 */
public class ResizingArrayStack<Item> implements Iterable<Item> {

    private Item[] array = (Item[]) new Object[2]; //这里原来是1，但是看书籍的官方网站，是2，
    // 考虑到每次扩大都是2的倍数，跟hashMap同样的增大原理，就使用了2
    private int counter;

    public void push(Item item) {
        if (counter == array.length) {
            resize(array.length * 2);
        }
        array[counter++] = item;
    }

    public Item pop() {
        //todo:这里漏掉了，非常重要的检查
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        /*Item temp =length array[counter - 1];
        array[counter - 1] = null;
        counter--;*/
        Item temp = array[--counter];
        array[counter] = null;

        if (counter > 0 && counter == array.length / 4) {
            resize(array.length / 2);
        }
        return temp;

    }

    //todo:在此实现peek

    public Item peek() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return array[counter - 1];
    }

    public boolean isEmpty() {
        return counter == 0;
    }

    public int size() {
        return counter;
    }

    public void resize(int newLength) {
        if (newLength <= array.length) {
            return;
        }
        Item[] newArray = (Item[]) new Object[newLength];

        for (int i = 0, length = array.length; i < length; i++) {
            newArray[i] = array[i];
        }
        array = newArray;
    }

    class MyStackIterator implements Iterator<Item> {

        private int i = counter;

        @Override
        public boolean hasNext() {
            return i > 0;
        }

        @Override
        public Item next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return array[--i];
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }


    @Override
    public Iterator<Item> iterator() {
        return new MyStackIterator();
    }


    public static void main(String[] args) {
        ResizingArrayStack<Integer> myStack = new ResizingArrayStack<>();
        int[] arr = {1, 3, 5, 7, 9, 2, 4, 6, 8};
        for (int item : arr) {
            myStack.push(item);
        }

        for (int item : myStack) {
            System.out.print(item + ",");
        }
        System.out.println();
    }
}
