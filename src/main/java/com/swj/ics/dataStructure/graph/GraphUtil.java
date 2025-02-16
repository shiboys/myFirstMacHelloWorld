package com.swj.ics.dataStructure.graph;

import lombok.experimental.UtilityClass;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/02/18 15:36
 */
@UtilityClass
public class GraphUtil {

  public static Graph buildDemoGraph(String fileName) {
    return new Graph(getGraphFilePath(fileName));
  }

  public static String getGraphFilePath(String fileName) {
    return getGraphFilePath(Graph.class, fileName);
  }

  public static String getGraphFilePath(Class<?> clazz, String fileName) {
    String filePath = clazz.getPackage().getName().replace(".", "/") + "/" + fileName;
    URL resource = ClassLoader.getSystemResource("");
    filePath = resource.getPath() + filePath;
    return filePath;
  }

  public BufferedReader read(String filePath) throws IOException {
    File file = new File(filePath);
    if (!file.exists()) {
      throw new IllegalArgumentException(String.format("filePath is not exists. [filePath='%s']", filePath));
    }
    return new BufferedReader(new FileReader(file));
  }
}
