package com.swj.ics.dataStructure.graph.traceback;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/03/02 11:33
 * N 皇后的问题
 * 八皇后问题：Eight Queue，是由国际象棋手马克斯·贝瑟尔与1848年提出的问题，是回溯算法的典型案例。
 * 问题表述为：在 8x8 的国际象棋上摆放 8 个皇后，使其不能互相攻击，即任意两个皇后都不能处于同一行，同一列或同一个斜线上，
 * 问有多少种摆法。高斯认为为 76 种。后来有人用图论的方法解除 92 种结果。计算机发明后，有多重编程语言可以编程解决此问题。
 * 回溯算法实现思路，请参考 traceBack.md
 */
public class QueueN {
  private final int queueN;
  private int[] queue;
  private int queuePosCount;

  public QueueN(int n) {
    this.queueN = n;
    init1(n);
  }

  private void init1(int n) {
    if (n <= 2) {
      throw new IllegalArgumentException("n is illegal. n=" + n);
    }
    queue = new int[queueN];
  }

  public void traceBack(int curr) {
    if (curr >= queueN) {
      printQueue();
      return;
    }
    // 否则尝试回溯
    for (int i = 0; i < queueN; i++) {
      // 当前行的值改为 i，表示第 i 列
      queue[curr] = i;
      // 如果检查发现当前位置的列上和对接线上都没有冲突的皇后，则尝试放下一行皇后
      if (check(curr)) {
        traceBack(curr + 1);
      }
      // 如果检查不通过，说明当前位置是由冲突的，则将 queue[curr] 挪到下一列。
    }
  }

  private boolean check(int currRowIdx) {
    for (int i = 0; i < currRowIdx; i++) {
      if (
        // 如果当前皇后所在的列有其他皇后，则会冲突
          queue[i] == queue[currRowIdx] ||
              // 如果处在当前皇后的对价先位置也有其他皇后，则也不满足。
              // 当前位置的对角线的运算，根基等腰直角三角形45度，得出行的长度和列的长度相等的点，都是其对角线位置
              Math.abs(currRowIdx - i) == Math.abs(queue[currRowIdx] - queue[i])) {
        return false;
      }
    }
    return true;
  }

  private void printQueue() {
    queuePosCount++;
    for (int i = 0; i < queueN; i++) {
      // 打印行列
      System.out.print(String.format("(%d,%d)", i, queue[i]));
    }
    System.out.println();
  }

  public int getQueuePosCount() {
    return queuePosCount;
  }

  public static void main(String[] args) {
    int queueNum = 4;
    QueueN queueN = new QueueN(queueNum);
    queueN.traceBack(0);
    System.out.println("all valid queue pos number is " + queueN.getQueuePosCount());

    queueN = new QueueN(queueNum,2);
    // 这里的 queueN 是从 1 开始的
    queueN.backTrack(1);

  }

  /**
   * 解法 2 ，参考百度百科
   */
  private int[] column; // 占位使用，该列是否有皇后，1表示有
  private int[] rup; // 皇后位的右上左下对角线，右上左下对角线有个特点，所有的对角线位置的坐标 i+(8-j) 都是相等的，比如说 4 皇后中的，
  // 比如 (2,1),(3,2),(4,3)，共同点是 i+(8-j) 是相等的，比如 2+7==3+6==4+5,因此这里使用 rup 数组中的一个点 i+(4-j)的值=1就 代表这个
  // 右上左下对角线整条线被占用，跟 column 是异曲同工之妙
  private int[] lup; // 皇后为的左上右下对角线，左上右下对角线有个特点：多有的对角线的坐标 i+j 都是相等的，比如说 4 皇后中的 (1,3),(2,2),(3,1)
  // 1+3==2+2==3+1 ,所以也可以用一个点来表示一条线是否被占用

  public QueueN(int n, int type) {
    this.queueN = n;
    if (type == 2) {
      init2(n);
    } else {
      init1(n);
    }
  }

  private void init2(int n) {
    // 该解法的索引要求必须从 1 做起
    column = new int[n + 1];
    for(int i=1;i<=n;i++) {
      column[i] = 0;
    }
    rup = new int[n * 2 + 1];
    lup = new int[n * 2 + 1];

    queue = new int[n + 1];
  }

  private void backTrack(int curr) {
    if (curr > queueN) {
      printQueueDetail();
    } else {
      for (int j = 1; j <= queueN; j++) {
        if (column[j] == 0 && lup[curr + j] == 0 && rup[curr + queueN - j] == 0) { // 当前列没有被占用，两个对角线也没有被占用
          // 标志位打上，表示相关位置被占用
          column[j] = lup[curr + j] = rup[curr + queueN - j] = 1;
          // 当前 curr 行的 j 列放置皇后
          queue[curr] = j;
          //回溯+递归
          backTrack(curr + 1);
          //解除标志
          column[j] = lup[curr + j] = rup[curr + queueN - j] = 0;
        }
      }
    }
  }

  private static final String QUEUE_SIGN = "Q";
  private static final String DEFAULT_SIGN = "*";

  private void printQueueDetail() {
    for (int i = 1; i <= queueN; i++) {
      for (int j = 1; j <= queueN; j++) {
        if (queue[i] == j) {
          System.out.print(QUEUE_SIGN + " ");
        } else {
          System.out.print(DEFAULT_SIGN+" ");
        }
      }
      System.out.println();
    }

    System.out.println();
  }

}
