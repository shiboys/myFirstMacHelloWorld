package com.swj.ics.dataStructure.tree.binarytree;

import com.swj.ics.dataStructure.queue.Queue;

import java.util.NoSuchElementException;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/01/19 10:57
 * 二叉搜索树，
 * 代码参考算法4 的实现
 * https://algs4.cs.princeton.edu/32bst/BST.java.html
 */
public class BinarySearchTree<Key extends Comparable<Key>, Value> {
  /**
   * 二叉搜索树，几个核心 api
   * 增删改查，构造函数，迭代器
   * put 增
   * put：改
   * search, get,
   * floor, ceiling 查
   * delete，deleteMin，deleteMax 删
   * 补充：
   * select 根据指定的 top k，反对第 k 大或者第 k 小的元素
   * rank 根据指定的 key ，返回该 key 所在的 rank。（默认中序序列）
   * 其他需要补充的：
   * contains，size, max, min，iterable<Key> keys(),
   * height(), levelOrder：广度优先的遍历
   * 辅助方法 ：
   * 1、check，检查 bst 的完整性，正确性
   * 2、isBST，检查 bst 的key 的先后顺序
   * 3、isSizeConsistent，检查 bst 的节点的 N 所代表的当前节点的子节点个数
   * 4、isRankConsistent，检查 bst 每个节点的的顺序性，调用 rank 检查
   */

  private class Node {
    private Key key;
    private Value value;
    private Node left;
    private Node right;
    private int size; // 子节点个数+1（本身）

    public Node(Key key, Value value, int size) {
      this.key = key;
      this.value = value;
      this.size = size;
    }

  }


  private Node root;

  public boolean isEmpty() {
    return size() == 0;
  }

  public int size() {
    return size(root);
  }

  public int size(Node node) {
    return node == null ? 0 : node.size;
  }

  // 二叉查找树的排序和查找实现
  public Value get(Key key) {
    return get(root, key);
  }

  private Value get(Node node, Key key) {
    if (node == null || key == null) {
      return null;
    }
    int cmp = key.compareTo(node.key);
    if (cmp == 0) {
      return node.value;
    } else if (cmp < 0) {
      return get(node.left, key);
    } else {
      return get(node.right, key);
    }
  }

  public void put(Key key, Value value) {
    if (root == null) {
      root = new Node(key, value, 1);
    } else {
      put(root, key, value);
    }
  }

  private Node put(Node node, Key key, Value value) {
    // Node pNode = root;
    if (node == null) {
      return new Node(key, value, 1);
    }
    int cmp = key.compareTo(node.key);
    if (cmp == 0) {
      node.value = value;
    } else if (cmp < 0) {
      node.left = put(node.left, key, value);
    } else {
      node.right = put(node.right, key, value);
    }
    // 递归向上更新更新 size 的值。在递归后面的语句，都是像栈一样，节点由下向上更新
    node.size = size(node.left) + size(node.right) + 1;
    return node;
  }

  public Key floor(Key key) {
    Node x = floor(root, key);
    if (x == null) {
      return null;
    }
    return x.key;
  }

  /**
   * 如果给定的 key 小于根节点的键，那么小于等于 key 的最大键 floor(key) 一定在根节点的左子树中；
   * 如果给定的 key 大于根节点的键，那么只有当右子树中存在小于等于 key 的节点时，小于等于 key 的最大键才会出现在右子树中
   * 否则根节点就是小于等于 key 的最大键
   *
   * @param node 节点
   * @param key  要查找的键
   * @return
   */
  private Node floor(Node node, Key key) {
    if (node == null) {
      return null;
    }
    int cmp = key.compareTo(node.key);
    if (cmp == 0) {
      return node;
    }
    /*
    // 这段代码逻辑是我写的
    Node t = null;

    if (cmp < 0) {
      t = floor(node.left, key);
    }
    if (t == null) {
      t = floor(node.right, key);
    }*/
    if (cmp < 0) {
      return floor(node.left, key);
    }
    Node t = floor(node.right, key);
    if (t != null) {
      return t;
    }
    return node;
  }

  public Key min() {
    return min(root).key;
  }

  private Node min(Node node) {
    if (node.left == null) {
      return node;
    }
    return min(node.left);
  }

  public Key max() {
    return max(root).key;
  }

  private Node max(Node node) {
    if (node.right == null) {
      return node;
    }
    return max(node.right);
  }

  /**
   * select 和 rank
   * 假设我们想查找排名为 k 的键，如果左子树的节点数 大于 k，则我们在左子树中递归第查找排名为 k 的键。
   * 如果 t == k，则返回根节点的键
   * 如果 t < k, 我们就递归地在右子树中查找排名为 K-t-1 的键。
   * <p>
   * rank() 方法是排名的意思，给定一个 key，返回该 key 在二叉搜索树中的排名，
   * 同 select 方法一样，将 key 与根节点的键比较，如果小于根节点的键，则在左子树中进行查找，
   * 否则我们会返回左子树的节点数 t + 1 再加上 该 key 在右子树中的排名
   */

  public int rank(Key key) {
    if (key == null) {
      return -1;
    }
    return rank(root, key);
  }

