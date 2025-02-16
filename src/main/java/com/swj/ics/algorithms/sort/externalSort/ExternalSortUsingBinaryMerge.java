package com.swj.ics.algorithms.sort.externalSort;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Random;
import java.util.regex.Pattern;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/02/03 11:42
 * 二路归并外排序
 */
public class ExternalSortUsingBinaryMerge {
  /**
   * 二路归并法外排序，主要思想是先将大文件进行切分，并对切分后的n个小文件进行排序，这里的切分原则是切分后的文件可以被放入到内存里面进行排序
   * 然后对切分后的已经排好序的文件进行二路归并排序，将两小文件合并为一个次小文件，以此类推，最终将整个切分后的文件合并为一个排好序的大文件
   * 在实现上主要是对大文件打开以流的形式一行一行读取，读取到内存一定数量的行之后，进行排序，然后写出到小文件中
   */
  private static final Pattern NUMBER_PATTERN = Pattern.compile("^[1-9][0-9]+$");
  private static final int MAX_SPLIT_COUNTER = 1000;
  private int flag = 0;
  private String fileName;

  public static void main(String[] args) {
    String splittingFilePath = "./bigfile.txt";
    int max = 1 << 26;
    int len = 10100;
    Random random = new Random();
    // int[] arr= new
    File splitFile = new File(splittingFilePath);
    /*if(splitFile.exists()) {
      splitFile.delete();
    }*/
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
    ExternalSortUsingBinaryMerge instance = new ExternalSortUsingBinaryMerge();
    instance.externalSort(splittingFilePath);
  }

  public void externalSort(String fileName) {
    if (fileName == null || fileName.isEmpty()) {
      System.err.println("filename is empty.");
      return;
    }
    int[] memoryArr = new int[MAX_SPLIT_COUNTER];
    File file = new File(fileName);
    if (!file.exists()) {
      System.err.println(String.format("file is not exists. [filename='%s']", fileName));
      return;
    }
    this.fileName = fileName;
    tryReadContentBySorting(file,false);
    splitFile(file, memoryArr);
    String splitDir = "./tmp/" + this.flag + "/";
    tryMergeFiles(splitDir);
    tryReadContentBySorting(file,true);
  }

