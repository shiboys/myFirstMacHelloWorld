package com.swj.ics.dataStructure.graph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/02/24 22:32
 *        动态连接性：
 *        在学习动态连接性之前，我们来针对算法强调一下几点：
 *        1、优秀的算法应为能够解决实际问题而变得更为重要
 *        2、高效算法的代码也可很简单
 *        3、理解某个实现的性能特点(时间空间复杂度)是一项有趣而令人满足的挑战
 *        4、在解决同一个问题的多种算法之间进行选择时，科学方法是一种很重要的工具；
 *        5、迭代式改进能够让算法的效率越来越高
 */
public class UnionFind {
    // 组的级别数组
    private int[] levelArray;
    // 元素所属的组件Id，默认 组件Id 跟 元素Id 一致
    private int[] componentIdOfArray;

    // 组件的元素个数
    private int componnetCount;

    public UnionFind(int cpCount) {
        levelArray = new int[cpCount];
        componnetCount = cpCount;
        componentIdOfArray = new int[cpCount];
        for (int i = 0; i < cpCount; i++) {
            // 组件的 Id 默认初始化为组件 的索引
            componentIdOfArray[i] = i;
        }
    }

    /**
     * 查找 p 所在的组件Id
     * 
     * @param p
     * @return
     */
    private int find(int p) {
        if (p < 0 || p >= componentIdOfArray.length) {
            throw new IllegalArgumentException("p is illegal.[p=" + p + "]");
        }
        // p=5， componentId 此时=3
        // 第一轮 componentIdArray[5] = componentIdArray[3] = 1
        // 第二轮 comopoentIdArray[3] = componentIdArray[1] = 1
        // 结果 p = componentId = 1
        int componentId = componentIdOfArray[p];
        while (componentId != p) {
            componentIdOfArray[p] = componentIdOfArray[componentId];
            p = componentId;
            componentId = componentIdOfArray[p];
        }
        return p;
    }

    private void union(int p, int q) {
        int rootP = find(p);
        int rootQ = find(q);
        // p 和 q 本来就在同一个组件里面
        if (rootP == rootQ) {
            return;
        }
        // 将 level 较小的组件加入 level 较大的组件 圈子里面。
        // 就是将小组件连接大组件
        if (levelArray[rootP] > levelArray[rootQ]) {
            componentIdOfArray[rootQ] = rootP;
        } else if (levelArray[rootP] < levelArray[rootQ]) {
            componentIdOfArray[rootP] = rootQ;
        } else {
            // 加入 rootP
            componentIdOfArray[rootQ] = rootP;
            levelArray[rootP]++;
        }
        // 连接成了一个更大的组件，则减少组件个数
        componnetCount--;
    }

    public int getComponentCount() {
        return componnetCount;
    }

    /**
     * p 和 q 是否在同一个大组件内。
     * 
     * @param p
     * @param q
     * @return
     */
    public boolean isConnected(int p, int q) {
        return find(p) == find(q);
    }

    public static void main(String[] args) {
        String fileName = "tinyUF.txt";
        String filePath = GraphUtil.getGraphFilePath(UnionFind.class, fileName);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)))) {
            int cpCount = Integer.parseInt(br.readLine());
            UnionFind uf = new UnionFind(cpCount);
            String line;
            int i = 0;
            String [] splitArr;
            while((line = br.readLine()) != null && i++ < cpCount) {
                splitArr = line.split(" ");
                int p = Integer.parseInt(splitArr[0]);
                int q = Integer.parseInt(splitArr[1]);
                if(uf.find(p) == uf.find(q)) {
                    continue;
                }
                uf.union(p, q);
                System.out.println(p +"    " + q);
            }
            System.out.println(uf.getComponentCount()+" components");
            /**
             * 打印结果如下：
             * 4    3
             * 3    8
             * 6    5
             * 9    4
             * 2    1
             * 5    0
             * 7    2
             * 6    1
             * 2 components
             *
             * 跟原始文件对比发现，如果是已经在同一个组件中的组件，则不会被打印出来。
             */
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
