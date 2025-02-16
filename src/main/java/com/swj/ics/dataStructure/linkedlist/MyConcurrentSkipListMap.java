package com.swj.ics.dataStructure.linkedlist;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/11/23 17:10
 * 借鉴 JUC 的 ConcurrentSkipListMap 来实现跳表，
 * 非常的不容易，我读了一遍别人写的源码解读
 * 又写了一遍源码笔记
 * 最后又临摹了一遍这个源码，从中体会 并发安全的跳跃表是怎么书写的。
 */
public class MyConcurrentSkipListMap<K extends Comparable<K>, V> {
  // 先写内部类
  // 内部列主要有 3 个： Node<K,V> 代表最底层的节点对象
  // 索引节点对象 Index<K,V>
  // 头结点索引对象 HeadIndex<K,V>
  // 内部类有 cas 方法，用老了 unsafe 对象

  private static final Object BASE_HEADER = new Object();

  private volatile HeadIndex<K, V> head;

  private final Comparator<? super K> comparator;

  public MyConcurrentSkipListMap() {
    this.comparator = null;
    initialize();
  }

  public MyConcurrentSkipListMap(Comparator<? super K> cmp) {
    this.comparator = cmp;
    initialize();
  }

  private void initialize() {
    head = new HeadIndex<>(new Node<>(null, BASE_HEADER, null), // key 为 null ，value 为 null 表示节点被删除
        null, null, 1);
  }

  boolean casHead(HeadIndex<K, V> old, HeadIndex<K, V> newHead) {
    return UNSAFE.compareAndSwapObject(this, HEAD_OFFSET, old, newHead);
  }

  static final class Node<K, V> {
    private final K key;
    private volatile Object value;
    private volatile Node<K, V> next;

    public Node(K key, Object value, Node<K, V> next) {
      this.key = key;
      this.value = value;
      this.next = next;
    }

    /**
     * 将当前节点制作成 marker 节点，标记删除
     *
     * @param next
     */
    public Node(Node<K, V> next) {
      this.key = null;
      this.value = this;
      this.next = next;
    }

    // 包内可见性
    boolean casValue(Object oldValue, Object value) {
      return UNSAFE.compareAndSwapObject(this, VALUE_OFFSET, oldValue, value);
    }

    /**
     * @param n  next
     * @param nn the new next node of current node
     * @return
     */
    boolean casNext(Node<K, V> n, Node<K, V> nn) {
      return UNSAFE.compareAndSwapObject(this, NEXT_OFFSET, n, nn);
    }

    /**
     * 将当前节点的后继节点标记为 marker 节点，表明当前节点需要被删除了
     *
     * @param f follower 当前节点的后继节点
     * @return
     */
    boolean appendMarker(Node<K, V> f) {
      return casNext(f, new Node<>(f));
    }

    /**
     * 帮忙删除当前节点
     *
     * @param b before node，当前节点的前驱节点
     * @param f follower node, 当前节点后继节点
     */
    void helpDelete(Node<K, V> b, Node<K, V> f) {
      if (b.next == this && next == f) { // 检查下，确保当前节点处于 b 和 f 之间
        if (f != null && f.value == f) { // 说明当前节点的后继节点是一个 marker 节点，则删除当前节点
          //b.next = f.next; cas 还是没用熟练 cas ，没用熟练的话，你就不能说自己是线程安全的跳表~~！！！
          b.casNext(this, f.next);
        } else { // 标记f节点 为 marker 节点
          // cas 方式把当前节点的 next 节点 f 给替换成 marker 节点
          casNext(f, new Node<>(f));
        }
      }
    }



    // unsafe 机制
    private static final sun.misc.Unsafe UNSAFE;
    private static final long VALUE_OFFSET;
    private static final long NEXT_OFFSET;

