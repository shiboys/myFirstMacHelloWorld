package com.swj.ics.algorithms.sort.heapSort;

/**
 * author shiweijie
 * date 2019/6/10 下午8:12
 */
public class TopN {

    /**
     * 对于每个节点的值都大于等于子树中每个节点值的堆，我们叫作“大顶堆”。对于每个节点的值都小于等于子树中每个节点值的堆，我们叫作“小顶堆”。
     * TopN的主要操作是建堆&堆化。
     * 其中：建堆有2中方式，自底向上，从0开始，从第一个元素开始，采用insert的方式，插入队列最后一位，然后进行swap。
     * 顺着节点所在的路径，向上或者向下，对比，然后交换
     * 建堆的第二种方式，从就是从上倒下。
     * 第二种实现思路，第二种实现思路，跟第一种截然相反
     * 第一种建堆思路的处理过程是从前往后处理数组数据，并且每个数据插入堆中时，都是从下往上堆化。
     * 而第二种实现思路，是从后往前处理数组，从第一个非叶子节点开始向前堆化，并且每个数据都是从上往下堆化。
     */
    private int parent(int n) {
        return (n - 1) / 2;
    }

    private int leftChild(int n) {
        return 2 * n + 1;
    }

    private int rightChild(int n) {
        return 2 * n + 2;
    }

    /**
     * 建堆方法1.构建小顶堆，小顶堆对于实现topN的方式比较友好。
     *
     * @param n
     * @param data
     */
    private void buildHeap(int n, int[] data) {
        n = Math.min(n, data.length);
        for (int i = 1; i < n; i++) {
            int t = i;
            while (t > 0 && data[parent(t)] > data[t]) {
                int temp = data[parent(t)];
                data[parent(t)] = data[t];
                data[t] = temp;
                t = parent(t);
            }
        }
    }

    //public void adjust(int n)

    /**
     * 堆化
     *
     * @param n
     * @param data
     * @param i    从第i个元素开始。将第i个元素跟向下进行堆化。
     */
    private void heapify(int n, int[] data, int i) {
        if (data[i] <= data[0]) { // 如果小于堆顶元素，则舍弃
            return;
        }
        //替换堆顶元素
        int temp = data[0];
        data[0] = data[i];
        data[i] = temp;

        //重建堆
        int t = 0;
        while ((leftChild(t) < n && data[t] > data[leftChild(t)]) ||
                (rightChild(t) < n && data[t] > data[rightChild(t)])) {
            if (rightChild(t) < n && data[rightChild(t)] < data[leftChild(t)]) { //右子树更小。右子树<左子树，则替换右子树
                temp = data[rightChild(t)];
                data[rightChild(t)] = data[t];
                data[t] = temp;
                t = rightChild(t);
            } else { //替换左子树
                temp = data[leftChild(t)];
                data[leftChild(t)] = data[t];
                data[t] = temp;
                t = leftChild(t);
            }
        }
    }

    public void findTopN(int n, int[] data) {
        //先构建N个数的小顶堆
        buildHeap(n, data);
        //在堆n+1到data.length的数据，以及执行堆化操作
        for (int i = n; i < data.length; i++) {
            heapify(n, data, i);
        }
    }

    public void printData(int[] data) {
        for (int i = 0; i < data.length; i++) {
            System.out.print(data[i] + " ");
        }
        System.out.println();
    }
}
