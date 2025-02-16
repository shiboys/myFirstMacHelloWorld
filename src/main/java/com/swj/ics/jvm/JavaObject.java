package com.swj.ics.jvm;

import com.swj.ics.jvm.classload.ClassLoad;
import org.openjdk.jol.info.ClassLayout;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Vector;

/**
 * Created by swj on 2020/3/8.
 */
public class JavaObject {
    /**
     * 那么什么是 Java 对象头呢？Hotspot 虚拟机的对象头主要包括两部分数据：Mark Word（标记字段）、Klass Pointer（类型指针）。其中：

     Klass Point 是是对象指向它的类元数据的指针，虚拟机通过这个指针来确定这个对象是哪个类的实例。

     Mark Word 用于存储对象自身的运行时数据，它是实现轻量级锁和偏向锁的关键，所以下面将重点阐述 Mark Word 。
     Mark Word 用于存储对象自身的运行时数据，如哈希码（HashCode）、GC 分代年龄、锁状态标志、线程持有的锁、偏向线程 ID、偏向时间戳等等。Java 对象头一般占有两个机器码（在 32 位虚拟机中，1 个机器码等于 4 字节，也就是 32 bits）。但是如果对象是数组类型，则需要三个机器码，因为 JVM 虚拟机可以通过 Java 对象的元数据信息确定 Java 对象的大小，无法从数组的元数据来确认数组的大小，所以用一块来记录数组长度。

     下图是 Java 对象头的存储结构（32位虚拟机）：
        25bit        4bit           1bit        2bit
     对象的hashCode，对象的分带年龄    是否偏向锁   锁标志位
     */

    /**
     * 适应自旋锁
     JDK 1.6 引入了更加聪明的自旋锁，即自适应自旋锁。

     所谓自适应就意味着自旋的次数不再是固定的，它是由前一次在同一个锁上的自旋时间及锁的拥有者的状态来决定。它怎么做呢？

     线程如果自旋成功了，那么下次自旋的次数会更加多，因为虚拟机认为既然上次成功了，那么此次自旋也很有可能会再次成功，那么它就会允许自旋等待持续的次数更多。
     反之，如果对于某个锁，很少有自旋能够成功的，那么在以后要获得这个锁的时候自旋的次数会减少甚至省略掉自旋过程，以免浪费处理器资源。
     有了自适应自旋锁，随着程序运行和性能监控信息的不断完善，虚拟机对程序锁的状况预测会越来越准确，虚拟机会变得越来越聪明。
     */

    /**
     *  锁消除
     由来

     为了保证数据的完整性，我们在进行操作时需要对这部分操作进行同步控制。但是，在有些情况下，JVM检测到不可能存在共享数据竞争，
     这是JVM会对这些同步锁进行锁消除。如果不存在竞争，为什么还需要加锁呢？所以锁消除可以节省毫无意义的请求锁的时间。

     定义

     锁消除的依据是逃逸分析的数据支持。变量是否逃逸，对于虚拟机来说需要使用数据流分析来确定，但是对于我们程序员来说这还不清楚么？
     我们会在明明知道不存在数据竞争的代码块前加上同步吗？但是有时候程序并不是我们所想的那样？我们虽然没有显示使用锁，
     但是我们在使用一些 JDK 的内置 API 时，如 StringBuffer、Vector、HashTable 等，这个时候会存在隐性的加锁操作。
     比如 StringBuffer 的 #append(..)方法，Vector 的 add(...) 方法：
     */
   /* public void vectorTest(){
        Vector<String> vector = new Vector<String>();
        for (int i = 0 ; i < 10 ; i++){
            vector.add(i + "");
        }
        System.out.println(vector);
    }*/
    /**
     * 在运行这段代码时，JVM 可以明显检测到变量 vector 没有逃逸出方法 #vectorTest() 之外，
     * 所以 JVM 可以大胆地将 vector 内部的加锁操作消除。
     */

    /**
     * 锁粗化概念比较好理解，就是将多个连续的加锁、解锁操作连接在一起，扩展成一个范围更大的锁。
     * 如上面实例：vector 每次 add 的时候都需要加锁操作，JVM 检测到对同一个对象（vector）连续加锁、解锁操作，
     * 会合并一个更大范围的加锁、解锁操作，即加锁解锁操作会移到 for 循环之外。
     */

    /**
     * 锁的升级
     锁主要存在四种状态，依次是：无锁状态、偏向锁状态、轻量级锁状态、重量级锁状态。
     它们会随着竞争的激烈而逐渐升级。注意，锁可以升级不可降级，这种策略是为了提高获得锁和释放锁的效率。
     
     
     重量级锁通过对象内部的监视器（Monitor）实现。

     其中，Monitor 的本质是，依赖于底层操作系统的 Mutex Lock 实现。
     操作系统实现线程之间的切换，需要从用户态到内核态的切换，切换成本非常高。

     对于轻量级锁，其性能提升的依据是：“对于绝大部分的锁，在整个生命周期内都是不会存在竞争的”。如果打破这个依据则除了互斥的开销外，
     还有额外的 CAS 操作，因此在有多线程竞争的情况下，轻量级锁比重量级锁更慢。

     引入偏向锁主要目的是：为了在无多线程竞争的情况下，尽量减少不必要的轻量级锁执行路径。

     上面提到了轻量级锁的加锁解锁操作，是需要依赖多次 CAS 原子指令的。那么偏向锁是如何来减少不必要的 CAS 操作呢？我们可以查看 Mark Word 的数据结构就明白了。

     老艿艿：在上文，我们可以看到偏向锁时，Mark Word 的数据结构为：线程 ID、Epoch( 偏向锁的时间戳 )、对象分带年龄、是否是偏向锁( 1 )、锁标识位( 01 )。

     只需要检查是否为偏向锁、锁标识为以及 ThreadID 即可，处理流程如下：
     获取偏向锁

     检测 Mark Word是 否为可偏向状态，即是否为偏向锁的标识位为 1 ，锁标识位为 01 。
     若为可偏向状态，则测试线程 ID 是否为当前线程 ID ？如果是，则执行步骤（5）；否则，执行步骤（3）。
     如果线程 ID 不为当前线程 ID ，则通过 CAS 操作竞争锁。竞争成功，则将 Mark Word 的线程 ID 替换为当前线程 ID ，则执行步骤（5）；否则，执行线程（4）。
     通过 CAS 竞争锁失败，证明当前存在多线程竞争情况，当到达全局安全点，获得偏向锁的线程被挂起，偏向锁升级为轻量级锁，然后被阻塞在安全点的线程继续往下执行同步代码块。
     执行同步代码块
     */

    private int a = 8;
    private ArrayList list = new ArrayList();

    public static void main(String[] args) {
//        JavaObject javaObject = new JavaObject();
//        System.out.println(ClassLayout.parseInstance(javaObject).toPrintable());

        B b = new B();
        System.out.println(ClassLayout.parseInstance(b).toPrintable());
    }

    private static class B {
        private int i;
        private Locale l = Locale.US;
    }
}
