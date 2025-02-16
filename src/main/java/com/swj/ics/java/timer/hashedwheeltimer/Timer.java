package com.swj.ics.java.timer.hashedwheeltimer;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2024/05/17 20:15
 * Netty HashedWheelTimer 的抄作业版本。Netty 之所以要重新设计 Timer 类，是因为 JDK 的 Timer 或者 ScheduleThreadPoolExecutor 都是基于二叉堆的来实现的队列管理
 * 插入和删除的的时间复杂度为 O(logN),如果有后才百万级别的定时任务，这个就会比较影响性能，所以一言不合就重写了，使用基于 Hash 数组+链表的方式维护定时任务的增删
 * 达到 O(1) 的时间复杂度，不过 人家也是是由相关的论文指导的。
 * 论文可以参考 Netty Timer 的源码解释
 * 整个定时器的管理核心接口
 * 缺点也比较多，比如，会存在很多无用的推进，任务的执行也是像 JDK Timer 一样是单线程的，存在任务之间互相影响的问题，某个任务执行时间过长，会影响后面任务执行的时效性。
 */
public interface Timer {
  /**
   * 添加定时任务，同时定时器采用了懒加载的方式，并没有提供显式的诸如 start 之类的接口
   * 通过调用该方法来启动定时器
   * @param timerTask
   * @param delay
   * @param unit
   * @return
   */
  Timeout newTimeout(TimerTask timerTask, long delay, TimeUnit unit);

  /**
   * 停止整个定时器
   */
  Set<Timeout> stop();
}
