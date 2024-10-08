## 散列表

Java 的String 类的 hashCode 方法，采用了类似 Horner 法则，如下所示
```java
public int hashCode() {
        // 这里的 hash 是缓存对象的 hashcode
        int h = hash;
        if (h == 0 && value.length > 0) {
            char val[] = value;

            for (int i = 0; i < value.length; i++) {
                h = 31 * h + val[i];
            }
            hash = h;
        }
        return h;
    }
```

### 将 hashCode() 的返回值转化为一个数组索引

&ensp;&ensp;因为我们需要对的数组的索引而不是一个 32 位的整数，我们在实现中会将默认的 hashCode() 方法和除留余数法
合起来产生一个 0 到 M-1 的整数，方法如下：

```java
private int hash(Key x) {
    return (x.hashCode() & 0x7fffffff) % M;
}
```
这段代码会将符号位屏蔽（将一个 32 位的整数变为一个 31 位的非负数），然后用除留余数法计算它除以 M 的余数。在使用这样的代码时候，我们一般都会将数组中的大小 M 取为素数以充分利用原散列值的所有位。

### 自定义 hashCode() 方法

```java
class TransactionWithHash {
    private String who;
    private Date when;
    private double amount;

    @Override
    public int hashCode() {
      int hash = 17;
      hash = hash * 31 + who.hashCode();
      hash = hash * 31 + when.hashCode();
      hash = hash * 31 + ((Double) amount).hashCode();
      return hash;
    }
  }
```

### 软缓存

如果散列值计算很耗时，那么我们或许可以将每个键的散列值缓存起来，即在每个键中使用一个 hash 变量来保存它的 hashcode() 的返回值。第一次调用 hashCode() 方法时，我们需要计算对象的散列值，但之后对 hashCode()方法的调用会直接返回 hash 变量的值。Java 的 String 对象的 hashCode() 方法使用了这种方法来减少计算量。
    总的来说，要为一个数据类型实现一个优秀的散列方法需要满足三个条件：

* **一致性**——等价的键必然产生相等的散列值
* **高效性**——计算简便
* **均匀性**——均匀地散列所有的键

设计同时满足这三个条件的散列函是专家们的事。有了各种内置函数，Java 程序猿在使用散列时只需要调用 hashcode() 方法即可。


### 基于线性探测的散列表

&ensp;&ensp;实现散列表的另一种方法是用大小为 M 的数组保存 N 个键值对，其中 M>N。我们需要依靠数组中的空位解决碰撞冲突。基于这种策略的所有方法被统称为开放地址散列表。

&ensp;&ensp;开放地址散列表中最简单的方法叫做*线性探测法*，当碰撞发生时（一个键的散列值已经被另一不同的键所占用），我们直接检查散列表中的下一个位置（将索引值加 1）。这样的线性探测可能产生三种不同的结果：
* 命中，该位置的键和被查找的键相同；
* 未命中，键为空（该位置没有键）
* 继续查找，该位置的键和被查找的键不同。

我们用散列函数找到键所在数组中的索引，检查其中的键和被查找的键是否相同。如果不同则继续查找（将索引增大，到达数组结尾时这会数组的开头），直到找到该键或者遇到一个空元素。我们习惯检查一个数组位置是否含有被查找的键的操作称为*探测*。在这里可以等价于我们的比较，不过有些探测实际上是在测试键是否为空。

&ensp;&ensp;开放地址类的散列表的核心思想是与其将内存用作链表，不如将它们用作在散列表的空元素。这些空元素可以作为查找结束的标志。对于基于线性探测的散列表，我们是不允许散列表被占满，为了保证性能，我们会动态调整数组的大小来保证使用率在 1/8 到 1/2 之间。这个策略是基于数学上的分析。

### 线性探查法的缺点分析

线性探查法其实存在很大的问题。当散列表中插入的数据越来越多时，散列冲突发生的可能性就越来越大，空闲位置越来越少，线性探查的时间就会越来越久。
极端情况下，我们可能需要探查整个散列表，所以最坏情况下的时间复杂度为 O(n)。同理，在删除和查找时，也有可能会线性探查整张散列表，才能找到要查找或者删除的数据。

### 其他开放地址法
对于开放地址冲突解决方法，除了线性探查方法之外，还有另外两种比较经典的探测方法，**二次探测**(Quadratic probing) 和 **双重散列**(Double hashing)。

所谓二次探测，跟线性探测很像，线性探测的每次探测步长为1，那它探测的下标序列就是 hash(key)+0, hash(key)+1, hash(key)+2 ...。而二次探测的步长就变成了“二次方”，也就是 hash(key)+0, hash(key)+ 1^2, hash(key) + 2^2...

所谓双重散列，意思是不仅要使用一个散列函数。我们要使用一组散列函数 hash1(key), hash2(key), hash3(key),hash4(key)...... 我们首先使用一个散列函数，如果计算得到的存储位置已经被占用，则用第二个散列函数，以此类推，直到找到空闲位置。

### 开放寻址法的优缺点

#### 优点

开放寻址法并不像链表法，需要拉很多链表。散列表中的数据都存储在数组中，可以有效地利用 CPU 缓存加速查询速度。而且，这种方法实现的散列表，序列化起来比较简单。
链表法包含指针，序列化起来就没那么容易。

#### 缺点

开放寻址法的散列表中，删除数据的时候比较麻烦，需要特殊标记已经删除掉的数据。而且，在开放寻址法中，所有的数据都存储在一个数组中，比起链表来说，冲突的代价更高。所以，使用开放地址法来解决冲突的散列表，装载因子的上限不能太大。这也导致这种方法比链表法更浪费内存空间。