  private void tryReadContentBySorting(File file, boolean sorted) {
    BufferedReader br = null;
    try {
      br = new BufferedReader(new FileReader(file));
      int topN = 10;
      System.out.println("before sort get top " + topN + ":");
      int i = 0;
      String val = br.readLine();
      while (val != null && i < topN) {
        System.out.print(val + "\t");
        i++;
        val = br.readLine();
      }
      System.out.println();

    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (br != null) {
        try {
          br.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private void splitFile(File file, int[] valArr) {
    BufferedReader br = null;
    try {
      Arrays.fill(valArr, -1);
      br = new BufferedReader(new FileReader(file));
      int i = 0;
      int saveIndex = 0;
      String val = br.readLine();
      while (val != null) {
        if (!NUMBER_PATTERN.matcher(val).matches()) {
          System.out.println(String.format("line number %s '%s' is not a number.", i, val));
          continue;
        }
        valArr[i++] = Integer.parseInt(val);
        if (i == MAX_SPLIT_COUNTER) {
          Arrays.sort(valArr);
          saveSplitFileToDisk(valArr, saveIndex++);
          i = 0;
          Arrays.fill(valArr, -1);
        }
        val = br.readLine();
      }
      if (valArr[0] != -1) {

        Arrays.sort(valArr);
        saveSplitFileToDisk(valArr, saveIndex);
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (br != null) {
        try {
          br.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private void saveSplitFileToDisk(int[] sortedArr, int saveIndex) {
    String filePath = "./tmp/" + this.flag + "/" + saveIndex + ".sorted";
    File saveFile = new File(filePath);
    File parentFile = saveFile.getParentFile();
    if (!parentFile.exists()) {
      parentFile.mkdirs();
    }
    BufferedWriter bw = null;
    try {
      bw = new BufferedWriter(new FileWriter(saveFile));
      for (int i = 0, len = sortedArr.length; i < len; i++) {
        int val = sortedArr[i];
        if (val == -1) {
          continue;
        }
        bw.write(String.valueOf(val));
        if (i != len - 1) {
          bw.newLine();
        }
      }
      bw.flush();
      System.out.println("save the sored array to file " + filePath);
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (bw != null) {
        try {
          bw.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private void tryMergeFiles(String targetSplitDir) {
    File parentDir = new File(targetSplitDir);
    if (!parentDir.exists()) {
      System.err.println(String.format("target file path '%s' is not exists", targetSplitDir));
      return;
    }
    File[] mergingFiles = parentDir.listFiles();
    if (mergingFiles == null || mergingFiles.length == 0) {
      System.err.println(String.format("target file path '%s' is empty", targetSplitDir));
      return;
    }
    if (mergingFiles.length == 1) {
      copyFile(mergingFiles[0], this.fileName);
    } else if (mergingFiles.length == 2) { // 最后两个文件，直接进行归并
      merge2File(this.fileName, mergingFiles);
    } else {
      String targetFilePath = "./tmp/" + (1 - flag) + "/";
      File targetFile = new File(targetFilePath);
      if (targetFile.exists()) {
        targetFile.mkdirs();
      }
      int mergeIndex = 0;
      int i = 0;
      String tempFile;
      for (int len = mergingFiles.length; i < len - 1; i += 2) {
        tempFile = targetFilePath + "mergedFile" + (mergeIndex++) + ".sorted";
        merge2File(tempFile, mergingFiles[i], mergingFiles[i + 1]);
      }
      if (i == mergingFiles.length - 1) {
        tempFile = targetFilePath + "mergedFile" + mergeIndex + ".sorted";
        copyFile(mergingFiles[i], tempFile);
      }
      flag = 1 - flag;
      tryMergeFiles(targetFilePath);
    }
  }

  private void merge2File(String targetFileName, File... mergingFiles) {
    if (mergingFiles == null || mergingFiles.length < 2) {
      System.err.println("merging files size must be no less than 2");
      return;
    }
    for (File mergeFile : mergingFiles) {
      if (mergeFile == null || !mergeFile.exists()) {
        System.err.println("merging file " + mergeFile + " not exists.");
        return;
      }
    }
    File targetFile = new File(targetFileName);
    File targetDir = targetFile.getParentFile();
    if (!targetDir.exists()) {
      targetDir.mkdirs();
    }

    try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetFileName)));
        BufferedReader br1 = new BufferedReader(new FileReader(mergingFiles[0]));
        BufferedReader br2 = new BufferedReader(new FileReader(mergingFiles[1]))) {
      String line1 = br1.readLine();
      String line2 = br2.readLine();
      while (line1 != null && line2 != null) {
        if (!NUMBER_PATTERN.matcher(line1).matches()) {
          line1 = br1.readLine();
          continue;
        }
        if (!NUMBER_PATTERN.matcher(line2).matches()) {
          line2 = br2.readLine();
          continue;
        }
        int num1 = Integer.parseInt(line1);
        int num2 = Integer.parseInt(line2);
        if (num1 < num2) {
          bw.write(String.valueOf(num1));
          bw.newLine();
          line1 = br1.readLine();
        } else {
          bw.write(String.valueOf(num2));
          bw.newLine();
          line2 = br2.readLine();
        }
      }
      bw.flush();
      if (line1 != null || line2 != null) {
        while (line1 != null) {
          if (NUMBER_PATTERN.matcher(line1).matches()) {
            bw.write(line1);
          }
          line1 = br1.readLine();
          if (line1 != null && NUMBER_PATTERN.matcher(line1).matches()) {
            bw.newLine();
          }
        }
        while (line2 != null) {
          if (NUMBER_PATTERN.matcher(line2).matches()) {
            bw.write(line2);
          }
          line2 = br2.readLine();
          if (line2 != null && NUMBER_PATTERN.matcher(line2).matches()) {
            bw.newLine();
          }
        }
        bw.flush();
      }

      System.out.println("merge 2 files done and delete temp 2 files. the merged file is " + targetFileName);
      System.out.println("delete file1 " + mergingFiles[0]);
      mergingFiles[0].delete();
      System.out.println("delete file2 " + mergingFiles[1]);
      mergingFiles[1].delete();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void copyFile(File sourceFile, String targetFilePath) {
    File targetFile = new File(targetFilePath);
    /*if (targetFile.exists()) {
      targetFile.delete();
    }
    不需要刻意删除，因为 FileWriter 的默认构造函数是覆盖，append=false
    */
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(targetFile));
        BufferedReader br = new BufferedReader(new FileReader(sourceFile))) {
      String line = br.readLine();
      while (line != null) {
        bw.write(line);
        line = br.readLine();
        if (line != null) {
          bw.newLine();
        }
      }
      bw.flush();
      System.out.println("copy file done. target file is " + targetFilePath);
      System.out.println("delete source file " + sourceFile + " after copying done");
      sourceFile.delete();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
