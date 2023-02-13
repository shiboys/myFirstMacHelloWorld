package com.swj.ics.dataStructure.bitmap;

import org.roaringbitmap.RoaringBitmap;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/02/13 20:57
 * RoaringBitMap 学习
 */
public class RoaringBitMapTest {
  /**
   * 学习笔记参见 markdown 文档
   */
  public static void main(String[] args) {
      testBitMap();
  }

  static void testBitMap() {
    int[] arr = {1, 2, 4, 1_000_000};
    RoaringBitmap roaringBitmap = RoaringBitmap.bitmapOf(arr);
    int sizeInBytes = roaringBitmap.getSizeInBytes();
    System.out.println("roaring_bitmap size is " + sizeInBytes);
    System.out.println("bitmap size is " + 1_000_000 / 8);
      /**
       * 结果如下：
       * roaring_bitmap size is 28
       * bitmap size is 125000
       */
  }
}
