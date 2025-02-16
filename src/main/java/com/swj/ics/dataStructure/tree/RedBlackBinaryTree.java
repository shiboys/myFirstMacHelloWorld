package com.swj.ics.dataStructure.tree;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/01/25 14:55
 * 红黑二叉树重刷。
 * 红黑树的 5 个特性
 * 1、每个节点非红即黑
 * 2、根节点为黑色
 * 3、每个叶子节点黑色，叶子结点为 nil 节点，即空节点
 * 4、如果一个节点为红色，那么它的两个子节点一定是黑色（null 节点被视为黑色）
 * 5、从一个节点到子孙结点的所有路径包含相同数量的黑色节点(这里面包含 null 节点)
 */
public class RedBlackBinaryTree<Key extends Comparable<Key>, Value> {
  private RBNode root;


  public static enum Color {
    RED,
    BLACK;
  }

  public RBNode getRoot() {
    return root;
  }

  private class RBNode {
    private RBNode left;
    private RBNode right;
    private RBNode parent;
    private Key key;
    private Value value;
    private Color color = Color.RED;

    public RBNode(Key key, Value value) {
      this.key = key;
      this.value = value;
    }
  }

  public void insertNode(Key key, Value value) {
    RBNode newNode = new RBNode(key, value);
    if (root == null) {
      root = newNode;
      return;
    }
    RBNode parent = null;
    RBNode node = root;
    int cmp;
    while (node != null) {
      cmp = node.key.compareTo(key);
      if (cmp == 0) {
        node.value = value;
        return;
      }
      parent = node;
      if (cmp < 0) {
        node = node.right; // 节点的 key  < 当前key，需要往右子节点查找
      } else {
        node = node.left;
      }
    }

    cmp = parent.key.compareTo(key);
    if (cmp > 0) {
      parent.left = newNode; // key 比父节点小，则为父节点的左子节点
    } else {
      parent.right = newNode;
    }
    newNode.parent = parent;
    insertFix(newNode);
  }


  private void insertFix(RBNode newNode) {
    this.root.color = Color.BLACK;
    if (newNode == null) {
      return;
    }
    /**
     * 旋转逻辑：
     * 新增节点为红色
     * 如果父节点为黑色，则不需要旋转
     * 如果父节点为红色，则需要分情况处理
     *    如果叔父节点为红色，则将 parent, uncle 改为黑色，将 grandparent 改为红色，然后再将 grandparent 作为当前节点，进行递归着色
     *    如果叔父节点为黑色，则需要分为内侧插入还是外侧插入；如果是外侧插入
     *      外侧插入分为左左-LL 和 右右 RR 插入，如果是 LL 双红(父节点和当前节点都是左子节点) 则需要进行右旋一次，然后再进行着色，父节点染色为黑色，祖父节点为红色
     *      外侧插入的 RR 双红（父节点和当前节点都是右子节点），则需要进行左旋一次，父节点染色为黑色，祖父节点为红色，以祖父节点为起始点进行左旋
     *    如果是内侧插入，则分为 LR(父节点为左子节点，当前节点为右节点) 和 RL（父节点为右子节点， 当前节点为左子节点）
     *    如果是 LR 则需要先 以当前节点的父节点为起始点，先进行左旋,变成 LL，然后再进行右旋。然后再以祖父节点为起始点，进行递归
     *    如果是 RL 则需要以当前节点的父节点为起始节点，进行先右旋（变成 RR），然后再进行左旋，然后再以祖父节点为起始点，进行递归
     */
    RBNode parent = newNode.parent;
    if (parent == null) {
      return;
    }
    if (parent.color == Color.BLACK) {
      // 父节点为黑色
      return;
    }
    RBNode gp = parent.parent;
    RBNode uncle = null;
    if (gp != null) {
      if (parent == gp.left) {
        uncle = gp.right;
      } else {
        uncle = gp.left;
      }
    }
    // 父节点和叔父节点均为红色
    if (uncle != null && uncle.color == Color.RED) {
      uncle.color = Color.BLACK;
      parent.color = Color.BLACK;
      gp.color = Color.RED;
      insertFix(gp);
    } else {
      // 叔父节点为黑色. uncle == null 视为黑色
      if (newNode == parent.left) {
        // ll 型
        if (parent == gp.left) {
          rotateRight(gp);
          // 再变色
          parent.color = Color.BLACK;
          gp.color = Color.RED; // todo：看了 jdk ConcurrentHashMap 的红黑树，发现这里可能有个 bug ，通过调试发现这不是个问题，
          // 上述两个变色语句，可以正确地把节点颜色变过来，即使gp原理是根节点，经过旋转以后，是根节点的子节点，parent 变成跟节点
        } else { // rl 型 父节点是祖父节点的 right，当前节点是父节点的 left ，因此为 rl 型
          rotateRight(parent); // 先右旋变成 rr，然后再递归左旋
          insertFix(parent);// 经过右旋，parent 节点现在变成右子节点，然后变成了 parent 是我们新插入的节点，因此这里从 parent 节点开始递归
        }
      } else if (newNode == parent.right) {
        // rr 型
        if (parent == gp.right) {
          rotateLeft(gp);
          // 再变色
          parent.color = Color.BLACK;
          gp.color = Color.RED;
        } else { // 父节点是祖父节点的 left，当前节点是父节点的 right ，因此为 lr 型
          // 先左旋变成 ll
          rotateLeft(parent);
          // 左旋之后，parent 变成左子节点，
          // 使用 左子节点 的父节点当做新节点，进行递归
          // 然后在递归进行右旋
          insertFix(parent);
        }
      }
    }

  }