  /**
   * 返回节点根据 key 的排名，rank 的排名，这里是从 0 开始。获取的是以节点 node 为根节点的子树中小于 node.key 的节点数量
   *
   * @param node
   * @param key
   * @return
   */
  private int rank(Node node, Key key) {
    if (node == null) {
      return 0;
    }
    int cmp = key.compareTo(node.key);
    if (cmp < 0) {
      return rank(node.left, key);
    } else if (cmp > 0) {
      // 如果在右子树，则返回 t+1 + 节点在右子树中的排名
      return size(node.left) + 1 + rank(node.right, key);
    } else {
      return size(node.left);
    }
  }

  /**
   * 删除最大键和最小键
   * 二叉查找树中最难实现的方法就是 delete() 方法，即从符号表中删除一个键值对。作为热身运动，我们先考虑 deleteMin() 方法，
   * deleteMin 方法，我们需要不断地深入根节点的左子树直到遇到一个左子树为空的，然后将指向该节点的连接指向该节点的右子树。此时没有任何连接指向该节点
   * 则该节点会被垃圾回收。deleteMax 跟 deleteMin 类似。
   * 删除操作，
   * 我们可以使用类似的方式删除任意一个子节点（或者没有子节点）的点，但是应该怎么样删除一个拥有两个子节点的节点那？删除之后，我们要怎么处理两棵子树
   * 但是被删除的节点的父节点只有一条空出来的连接。T.Hibbard 在 1962 年提出来了解决这个难题的第一个方法，再删除 x 后用它的后继节点填补它的位置
   * 因为 x 节点有一个右子节点，因此它的后继节点就是其右子树中的最小节点。这样的替换仍然能够保证树的有序性。我们将用 4 个简单的步骤
   * 来描述这个删除的过程。该过程见图 《二叉搜索树的删除.jpg》
   */
  public void deleteMin() {
    root = deleteMin(root);
  }

  private Node deleteMin(Node node) {
    if (node == null) {
      return null;
    }
    if (isEmpty()) {
      throw new NoSuchElementException("current bst tree is empty。");
    }
    // 213-216 行代码还是比较玄的，实现了：我们需要不断地深入根节点的左子树直到遇到一个左子树为空的，然后将指向该节点的连接指向该节点的右子树。
    if (node.left == null) {
      return node.right;
    }
    node.left = deleteMin(node.left);
    node.size = size(node.left) + size(node.right) + 1;
    // todo :check
    return node;
  }

  /**
   * 删除一个给定的节点 x，利用 T.Hibbard 的理论，主要分为 4 步
   * 1、将被删除的节点标记为 t
   * 2、将 x 指向它的后继节点 min(t.right)
   * 3、将 x 的右节点(原本指向一颗所有的节点都大于x.key 的二叉查找树)指向 deleteMin(t.right)，也就是在删除最小节点后仍然都大于 x.key 的
   * 子二叉查找树
   * 4、将 x 的左连接（本为空）设置为 t.left , 完成对 t 的替换
   */
  public void delete(Key key) {
    delete(root, key);
  }

  private Node delete(Node node, Key key) {
    if (node == null) {
      return null;
    }
    if (key == null) {
      System.out.println("delete node ,key is null");
      return null;
    }
    int cmp = key.compareTo(node.key);
    if (cmp < 0) {
      node.left = delete(node.left, key);
    } else if (cmp > 0) {
      node.right = delete(node.right, key);
    } else {
      // 两种边界情况
      if (node.right == null) {
        return node.left;
      }
      if (node.left == null) {
        return node.right;
      }
      Node t = node;
      node = min(t.right);
      node.right = deleteMin(t.right);
      node.left = t.left;
    }
    node.size = size(node.left) + size(node.right) + 1;
    return node;
    /**
     * 上述代码实现了 Hibbard 的二叉树中对节点的及时删除。delete() 方法的代码很简洁，但是不简单。理解它的最好办法是结合图片读懂
     * 上文中的文字描述的 4 步骤，尝试自己实现并比对自己的代码。一般情况下这段代码效率不错，但是对于大规模的应用可能有问题。
     * 这里记个个 todo: 需要参看算法 4 的联系 3.2.42
     */

    /**
     * 范围查找
     * 要实现能够放回给定范围内的 keys() 方法，我们可以通过参考中序遍历的方式
     */
  }

  public Iterable<Key> keys() {
    return keys(min(), max());
  }

  private Iterable<Key> keys(Key min, Key max) {
    Queue<Key> queue = new Queue<>();
    keys(root, queue, min, max);
    return queue;
  }

  private void keys(Node node, Queue<Key> queue, Key min, Key max) {
    if (node == null) {
      return;
    }
    int cmplo = min.compareTo(node.key);
    int cmphi = max.compareTo(node.key);
    // 下面根据中序遍历的思想把 处于 min 和 max 之间的元素加入队列
    if (cmplo < 0) {
      keys(node.left, queue, min, max);
    }
    if (cmplo <= 0 && cmphi >= 0) {
      queue.enqueue(node.key);
    }
    if (cmphi > 0) { // 中序遍历右节点，如果父节点此时已经 不满足 [min,max] 的范围了 cmphi <=0 ，那么没有必要再去遍历右节点
      keys(node.right, queue, min, max);
    }
  }

}
