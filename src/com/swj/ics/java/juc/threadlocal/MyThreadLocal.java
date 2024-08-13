package com.swj.ics.java.juc.threadlocal;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/04/04 15:22
 * ThreadLocal 最佳实践：
 * 废弃项目的回收依赖于显式地触发，否则就要等待线程结束，进而回收相应ThreadLocalMap！
 * 这就是很多OOM的来源，所以通常都会建议，应用一定要自己负责remove，
 * 并且不要和线程池配合，因为worker线程往往是不会退出的。
 */
public class MyThreadLocal<T> {

  private final int threadLocalHashCode = nextHashCode();

  private static final int HASH_INCREMENT = 0x61c88647;

  private static AtomicInteger nextHashCode = new AtomicInteger();

  private static int nextHashCode() {
    return nextHashCode.getAndAdd(HASH_INCREMENT);
  }

  protected T initialValue() {
    return null;
  }

  public MyThreadLocal() {

  }

  public T get() {
    Thread t = Thread.currentThread();
    if (!(t instanceof MyThreadLocalThread)) {
      throw new IllegalArgumentException("current thread is not instance of MyThreadLocalThread。");
    }
    MyThreadLocalThread tt = (MyThreadLocalThread) t;
    MyThreadLocalMap map = getMap(tt);
    if (map != null) {
      MyThreadLocalMap.Entry entry = map.getEntry(this);
      if (entry != null) {
        @SuppressWarnings("unchecked")
        T result = (T) entry.value;
        return result;
      }
    }
    return setInitialValue(tt);
  }

  private T setInitialValue(MyThreadLocalThread tt) {
    T initialValue = initialValue();
    MyThreadLocalMap map = getMap(tt);
    if (map != null) {
      map.set(this, initialValue);
    } else {
      createMap(tt, initialValue);
    }
    return initialValue;
  }

  public void set(T value) {
    Thread t = Thread.currentThread();
    if (!(t instanceof MyThreadLocalThread)) {
      throw new IllegalArgumentException("current thread is not instance of MyThreadLocalThread。");
    }
    MyThreadLocalThread tt = (MyThreadLocalThread) t;
    MyThreadLocalMap map = getMap(tt);
    if (map != null) {
      map.set(this, value);
    } else {
      createMap(tt, value);
    }
  }

  private void createMap(MyThreadLocalThread tt, T value) {
    tt.threadLocalMaps = new MyThreadLocalMap(this, value);
  }

  private MyThreadLocalMap getMap(MyThreadLocalThread tt) {
    return tt.threadLocalMaps;
  }

  public void remove() {
    Thread t = Thread.currentThread();
    if (!(t instanceof MyThreadLocalThread)) {
      throw new IllegalArgumentException("current thread is not instance of MyThreadLocalThread。");
    }
    MyThreadLocalThread tt = (MyThreadLocalThread) t;
    MyThreadLocalMap map = getMap(tt);
    if (map != null) {
      map.remove(this);
    }
  }

  static class MyThreadLocalMap {

    static class Entry extends WeakReference<MyThreadLocal<?>> {
      Object value;

      public Entry(MyThreadLocal<?> referent, Object v) {
        super(referent);
        value = v;
      }
    }


    private static final int INITIAL_CAPACITY = 16;
    public Entry[] table;
    // map 的元素个数
    private int size;
    // map 元素个数的阈值
    private int threshold;

    public void setThreshold(int len) {
      threshold = len * 2 / 3; // 2/3 作为 map 的负载因子
    }

    MyThreadLocalMap(MyThreadLocal<?> firstKey, Object firstValue) {
      table = new Entry[INITIAL_CAPACITY];
      int firstKeyIndex = firstKey.threadLocalHashCode & (INITIAL_CAPACITY - 1);
      table[firstKeyIndex] = new Entry(firstKey, firstValue);
      size = 1;
      setThreshold(INITIAL_CAPACITY);
    }

    private MyThreadLocalMap(MyThreadLocalMap parentMap) {
      // todo : 给 inheritableThreadLocal 使用
    }

