package com.swj.ics.dataStructure.graph;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/02/21 20:34
 *        判断一个图是否是二分图
 */
public class TwoColor {
    /**
     * 二分图（bipartite graph），根据维基百科, 是一类特殊的图，又称为双分图。二分图的定点可以分成互斥的独立集 U 和 V 的图
     * 使得所有的边都是连接一个 U 中的点和一个 V 中的点。顶点集 U、V 被称为是图的两部分。
     * 比如: U 中所有的顶点为蓝色，V 中所有的顶点为绿色。每条边的两个断电的颜色不同，符合着色问题的要求。相反的，非二分图无法被二着色。
     * 这里就是利用一条边的颜色应该互斥来判断是否是二分图
     * 示例图片请查看 480px-Simple-bipartite-graph.svg.npg
     */

    private boolean[] marked;
    private boolean[] colorArr;
    private boolean isTwoColor = true;

    /**
     * @param graph
     * @param statNodeIdx
     */
    public TwoColor(Graph graph, int statNodeIdx) {
        marked = new boolean[graph.edgeSize()];
        colorArr = new boolean[graph.edgeSize()];
        for (int i = 0; i < graph.edgeSize(); i++) {
            if (!marked[i]) {
                dfs(graph, i);
            }
        }
    }

    private void dfs(Graph graph, int nodeIndex) {
        marked[nodeIndex] = true;
        for (int subNodeIdx : graph.linkedNeighbors(nodeIndex)) {
            if (!marked[subNodeIdx]) {
                colorArr[subNodeIdx] = !colorArr[nodeIndex];
                dfs(graph, subNodeIdx);
            } else if (colorArr[subNodeIdx] == colorArr[nodeIndex]) {
                isTwoColor=false;
            }
        }
    }

    public boolean isBipartite(){
        return isTwoColor;
    }
}
