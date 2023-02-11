package com.swj.ics.dataStructure.tree.btree;

import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedList;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/02/10 10:43
 * B 树的实现。主要是 4 个核心 API，增删改查
 * 增：insertNode
 * 删：deleteNode
 * 改：putNode
 * 查：search
 * 参数均为 Entry<K,V>
 */
public class BTree<K, V> {

  /**
   * 在开始之前，这里先讲述下 B 树的度和阶的概念区别
   * 度的说法是算法导论的一种叫法
   * 阶的说法是维基百科的一种叫法
   * 度数：(degree)在树中，每个节点的子节点（子树）的个数就成为该节点的度，一般用 t 表示
   * 阶数：(order) 阶定义为一个节点的最大子节点数，一般用 m 表示
   * 他们的关系如下：
   * t = Math.ceil(m/2) 向上取整
   * m = 2t
   * 假定一棵 B 树的最小度为 3 ，那么节点（根节点除外）的关键字数在 2~5，孩子数最大为 6。
   */

  // 默认的度为 2，不传入默认 2-3 树
  private static final int DEFAULT_BTREE_DEGREE = 2;
  private static final String SPLITTER_LINE = "----------------";
  private BTNode<K, V> rootNode;
  private int t = DEFAULT_BTREE_DEGREE;
  private int minKeySize = t - 1;
  private int maxKeySize = (2 * t) - 1;

  public BTree() {
    rootNode = new BTNode<>();
  }

  public BTree(int degree) {
    assert degree >= 2;
    t = degree;
    minKeySize = t - 1;
    maxKeySize = 2 * t - 1;
    rootNode = new BTNode<>();
  }

  //B 树按照给定的排序方式排序
  public BTree(int degree, Comparator<K> kComparator) {
    assert degree >= 2;
    t = degree;
    minKeySize = t - 1;
    maxKeySize = (2 * t) - 1;
    rootNode = new BTNode<>(kComparator);
  }

  /**
   * B 树的查询
   *
   * @param key 查询关键字
   * @return 要查询的 value
   */
  public V search(K key) {
    return null;
  }
  /**
   * B 树的插入
   * 思路分析：
   * 1、首先分析根节点是否满足分裂，如果满足分裂，将当前根节点分裂生成一个新的根节点
   * 2、沿着根节点的路径向下查找，知道找到对应的叶子节点
   * 3、如果当前查找路径上的某个节点已经是满阶，关键字个数为 2t-1，则需要对当前这个节点进行分裂，一分为二成两个节点，这样做其实是插入时就应该分裂的延后处理
   *   3.1 判断分裂后的两个节点，关键字应该插入分裂出来的左子节点还是右子节点
   * 4、最终找到需要插入的叶子节点，将待插入的 key-value 插入叶子节点
   * （关于分裂的方式，当然还有其他实现方式，比如每次插入后都检查下当前节点是否需要分裂，沿着搜素路径回溯向上融合）
   */


  /**
   * 节点分裂：
   * 中间节点向上融合，当前节点作为左子节点，新节点作为右子节点
   *
   * @param parent      当前要分裂节点的父节点
   * @param currentNode 当前节点
   * @param index       当前节点在父节点中的索引位置
   */
  private void splitNode(BTNode<K, V> parent, BTNode<K, V> currentNode, int index) {
    // 根据节点满之后才能分裂的需求，mid 也可以等于 t-1。
    int mid = (currentNode.entrySize() - 1) / 2;
    Entry<K, V> midEntry = currentNode.entryAt(mid);
    BTNode<K, V> newNode = new BTNode<>();
    //如果当前要分裂的节点不是叶子节点，则新节点也不是叶子结点
    newNode.setLeaf(currentNode.isLeaf());
    // 从后向前将当前节点的关键字的后半部分迁移至新节点
    for (int i = currentNode.entrySize() - 1; i > mid; i--) {
      Entry<K, V> entry = currentNode.removeEntry(i);
      newNode.insertEntry(entry, 0);
    }

    // 如果当前节点有子节点，则子节点一并移走
    if (!currentNode.isLeaf()) {
      // 仍然是从后向前迁移子节点
      for (int i = currentNode.nodeSize() - 1; i > mid; i--) {
        BTNode<K, V> childNode = currentNode.removeChild(i);
        newNode.insertChild(childNode, 0);
      }
    }

    // 中间关键字向上向父节点融合
    currentNode.removeEntry(mid);
    parent.insertEntry(midEntry);
    // 父节点中在当前节点的后面 1 位插入新的节点
    parent.insertChild(newNode, index + 1);
    newNode.setParent(parent);
  }

  public void insertNode(K key, V val) {
    insertNode(new Entry<>(key, val));
  }