    static {
      UNSAFE = getUnsafe();
      try {
        Class<?> clazz = Node.class;
        VALUE_OFFSET = UNSAFE.objectFieldOffset(clazz.getDeclaredField("value"));
        NEXT_OFFSET = UNSAFE.objectFieldOffset(clazz.getDeclaredField("next"));
      } catch (Exception e) {
        throw new Error(e);
      }
    }
  }


  static class Index<K, V> {
    final Node<K, V> node;
    final Index<K, V> down;
    volatile Index<K, V> right;

    // 为什么 node 和 down 是 final 的 而 right 是 volatile 的，因为 node 和 down 一旦构造函数生成，就不变了，而right 是会被改变的
    public Index(Node<K, V> node, Index<K, V> down, Index<K, V> right) {
      this.node = node;
      this.down = down;
      this.right = right;
    }

    static final sun.misc.Unsafe UNSAFE;
    static final long RIGHT_OFFSET;

    static {
      UNSAFE = getUnsafe();
      Class<?> clazz = Index.class;
      try {
        RIGHT_OFFSET = UNSAFE.objectFieldOffset(clazz.getDeclaredField("right"));
      } catch (NoSuchFieldException e) {
        throw new Error(e);
      }
    }

    final boolean casRight(Index<K, V> old, Index<K, V> newIdx) {
      return UNSAFE.compareAndSwapObject(this, RIGHT_OFFSET, old, newIdx);
    }
    //链表节点的增加和删除

    /**
     * 采用头插法将 newSucc 插入到当前 Index 节点的后面
     *
     * @param succ    index 节点的原来后继节点
     * @param newSucc index 节点的新后继节点
     * @return
     */
    final boolean link(Index<K, V> succ, Index<K, V> newSucc) {
      // newSucc 还没有多线程线程竞争的情况
      newSucc.right = succ;
      // 确保当前 index 节点所指向的 node 节点没有被删除 且 能 成功 cas right 指针
      return node.value != null && casRight(succ, newSucc);
    }

    // 删除 succ 节点，也就是将 succ 节点从 当前 level 的 水平的链表中删除
    final boolean unlink(Index<K, V> succ) {
      return node.value != null && casRight(succ, succ.right);
    }
  }


  /**
   * 头节点索引类。头结点索引类，就是 索引的第一列，保存着最高的索引层级高度
   *
   * @param <K>
   * @param <V>
   */
  static class HeadIndex<K, V> extends Index<K, V> {

    private int level;

    public HeadIndex(Node<K, V> node, Index<K, V> down, Index<K, V> right, int level) {
      super(node, down, right);
      this.level = level;
    }
  }


  /* ---------------------------- Comparision Utilities -------------- */

  @SuppressWarnings({"rawtypes", "unchecked"})
  static final int cmpr(Comparator c, Object a, Object b) {
    return c != null ? c.compare(a, b) : ((Comparable) a).compareTo(b);
  }

  /* ---------------------------- 遍历 Traversal -------------- */

  /**
   * traverse 遍历跳表，返回 等于 key 的前驱节点。如果不存在，则返回 < key 的最后一个前驱节点
   */

  private Node<K, V> findPredecessor(Object key, Comparator<? super K> comparator) {
    if (key == null) {
      throw new NullPointerException();
    }
    //int loopCount =0;
    for (; ; ) {
      for (Index<K, V> p = head, r = p.right; ; ) {
        //System.out.println("查找次数：" + (++loopCount));
        if (r != null) { // 当前 level 的链表没有遍历到头
          if (r.node.value == null) { // r 索引节点所引用的 node 被标记删除了
            // 删除 r 节点
            if (!p.unlink(r)) {
              break; // 如果未能 cas 删除 r 节点，说明 r 节点被别的线程更改了，则退出内层循环重新查找。这也是嵌套两层循环的原因。
            }
            // r 节点被删除，r 指针后移。此时 p 的 right 指针在 unlink 函数被更改了，所以这里直接重复使用没问题。
            r = p.right;
            continue;
          }
          K k = r.node.key;
          // 如果当前节点 k 比 key 小，则需要后移 r，直到 r == null 或者 r.node.key >= key
          if (cmpr(comparator, k, key) < 0) {
            p = r; // p 指针移动
            r = r.right;
            continue;
          }
        }
        if (p.down == null) { // 遍历到最后，level=1 层的 index 节点 down 为 null
          return p.node;
        }
        // 如果当前 level 遍历到头，则下降 level
        p = p.down;// p 只会一直降，保持在第一列的索引位
        r = p.right;
        // NOTE：注意，本方法是获取 key 的前驱节点， 不是快速找到 等于 key 的 Node 节点，因此也没有判断 cmp ==0 的情况。
      }
    }
  }

