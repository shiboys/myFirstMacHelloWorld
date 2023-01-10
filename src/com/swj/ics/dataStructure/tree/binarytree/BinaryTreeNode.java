package com.swj.ics.dataStructure.tree.binarytree;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/01/10 18:16
 */
public class BinaryTreeNode {
    public int value;
    public BinaryTreeNode leftChild;
    public BinaryTreeNode rightChild;

    public BinaryTreeNode() {

    }

    public BinaryTreeNode(int val) {
        this.value = val;
    }
}