  private void insertNode(Entry<K, V> newEntry) {
    if (rootNode.entrySize() == maxKeySize) {
      splitRoot();
    }
    insertNotFull(rootNode, newEntry);
  }

  private void splitRoot() {
    if (rootNode.entrySize() == maxKeySize) {
      BTNode<K, V> newRoot = new BTNode<>();
      newRoot.setLeaf(false);
      // 根节点初始化 2 个child，一左一右，一个关键字，左边是 老的 root，右边是从老的 root 分裂出来的新节点。
      // 关键字是在老的 root 分裂中向上融合上升上去的。
      newRoot.insertChild(rootNode, 0);
      rootNode.setParent(newRoot);
      splitNode(newRoot, rootNode, 0);
      this.rootNode = newRoot;
    }
  }

  private boolean insertNotFull(BTNode<K, V> root, Entry<K, V> newEntry) {
    SearchResult<V> searchResult = root.search(newEntry);
    if (searchResult.isExist) {
      System.out.println(
          String.format("the key of entry exists in the tree. ignore insert. [key='%s']", newEntry.getKey()));
      return false;
    }
    int index = searchResult.index;
    if (root.isLeaf()) {
      root.insertEntry(newEntry, index);
      return true;
    }

    BTNode<K, V> searchNode = root.childAt(index);

    // 递归判断是否落在叶子节点上
    boolean success = insertNotFull(searchNode, newEntry);

    // 如果当前节点的 个数已满，则进行分裂
    while (searchNode.entrySize() >= maxKeySize) {
      if (searchNode == rootNode) {
        splitRoot();
        break;
      }
      // 普通节点的分裂
      searchResult = root.search(newEntry);
      splitNode(root, searchNode, searchResult.index);
      searchNode = root;
      root = root.getParent();
    }
    return success;
  }

  /**
   * 接下来开始开发最复杂的删除节点。删除节点有讲究
   * 目前来说，删除操作从大的逻辑上讲主要是分为查找被删除的关键字是否存在
   * 1. 关键字的节点存在
   * 1.1 关键字所在的节点是叶子节点且叶子节点比较富裕，则直接删除
   * 1.2 关键字所在的节点是非叶子节点
   * 1.2.1 如果关键字所在节点的左右子节点比较富裕(或者肥胖 关键字数 > t-1) , 则需要把左右节点跟关键字所在的节点交换关键字，将关键字交换下来到叶子节点，来进行删除，比如 文档中的删除 T
   * 1.2.2 如果关键字所在的左右子节点都比较贫穷(关键字数 <= t-1 ) 那么将关键字和 左右子节点进行合并 参考 《B树.md》删除最后一步，删除 E，需要合并根节点和左右子节点
   * 2. 关键字的节点不存在
   * 2.1 如果当前节点是在叶子节点上，则认为没有查到关键字，可以抛出运行时异常
   * 2.2 如果当前节点是在非叶子结点上
   * 2.2.1 如果当前节点的关键字数数相对富裕，则继续递归其当前节点
   * 2.2.2 如果当前节点的关键字数目比较贫穷，不能直接递归删除，如果当前节点的左右兄弟节点叶子节点比较富裕，需要左旋或者右旋补充当前节点的关键字数，比如文档中的删除第 3 步，删除 R，需要左旋借到父节点的 w，父节点借到 又子节点的x
   * 2.2.2.1 左旋或者右旋，都是通过向当前节点的左右兄弟节点借关键字，借的过程主要是通过左旋或者右旋，也即是 兄弟节点上去，父节点关键字下来
   * 2.2.2.2 补充完节点后，则继续对当前节点递归删除关键字
   * 2.2.3 如果左右左右兄弟都比较贫穷，无法补充当前节点关键字，则把左右兄弟和父节点尝试合并，优先尝试合并右边和节点，无右边兄弟的，则尝试合并左边和父节点，比如文档的删除第 4 步，
   * 2.2.3.1 合并完成之后，继续进行递归删除,在逻辑执行中，如果发现当前父节点是根节点，那说明已经开始了根节点的合并，则把当前根节点指定为 合并后的节点
   */