  /**
   * 查找等于指定 key 的节点
   * 思路是先找到 前驱节点 p，然后经过 一些列 线程安全的检查
   * 最后再比较，最终比较结果等于 0 的情况返回，其他情况返回 null
   * 为了防止其他线程破坏 查找过程，这里也是使用了双循环
   *
   * @param key
   * @return
   */
  Node<K, V> findNode(Object key) {
    if (key == null) {
      throw new NullPointerException();
    }
    Comparator<? super K> cmp = comparator;
    outer:
    for (; ; ) {
      for (Node<K, V> p = findPredecessor(key, cmp), n = p.next; ; ) {
        if (n == null) { // p 是链表最后一个节点且不等于 p.key != key
          break outer;
        }
        // 线程安全的检查，主要有 3 项：
        //1、一致性读，2、n 被标记删除，3、p 被标记删除。满足三项其一，则重新进行外层循环，重新进入内层
        if (n != p.next) {
          break;
        }
        Object v;
        Node<K, V> nx = n.next;
        if ((v = n.value) == null) {
          // 删除 n 节点
          n.helpDelete(p, nx);
          break;
        }
        if (p.value == null || v == n) { // p 被标记删除，或者 n 是个 marker 节点，则都需要将 p 删除，但是上下文没有 p 的前驱节点，
          // 则只能 通过 findPredecessor() 方法重新查找
          break;
        }
        // 三项线程安全检查做完，则开始比较值
        int c = cmpr(cmp, key, n.key);
        if (c == 0) {
          return n;
        } else if (c < 0) { // key < n.key 比较罕见，说明 n 节点比要查找的 key 节点大，那就需要
          break outer; // 重新回炉进行查找
        }
        // 其他情况就是 key > n.key ，则 n 指针需要继续后移
        p = n;
        n = nx;
      }
    }
    return null;
  }


  public V get(Object key) {
    return doGet(key);
  }

  /**
   * doGet 方法的内部逻辑跟 跟 findNode 几乎一摸一样，本来可以重用，但是 jdk 没有重用，可能是为了显得代码行数多？
   * 那这里再重写一遍，就不再添加很多注释了
   *
   * @param key
   * @return
   */
  private V doGet(Object key) {
    if (key == null) {
      throw new NullPointerException();
    }
    Comparator<? super K> cmp = comparator;
    outer:
    for (; ; ) {
      for (Node<K, V> p = findPredecessor(key, cmp), n = p.next; ; ) {
        if (n == null) { // 根本没找到
          break outer;
        }
        // 还是老样的的线程安全 3 检查
        if (n != p.next) { // 非一致性读
          break;
        }
        Object v;
        Node<K, V> nx = n.next;
        if ((v = n.value) == null) {
          // 删除 n 节点
          n.helpDelete(p, nx);
          break;
        }
        if (p.value == null || v == n) { // p is deleted
          break;
        }
        int c = cmpr(cmp, key, n.key);
        if (c == 0) { // n 节点就是
          @SuppressWarnings("unchecked")
          V val = (V) v;
          return val;
        }
        if (c < 0) { // key < n.key  key 处在 p 和 n 之间，那就是没找到
          break outer;
        }
        // key > n.key ，需要 n 向后移动
        p = n;
        n = nx;
      }
    }
    return null;
  }

