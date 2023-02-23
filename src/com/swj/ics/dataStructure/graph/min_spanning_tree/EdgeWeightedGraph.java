package com.swj.ics.dataStructure.graph.min_spanning_tree;

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
 * @since 2023/02/23 15:12
 * 边带权重的图
 */
public class EdgeWeightedGraph {
  private static final String NEW_LINE = System.getProperty("line.separator");
  private int nodeSize;
  private int edgeSize;
  private Bag<Edge>[] neighbors;

  public EdgeWeightedGraph(int nodeSize) {
    if (nodeSize < 1) {
      throw new IllegalArgumentException(
          String.format("illegal nodesize for edge weighted graph. [nodeSize=%d]", nodeSize));
    }
    this.nodeSize = nodeSize;
    neighbors = (Bag<Edge>[]) new Object[nodeSize];
    for (int i = 0; i < nodeSize; i++) {
      neighbors[i] = new Bag<>();
    }
  }

  public EdgeWeightedGraph(String filePath, String delimiter) {
    File file = new File(filePath);
    if (!file.exists()) {
      throw new IllegalArgumentException(String.format("file path is not exists. [filePath='%s']", filePath));
    }
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      nodeSize = Integer.parseInt(br.readLine());
      edgeSize = Integer.parseInt(br.readLine());
      neighbors = (Bag<Edge>[]) new Bag[nodeSize];
      for (int i = 0; i < nodeSize; i++) {
        neighbors[i] = new Bag<>();
      }
      String line;
      int i = 0;
      int headIndex;
      int tailIndex;
      double weight;
      String[] splitLineArr;
      while ((line = br.readLine()) != null && !line.isEmpty() && (i++) < edgeSize) {
        splitLineArr = line.split(delimiter);
        if (splitLineArr.length < 3) {
          continue;
        }
        headIndex = Integer.parseInt(splitLineArr[0]);
        tailIndex = Integer.parseUnsignedInt(splitLineArr[1]);
        weight = Double.parseDouble(splitLineArr[2]);
        Edge newEdge = new Edge(headIndex, tailIndex, weight);
        addEdge(newEdge);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public EdgeWeightedGraph(EdgeWeightedGraph other) {
    if (other == null) {
      throw new IllegalArgumentException("other is null");
    }
    this.edgeSize = other.edgeSize;
    this.nodeSize = other.nodeSize;
    neighbors = (Bag<Edge>[]) new Bag[nodeSize];
    for (int i = 0; i < nodeSize; i++) {
      neighbors[i] = new Bag<>();
    }
    Stack<Edge> edgeSatck = new Stack<>();
    for (int i = 0, len = other.neighbors.length; i < len; i++) {
      neighbors[i] = new Bag<>();
      // 因为 bag 是采用 头插法，但是遍历的 Iterable<> 接口又是从头到尾遍历，所以这里使用栈将结果暂存，移植到当前对象的时候，
      // 最先插入 Bag 链表里面的是 other 的 neighbours 最后一个元素，符合转移过来以后顺序一直
      for (Edge edge : other.neighbors[i]) {
        edgeSatck.push(edge);
      }
      while (!edgeSatck.isEmpty()) {
        neighbors[i].add(edgeSatck.pop());
      }
    }
  }

  private void addEdge(Edge edge) {
    int headIndex = edge.head();
    int tailIndex = edge.tail(headIndex);
    neighbors[headIndex].add(edge);
    neighbors[tailIndex].add(edge);
  }

  public int nodeSize() {
    return nodeSize;
  }

  public int edgeSize() {
    return edgeSize;
  }

  public Iterable<Edge> edges() {
    Bag<Edge> edgeList = new Bag<>();
    for (int i = 0; i < nodeSize; i++) {
      Bag<Edge> currNeighbours = neighbors[i];
      int selfCycleCount = 0;
      for (Edge edge : currNeighbours) {
        if (edge.tail(i) > i) {
          edgeList.add(edge);
        } else if (edge.tail(i) == i) {
          if (selfCycleCount % 2 == 0) {
            edgeList.add(edge);
          }
          selfCycleCount++;
        }
      }
    }
    return edgeList;
  }

  private void validateNodeIndex(int nodeIndex) {
    if (nodeIndex < 0 || nodeIndex >= nodeSize) {
      throw new IllegalArgumentException(String.format("nodeIndex should be between 0 and %s", nodeSize - 1));
    }
  }

  public Iterable<Edge> linkedNeighbors(int headIndex) {
    validateNodeIndex(headIndex);
    return neighbors[headIndex];
  }

  public int getDegreeOfNode(int nodeIndex) {
    validateNodeIndex(nodeIndex);
    return neighbors[nodeIndex].size();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(String.format("nodeSize:%s , edgeSize:%s, edges:", nodeSize, edgeSize)).append(NEW_LINE);
    for (int i = 0; i < nodeSize; i++) {
      for (Edge edge : neighbors[i]) {
        sb.append(edge + ";");
      }
      sb.append(NEW_LINE);
    }
    return sb.toString();
  }

  public static void main(String[] args) {
    String fileName = "tinyEWG.txt";
    String filePath = GraphUtil.getGraphFilePath(EdgeWeightedGraph.class, fileName);
    EdgeWeightedGraph ewg = new EdgeWeightedGraph(filePath, " ");
    System.out.println(ewg);
  }
}
