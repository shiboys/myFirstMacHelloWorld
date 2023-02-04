package com.swj.ics.algorithms.sort.externalSort;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/02/04 10:32
 * 多路归并外排序
 * 核心思想还是把大文件分割成 n 个内存能装得下且已经排好序的小文件，
 * 然后建立长度为 n 的数组，从 n 个文件中每次读取一个数据，
 * 对 n 个数据取最小的数据，输出到新的归并结果集，然后把最小数据所在的文件流阅读器向后移动一步，将下一个数字替换当前的已经被归并的数字。
 * 这样做的好处是归并一次，不会生成中间文件
 */
public class MultiMergeSort {

  private static final String SPLIT_DIR_PATH = "./multiMergeSort/split/";
  private static final String SPLIT_FILE_SUFFIX = ".sorted";
  private static final Pattern NUMBER_PATTERN = Pattern.compile("^[1-9][0-9]+$");


  private static class Sort {
    private String inputFilePath;
    private int bufferSize;
    private int[] buffer;

    public Sort(int bufferSize) {
      this.bufferSize = bufferSize;
      this.buffer = new int[bufferSize];
      Arrays.fill(buffer, -1);
    }

    private void splitAndSorted(List<String> splitFiles) {
      if (inputFilePath == null || inputFilePath.length() < 1) {
        System.err.println("input file path is empty");
        return;
      }
      File inputFile = new File(inputFilePath);
      if (!inputFile.exists()) {
        System.err.println("input file " + inputFilePath + " does not exists");
        return;
      }
      if (splitFiles == null) {
        System.err.println("splitFiles is empty");
        return;
      }
      try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
        String line;
        int lineCounter = 0;
        while ((line = br.readLine()) != null) {
          if (!NUMBER_PATTERN.matcher(line).matches()) {
            continue;
          }
          buffer[lineCounter++] = Integer.parseInt(line);
          if (lineCounter == bufferSize) {
            Arrays.sort(buffer);
            saveSorted(buffer, splitFiles);
            lineCounter = 0;
            Arrays.fill(buffer, -1);
          }
        }
        // 把剩余的数组内容写入文件
        if (Arrays.stream(buffer).anyMatch(x -> x != -1)) {
          Arrays.sort(buffer);
          saveSorted(buffer, splitFiles);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    private void saveSorted(int[] sortedArr, List<String> splitFiles) {
      if (sortedArr == null || sortedArr.length < 1) {
        System.err.println("sorted array is empty, so do not save it");
        return;
      }
      String fileName = UUID.randomUUID().toString();
      String filePath = SPLIT_DIR_PATH + fileName + SPLIT_FILE_SUFFIX;
      File file = new File(filePath);
      File parentDir = file.getParentFile();
      if (!parentDir.exists()) {
        parentDir.mkdirs();
      }

      try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
        String val;
        for (int i = 0, len = sortedArr.length; i < len; i++) {
          if (sortedArr[i] == -1) { // 默认值
            continue;
          }
          val = String.valueOf(sortedArr[i]);
          if (NUMBER_PATTERN.matcher(val).matches()) {
            bw.write(val);
            if (i != len - 1) {
              bw.newLine();
            }
          }
          if (i > 0 && (i % 200) == 0) {
            bw.flush();
          }
        }
        bw.flush();
        splitFiles.add(filePath);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    public void sort(String inputFilePath, String outputFile) {
      this.inputFilePath = inputFilePath;
      List<String> splitFileList = new ArrayList<>();
      splitAndSorted(splitFileList);
      new Merger(splitFileList, outputFile).merge();
      System.out.println("the sorted output file is generated.");
      System.out.println("begin to clear the temp files");
      for (String filePath : splitFileList) {
        File tmpFile = new File(filePath);
        if (tmpFile.exists()) {
          tmpFile.delete();
          System.out.println("delete file " + tmpFile.getName());
        }
      }
    }
  }


  private static class Merger {
    private List<String> tmpFiles;
    private int splitFileSize;
    private int[] memoryArr;
    private BufferedReader[] brs;
    private int minValue;
    private String outputFile;
    private BufferedWriter bw;

    public Merger(List<String> splitFiles, String outputFile) {
      this.tmpFiles = splitFiles;
      splitFileSize = splitFiles.size();
      memoryArr = new int[splitFileSize];
      brs = new BufferedReader[splitFileSize];
      this.outputFile = outputFile;
      try {
        bw = new BufferedWriter(new FileWriter(outputFile));
      } catch (IOException e) {
        e.printStackTrace();
      }
      init();
    }

    private void init() {
      Arrays.fill(memoryArr, -1);
      String val;
      for (int i = 0; i < splitFileSize; i++) {
        try {
          brs[i] = new BufferedReader(new FileReader(tmpFiles.get(i)));
          val = brs[i].readLine();
          if (!NUMBER_PATTERN.matcher(val).matches()) {
            continue;
          }
          memoryArr[i] = Integer.parseInt(val);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

    public void merge() {
      int fileReadFinished = 0;
      try {
        while (fileReadFinished < splitFileSize) {
          updateMinValueFromMemory();
          for (int i = 0; i < memoryArr.length; i++) {
            boolean foundMin = false;
            while (memoryArr[i] == minValue) {
              foundMin = true;
              bw.write(String.valueOf(memoryArr[i]));
              // 更新 i 位置的值
              boolean success = readNextValueFromStream(i);
              if (!success) {
                fileReadFinished++;
              }
              if (fileReadFinished != splitFileSize) {
                bw.newLine();
              }
            }
            if (foundMin) {
              break;
            }
          }
        }

        bw.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    boolean readNextValueFromStream(int fileReadIndex) {
      try {
        String val = brs[fileReadIndex].readLine();
        while (val != null) {
          if (NUMBER_PATTERN.matcher(val).matches()) {
            memoryArr[fileReadIndex] = Integer.parseInt(val);
            return true;
          } else {
            val = brs[fileReadIndex].readLine();
          }
        }
        // end of file stream
        memoryArr[fileReadIndex] = -1;
        brs[fileReadIndex].close();
        return false;
      } catch (IOException e) {
        e.printStackTrace();
      }
      return false;
    }

    private void updateMinValueFromMemory() {
      // 如果数组刚被初始化或者所有的文件已经被读取完毕，返回默认的 -1
      this.minValue = Arrays.stream(memoryArr).filter(x -> x != -1).min().orElse(Integer.MIN_VALUE);
    }
  }

  public static void main(String[] args) {
    String inputFilePath = SPLIT_DIR_PATH + "inputFile.txt";
    int max = 1 << 26;
    int len = 10100;
    Random random = new Random();
    File splitFile = new File(inputFilePath);
    File parentFile = splitFile.getParentFile();
    if (!parentFile.exists()) {
      parentFile.mkdirs();
    }
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(splitFile))) {
      for (int i = 0; i < len; i++) {
        bw.write(String.valueOf(random.nextInt(max)));
        if (i != len - 1) {
          bw.newLine();
        }
        if (i > 0 && (i % 1000) == 0) {
          bw.flush();
        }
      }
      bw.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
    System.out.println("big file generated. file path is " + splitFile.getPath());

    String outPutFile = SPLIT_DIR_PATH + "outputFile.txt";

    new Sort(1000).sort(inputFilePath, outPutFile);
  }
}