最后，这里总结下：**当数据量较小的时候，适合采用开放寻址法。这也是 Java 中 ThreadLocalMap 使用开放寻址法解决散列冲突的原因。**

### 如何避免低效扩容？

为了解决一次性扩容耗时过多的情况，我们可以将扩容操作穿插在插入操作的过程中，分批完成。当容量到达装载因子所规定的阈值之后，我们只申请新空间，但并不将老的数据搬移到新的散列表中。（好像是借鉴了 redis 的扩容机制, 有点类似于渐进式 rehash，但是又不完全相同）

当有新数据插入时，我们将新数据插入新散列表中，并且从老的散列表中拿出一个数据放入到新的散列表。每当插入一个数据到散列表，我们都重复上述过程。经过多次插入操作之后，老的散列表中的数据就一点一点全部搬移到新的散列表中了。这样就没有了集中式的一次性数据搬移，插入操作就会变得快多了。

总结：
这个是渐进式 rehash 一种平易描述，渐进式 rehash 在 rehash 的过程中，字典会同时使用 ht[0] 和 ht[1] 两个哈希表，所以在渐进式 rehash 运行期间，字典的删除(delete)、查找，更新等操作都会在两个哈希表上进行。另外，在渐进式 rehash 执行期间，新添加到字典的键值对会一律被保存到 ht[1] 里面，而 ht[0] 则不再进行任何添加操作：这一项措施保证了 ht[0] 包含的键值对数量只减不增，并随着 rehash 操作的执行而最终变成空表。


### 链表法跟开放地址法的对比

首先，链表法对内存的利用率比开放寻址法要高。因为链表节点可以在需要的时候再创建，并不需要像开放地址法那样事前申请好。实际上，这也是链表优于数组的一个地方。
（也适用于 ArrayList 和 LinkedList）

链表法比起开放寻址法，对大装载因子的容忍度更高。开放寻址法只适用于装载因子小于 1 的情况。接近 1时，就可能会有大量的散列冲突，导致大量的探测、再散列等，性能会下降很多。但是对于链表来说，只要散列函数的值随机均匀，即便装载因子变成 10，也就是链表长度变了而已（请看考 SeparateChainHashTable.java ，装载因子就是 10），也就是链表的长度变长而已，虽然查找效率有所下降，但是比起顺序查找还是快很多。

总结：

**基于链表的散列冲突解决方法比较适合存储大对象、大数据量的散列表，而且，比起开放地址法，它更灵活，支持更多的有优化策略，比如用红黑树代替链表。（Java jdk8的 的 HashMap 就是这么做的）**

## Q & A

Q: Java 的 Integer、Double和Long 类型的 hashCode 是如何实现的？
A：Integer 类型会直接返回该整数 32 位的值。对于 Double 和 Long 类型，Java 会返回高 32 位和低 32 位异或的结果，这些方法可能不够随机，但是他们的确能够将值散列

```java
// java.lang.Long#hashCode(long)
public static int hashCode(long value) {
        return (int)(value ^ (value >>> 32));
    }
```

Q: 当能够动态调整数组大小时，散列表的大小总是 2 的幂，这不是个问题吗？这样 hash() 方法只使用了 hashCode() 返回值的低位。

A：是的，这个问题在默认实现中特别明显。解决这个问题的一种方法是将高位参与运算，根据 JDK 的做法
```java
// JDK7 hashmap 的 hash 扰动算法
final int hash(Object k) {
    //hashSeed用于计算key的hash值，它与key的hashCode进行按位异或运算
//hashSeed是一个与实例相关的随机值，用于解决hash冲突
//如果为0则禁用备用哈希算法
    int h = hashSeed;
    //默认是0，不是0那么需要key是String类型才使用stringHash32这种hash方法
    if (0 != h && k instanceof String) {
        return sun.misc.Hashing.stringHash32((String) k);
    }
    //这段代码是为了对key的hashCode进行扰动计算，防止不同hashCode的高位不同但低位相同导致的hash冲突。简单点
    //说，就是为了把高位的特征和低位的特征组合起来，降低哈希冲突的概率，也就是说，尽量做到任何一位的变化都能对
    //最终得到的结果产生影响
    h ^= k.hashCode();
    h ^= (h >>> 20) ^ (h >>> 12);
    return h ^ (h >>> 7) ^ (h >>> 4);
}

// jdk8 的 hashmap 的 hash 算法直接让高 16 位参与运算，更加直观简洁。跟 Long 类型的 hash 算法有点类似。
static final int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }
```

Q：为什么不将 hash(x) 实现为 x.hashCode()% M ?
A：散列值必须在 0 到 M-1 之间，~~而在 Java 中，取余 （%）的结果可能是负数~~。hashcode 可能是负值

Q：为什么不将 hash(X) 实现为 Math.abs(x.hashCode()) % M ？
A：对于最大的整数，Math.abs() 会返回一个负值。对于许多典型情况，这种移除不会造成什么问题，但是对于散列表这可能使你的程序在几十亿次插入之后崩溃，这很难说。

Q: 工业级的散列表应该具有哪些特征？
A： 应该具有如下特征
*  支持快速地查询、插入、删除操作；
*  内存占用合理，不能浪费过多的内存空间
*  性能稳定、在极端情况下、散列表的性能也不会退化到无法接受的情况。

Q：如何实现这样一个散列表那？
A：我认为应该从一下这 3 个方面来考虑：
* 设计一个合适的散列函数；
* 定义装载因子阈值，并且设计动态扩容策略
* 选择合适的散列冲突解决方法
