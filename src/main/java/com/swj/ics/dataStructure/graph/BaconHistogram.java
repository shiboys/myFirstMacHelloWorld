package com.swj.ics.dataStructure.graph;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/02/22 14:54
 *        贝肯直方图——计算一个演员的贝肯数。
 *        一个演员的贝肯数（英语：bacon numbers）是他们与演员凯文·贝肯有多少度的分离，由凯文·贝肯定义。
 *        贝肯数越高，演员离凯文·贝肯越远。
 *        贝肯数的理论依据是，六度分割理论（Six Degrees of Separation) 认为世界上任何两个互相不认识的人，
 *        平均只需要经过 6 步就可以建立联系。
 *        输入数据的文件参见 movies.txt
 *        最终的图形展现如图 movies.png
 *        如果根据宽度优先遍历的原则，始发节点如果是演员节点，比如说是 凯文·贝肯，那么下一层节点就是电影，下下一层节点又开始是演员
 *        也就是说递归的 偶数层是演员，奇数层是电影
 */
public class BaconHistogram {

    private static final int MAX_DEGREE = 100;
    private static final String START_SOURCE_NAME = "Bacon, Kevin";
    private SymbolGraph symbolGraph;

    private int[] histogramArr;

    public BaconHistogram(String fileName, String delimeter) {
        symbolGraph = new SymbolGraph(fileName, delimeter);
        histogramArr = new int[MAX_DEGREE + 1];
    }

    public void printBaconHistogram() {
        if (!symbolGraph.contains(START_SOURCE_NAME)) {
            throw new IllegalArgumentException("the source name of " + START_SOURCE_NAME + " is not in the database.");
        }

        int startNodeIndex = symbolGraph.indexOf(START_SOURCE_NAME);

        Graph graph = symbolGraph.getGraph();
        // 使用广度优先遍历的方式遍历图
        BreadthFirstPath bfs = new BreadthFirstPath(graph, startNodeIndex);
        for (int i = 0; i < graph.nodeSize(); i++) {
            int distance = bfs.distanceTo(i);
            int maxBacon = Math.min(distance, MAX_DEGREE);
            histogramArr[maxBacon]++;

            // 如果 度大于 6 就是比较特别的存在了，因此这里对此进行了特别打印
            if (maxBacon / 2 >= 7 && maxBacon < MAX_DEGREE) {
                System.out.println(
                        String.format("found special bacon node : %2d , %s", maxBacon / 2, symbolGraph.nameOf(i)));
            }
        }

        for (int i = 0; i < MAX_DEGREE; i += 2) {
            if (histogramArr[i] == 0) {
                break;
            }
            System.out.println(String.format("%3d %8d", i / 2, histogramArr[i]));
        }
        System.out.println(String.format("Inf histogram: %8d", histogramArr[MAX_DEGREE]));

    }

    public static void main(String[] args) {
        String fileName = "movies.txt";
        String filePath = GraphUtil.getGraphFilePath(fileName);
        String delimiter = "/";

        BaconHistogram baconHistogram = new BaconHistogram(filePath, delimiter);

        baconHistogram.printBaconHistogram();
    }

}
