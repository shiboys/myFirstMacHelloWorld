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

  public static void main(String[] args) throws ClassNotFoundException {
    //Child c = new Child();
     System.out.println(PassiveChild.v);
     // Class.forName 能进行初始化 PassiveChild，但是 如果只是调用 PassiveChild.v 则只能加 PassiveChild 类，但是不会对其进行初始化
     //Class.forName(PassiveChild.class.getName());
    //System.out.println(FinalFieldClass.CONST_STRING);


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
