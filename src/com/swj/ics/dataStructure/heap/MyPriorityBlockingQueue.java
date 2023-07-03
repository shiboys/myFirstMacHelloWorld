package com.swj.ics.dataStructure.heap;

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/04/29 14:40
 */
public class MyPriorityBlockingQueue<E> extends AbstractQueue<E> implements BlockingQueue<E> {

  private static final int DEFAULT_CAPACITY = 11;
  private static final int MAX_CAPACITY = Integer.MAX_VALUE - 8;

  private ReentrantLock mainLock;

  private Condition notEmtpy;

  private int size = 0;

  private Object[] queue;

  private Comparator<? super E> comparator;

  public MyPriorityBlockingQueue() {

  }

  public MyPriorityBlockingQueue(int capacity, Comparator<? super E> comparator) {
    if (capacity < 1) {
      throw new IllegalArgumentException();
    }
    capacity = Math.min(capacity, MAX_CAPACITY);
    mainLock = new ReentrantLock();
    notEmtpy = mainLock.newCondition();
    queue = new Object[capacity];
    this.comparator = comparator;
  }


  /**
   * 获取堆顶元素，该方法应该在获取锁的情况下被调用
   *
   * @return
   */
  private E dequeue() {
    int n = size - 1;
    if (n < 0) {
      return null;
    }
    Object[] array = queue;
    E result = (E) array[0];
    E x = (E) array[n];
    array[0] = x;
    array[n] = null;
    Comparator<? super E> cmp = this.comparator;
    if (cmp != null) {
      shiftDownUseComparator(x, 0, array, n, cmp);
    } else {
      shiftDownComparable(x, 0, array, n);
    }
    size = n;
    return result;
  }

  /**
   * 将 key 元素从 k 位置开始向下移动。这里定义为 static 是为了工具类方法使用，减少类实力的成员方法元数据数
   *
   * @param key   目标元素
   * @param k     起始位置
   * @param array
   * @param n
   * @param <T>
   */
  private static <T> void shiftDownComparable(T key, int k, Object[] array, int n) {
    if (n < 0) {
      return;
    }
    int half = n >> 1;
    while (k < half) {
      int childIndex = (k << 1) + 1;
      // 如果比最下的元素还要小，则不需要下沉，否则需要继续下沉
      int rightChildIndex = childIndex + 1;
      Object o = array[childIndex];
      if (rightChildIndex < n && (((Comparable<? super T>) o).compareTo((T) array[rightChildIndex]) > 0)) {
        childIndex = rightChildIndex;
      }
      // 如果目标元素比左右子节点的值都小，则 k 位置就确定了
      if (((Comparable<? super T>) key).compareTo((T) array[childIndex]) < 0) {
        break;
      }
      // 否则，就交换 k 位置和 childIndex 位置的值，让 key 下降
      exchange(k, childIndex, array);
      k = childIndex;
    }
    array[k] = key;
  }

  private static <T> void shiftDownUseComparator(T key, int k, Object[] array, int n,
      Comparator<? super T> comparator) {
    if (key != array[k]) {
      throw new IllegalStateException();
    }
    int half = n >>> 1;
    while (k < half) {
      int childIndex = (k << 1) + 1;
      int rightChildIndex = childIndex + 1;
      Object o = array[childIndex];
      if (rightChildIndex < n && (comparator.compare((T) o, (T) array[rightChildIndex]) > 0)) {
        childIndex = rightChildIndex;
      }
      if (comparator.compare(key, (T) array[childIndex]) < 0) {
        break;
      }
      exchange(k, childIndex, array);
      k = childIndex;
    }
    array[k] = key;
  }

  /**
   * 堆元素上浮，这里跟 JDK 不一样的地方在于，调用方一定要确保 x == array[k]
   *
   * @param x
   * @param k
   * @param array
   * @param <T>
   */
  private static <T> void shiftUpComparable(T x, int k, Object[] array) {
    if (x != array[k]) {
      throw new IllegalStateException();
    }
    Comparable<? super T> key = (Comparable<? super T>) x;
    while (k > 0) {
      int parentIndex = (k - 1) >>> 1;
      Object e = array[parentIndex];
      if (key.compareTo((T) e) >= 0) { //当前节点和父节点的值相等，则也不再上升。
        break;
      }
      // 只有 x == array[k】这样才能使用 exchange，将元素在数组中的位置互换。
      exchange(k, parentIndex, array);
      k = parentIndex;
    }
    array[k] = key;
  }


  private static <T> void shiftUpUseComparator(T x, int k, Object[] array, Comparator<? super T> comparator) {
    if (x != array[k]) {
      throw new IllegalStateException();
    }
    T key = x;
    while (k > 0) {
      int parentIndex = (k - 1) >>> 1;
      Object e = array[parentIndex];
      if (comparator.compare(key, (T) e) >= 0) {
        break;
      }
      // 只有 x == array[k】这样才能使用 exchange，将元素在数组中的位置互换。
      exchange(k, parentIndex, array);
      k = parentIndex;
    }
    array[k] = key;
  }

  private static void exchange(int i, int j, Object[] array) {
    Object x = array[i];
    array[i] = array[j];
    array[j] = x;
  }



  @Override
  public int size() {
    return size;
  }

  @Override
  public void put(Object o) throws InterruptedException {

  }

  @Override
  public boolean offer(Object o, long timeout, TimeUnit unit) throws InterruptedException {
    return false;
  }

  @Override
  public E take() throws InterruptedException {
    return null;
  }

  @Override
  public E poll(long timeout, TimeUnit unit) throws InterruptedException {
    return null;
  }

  @Override
  public int remainingCapacity() {
    return 0;
  }

  @Override
  public int drainTo(Collection c) {
    return 0;
  }

  @Override
  public int drainTo(Collection c, int maxElements) {
    return 0;
  }

  @Override
  public boolean offer(Object o) {
    return false;
  }

  @Override
  public E poll() {
    return null;
  }

  @Override
  public E peek() {
    return null;
  }

  @Override
  public boolean remove(Object o) {
    return super.remove(o);
  }

  @Override
  public Iterator iterator() {
    return null;
  }
}