  public Entry<K, V> deleteNode(BTNode<K, V> root, Entry<K, V> targetEntry) {
    if (targetEntry == null) {
      return null;
    }
    SearchResult<V> searchResult = root.search(targetEntry);
    if (searchResult.isExist) {
      // 找到且在叶子节点上
      if (root.isLeaf()) {
        // todo : 目标关键子被转移到到子节点上，但是原始的子节点并没有被删除，debug 下，不过不影响打印结果
        return root.removeEntry(searchResult.index);
      } else {
        // 找到且在非叶子节点上
        BTNode<K, V> leftChild = root.childAt(searchResult.index);
        if (leftChild.entrySize() >= t) {
          // 将父节点的关键字和左子节点的关键字进行互换
          Entry<K, V> deletedEntry = root.removeEntry(searchResult.index);
          root.insertEntry(leftChild.entryAt(leftChild.entrySize() - 1), searchResult.index);
          // 左子节点的最后插入父节点的关键字
          leftChild.removeEntry(leftChild.entrySize() - 1);
          // leftChild.insertEntry(deletedEntry, leftChild.entrySize());
          // 这里 deleteEntry 和 targetEntry 的 key 是相等的；
          // 另外 leftChild.insertEntry(targetEntry, leftChild.entrySize()) 和 insertEntry(targetEntry) 的效果应该是一样的
          // leftChild.insertEntry(targetEntry, leftChild.entrySize());
          leftChild.insertEntry(targetEntry);
          // 递归地将目标节点往叶子节点下沉，最终在叶子结点进行删除。
          return deleteNode(leftChild, targetEntry);
        }
        BTNode<K, V> rightChild = root.childAt(searchResult.index + 1);
        if (rightChild.entrySize() >= t) {
          //参照 文档中的删除第 2 步的 删除 T 关键字，T 和 W 进行互换，然后 T 在右子节点中被删除
          root.removeEntry(searchResult.index);
          root.insertEntry(rightChild.entryAt(0), searchResult.index);
          rightChild.removeEntry(0);
          rightChild.insertEntry(targetEntry, 0);
          return deleteNode(rightChild, targetEntry);
        }
        // 左右子节点都不够富裕，则 合并左右子节点和根节点，然后进行删除
        // 合并的原则是，将跟节点，右子节点都合并到左子节点上，
        Entry<K, V> deletedEntry = root.removeEntry(searchResult.index);
        // 减少父节点的节点数目
        root.removeChild(searchResult.index + 1);
        // 有序的时候，不需要传入 插入的位置，方法自己查找插入的位置
        leftChild.insertEntry(deletedEntry);
        for (int i = 0; i < rightChild.entrySize(); i++) {
          leftChild.insertEntry(rightChild.entryAt(i));
        }
        // 如果右子节点非叶子节点，则需要把右子节点的子节点也要转移过来
        if (!rightChild.isLeaf()) {
          for (int j = 0; j < rightChild.nodeSize(); j++) {
            leftChild.insertChild(rightChild.childAt(j), rightChild.nodeSize());
          }
        }
        return deleteNode(leftChild, targetEntry);
      }
    } else {// 没有找到目标节点
      if (root.isLeaf()) {
        for (int i = 0; i < root.entrySize(); i++) {
          System.out.print(SPLITTER_LINE);
          System.out.print(root.entryAt(i));
          System.out.print(SPLITTER_LINE);
        }
        throw new RuntimeException(
            String.format("target entry can not be found in the tree. [key='%s']", targetEntry.getKey()));
      }
      //
      BTNode<K, V> searchNode = root.childAt(searchResult.index);
      // 如果当前节点的度/关键字数比较富裕，则可以继续递归删除
      if (searchNode.entrySize() >= t) {
        return deleteNode(searchNode, targetEntry);
      }
      // 如果当前节点的关键字数比较贫穷，则需要从左右兄弟节点借点关键字过来。通过左旋或者右旋
      BTNode<K, V> siblingNode = null;
      int siblingIndex = -1;
      if (searchResult.index < root.entrySize() - 1) {
        BTNode<K, V> rightBrother = root.childAt(searchResult.index + 1);
        if (rightBrother.entrySize() >= t) {
          siblingNode = rightBrother;
          siblingIndex = searchResult.index + 1;
        }
      } else if (searchResult.index > 0) {
        BTNode<K, V> leftBrother = root.childAt(searchResult.index - 1);
        if (leftBrother.entrySize() >= t) {
          siblingNode = leftBrother;
          siblingIndex = searchResult.index - 1;
        }
      }
      // 找到可借关键字的左右兄弟节点,则通过左旋或者右旋，增加当前节点的关键字数
      if (siblingNode != null) {
        if (siblingIndex > searchResult.index) {
          // 右兄弟节点，则需要进行左旋， 可参照文档中的删除步骤第 3 步。
          Entry<K, V> deleteEntry = root.removeEntry(searchResult.index);
          root.insertEntry(siblingNode.entryAt(0), searchResult.index);
          siblingNode.removeEntry(0);
          searchNode.insertEntry(deleteEntry, searchNode.entrySize() - 1);
          if (!siblingNode.isLeaf()) {
            // 这里使用 root.insertChild 是错误的，因为 root 的 entry size 没有发生变化，所以 child size 也不能发生变化
            // root.insertChild(siblingNode.childAt(0), root.nodeSize());
            // entry size 发生变化的是 searchNode ，因为 searchNode 借到了关键字，因此子节点也要跟着去到 searchNode
            searchNode.insertChild(siblingNode.childAt(0), searchNode.nodeSize());
            siblingNode.removeChild(0);
          }
        } else { // 是左兄弟，左节点的最后一项需要进行右旋, 此时 siblingIndex = searchIndex -1,
          Entry<K, V> deleteEntry = root.removeEntry(siblingIndex);
          root.insertEntry(siblingNode.entryAt(siblingNode.entrySize() - 1), siblingIndex);
          siblingNode.removeEntry(siblingNode.entrySize() - 1);
          searchNode.insertEntry(deleteEntry, 0);
          if (!siblingNode.isLeaf()) {
            searchNode.insertChild(siblingNode.childAt(siblingNode.nodeSize() - 1), 0);
            siblingNode.removeChild(siblingNode.childAt(siblingNode.nodeSize() - 1));
          }
        }
        return deleteNode(searchNode, targetEntry);
      }
      //左右兄弟节点都比较贫穷，则将当前节点和左右节点合并，这里可能跟文档上面的不一样，比如文档删除的最后一步，是 AC ，D，EF 进行合并
      // 这里先尝试合并右兄弟，如果没有右兄弟才尝试合并左兄弟，因此这里的可能会被合并的节点是，EF，G，KL，最终 E 被删除 剩余节点 FGKL
      if (searchResult.index < root.entrySize()) {
        BTNode<K, V> rightChild = root.childAt(searchResult.index + 1);
        searchNode.insertEntry(root.entryAt(searchResult.index), searchNode.entrySize());
        root.removeEntry(searchResult.index);
        // 减少子节点树，此时该子节点由 rightChild 所引用
        root.removeChild(searchResult.index + 1);
        for (int i = 0; i < rightChild.entrySize(); i++) {
          searchNode.insertEntry(rightChild.entryAt(i));
        }
        if (!rightChild.isLeaf()) {
          for (int j = 0; j < rightChild.nodeSize(); j++) {
            searchNode.insertChild(rightChild.childAt(j), rightChild.nodeSize());
          }
        }
        if (root == this.rootNode) {
          this.rootNode = searchNode;
        }
      } else if (searchResult.index > 0) {
        // 尝试合并左兄弟节点,从右往左合并。
        BTNode<K, V> leftChild = root.childAt(searchResult.index - 1);
        // left child 是左边前一个，entry 也相应的是左边前一个
        // 对照文档删除的最后一步，要删除 E，此时的 root 是 DG，此时的 searchNode 是 EF，searResult.index=1, 因此应该被合并的父节点关键字是 D
        searchNode.insertEntry(root.entryAt(searchResult.index - 1), 0);
        root.removeEntry(searchResult.index - 1);
        // 同样删除子节点，由 leftChild 引用
        root.removeChild(searchResult.index - 1);
        for (int i = 0; i < leftChild.entrySize(); i++) {
          searchNode.insertEntry(leftChild.entryAt(i));
        }
        // 将子节点倒着每次插入 新节点的 0 位，保证了新节点有序性
        for (int j = leftChild.nodeSize(); j >= 0; j--) {
          searchNode.insertChild(leftChild.childAt(j), 0);
        }
        if (root == this.rootNode) {
          this.rootNode = searchNode;
        }
      }
      return deleteNode(searchNode, targetEntry);
    }
  }
  //todo: putEntry 更改节点。

  // todo: 广度优先遍历
  public void printBTree() {
    /**
     * 要实现广度优先遍历，这里使用队列来实现
     */
    Deque<BTNode<K, V>> queue = new LinkedList<>();
    queue.offer(rootNode);
    while (!queue.isEmpty()) {
      BTNode<K, V> btNode = queue.poll();
      if (btNode != null) {
        for (int i = 0; i < btNode.entrySize(); i++) {
          System.out.print(btNode.entryAt(i).toShortString() + "(" + btNode.nodeSize() + ")\t");
        }
        System.out.println();
        if (!btNode.isLeaf()) {
          for (int j = 0; j < btNode.nodeSize(); j++) {
            queue.offer(btNode.childAt(j));
          }
        }
      }
    }
  }

  public static void main(String[] args) {
    String inputString = "CNGAHEKQMFWLTZDPRXYS";
    inputString += "VBIJ";
    char[] arr = inputString.toCharArray();
    BTree<Character, Character> tree = new BTree<>(3);
    for (char ch : arr) {
      tree.insertNode(ch, ch);
    }
    tree.printBTree();
  }
}
