package com.swj.ics.dataStructure.graph;

import java.util.ArrayList;
import java.util.List;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/02/22 11:39
 * 分离度。用来描述一个图中节点与节点之间的距离。
 * 本例子继续使用 routes.txt 中的文件，来表述 route.txt 中两个机场地址之间的路径（经过哪些节点）
 * 按照原文作者的说法也可以表述社交网络中人与人之间的联系。
 */
public class DegreeOfSeparation {

  private SymbolGraph symbolGraph;

  public DegreeOfSeparation(String filePath, String delimiter) {
    symbolGraph = new SymbolGraph(filePath, delimiter);
  }

  public List<String> getPathNodeNameList(String sourceAddr, String targetAddr) {
    if (!symbolGraph.contains(sourceAddr)) {
      System.err.println("source addr " + sourceAddr + " is not in the graph");
      return null;
    }
    if (!symbolGraph.contains(targetAddr)) {
      System.err.println("target addr " + targetAddr + " is not in the graph");
      return null;
    }
    int nodeIndex = symbolGraph.indexOf(sourceAddr);
    BreadthFirstPath bfs = new BreadthFirstPath(symbolGraph.getGraph(), nodeIndex);
    int targetNodeIndex = symbolGraph.indexOf(targetAddr);
    if (!bfs.hasPathTo(targetNodeIndex)) {
      System.err.println("target address " + sourceAddr + " is not connected with source address " + targetAddr);
      return null;
    }
    List<String> addressNameList = new ArrayList<>();
    for (int nodeIdx : bfs.pathTo(targetNodeIndex)) {
      addressNameList.add(symbolGraph.nameOf(nodeIdx));
    }
    return addressNameList;
  }

  public static void main(String[] args) {
    String fileName = "routes.txt";
    String filePath = GraphUtil.getGraphFilePath(fileName);
    String delimiter = " ";
    DegreeOfSeparation dos = new DegreeOfSeparation(filePath, delimiter);
    String sourceAddr="JFK";
    String targetAddr = "LAS";
    List<String> pathNodeNameList = dos.getPathNodeNameList(sourceAddr, targetAddr);
    System.out.println(pathNodeNameList);

    targetAddr = "DFW";
    pathNodeNameList = dos.getPathNodeNameList(sourceAddr, targetAddr);
    System.out.println(pathNodeNameList);

    targetAddr = "EWR";
    pathNodeNameList = dos.getPathNodeNameList(sourceAddr, targetAddr);
    System.out.println(pathNodeNameList);

    /**
     * 作者给出的路径是如下，
     *  %  java DegreesOfSeparation routes.txt " " "JFK"
     *  LAS
     *     JFK
     *     ORD
     *     DEN
     *     LAS
     *  DFW
     *     JFK
     *     ORD
     *     DFW
     *  EWR
     *     Not in database.
     *
     * 我自己的路径是如下：
     * [JFK, ORD, PHX, LAS] 经过跟原图对比发现，这个也是正确的。请参考原图 symbol-graph.png
     *
     */
  }
}
