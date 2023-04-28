package com.swj.ics.jvm.classload;

import sun.misc.Launcher;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Random;

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

       // Class.forName("com.swj.ics.jvm.classload.DemoObj");

        //System.out.println(Child.age);//这里初始化的顺序是 父类->子类
        //下面演示一个诡异的事情
        //诡异一：
        //System.out.println(Child.salary);//这里会不会初始化父类那？会不会初始化子类那？
        //结果证明，会初始化父类，但是不会初始化子类。这是类的被动引用的一部分。
        //诡异二：直接使用数组，类并不会被初始化
       // DemoObj[] demoObjs = new DemoObj[10];

        printParentClassLoaderPath();
    }

    public static void printParentClassLoaderPath() {
        URL[] urLs = Launcher.getBootstrapClassPath().getURLs();
        System.out.println("BootStrap ClassLoader paths are listed:");
        for(URL url : urLs) {
            System.out.println(url);
        }
        System.out.println("Extension ClassLoader paths are listed:");
        urLs = ((URLClassLoader)java.lang.ClassLoader.getSystemClassLoader().getParent()).getURLs();

        for(URL url : urLs) {
            System.out.println(url);
        }
    }
}

class DemoObj {
    public static int salary = 10000;
    //static final类型的在主动访问的时候，是不会初始化静态类。引用常量不会被初始化，因为常量在编译的时候就放入常量池了

    public static final int salaryB = 20000;
    //这个调用的话，类会被初始化。
    public static final int x = new Random().nextInt(100);

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

/**
 * 验证：验证的主要目的是确保class文件中的字节流包含的信息符合虚拟机的要求，并且不会损害到jvm自身的安全。
 *
 * 文件格式验证：
 * 1、魔术因子是否正确。0xCAFEBABE
 * 2、主从版本号是否符合当前虚拟机。
 * 3、常量池中的常量是不是不支持。
 * 4、etc...
 * 元数据的验证：
 * 1、是否有父类
 * 2、父类是否允许被继承
 * 3、是否实现了抽象方法
 * 4、是否覆盖了父类的final字段
 * 5、其他的语义检查。
 * 字节码验证：
 * 主要进行数据流和控制流的验证，不会出现这样的情况：在堆栈中存放了一个int类型的，但是却给了一个long型的数据
 * 符号引用验证：
 *  调用一个不存在的方法，字段等。符号引用验证的目的是为了确保解析动作正常执行，如果无法通过符号引用验证，就会抛出一个异常。
 *  诸如：noSuchMethodException,nuSuchFieldException
 */

/**
 * 准备：
 * 就是为类的变量分配初始值。
 */
/**
 * 解析：
 * 类或者接口的解析
 * 字段的解析
 * 类方法的解析
 * 接口方法的解析
 *
 */
/**
 * 初始化：
 * 类加载的最后一步
 * 初始化是执行构造函数<clinit>()方法的过程。
 * <clinit>()方法是由编译器自动收集类中的所有变量赋值动作和静态语句块中的 语句合并产生的。
 * 静态语句块只能访问到定义在静态语句块之前的变量，定义在它之后的变量，只能赋值，不能访问。
 * <clinit>()方法与类的构造函数有点区别，它不需要显式的调用父类的构造函数，虚拟机会保证在子类的<clinit>()执行之前，
 * 先执行父类的<clinit>()，因此在虚拟机中先被执行的是Object的<clinit>()
 * 由于父类的<clinit>()方法要先执行，也就意味着父类中定义的静态语句块，要优先于子类。
 */


