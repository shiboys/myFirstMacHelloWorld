package com.swj.ics.dataStructure.tree;

import org.omg.CORBA.PRIVATE_MEMBER;

/**
 * @author shiweijie
 * @date 2020/3/24 下午9:02
 */
public class RedBlackTree<K extends Comparable<K>, V> {
    private static final boolean RED = true;
    private static final boolean BLACK = false;
    private RBNode root = null;

    static class RBNode<K extends Comparable<K>, V> {
        private RBNode left;
        private RBNode right;
        private RBNode parent;
        private K key;
        private V value;
        private boolean color;

        public RBNode(RBNode left, RBNode right, RBNode parent, K key, V value, boolean color) {
            this.left = left;
            this.right = right;
            this.parent = parent;
            this.key = key;
            this.value = value;
            this.color = color;
        }

        public RBNode() {
        }

        public RBNode getLeft() {
            return left;
        }

        public RBNode getRight() {
            return right;
        }

        public RBNode getParent() {
            return parent;
        }

        public void setKey(K key) {
            this.key = key;
        }

        public void setValue(V value) {
            this.value = value;
        }

        public void setColor(boolean color) {
            this.color = color;
        }

        public boolean isColor() {
            return color;
        }
    }

    public RBNode getRoot() {
        return root;
    }

    private RBNode parentOf(RBNode node) {
        if (node != null) {
            return node.parent;
        }
        return null;
    }

    private boolean isRed(RBNode node) {
        if (node != null) {
            return node.color == RED;
        }
        return false;
    }

    /**
     * 着红色
     *
     * @param node
     */
    private void setRed(RBNode node) {
        if (node != null) {
            node.color = RED;
        }
    }

    private boolean isBlack(RBNode node) {
        if (node != null) {
            return node.color == BLACK;
        }
        return false;
    }

    /**
     * 着黑色
     *
     * @param node
     */
    private void setBlack(RBNode node) {
        if (node != null) {
            node.color = BLACK;
        }
    }

    private void inOrderPrint() {
        inOrderPrint(this.root);
    }

    /**
     * 中序遍历红黑树
     *
     * @param node
     */
    private void inOrderPrint(RBNode node) {
        if (node != null) {
            //先打印左节点
            inOrderPrint(node.left);
            System.out.println("key =" + node.key + ",value = " + node.value);
            inOrderPrint(node.right);
        }
    }

    /**
     * 左旋
     * 左旋示意图
     * p               p
     * |               |
     * x               y
     * / \    ----->   / \
     * lx   y           x  ry
     * /  \         / \
     * ly  ry       lx  ly
     * <p>
     * 1、将y的左子节点ly的父节点更新为x，并将x的右子节点指向y的左子节点ly
     * 2、当x的父节点不为空时，更新y的父节点为x的父节点，并将x的父节点的指定子树(本例为x)指定为y
     * 3、x如何下来？x的父节点更新为y，将y的左子节点更新为x
     *
     * @param x
     */
    private void rotateLeft(RBNode x) {
        //1、将y的左子节点ly的父节点更新为x，并将x的右子节点指向y的左子节点ly
        RBNode y = x.right;
        //将x的右子节点指向y的左子节点ly,避免空指针
        x.right = y.left;
        if (y.left != null) {
            y.left.parent = x;
        }
        //当x的父节点不为空时，更新y的父节点为x的父节点，并将x的父节点的指定子树(本例为x)指定为y
        if (x.parent != null) {
            y.parent = x.parent;
            //将x的父节点的指定子树(本例为x)指定为y
            if (x.parent.left == x) {
                x.parent.left = y;
            } else {
                x.parent.right = y;
            }
        } else { //这说明X为根节点，此时需要更新y为根节点。
            this.root = y;
            this.root.parent = null;
        }

        // 3、x如何下来？x的父节点更新为y，将y的左子节点更新为x
        x.parent = y;
        y.left = x;
    }

    /**
     * 右旋
     * p               p
     * |               |
     * y   ----->      x
     * / \             / \
     * x   ry          lx  y
     * / \                 / \
     * lx  ly              ly  ry
     * <p>
     * 1、 将x的左子节点ly的父节点更新为y节点，将y的左子节点更新为x的左子节点ly
     * 2、当y的父节点不为空时，更新x的父节点为y的父节点，更新y的父节点的指定子节点(y当前的位置)为x
     * 3、更新X与y的关系。更新y的父节点为x，更新x的右子节点为y
     */

    private void rotateRight(RBNode y) {
        //1、 将y的左子节点更新为x右子节点ly,将x的右子节点ly的父节点更新为y节点，
        RBNode x = y.left;
        y.left = x.right;
        if (x.right != null) {
            x.right.parent = y;
        }
        //2、当y的父节点不为空时，更新x的父节点为y的父节点，更新y的父节点的指定子节点(y当前的位置)为x
        if (y.parent != null) {
            x.parent = y.parent;
            if (y.parent.left == y) {
                y.parent.left = x;
            } else {
                y.parent.right = x;
            }
        } else {//y为根节点
            this.root = x;
            this.root.parent = null;
        }
        //3、更新X与y的关系。更新y的父节点为x，更新x的右子节点为y
        y.parent = x;
        x.right = y;
    }

    /**
     * public 的插入方法
     *
     * @param key
     * @param value
     */
    public void insertKey(K key, V value) {
        RBNode node = new RBNode();
        node.setKey(key);
        node.setValue(value);
        //新节点一定是红色
        node.setColor(RED);
        insert(node);
    }

