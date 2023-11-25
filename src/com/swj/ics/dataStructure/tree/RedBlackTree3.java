package com.swj.ics.dataStructure.tree;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/11/21 20:30
 * 红黑树 3 刷
 */
public class RedBlackTree3<K extends Comparable<K>, V> {

  /**
   * 红黑树入门之前，请再复述一下红黑树的 5 大特点
   * 1、所有节点非红即黑
   * 2、根节点为黑色
   * 3、所有叶子节点为黑色(指的是所有 nil 的节点)
   * 4、如果父节点为红色，那么它的两个子节点必定是黑色的，子节点也包括 nil 节点
   * 5、一个节点到它所有的叶子节点所经过的黑色节点的个数相同
   */

  static class Node<K, V> {
    public Node<K, V> left;
    public Node<K, V> right;
    public Node<K, V> parent;

    public K key;
    public V val;
    public Color color = Color.RED;

    public Node(K k, V v) {
      this.key = k;
      this.val = v;
    }


  }


  enum Color {
    RED,
    BLACK
  }


  private Node<K, V> root;

  public void insertNode(K k, V val) {
    if (k == null) {
      throw new IllegalArgumentException("k is null");
    }
    if (root == null) {
      root = new Node<>(k, val);
      root.color = Color.BLACK;
      return;
    }
    Node<K, V> p = root;
    Node<K, V> parent = null;
    int cmp = 0;
    while (p != null) {
      cmp = p.key.compareTo(k);
      if (cmp == 0) {// 找到当前节点,直接更新并返回
        p.val = val;
        return;
      }
      parent = p;
      if (cmp > 0) { // 当前节点的值大于 k,去左子树寻找
        p = p.left;
      } else { // 当前节点的值 小于 k, 则去右子树查找
        p = p.right;
      }
    }
    // 到这里，p 节点即使被插入的节点
    p = new Node<>(k, val);
    p.parent = parent;
    cmp = parent.key.compareTo(k);
    if (cmp > 0) {
      parent.left = p;
    } else {
      parent.right = p;
    }
    // 维护树的平衡
    balanceTree(p);
  }

  private void balanceTree(Node<K, V> newNode) {
    if (newNode == null || newNode.parent == null) {
      return;
    }
    /**
     *  维护树的平衡，需要做到一下 4 点：
     * 1、如果当前节点的父节点是黑色的，则已经是平衡的了。
     * 2、如果当前节点的父节点是红色的，要分 2 种情况套路
     * 3、情况一：如果叔父节点也是红色的，则将父节点和叔父节点都变成黑色，祖父节点为红色，再以祖父节点为起点，递归着色。
     * 4、情况二：如果叔父节点是黑色的，则当前节点和父节点都是红色的，则要需要分情况
     *  4.1 如果是 RR 情况，也就是父节点是祖父节点的右子节点，当前节点是父节点的右子节点，则以父节点为旋转点进行左旋，并重新着色，着色的规则是父节点变成黑色，祖父节点变成红色
     *  4.2 如果是 RL 情况，则需要先进行右旋，变成 RR， 再对 RR 进行左旋，并重新着色
     *  4.3 同理，如果是 LL 情况，则进行右旋，并重新着色
     *  4.4 如果是 LR，则先进行左旋变成 LL，然后进行右旋，并重新着色
     */
    // 根节点始终为黑色
    this.root.color = Color.BLACK;
    Node<K, V> parent = newNode.parent;
    Node<K, V> gp = parent.parent;
    // 父节点为黑色, 不需要平衡
    if (parent.color == Color.BLACK) {
      return;
    }
    //父节点红红色
    Node<K, V> uncle = null;
    if (gp != null) {
      if (parent == gp.left) {
        uncle = gp.right;
      } else {
        uncle = gp.left;
      }
    }
    // 父节点和叔父节点都是红色的
    if (uncle != null && uncle.color == Color.RED) {
      parent.color = Color.BLACK;
      uncle.color = Color.BLACK;
      gp.color = Color.RED;
      // 以祖父节点为新节点，开始进行重新递归平衡。
      balanceTree(gp);
    } else { // 叔父节点为黑色，也即是 nil 或者 黑色节点
      if (parent == gp.left) {
        if (newNode == parent.left) { // ll 形状，则以 gp 节点为旋转起始点进行右旋
          rotateRight(gp);
          // 旋转之后重新着色,此时的 parent 节点也就是下图中旋转后的 l 节点变为黑色
          parent.color = Color.BLACK;
          gp.color = Color.RED;// gp 节点也就是图中的p节点变成红色
        } else { // lr 形状，则先以 parent 为起始点进行左旋，变成 ll ，然后在 递归调用进行右旋并重新着色
          rotateLeft(parent);
          // 左旋之后，变成 ll, 以 parent 节点为参数，重新进行旋转，此时 parent 节点可以被认为是新插入的 newNode 节点
          balanceTree(parent);
        }
      } else { // parent == gp.right
        if (newNode == parent.right) { // rr 形状
          rotateLeft(gp);
          // 旋转后重新着色
          parent.color = Color.BLACK;
          gp.color = Color.RED;
        } else { // rl 形状，先旋转 parent 和 当前节点，以 parent 节点为旋转开始点，变成 rr ，然后再递归进行左旋
          rotateRight(parent);
          // 此时 parent 节点变成子节点
          balanceTree(parent);
        }
      }
    }
  }

