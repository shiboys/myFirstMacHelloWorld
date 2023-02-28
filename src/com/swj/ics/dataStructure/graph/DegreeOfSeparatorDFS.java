package com.swj.ics.dataStructure.graph;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/02/22 16:44
 * 基于深度优先的 分离度
 * 本次示例以电影为输入数据源，演示 演员 妮可基德曼的贝肯举例
 */
public class DegreeOfSeparatorDFS {

  public static void main(String[] args) {
    String fileName = "movies.txt";
    String filePath = GraphUtil.getGraphFilePath(fileName);
    SymbolGraph symbolGraph = new SymbolGraph(filePath, "/");
    String sourceActorName = "Bacon, Kevin";
    String targetActorName = "Kidman, Nicole";
    if (!symbolGraph.contains(sourceActorName)) {
      throw new IllegalArgumentException("source name of " + sourceActorName + " does not exists in the graph.");
    }
    if (!symbolGraph.contains(targetActorName)) {
      throw new IllegalArgumentException("target name of " + targetActorName + " does not exists in the graph.");
    }
    int startNodeIndex = symbolGraph.indexOf(sourceActorName);
    int targetNodeIndex = symbolGraph.indexOf(targetActorName);
    DepthFirstPath dfs = new DepthFirstPath(symbolGraph.getGraph(), startNodeIndex, DepthFirstPath.VisitStyle.LOOP);
    if (!dfs.hasPathTo(targetNodeIndex)) {
      System.err.println(String.format("target actor name %s is not connected with source name of %s",
              targetActorName, sourceActorName));
      return;
    }
    for (int nodeIndex : dfs.pathTo(targetNodeIndex)) {
      System.out.println("    " + symbolGraph.nameOf(nodeIndex));
    }
  }
}
