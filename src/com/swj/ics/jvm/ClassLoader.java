package com.swj.ics.jvm;

/**
 * author shiweijie
 * date 2018/5/4 下午5:24
 * Java的类加载器
 * 类加在过程
 * 加载——>连接——>初始化
 * 其中 连接分为 3步：验证->准备->解析
 * 总的步骤为 Loading -> Linking(Verifying->Preparing->Resolving)->Initialising
 * 加载：查找并加载类的二进制数据
 * 验证：确保被加载类的正确性
 * 准备：为类的静态变量分配内存，并将其初始化为默认值（第二个阶段的第二个小阶段）
 * 解析：把类中的符号引用转换为直接引用。比如类中有个Object obj->this.obj.hashCode()将符号引用就变成直接引用。（具柄或者指针方式访问）
 *
 * 初始化，为类的静态变量赋予正确的初始值
 *
 * 1.3 java程序对类的使用方式
 * 1、主动使用
 * 2、被动使用
 * 所有的java虚拟机实现必须在每个类或者接口被java程序首次主动使用时才初始化他们，当然现代的jvm有可能根据程序上下文语义推断出
 * 接下来要初始化谁
 *
 * 主动使用的分类
 * 1、new 直接使用
 * 2、访问某个类或者接口的静态变量，或者对该静态变量进行赋值操作
 * 3、调用静态方法
 * 4、反射某个类
 * 5、初始化一个子类,其父类也会被初始化。
 * 6、启动类，比如：java HelloWorld
 * 出了上述6种方式外，其余的都是被动使用，不会导致类的初始化
 */
public class ClassLoader {
    public static void main(String[] args) throws ClassNotFoundException {
        System.out.println(I.a);
        //I.a = 12; 赋值语句是非法的，因为接口里面的变量是 static final的

       // Class.forName("com.swj.ics.jvm.DemoObj");

        //System.out.println(Child.age);//这里初始化的顺序是 父类->子类
        //下面演示一个诡异的事情
        //诡异一：
        //System.out.println(Child.salary);//这里会不会初始化父类那？会不会初始化子类那？
        //结果证明，会初始化父类，但是不会初始化子类。这是类的被动引用的一部分。
        //诡异二：直接使用数组，类并不会被初始化
        DemoObj[] demoObjs = new DemoObj[10];

    }
}

class DemoObj {
    public static int salary = 10000;

    static {
        System.out.println("obj 被初始化。。。");
    }
}
class Child extends DemoObj {
    public static int age =35;
    static {
        System.out.println("child 被初始化");
    }
}

interface I {
    int a = 10;
}
