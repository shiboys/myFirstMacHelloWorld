package com.swj.ics.dataStructure.graph;

import com.swj.ics.dataStructure.hash.ST;
import com.swj.ics.dataStructure.hash.SequentialLinearLiteST;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/02/21 21:01
 * 符号图
 * 在典型应用中，图都是通过文件或者网页定义的，使用的是字符串而非整数来表示和指代定点。
 * 为了适应这样的引用，我们定义了具有一下性质的输入格式：
 * 1、定点为字符串
 * 2、用指定的分隔符来隔开顶点名（允许顶点名中含有空格）；
 * 3、每一行都表示一组边的集合，每一条边都连接着这一行的第一个名称表示的顶点和其他名称所表示的顶点；
 * 4、顶点总数 V 和边的总数 E 都是隐式定义的。
 * 参见示例 routes.txt, 他表示的一个小型运输系统所连接的美国机场的代码。
 */
public class SymbolGraph {
  // 通过名称找编号
  private ST<String, Integer> nameSt;
  private Graph graph;
  // 通过编号找名称
  private String[] keys;

  public SymbolGraph(String fileName, String delimiter) {
    nameSt = new SequentialLinearLiteST<>();
    readFileContentToFillContainer(fileName, delimiter);
    initKeys();
    initGraph(fileName, delimiter);
  }

  private void readFileContentToFillContainer(String fileName, String delimiter) {
    readFileContentToFunc(fileName, delimiter, line -> {
      String[] addrArr = line.split(delimiter);
      for (String address : addrArr) {
        // st 实现给地址名称编号，编完号之后，后面就可以利用编号生成图，在图里面进行玩了。
        // 比如说 A 机场的航线有 B C D
        // 这里需要增加这个 !contains 的逻辑判断，否则元素的 nodeIndex 不稳定，就会造成 不同的元素有相同的 nodeIndex。
        if (!nameSt.contains(address)) {
          nameSt.put(address, nameSt.size());
        }
      }
    });
  }


  private void readFileContentToFunc(String fileName, String delimiter, Consumer<String> func) {
    File file = new File(fileName);
    if (!file.exists()) {
      System.err.println(String.format("file not exists. [filename='%s']", fileName));
      return;
    }
    try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
      String line;
      while ((line = br.readLine()) != null && !line.isEmpty()) {
        func.accept(line);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void initKeys() {
    // 用 keys 数组记录 所有的关键词
    keys = new String[nameSt.size()];
    for (String key : nameSt.keys()) {
      keys[nameSt.get(key)] = key;
    }
  }

  private void initGraph(String fileName, String delimiter) {
    this.graph = new Graph(nameSt.size());
    readFileContentToFunc(fileName, delimiter, line -> {
      String[] addrArr = line.split(delimiter);
      int nodeHeadIndex = nameSt.get(addrArr[0]);
      // 将地址名称映射为编号，用边将头节点和其他节点连接起来，放入图中
      for (int i = 1; i < addrArr.length; i++) {
        graph.addEdge(nodeHeadIndex, nameSt.get(addrArr[i]));
      }
    });
  }

  public boolean contains(String key) {
    return nameSt.contains(key);
  }

  public int indexOf(String key) {
    return nameSt.get(key);
  }

  public String nameOf(int nodeIndex) {
    return keys[nodeIndex];
  }

  public Graph getGraph() {
    return graph;
  }

  public static void main(String[] args) {
    String fileName = "routes.txt";
    String filePath = GraphUtil.getGraphFilePath(fileName);
    String delimiter = " ";
    SymbolGraph symbolGraph = new SymbolGraph(filePath, delimiter);
    Graph graph = symbolGraph.getGraph();
    String addrs = "JFK,LAX,ORD,BEIJING";
    for (String addr : addrs.split(",")) {
      if (!symbolGraph.contains(addr)) {
        System.out.println("the graph does not contains addr: " + addr);
        continue;
      }
      System.out.println(addr);
      int nodeIndex = symbolGraph.indexOf(addr);
      Iterable<Integer> nodeIndexIt = graph.linkedNeighbors(nodeIndex);
      System.out.print("    ");
      for (int nodeIdx : nodeIndexIt) {
        System.out.print(symbolGraph.nameOf(nodeIdx) + " ");
      }
      System.out.println();
    }
  }
}
