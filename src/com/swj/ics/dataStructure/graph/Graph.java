package com.swj.ics.dataStructure.graph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Stack;

/**
 * 无向图。
 * 参考算法 4 中的无向图
 */
public class Graph implements GraphApi {
  private static final String LINE_SEPARATOR = System.getProperty("line.separator");
  // 图的边数
  private int edgeSize;
  // 节点 Id 的多少，默认节点 Id 从 0 开始
  private int nodeSize;

  /**
   * 图的边使用数组+链表的形式保存，数组的索引就是节点的 Id，数组的 bag 对象就是以该节点为起始点的其他节点集合，
   * 可以叫 neighbors 或者 adjacency（这个是算法 4 推荐的叫法）
   */
  private Bag<Integer>[] edges;

  /**
   * 从 文件中读取一个图所需要的数据。文件的内容如 tinyG.txt 所示
   * 13
   * 13
   * 0 5
   * 4 3
   * 0 1
   * 9 12
   * 6 4
   * 5 4
   * 0 2
   * 11 12
   * 9 10
   * 0 6
   * 7 8
   * 9 11
   * 5 3
   * 第一行表示节点数量，第二行表示边的数量。从第 3 行到最后一行，都是表示边的两个节点。
   * 最终形成一幅图
   *
   * @param filepath 文件内
   */
  public Graph(String filepath) {
    if (filepath == null || filepath.isEmpty()) {
      throw new IllegalArgumentException("filePath is emtpy");
    }
    File file = new File(filepath);
    if (!file.exists()) {
      throw new IllegalArgumentException(String.format("file is not exists.[filepath='%s']", filepath));
    }
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      nodeSize = Integer.parseInt(br.readLine());
      edgeSize = Integer.parseInt(br.readLine());
      edges = (Bag<Integer>[]) new Bag[nodeSize];
      String line = null;
      while ((line = br.readLine()) != null && !line.isEmpty()) {
        String[] splitArr = line.split(" ");
        int fromNodeIdx = Integer.parseInt(splitArr[0]);
        int toNodeIndex = Integer.parseInt(splitArr[1]);
        addEdge(fromNodeIdx, toNodeIndex);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * 一个 graph 的深度 clone
   *
   * @param graph 参数
   */
  public Graph(Graph graph) {
    this.edgeSize = graph.edgeSize;
    this.nodeSize = graph.nodeSize;
    this.edges = (Bag<Integer>[]) new Bag[nodeSize];
    Stack<Integer> stack = new Stack<>();
    for (int i = 0; i < nodeSize; i++) {
      if (graph.edges[i] != null && graph.edges[i].size() > 0) {
        stack.clear();
        // 将原始 图的 节点的 邻居节点拷贝到当前图的当前节点的邻居节点。
        // 因为 bag 的 add 方法时头插法，但是 bag 的 iterator 是从头到尾，因此这里需要将遍历到的第一个节点
        // 先暂存到的栈底，最后一个元素则先插入当前节点的邻居链表中，这样才能保证复制后的图的各个节点的邻居节点顺序一直
        for (int nodeIndex : graph.edges[i]) {
          stack.push(nodeIndex);
        }
        if (this.edges[i] == null) {
          this.edges[i] = new Bag<>();
        }
        for (int nodeIndex : stack) {
          this.edges[i].add(nodeIndex);
        }
      }
    }
  }


  @Override
  public void addEdge(int fromNodeIndex, int toNodeIndex) {
    if (edges[fromNodeIndex] == null) {
      edges[fromNodeIndex] = new Bag<>();
    }
    edges[fromNodeIndex].add(toNodeIndex); // 将 toNodeIndex 添加到 fromNodeIndex 的链表中
    // 因为无向图的边是双向的，因此这里需要在 toNodeIndex 为起始点的边里面也要维护这条边
    if (edges[toNodeIndex] == null) {
      edges[toNodeIndex] = new Bag<>();
    }
    edges[toNodeIndex].add(fromNodeIndex);
    edgeSize++;
  }

  @Override
  public int nodeSize() {
    return nodeSize;
  }

  @Override
  public int edgeSize() {
    return edgeSize;
  }

  /**
   * 获取某个节点的所有相连节点。算法4 称为  adjacency/adj: 毗邻；四周；邻接物
   *
   * @param nodeIndex
   * @return
   */
  @Override
  public Iterable<Integer> linkedNeighbors(int nodeIndex) {
    if (edges[nodeIndex] == null) {
      edges[nodeIndex] = new Bag<>();
    }
    return edges[nodeIndex];
  }



  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("node size: " + nodeSize + ", edge size : " + edgeSize + LINE_SEPARATOR);
    for (int i = 0; i < nodeSize; i++) {
      sb.append(i + ":");
      if (edges[i] != null && edges[i].size() > 0) {
        for (int nodeIdx : edges[i]) {
          sb.append(nodeIdx + " ");
        }
      }
      sb.append(LINE_SEPARATOR);
    }
    return sb.toString();
  }

  public static void main(String[] args) {
    String filePath = Graph.class.getPackage().getName().replace(".", "/") + "/tinyG.txt";
    URL resource = ClassLoader.getSystemResource("");
    /*System.out.println(System.getProperty("user.dir"));
    assert resource != null;
    System.out.println(resource.getPath());*/
    // System.out.println(resource.getPath()+filePath);
    filePath = resource.getPath() + filePath;
    Graph g = new Graph(filePath);
    System.out.println(g);
  }


}
