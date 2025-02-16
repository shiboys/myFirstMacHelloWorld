package com.swj.ics.dataStructure.graph.dag;

import com.swj.ics.dataStructure.graph.Bag;
import com.swj.ics.dataStructure.graph.GraphUtil;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/02/26 14:57
 * 有向图
 */
public class Digraph {
  private static final String LINE_SEPARATOR = System.getProperty("line.separator");
  private int nodeSize;
  private int edgeSize;
  private Bag<Integer>[] neighbours;
  int[] inDegreeArray;

  public Digraph(int nodeSize) {
    init(nodeSize);
  }

  public Digraph(String filePath, String delimiter) {
    try (BufferedReader br = GraphUtil.read(filePath)) {
      this.nodeSize = Integer.parseInt(br.readLine());
      this.edgeSize = Integer.parseInt(br.readLine());
      init(nodeSize);
      String line;
      int k = 0;
      String[] splitArr;
      while ((line = br.readLine()) != null && !line.isEmpty() && (k++) < edgeSize) {
        splitArr = line.replace("  ", delimiter).trim().split(delimiter);
        int to = Integer.parseInt(splitArr[1].trim());
        int from = Integer.parseInt(splitArr[0].trim());
        addEdge(from, to);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void init(int nodeSize) {
    this.nodeSize = nodeSize;
    this.neighbours = (Bag<Integer>[]) new Bag[nodeSize];
    for (int i = 0; i < nodeSize; i++) {
      neighbours[i] = new Bag<>();
    }
    inDegreeArray = new int[nodeSize];
  }

  private void addEdge(int from, int to) {
    neighbours[from].add(to);
    inDegreeArray[to]++;
  }

  public Iterable<Integer> linkedNeighbours(int nodeIndex) {
    return neighbours[nodeIndex];
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("nodeSize=" + nodeSize +
        ", edgeSize=" + edgeSize).append(LINE_SEPARATOR);
    for (int i = 0; i < nodeSize; i++) {
      sb.append(i + ":");
      for (Integer nodeIndex : neighbours[i]) {
        sb.append("->" + nodeIndex);
      }
      sb.append(LINE_SEPARATOR);
    }
    return sb.toString();
  }

  public static void main(String[] args) {
    Digraph digraph = new Digraph(GraphUtil.getGraphFilePath(Digraph.class, "tinyDG.txt"), " ");
    System.out.println(digraph);
  }

  public int getNodeSize() {
    return nodeSize;
  }
}
