package com.swj.ics.jvm;

/**
 * author shiweijie
 * date 2018/5/4 下午5:24
 * Java的类加载器
 * 类加在过程
 * 加载->连接->初始化
 * 其中 连接分为 3步：验证-》准备-》解析
 * 总的步骤为 Loading -> Linking(Verifying->Preparing->Resolving)->Initialising
 * 加载：查找并加载类的二进制数据
 * 验证：确保被加载类的正确性
 * 准备：为类的静态变量分配内存，并将其初始化为默认值
 * 解析：把类中的符号引用转换为直接引用。
 */
public class ClassLoader {

}