    private void insert(RBNode node) {
        RBNode parent = null;
        RBNode curr = this.root;
        while (curr != null) {
            parent = curr.parent;
            int cmp = node.key.compareTo(curr.key);
            //如果cmp > 0 说明需要到curr的右子树寻找
            //如果cmp ==0 说明找到了当前节点，则直接替换value
            //else 说明需要到curl的左子树查找
            if (cmp > 0) {
                curr = curr.right;
            } else if (cmp == 0) {
                curr.setValue(node.value);
                //这里一定要记得返回，因为找到了节点，不需要再遍历了
                return;
            } else {
                curr = curr.left;
            }
        }
        //跳出循环之后，就找到了要插入的节点
        node.parent = parent;
        //再判断node与parent的左子树与右子树的关系。判断大小
        if(parent != null) {
            int cmp = node.key.compareTo(parent.key);
            if(cmp > 0) {
                parent.right = node;
            } else {
                //没有等于0的情况，等于0的情况，上面已经替换并返回了
                parent.left = node;
            }
        } else {
            //首次插入
            this.root = node;
        }
        //这里新插入了节点，需要调用修复红黑树的平衡
        insertFixup(node);
    }

    /**
     * 插入后修复红黑树平衡的方法
     *      |--情景1：红黑树为空树
     *      |--情景2：插入节点的key已经存在。不需要处理。
     *      |--情景3：插入的节点的父节点为黑色。因为所插入的路径，黑色节点没有发生变化，所以红黑树依然平衡，所以不需要处理。
     *
     *      |--情景4：需要处理的，插入的节点的父节点为红色
     *          |--情景4.1：叔叔节点存在，且叔叔节点为红色(父-叔为双红)。将父节点和叔叔节点染色为黑色，将祖父节点染色为红色，并以祖父节点为当前节点，进行下一轮处理
     *          |--情景4.2：叔叔节点不存在或者为黑色，父亲节点为爷爷节点的左子树。
     *              |--4.2.1 插入节点为父亲节点的左子节点，(LL情况，双红），将父节点染色为黑色，将爷爷染色为红色，然后以祖父节点为支点进行右旋
     *              |--4.2.2 插入节点为父亲节点的右子树（LR情况）以父节点为支点进行一次左旋，得到LL双红的情况，再指定父节点为当前节点，进行下一轮处理。
     *          |--情景4.3：叔叔节点不存在或者为黑色，父亲节点为爷爷节点的右子树
     *              |--4.3.1：插入节点为父亲节点的右子树(RR双红情况）。将父节点染色为黑色，将祖父节点染色为红色，以祖父节点为支点进行左旋，就完了
     *              |--4.3.2：插入节点为父亲节点的左子树(RL情况）。以父节点为支点，进行一次右旋，得到RR双红的的情况，指定父节点为当前节点进行下一轮处理。
     *
     */
    private void insertFixup(RBNode node) {
        this.root.setColor(RED);
        RBNode parent = node.getParent();
        RBNode gParent = parent.getParent();//祖父节点
        //进入情景4
        if(parent != null && isRed(parent)) {
            //parent 是红色节点，则一定存在祖父节点，因为根节点不是红色
            RBNode uncle = null;
            if(parent == gParent.left) {
                uncle = gParent.right;
                if(uncle != null && isRed(uncle)) {
                    //情景4.1 ，重新染色并重新旋转
                    setBlack(parent);
                    setBlack(uncle);
                    setRed(gParent);
                    insertFixup(gParent);
                    //重新旋转之后，需要返回
                    return;
                }
                //4.2 叔叔节点不存在或者为黑色
                if(uncle == null || isBlack(uncle)) {
                    //叔叔节点不存在或者为黑色
                    //4.2.1 插入节点为父亲节点的左子节点，(LL情况，双红）
                    if(node == parent.left) {
                        setBlack(parent);
                        setRed(gParent);
                        rotateRight(gParent);
                        return;
                    }
                    //4.2.2插入节点为父节点的右节点-LR.以父节点为支点进行一次左旋，得到LL双红的情况
                    if(node == parent.right) {
                        rotateLeft(parent);
                        insertFixup(parent);
                        return;
                    }

                }
            } else {
                //父节点为祖父节点的右子树
                uncle = gParent.left;
                if(uncle != null && isRed(uncle)) {
                    //情景4.1 ，重新染色并重新旋转
                    setBlack(parent);
                    setBlack(uncle);
                    setRed(gParent);
                    insertFixup(gParent);
                    //重新旋转之后，需要返回
                    return;
                }
                //4.3情况，
                if(uncle == null || isBlack(uncle)) {
                   if(node == parent.right) {
                       //4.3.1：插入节点为父亲节点的右子树(RR双红情况）
                       //将父节点染色为黑色，将祖父节点染色为红色，以祖父节点为支点进行左旋
                       setBlack(parent);
                       setRed(gParent);
                       rotateLeft(gParent);
                       return;
                   }
                   if(node == parent.left) {
                       //4.3.2：插入节点为父亲节点的左子树(RL情况）。以父节点为支点，进行一次右旋，得到RR双红的的情况，指定父节点为当前节点进行下一轮处理
                       rotateRight(parent);
                       //此时parent节点转到最右子树子节点上，可认为是RR的新增节点
                       insertFixup(parent);
                       return;
                   }
                }

            }
        }
    }
}
