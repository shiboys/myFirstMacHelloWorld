package com.swj.ics.jvm;

import org.openjdk.jol.info.ClassLayout;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/03/31 14:27
 * 栈上分配示例
 */
public class StackAllocate {
  public static class User {
    public int id = 0;
    public String name = "";
  }


  private static User u;

  /**
   * 对象 User u 是类的成员变量，该字段有可能被任何线程访问，因此属于逃逸对象。
   */
  public static void allocate() {
    u = new User();
    u.id = 5;
    u.name = "peter";
  }

  /**
   * 下面这个代码中  User 以局部变量的形式存在，并且该对象没有被 alloc() 函数返回，
   * 或者出现任何显示的公开，因此他并未发生逃逸。所以对于这种情况，虚拟机就很有可能将 User 分配到栈上
   * 而不是堆上。
   */
  public static void alloc() {
    User u1 = new User();
    u1.id = 5;
    u1.name = "peter";
  }

  /**
   * 测试对象的栈上分配。开启逃逸分析(只有开启逃逸分析时，虚拟机任务对象不会逃逸，才有可能进行栈上分配)
   * 关闭 TLAB（Thread Local Allocator Buffer） java 对针对每个线程分配的一个线程私有的分配缓冲区，开启后，如果有对象分配到堆上，优先分配到这个
   * -XX:+EliminateAllocations 开启标量替换（默认打开），允许将对象打散分散在栈上。比如 User 对象拥有 id 和 name 两个字段，那么这两个
   * 字段将被视为两个独立的局部变量进行分配。
   * 使用虚拟机参数 -Xmx20m -Xms20m -XX:+DoEscapeAnalysis -XX:+PrintGC -XX:-UseTLAB -XX:+EliminateAllocations
   * 这个测试在循环中进行了 1 亿次的 alloc() 调用进行对象创建，由于 User 对象占用 24 字节 的空间，因此累计空间分配将达到 2.4 G。如果对空间小于这个值，
   * 必然会引起 GC 。
   * User 对象占用 24 字节是怎么计算出来的，使用 openJdk 提供的 jol (Java-Object-Layout) 工具,可以统计 java 对象的布局情况
   * 调用方法  ClassLayout.parseInstance(instance).toPrintable()
   * 对象头占用 12 字节 (markworld 8 字节，kclass 指针(指向类的元数据指针) 4 字节)
   * User.id 4 字节
   * User.name 4 引用类型的，4 字节
   * 再加上 4 字节的偏移量对齐
   * 一共 24 字节，满足 Java 对象的大小都是 8 字节的倍数要求
   */
  static void testStackAlloc() {
    long m = System.currentTimeMillis();
    for (int i = 0; i < 100_000_000; i++) {
      alloc();
    }
    long elapsed = System.currentTimeMillis() - m;
    System.out.println(elapsed);
    /**
     * 打印结果为 13 ，并没有出现 GC 日志，说明在执行过程中，User 对象的分配过程被优化。
     * 如果关闭逃逸分析或者标量替换中的任意一个，再次执行程序，就会看到大量的 GC 日志，说明栈上分配依赖逃逸分析和标量替换的实现
     * 比如 关闭逃逸分析
     * -Xms20m -Xmx20m -XX:-DoEscapeAnalysis -XX:+PrintGC -XX:-UseTLAB -XX:+EliminateAllocations
     * 关闭 变量替换
     * -Xms20m -Xmx20m -XX:+DoEscapeAnalysis -XX:+PrintGC -XX:-UseTLAB -XX:-EliminateAllocations
     * 最终结果如下
     * [GC (Allocation Failure)  7092K->1460K(19968K), 0.0003861 secs]
     * [GC (Allocation Failure)  7092K->1460K(19968K), 0.0004073 secs]
     * [GC (Allocation Failure)  7092K->1460K(19968K), 0.0005781 secs]
     * 1824
     */
  }

  public static void main(String[] args) {
    // testObjectLayout();
    testStackAlloc();
  }

  private static void testObjectLayout() {
    User instance = new User();
    System.out.println(ClassLayout.parseInstance(instance).toPrintable());
  }
}