  /**
   * 节点左旋
   *  gp             gp
   *  |              |
   *  p     左旋      r
   * / \    --->    / \
   * l   r          p   rr
   *    / \        / \
   *   rl  rr     l  rl
   *
   * @param pNode 左旋 3 大步骤
   *              1、p.right = rl; if(rl != null) rl.parent = p;
   *              2、p.parent=r; r.left = p;
   *              3、r.parent=gp; if(gp.left == p) gp.left = r; else gp.right = r;
   */

  private void rotateLeft(Node<K, V> pNode) {
    Node<K, V> gp = pNode.parent;
    Node<K, V> r = pNode.right;
    // 第一步
    pNode.right = r.left;
    if (r.left != null) {
      r.left.parent = pNode;
    }
    // 第二步骤
    pNode.parent = r;
    r.left = pNode;
    // 第三步
    if (gp != null) {
      r.parent = gp;
      if (gp.left == pNode) {
        gp.left = r;
      } else {
        gp.right = r;
      }
    } else { // gp 为 null，则 新的 root 节点为 r
      this.root = r;
      this.root.parent = null;// 这句话没有写，导致大 bug
    }
  }

  /**
   * 节点右旋
   *  gp             gp
   *  |              |
   *  p     右旋      l
   * / \    --->    / \
   * l   r         ll   p
   * / \                / \
   * ll  lr             lr  r
   *
   * @param pNode 右旋 3 大步：
   *              1、p.left = lr; if(lr != null) lr.right = p;
   *              2、 l.right = p ; p.parent =l
   *              3、 if(p == gp.left) gp.left = l  else gp.right = l; l.parent = gp;
   */
  private void rotateRight(Node<K, V> pNode) {
    Node<K, V> l = pNode.left;
    Node<K, V> gp = pNode.parent;
    //第一步
    pNode.left = l.right;
    if (l.right != null) {
      l.right.parent = pNode;
    }
    // 第二步
    l.right = pNode;
    pNode.parent = l;
    // 第三步
    if (gp != null) {
      l.parent = gp;
      if (gp.left == pNode) {
        gp.left = l;
      } else {
        gp.right = l;
      }
    } else { // gp 为 null，说明 parent 是 root，需要改为 l 为 root
      this.root = l;
      this.root.parent = null;// 这句话没有写，导致大 bug
    }
  }

  public void traverseMid() {
    traverseMid(root);
  }

  public void traverseMid(Node<K, V> rn) {
    if (rn == null) {
      return;
    }
    traverseMid(rn.left);
    System.out.print(rn.key + " ");
    traverseMid(rn.right);
  }

  private int maxDepth(Node<K, V> node) {
    if (node == null) {
      return 0;
    }
    // 采用分解问题的解法
    int leftMax = maxDepth(node.left);
    int rightMax = maxDepth(node.right);
    return Math.max(leftMax, rightMax) + 1;
  }

  private int maxDepth() {
    return maxDepth(root);
  }


  public static void main(String[] args) {
    // 往自己写的红黑树里面灌入 50 个数字，然后中序遍历，看看是否有序
    // 最后计算二叉树的最大的深度，看看是否是 log(50) = 6;
    Object obj = new Object();
    RedBlackTree3<Integer, Object> tree = new RedBlackTree3<>();
    int max = 50;
    for (int i = 1; i <= max; i++) {
      tree.insertNode(i, obj);
    }
    tree.traverseMid();
    System.out.println();

    System.out.println("max depth of red-black tree which contains " + max + " nodes is " + tree.maxDepth());
    // 50 个节点的高度是 9， 10 个节点的高度是 5，所以红黑树也不完全是 log2n 层的高
  }



}
