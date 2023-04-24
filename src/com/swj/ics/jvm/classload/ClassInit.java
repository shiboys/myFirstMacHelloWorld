package com.swj.ics.jvm.classload;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/04/23 17:45
 */
public class ClassInit {

  public static String constString= "constString";

  static class Parent {
    static {
      System.out.println("Parent init");
    }
  }


  static class Child extends Parent {
    static {
      System.out.println("Child init");
    }
  }

  public static void main(String[] args) {
    //Child c = new Child();
    // System.out.println(PassiveChild.v);
    System.out.println(FinalFieldClass.CONST_STRING);
  }

  static class PassiveParent {
    static {
      System.out.println("Passive Parent init");
    }

    public static int v = 100;
  }


  static class PassiveChild extends PassiveParent {
    static {
      System.out.println("passive child init");
    }
  }

}