    private Entry getEntry(MyThreadLocal<?> searchKey) {
      int searchIndex = searchKey.threadLocalHashCode & (table.length - 1);
      Entry e = table[searchIndex];
      if (e.get() == searchKey) {
        return e;
      } else {
        return getEntryAfterMiss(searchKey, searchIndex, e);
      }
    }

    /**
     * 使用现象探查法解决 hash 查找冲突的问题
     *
     * @param searchKey
     * @param i
     * @param e
     * @return
     */
    private Entry getEntryAfterMiss(MyThreadLocal<?> searchKey, int i, Entry e) {
      Entry[] tab = table;
      int len = tab.length;

      while (e != null) {
        MyThreadLocal<?> k = e.get();
        if (k == searchKey) {
          return e;
        } else if (k == null) { // thread local 对象已经被清除了
          deleteExpiredEntry(i);
        } else {
          i = nextIndex(i, len);
        }
        e = table[i];
      }
      return null;
    }

    /**
     * 删除已经过期的Entry(key 已经为 null 的)
     *
     * @param expiredSlot 已经过期(key == null)的索引为
     * @return 下一个 entry == null 的位置
     */
    private int deleteExpiredEntry(int expiredSlot) {
      Entry[] tab = table;
      int len = tab.length;

      // 将 expireSlot 位置清空
      tab[expiredSlot].value = null;
      tab[expiredSlot] = null;
      size--;

      // 需要把 expireSlot 到下一个 entry 为 null 之间的所有 元素进行 rehash
      int i;
      Entry e;
      for (i = nextIndex(expiredSlot, len);
           (e = tab[i]) != null;
           i = nextIndex(i, len)) {
        MyThreadLocal<?> key = e.get();
        if (key == null) {
          // 将 i 位置清空
          tab[i].value = null;
          tab[i] = null;
          size--;
        } else {
          // rehash，如果 newIndex != i, 说明，i 不是 e 的原始位置，将 e 放入到一个 新的为 null 空位置上
          int newSlotIndex = key.threadLocalHashCode & (len - 1);
          if (newSlotIndex != i) {
            tab[i] = null; // 元素要转移到 newSlotIndex ，原来的  i 位置需要清空
            while (tab[newSlotIndex] != null) {
              newSlotIndex = nextIndex(newSlotIndex, len);
            }
            tab[newSlotIndex] = e;
          }
        }
      }
      return i;
    }

    private int nextIndex(int i, int len) {
      int nextIndex = i + 1;
      if (nextIndex >= len) {
        return 0;
      }
      return nextIndex + 1;
    }

    private int prevIndex(int i, int len) {
      return i <= 0 ? len - 1 : i - 1;
    }

    public void set(MyThreadLocal<?> key, Object value) {
      Entry[] tab = table;
      int len = tab.length;
      int i = key.threadLocalHashCode & (len - 1);
      for (Entry e = tab[i]; e != null; e = tab[i = nextIndex(i, len)]) {
        MyThreadLocal<?> k = e.get();

        if (k == key) {
          e.value = value;
          return;
        }

        if (k == null) {
          replaceOldEntry(key, value, i);
          return;
        }
      }
      // 走到这里，找到了一个为 null 的位置，可以插入元素
      tab[i] = new Entry(key, value);
      int sz = ++size;
      if (!cleanSomeSlot(i, sz) && sz >= threshold) {
        rehash();
      }

    }