  /**
   * 增加元素
   *
   * @param key
   * @param value
   * @return
   */
  public V put(K key, V value) {
    if (value == null) {
      throw new NullPointerException();
    }
    return doPut(key, value, false);
  }

  private V doPut(K key, V value, boolean onlyIfAbsent) {
    /**
     * 跳表增加元素非常复杂，但复杂归复杂，我们要化繁为简，将整个过程分为 3 大步：
     * 1、跟 findNode 逻辑很类似，找到前驱节点，将 key-value 封装成新的 newNode 插入前驱和后继之间
     * 2、判断是否生成 index。如果不生成 index 75% 的概率，则 put 结束。否则根据根据随机元素的奇偶特性生成层高 level。
     * 然后生成 level 个 索引节点的链表表示生成 基于 newNode 索引
     * 3、将 index 索引 插入到跳表中。将 index 链表的每一层的节点跟前后连接起来
     */
    if (key == null) {
      throw new NullPointerException();
    }
    Comparator<? super K> cmp = comparator;
    Node<K, V> newNode = new Node<>(key, value, null);
    outer:
    for (; ; ) {
      for (Node<K, V> p = findPredecessor(key, cmp), n = p.next; ; ) {
        if (n != null) {// 判断是否空跳表
          if (n != p.next) { // 不一致读
            continue;
          }
          Node<K, V> nx = n.next;
          Object v;
          if ((v = n.value) == null) {// 将 n 节点删除
            n.helpDelete(p, nx);
            n = p.next;// 后移 n 指针
            continue;
          }
          if (p.value == null || v == n) { // p 被删除，则重新查找 p
            break;
          }
          // 比价当前值
          int c = cmpr(cmp, key, n.key);
          if (c == 0) {// 找到相同 key 的节点。
            @SuppressWarnings("unchecked")
            V oldVal = (V) v;
            // 如果不替换 或者 能成功替换，则返回原来的值
            if (onlyIfAbsent || p.casValue(v, value)) {
              return oldVal;
            }
            break;// 替换不成功，则跳出循环重试
          } else if (c > 0) { // key > n.key 。n 需要继续后移
            p = n;
            n = nx;
            continue; //没有找到正确的 n  则继续查找
          }
        }
        // 将 newNode 插入最底层链表中
        newNode.next = n;
        if (!p.casNext(n, newNode)) {
          break; // cas 替换不成功，则重试
        }
        break outer;// cas 替换成功，表明节点被成功地插入最底层链表中，跳出循环
      }
    }
    //2 判断是否生成 index
    int rnd = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE) + 1;
    if ((rnd & 0x80000001) != 0) { // 判断当前随机数是否是正偶数，概率为 25%
      // 75% 的概率不生成索引节点，直接插入根部就完事了。
      return null;
    }
    int level = 1;
    while (((rnd >>>= 1) & 1) != 0) {
      // 50 % 的概率升到第 2 层，然后 12.5% 的概率升到第 3 层，依次类推。。。
      level++;
    }
    // level 定下来后，生成 Level 长度的 Index 链表索引
    HeadIndex<K, V> h = head;
    Index<K, V> idx = null;
    int max;
    if (level <= (max = h.level)) { // 没有超过最高高度
      for (int i = 1; i <= level; i++) {
        idx = new Index<>(newNode, idx, null);
      }
    } else {// 超过 最高高度
      // 用 index 数组和链表 将 index 索引节点存储起来
      // try to grow one level;
      level = max + 1;
      @SuppressWarnings("unchecked")
      Index<K, V>[] idxs = (Index<K, V>[]) new Index<?, ?>[level + 1];
      for (int i = 1; i <= level; i++) {
        idxs[i] = idx = new Index<>(newNode, idx, null);
      }
      // 接下来 增长头部，因为 存在多线程都在争抢的增长头部，所以开启自旋模式
      for (; ; ) {
        // 多线程竞争判断
        h = head;
        int oldLevel = h.level;
        if (oldLevel > level) {// 竞争失败
          break;// 结束自旋
        }
        HeadIndex<K, V> newH = h;
        Node<K, V> headNode = newH.node;
        // oldLevel 之前的每一层都建立 头结点的索引项
        for (int i = oldLevel + 1; i <= level; i++) {
          // 将 newHead 构造出来，并且和 index[i] 链起来
          newH = new HeadIndex<>(headNode, newH, idxs[i], i);
        }
        if (casHead(h, newH)) {
          h = newH;
          // 从 oldLevel 层开始向下处理 index 的索引节点
          idx = idxs[level = oldLevel];
          break;// 结束自旋
        }
      }
      // 3、也是最难的一步。oldLevel 和 maxLevel 之间的头索引节点已经生成完毕，接下来就是将 idx 索引链表插入跳表中
      split:
      for (int insertionLevel = level; ; ) {
        int j = h.level;
        // 使用 p 指针指向 idx 的前面一个节点，r 指针指向 p 后面一个接地那
        // 最终将 t 指针所指向的 index 索引节点插入到 p 和 r 之间
        for (Index<K, V> p = h, r = p.right, t = idx; ; ) {
          // base case 判断
          if (p == null || t == null) {
            break split;
          }
          if (r != null) {
            Node<K, V> n = r.node;
            if (n.value == null) {
              if (!p.unlink(r)) { // boolean 类型的 cas 操作都要判断结果
                break;
              }
              r = p.right;
              continue;
            }
            if (cmpr(cmp, key, n.key) > 0) {
              p = r;
              r = r.right;
              continue;
            }
          }
          if (insertionLevel == j) { // 头结点指针和 index 节点指针处于同一层
            // 最核心的代码，就是插入跳表中
            if (!p.link(r, t)) { // 如果不能 cas 插入跳表中，则重试
              break;
            }
            // 判断 t 节点的有效性
            if (t.node.value == null) {// 当前 索引指向的 node 被标记删除，
              findNode(key); //删除 node 节点以及 node 节点上的索引。node 节点上的索引的删除时通过 findPredecessor,
              // 而 node 节点的删除是通过 findNode 方法体进行的
              break split; // 跳出外层整个循环，表示本次增加 newNode 节点失败
            }
            if (--insertionLevel == 0) { // index索引节点全部插入跳跃表完毕
              break split;
            }
          }
          if (--j >= insertionLevel && j < level) {
            t = t.down;// 索引节点下移一层
          }
          // p 指针每次都下降一层
          p = p.down;
          r = p.right;
        }
      }
    }
    return null;
    // 至此跳跃表增加节点的代码写完，还是很烧脑的。
  }

  public V remove(Object key) {
    return doRemove(key, null);
  }

  /**
   * 跳表的删除，删除操作是增加的操作的逆操作，不过由于删除操作被 findNode 和 findPredecessor 方法分别实现了删除节点和索引
   * 删除本身的逻辑就是 主要就是 4 点：
   * 1、仍然是一致性检查那一套。
   * 2、根据 value 判断当前查找到的节点 value 是否相同，
   * 3、就是主动的将 当前节点的 value 值为 null，然后执行逻辑删除动作 (追加 marker 节点) 和 实际删除动作 casNext
   * 4、节点清理之后，清理索引，最后尝试降低跳表的高度
   *
   * @param key
   * @param value
   * @return
   */
  private V doRemove(Object key, Object value) {
    if (key == null) {
      throw new NullPointerException();
    }
    Comparator<? super K> cmp = comparator;
    outer:
    for (; ; ) {
      for (Node<K, V> p = findPredecessor(key, cmp), n = p.next; ; ) {
        if (n == null) { // n 就是要被移除的节点，如果 没有找到，则结束循环
          break outer;
        }
        Node<K, V> nx = n.next;
        Object v;
        int c;
        if (n != p.next) { // 出现不一致性读
          break;
        }
        if ((v = n.value) == null) {// n 被别的前程标记删除
          n.helpDelete(p, nx);
          break;
        }
        if (p.value == null || v == n) { // p 被标记删除或者 p 后面的节点是个 marker 节点
          break; // 重新查找 p
        }

        // 比较 comparator
        c = cmpr(cmp, key, n.key);
        if (c < 0) { // key < n.key ,也就是 n.key > k ，找到一个大于 key 但是不等于 key 的元素， 说明 key 不存在跳表中
          break outer;
        } else if (c > 0) { // key > n.key 说明 n 可以继续向后查找
          p = n;
          n = nx;
          continue;
        }
        // 此处 n.key == key
        // 对比 value
        if (value != null && !value.equals(v)) {
          break outer; // 根据 value 没有找到相应的元素
        }
        // 开始指向删除动作
        if (!n.casValue(v, null)) {
          // 删除失败，跳出内层循环重试
          break;
        }
        // 尝试将 n 的 next 指针从 nx 改为 marker 节点，并尝试将 n 节点和 marker 一起删除
        if (n.casNext(nx, new Node<K, V>(nx)) && p.casNext(n, nx)) {
          // 如果删除成功，则尝试删除对应的 Index 索引节点
          findPredecessor(key, cmp);
          if (head.right == null) { // 如果 topmost 节点的 right 指针为null，则尝试降低高度
            tryReduceLevel();
          }
        } else { // 尝试删除 n 节点失败
          findNode(key); // 通过 findNode 再次尝试 删除 n 节点及其 索引接地那
        }
        @SuppressWarnings("unchecked")
        V val = (V) v;
        return val;
      }
    }
    return null;
  }

  /**
   * 尝试降低跳表的高度
   * 这个方法的逻辑是比较保护的，就是要保证 top 3 level 的 right 指针都为 null，也就是 top 3 的链表 都是空的，
   * 这种情况下才会尝试降低 1 个 level。
   * 因为 跳表的性能完全是靠 level 来实现的，level 涨起来容易，降下来就比较困难了
   */
  private void tryReduceLevel() {
    HeadIndex<K, V> h = head;
    HeadIndex<K, V> d, e;
    if (h.level > 3
        && (d = (HeadIndex<K, V>) h.down) != null
        && (e = (HeadIndex<K, V>) d.down) != null
        && h.right == null
        && d.right == null
        && e.right == null // top 3 层的链表都是空的
        && casHead(h, d) // 使用 cas 将 head 节点下降一层
        && h.right != null) { // cas 之后，如果 h.right 此时别别的线程改为 != null, 则重现把 head 从 d 改为 h.
      casHead(d, h);
    }
  }

  private static final sun.misc.Unsafe UNSAFE;
  private static final long HEAD_OFFSET;

  static {
    UNSAFE = getUnsafe();
    try {
      Class<?> clazz = MyConcurrentSkipListMap.class;
      assert UNSAFE != null;
      HEAD_OFFSET = UNSAFE.objectFieldOffset(clazz.getDeclaredField("head"));
    } catch (Exception e) {
      throw new Error(e);
    }
  }

  private static sun.misc.Unsafe getUnsafe() {
    try {
      Class<?> clazz = sun.misc.Unsafe.class;
      Field unsafeField = clazz.getDeclaredField("theUnsafe");
      unsafeField.setAccessible(true);
      return (sun.misc.Unsafe) unsafeField.get(null);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public static void main(String[] args) {
    MyConcurrentSkipListMap<Integer, Integer> skipList = new MyConcurrentSkipListMap<>();
    for (int i = 1; i <= 50; i++) {
      skipList.put(i, i * 2);
    }
    System.out.println(skipList.get(50));
    skipList.remove(50);
    System.out.println(skipList.get(50));
  }
}
