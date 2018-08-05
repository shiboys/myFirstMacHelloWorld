package com.swj.ics.jvm;

/**
 * author shiweijie
 * date 2018/8/2 下午7:31
 */
public class LoadClass {
    public static void main(String[] args) {
        MyObject myObject1 = new MyObject();
        MyObject myObject2 = new MyObject();
        MyObject myObject3 = new MyObject();
        MyObject myObject4 = new MyObject();

        System.out.println(myObject1.getClass() == myObject2.getClass());
        System.out.println(myObject1.getClass() == myObject3.getClass());
        System.out.println(myObject1.getClass() == myObject4.getClass());
        System.out.println(MyObject.x);
    }

}
class MyObject {
    public static int x = 10;
}