    /**
     * 将 expireSlot 位置的元素替换为 key,value 所代表的元素(用已存在的或者更新的)
     *
     * @param key         threadLocal instance
     * @param value       value
     * @param expiredSlot 过期的index
     */
    private void replaceOldEntry(MyThreadLocal<?> key, Object value, int expiredSlot) {
      Entry[] tab = table;
      int len = tab.length;
      int startIndexToClean = expiredSlot;
      Entry e;

      /**
       * 需要查找一次替换中 expireSlot 相邻的最前面的那个 过期的元素，元素排列如下所示
       * NULL ...N-3, N-2, N-1, N(expireSlot) , N+1, N+2，N+3 ... NULL
       */
      for (int i = prevIndex(expiredSlot, len); (e = tab[i]) != null; i = prevIndex(i, len)) {
        if (e.get() == null) { // 如果找到 N 之前的已过期的元素,则标记之
          startIndexToClean = i;
        }
      }

      // 接下来需要进行替换操作，替换之前，先查找 N 以及以后的元素是否有 key == paramKey 的情况
      for (int i = nextIndex(expiredSlot, len); (e = tab[i]) != null; i = nextIndex(i, len)) {
        MyThreadLocal<?> k = e.get();
        if (k == key) { // 找到了这个元素
          e.value = value;
          // 交换他们
          tab[i] = tab[expiredSlot];
          tab[expiredSlot] = e;
          // 如果 N 之前没有找到过期元素，则将起始过期元素标记设为当前的 i
          if (startIndexToClean == expiredSlot) {
            startIndexToClean = i;
          }
          cleanSomeSlot(deleteExpiredEntry(startIndexToClean), len);
          return;
        }
        // 如果 在 N 之后的位置找到新的过期元素，则更新开始清理过期元素的标记位置
        if (k == null && startIndexToClean == expiredSlot) {
          startIndexToClean = i;
        }
      }
      // 到这里，说明N之后没有元素的 key 跟 paramKey 相等，当前 slotIndex 也已经过期，则在当前 slot 的位置放置新元素
      tab[expiredSlot].value = null;
      tab[expiredSlot] = new Entry(key, value);
      // 如果在 N 之前或者之后找到了 另外一个 需要清理的位置，则从该位置起清理部分过期元素
      if (startIndexToClean != expiredSlot) {
        cleanSomeSlot(deleteExpiredEntry(startIndexToClean), len);
      }
    }

    /**
     * 清理部分已过期的元素
     *
     * @param expireSlot 清理元素的起始位置
     * @param n          控制器，默认向后遍历 logN 个元素。
     */
    private boolean cleanSomeSlot(int expireSlot, int n) {
      Entry[] tab = table;
      int len = tab.length;
      int i = expireSlot;
      boolean removed = false;
      do {
        i = nextIndex(i, len);
        Entry e = tab[i];
        if (e != null && e.get() == null) {
          n = len;
          removed = true;
          i = deleteExpiredEntry(i);
        }
      } while ((n >>>= 1) != 0);

      return removed;
    }

    /**
     * 扩容 & rehash
     */
    private void rehash() {
      deleteExpiredEntries();
      if (size >= threshold - threshold / 4) {
        resize();
      }
    }

    /**
     * double the capacity of the table
     */
    private void resize() {
      Entry[] oldTab = table;
      int oldLen = oldTab.length;
      int newLength = oldLen * 2;
      Entry[] newTab = new Entry[newLength];
      Entry e;
      int count = 0;
      for (int j = 0; j < oldLen; j++) {
        e = oldTab[j];
        if (e != null) {
          MyThreadLocal<?> k = e.get();
          if (k == null) {
            e.value = null;// help gc
          } else {
            int h = k.threadLocalHashCode & (newLength - 1);
            while (newTab[h] != null) {
              h = nextIndex(h, newLength);
            }
            newTab[h] = e;
            count++;
          }
        }
      }
      table = newTab;
      size = count;
      setThreshold(newLength);
    }

    private void deleteExpiredEntries() {
      Entry[] tab = table;
      int len = tab.length;
      for (int j = 0; j < len; j++) {
        Entry e = tab[j];
        if (e != null && e.get() == null) {
          deleteExpiredEntry(j);
        }
      }
    }

    private void remove(MyThreadLocal<?> key) {
      Entry[] tab = table;
      int len = tab.length;
      int h = key.threadLocalHashCode & (len - 1);
      Entry e;
      for (; (e = tab[h]) != null; h = nextIndex(h, len)) {
        if (e.get() == key) {
          e.clear();
          deleteExpiredEntry(h);
          return;
        }
      }
    }
  }

}