  /**
   * 以 x 节点为中心进行左旋。左旋一般发生在右子树上
   * p                p
   * |                |
   * x                y
   * / \    左旋      /\
   * lx  y  ----->   x  ry
   *    / \         / \
   *    ly  ry     lx  ly
   *
   * @param x ,以 x 节点为起始点，开始进行旋转。
   */
  private void rotateLeft(RBNode x) {
    /**
     * 左旋的基本操作分为 3 个
     * 1、x.right = ly,  if(ly != null) then ly.parent = x
     * 2、y.left = x, x.parent =y
     * 3、y.parent = p ; if(p.left==x)  p.left=y else p.right = y
     */
    RBNode p = x.parent;
    RBNode y = x.right;
    //1.x.right = ly, ly.parent = x
    RBNode ly = y.left;
    x.right = ly;
    if (ly != null) {
      ly.parent = x;
    }
    //2. y.left = x, x.parent =y
    y.left = x;
    x.parent = y;
    //3. y.parent = p ; 3、if(p.left==x)  p.left=y else p.right = y
    y.parent = p;
    if (p != null) {
      if (p.left == x) {
        p.left = y;
      } else {
        p.right = y;
      }
    } else { // y 就是新的根节点
      this.root = y;
      this.root.parent = null;
    }
  }

  /**
   * 右旋。左边连续两个红色节点，需要右旋来维持平衡。
   * p                   p
   * |     右旋转         |
   * y   ---------->     x
   * / \                 / \
   * x  ry               lx  y
   * / \                     / \
   * lx rx                  rx  ry
   */
  private void rotateRight(RBNode y) {
    /**
     * 右旋转也是主要有 3 步：
     * 1、y.left = rx; if(rx != null) then rx.parent = y;
     * 2、x.right=y; y.parent =x;
     * 3、x.parent=p; if(p.left==y) p.left=x ; else p.right = x
     */
    RBNode p = y.parent;
    RBNode x = y.left;
    // 1、y.left = rx; if(rx != null) then rx.parent = y;
    y.left = x.right;
    if (x.right != null) {
      x.right.parent = y;
    }

    //2、x.right=y; y.parent =x;
    x.right = y;
    y.parent = x;
    //x.parent=p; if(p.left==y) p.left=x ; else p.right = x
    x.parent = p;
    if (p != null) {
      if (p.left == y) {
        p.left = x;
      } else {
        p.right = x;
      }
    } else {
      this.root = x;
      this.root.parent = null;
    }
  }

  public static void main(String[] args) {
    RedBlackBinaryTree<Integer, Integer> redBlackTree = new RedBlackBinaryTree<>();
    // 12-1-9-2-0-11-7-19
    redBlackTree.insertNode(12, 12);
    redBlackTree.insertNode(1, 1);
    redBlackTree.insertNode(9, 9);
    redBlackTree.insertNode(2, 2);
    redBlackTree.insertNode(0, 0);
    redBlackTree.insertNode(11, 11);
    redBlackTree.insertNode(7, 7);
    redBlackTree.insertNode(19, 19);
    redBlackTree.preOrderVisit(redBlackTree);
  }

  void preOrderVisit(RedBlackBinaryTree<Integer, Integer> redBlackTree) {
    doPreOrderVisit((RBNode) redBlackTree.getRoot());
  }

  void doPreOrderVisit(RBNode p) {
    if (p == null) {
      return;
    }
    doPreOrderVisit(p.left);
    System.out.print(p.key + " ");
    doPreOrderVisit(p.right);
  }

}
