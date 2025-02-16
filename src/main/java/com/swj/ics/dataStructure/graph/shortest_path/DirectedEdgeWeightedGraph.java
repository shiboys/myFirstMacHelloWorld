package com.swj.ics.dataStructure.graph.shortest_path;

import com.swj.ics.dataStructure.graph.Bag;
import com.swj.ics.dataStructure.graph.GraphUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Stack;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/02/25 14:54
 */
public class DirectedEdgeWeightedGraph {

  private static final String LINE_SEPARATOR = System.getProperty("line.separator");

  private int nodeSize;
  private int edgeSize;
  private Bag<DirectedEdge>[] neighbors;
  private int[] inDegreeArr; // 节点的入度。

  public DirectedEdgeWeightedGraph(int nodeSize) {
    init(nodeSize);
  }

  private void init(int nodeSize) {
    this.nodeSize = nodeSize;
    // 每个节点都应该有一个入度数
    inDegreeArr = new int[nodeSize];
    neighbors = (Bag<DirectedEdge>[]) new Bag[nodeSize];
    for (int i = 0; i < nodeSize; i++) {
      neighbors[i] = new Bag<>();
    }
  }

  public DirectedEdgeWeightedGraph(String filePath, String delimiter) {
    File file = new File(filePath);
    if (!file.exists()) {
      throw new IllegalArgumentException(String.format("filePath is not exists. [filePath='%s']", filePath));
    }
    String line;
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      int nodeSize = Integer.parseInt(br.readLine());
      this.edgeSize = Integer.parseInt(br.readLine());
      init(nodeSize);
      int k = 0;
      String[] splitArr;
      while ((line = br.readLine()) != null && (k++) < edgeSize) {
        splitArr = line.replace("  ", delimiter).split(delimiter);
        if (splitArr.length < 3) {
          continue;
        }
        int head = Integer.parseInt(splitArr[0]);
        int tail = Integer.parseInt(splitArr[1]);
        double weight = Double.parseDouble(splitArr[2]);
        addEdge(new DirectedEdge(head, tail, weight));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public DirectedEdgeWeightedGraph(DirectedEdgeWeightedGraph directedGraph) {
    this.edgeSize = directedGraph.getEdgeSize();
    this.nodeSize = directedGraph.getNodeSize();
    Stack<DirectedEdge> edgeStack = new Stack<>();
    for (int i = 0; i < nodeSize; i++) {
      for (DirectedEdge edge : directedGraph.linkedNeighbors(i)) {
        edgeStack.push(edge);
      }
      while (!edgeStack.isEmpty()) {
        neighbors[i].add(edgeStack.pop());
      }
    }
  }

  private void addEdge(DirectedEdge edge) {
    int head = edge.getHeadNodeIndex();
    int tail = edge.getTailNodeIndex();
    neighbors[head].add(edge);
    inDegreeArr[tail]++;
  }

  public int inDegreeOf(int nodeIndex) {
    if (nodeIndex < 0 || nodeIndex >= inDegreeArr.length) {
      return -1;
    }
    return inDegreeArr[nodeIndex];
  }

  public int outDegreeOf(int nodeIndex) {
    if (nodeIndex < 0 || nodeIndex >= neighbors.length) {
      return -1;
    }
    return neighbors[nodeIndex].size();
  }

  public Iterable<DirectedEdge> linkedNeighbors(int nodeIndex) {
    return neighbors[nodeIndex];
  }

  /**
   * @return 返回带权重的有向图所有边
   */
  public Iterable<DirectedEdge> edges() {
    Bag<DirectedEdge> list = new Bag<>();
    for (int i = 0; i < nodeSize; i++) {
      for (DirectedEdge edge : neighbors[i]) {
        list.add(edge);
      }
    }
    return list;
  }

  public int getNodeSize() {
    return nodeSize;
  }

  public int getEdgeSize() {
    return edgeSize;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("nodeSize:" + nodeSize +
        ", edgeSize:" + edgeSize + LINE_SEPARATOR);
    for (DirectedEdge edge : edges()) {
      sb.append(edge).append(LINE_SEPARATOR);
    }
    return sb.toString();
  }



  public static void main(String[] args) {
    String fileName = "tinyEWD.txt";
    String filePath = GraphUtil.getGraphFilePath(DirectedEdgeWeightedGraph.class, fileName);
    DirectedEdgeWeightedGraph directedGraph = new DirectedEdgeWeightedGraph(filePath, " ");
    System.out.println(directedGraph);
  }
}
