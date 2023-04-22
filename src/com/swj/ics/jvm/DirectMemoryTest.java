package com.swj.ics.jvm;

import java.nio.ByteBuffer;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/04/13 18:35
 */
public class DirectMemoryTest {

  public void directAccess() {
    long startTime = System.currentTimeMillis();
    ByteBuffer directBuffer = ByteBuffer.allocateDirect(500);
    for (int i = 0; i < 1_000_000; i++) {
      for (int j = 0; j < 100; j++) {
        directBuffer.putInt(j);
      }
      directBuffer.flip();
      for (int j = 0; j < 100; j++) {
        directBuffer.getInt();
      }
      directBuffer.clear();
    }

    long duration = System.currentTimeMillis() - startTime;
    System.out.println("testDirectWrite: " + duration);
  }

  public void heapMemoryAccess() {
    long startTime = System.currentTimeMillis();
    ByteBuffer byteBuffer = ByteBuffer.allocate(500);
    for (int i = 0; i < 1_000_000; i++) {
      for (int j = 0; j < 100; j++) {
        byteBuffer.putInt(j);
      }
      byteBuffer.flip();
      for (int j = 0; j < 100; j++) {
        byteBuffer.getInt();
      }
      byteBuffer.clear();
    }
    long duration = System.currentTimeMillis() - startTime;
    System.out.println("test heapMemoryAccess: " + duration);
  }

  public static void main(String[] args) {
    DirectMemoryTest instance = new DirectMemoryTest();
    /*instance.directAccess();
    instance.heapMemoryAccess();*/
    instance.allocateDirect();
    instance.allocateBuffer();
  }

  public void allocateDirect() {
    long startTime = System.currentTimeMillis();
    for(int i=0;i<200_000;i++) {
      ByteBuffer directBuffer = ByteBuffer.allocateDirect(1000);
    }
    long duration = System.currentTimeMillis() - startTime;
    System.out.println("allocate direct takes " + duration);
  }

  public void allocateBuffer() {
    long startTime = System.currentTimeMillis();
    for(int i=0;i<200_000;i++) {
      ByteBuffer byteBuffer = ByteBuffer.allocate(1000);
    }
    long duration = System.currentTimeMillis() - startTime;
    System.out.println("allocate buffer takes " + duration);
  }

}
