package com.swj.ics.algorithms.StackQueueBag;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * author shiweijie
 * date 2018/5/5 下午5:34
 * 可动态扩容的基于数组的队列
 */
public class ResizingArrayQueue<Item> implements Iterable<Item> {

    private int first;
    private int last;
    private int counter;

    private Item[] array;

    public ResizingArrayQueue() {
        first = 0;
        last = 0;
        counter = 0;
        array = (Item[]) new Object[2];
    }

    public void resizeQueue(int newLength) {
        if (newLength <= counter) {
            return;
        }
        Item[] newArray = (Item[]) new Object[newLength];
        for (int i = 0; i < counter; i++) {
            newArray[i] = array[(i + first) % array.length];//todo:这里理解错了，使用了 counter
        }
        first = 0;
        last = counter;
        array = newArray;
    }

    public boolean isEmpty() {
        return counter == 0;
    }

    public void enqueue(Item item) {
        if (counter == array.length) {
            resizeQueue(array.length * 2);
        }
        array[last++] = item;
        counter++;
        //todo:这里理解错了，使用了 counter
        if (last == array.length) {//如果last指针指向了，队列的最后一个一个元素，则将last round为0，
            //这是因为 有这种情况，enqueue了10个元素，dequeue了5个元素，然后又enqueue了5个，此时总数为10个，但是
            //此时first指针为5，last 指针只能为4了
            last = 0;
        }
    }

    public Item dequeue() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        Item item = array[first];

        array[first] = null;
        first++;
        //老外把它叫做wrapRound
        if (first == array.length) {//todo:这里理解错了，使用了 counter
            first = 0;
        }
        counter--;
        //todo:没有把数组缩小
        if (counter > 0 && counter == array.length / 4) {
            resizeQueue(array.length / 2);
        }
        return item;
    }

    public Item peek() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return array[first];
    }

    class MyArrayQueueIterator implements Iterator<Item> {

        private int current = 0;

        @Override
        public boolean hasNext() {
            //我的想法是 counter > 0 ,这样就跟isEmpty 一样了，失去了MyArrayQueueIterator的独立性
            return current < counter;
        }

        @Override
        public Item next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            Item item = array[(current + first) % array.length];//todo:这里理解错了，使用了 counter
            current++;
            return item;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public Iterator<Item> iterator() {
        return new MyArrayQueueIterator();
    }

    public static void main(String[] args) {
        ResizingArrayQueue<Integer> myQueue = new ResizingArrayQueue<>();
        int[] arr = {1, 3, 5, 7, 9, 2, 4, 6, 8};
        for (int item : arr) {
            myQueue.enqueue(item);
        }
        System.out.println("deQueue...");
        for (int i = 0; i < 5; i++) {
            System.out.print(myQueue.dequeue() + ",");
        }

        System.out.println("\n enqueue again...");

        int[] arr2 = {11, 13, 15, 17, 19};

        for (int item2 : arr2) {
            myQueue.enqueue(item2);
        }
        System.out.println("iterator...");

        for (int item : myQueue) {
            System.out.print(item + ",");
        }
        System.out.println();
    }


}
