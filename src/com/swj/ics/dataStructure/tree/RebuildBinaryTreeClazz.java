package com.swj.ics.dataStructure.tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.omg.CORBA.INTERNAL;

/**
 * @author shiweijie
 * @date 2020/3/30 下午12:33
 * 输入某二叉树的前序和中序遍历结果，请重建该二叉树
 * 假设前序和中序遍历的结果都不包含重复的数字。
 * 输入：
 * 前序遍历序列{1,2,4,7,3,5,6,8}
 * 中序遍历{4,7,2,1,5,3,8,6}
 */
public class RebuildBinaryTreeClazz {
    /**
     * 前序遍历：根左右
     * 中序遍历：左根右
     * 递归思想：
     * 1、我们先根据前序遍历的第一个元素确定根元素，然后在中序遍历的序列中找到根的位置，根的左边就是其左子树，右边就是其右子树
     * 2、构建根和左右子树
     * 3、递归进行1和2
     */

    private class TreeNode {
        private int val;
        private TreeNode leftNode;
        private TreeNode rightNode;
    }

    /**
     * 前序遍历
     *
     * @param rootNode
     */
    public void preOrder(TreeNode rootNode) {
        if (rootNode == null) {
            return;
        }
        System.out.print(rootNode.val + " ");
        preOrder(rootNode.leftNode);
        preOrder(rootNode.rightNode);
    }

    /**
     * 中序遍历
     *
     * @param rootNode
     */
    public void inOrder(TreeNode rootNode) {
        if (rootNode == null) {
            return;
        }
        inOrder(rootNode.leftNode);
        System.out.print(rootNode.val + " ");
        inOrder(rootNode.rightNode);
    }

    public TreeNode reconstructBinaryTree(List<Integer> preNodes, List<Integer> inNodes) {
        if (preNodes == null || inNodes == null) {
            System.out.println("preNodes is null || inNodes is null");
            return null;
        }
        if (preNodes.size() != inNodes.size()) {
            System.out.println("the length of preNodes is not equals the length of inNodes");
            return null;
        }
        if (preNodes.size() < 1) {
            System.out.println("the length of preNodes is 0!");
            return null;
        }
        int length = preNodes.size();
        TreeNode rootNode = new TreeNode();
        int rootVal = preNodes.get(0);
        rootNode.val = rootVal;
        //遍历中序集合，找到根节点的位置
        Integer rootIndex = null;
        int counter = 0;
        for (Integer treeVal : inNodes) {
            if (Objects.equals(treeVal, rootVal)) {
                rootIndex = counter;
                break;
            }
            counter++;
        }
        int leftLength = rootIndex;
        int rightLength = length - leftLength - 1;
        List<Integer> preLeftNodes = new ArrayList<>(leftLength);
        List<Integer> inLeftNodes = new ArrayList<>(leftLength);
        //前、中序遍历右子树
        List<Integer> preRightNodes = new ArrayList<>(rightLength);
        List<Integer> inRightNodes = new ArrayList<>(rightLength);

        //add 的 (index,element)重载方法，效率比较低，每次都会调用system.arrayCopy,建议更换成数组。
        for (int i = 0; i < length; i++) {
            if (i < rootIndex) {
                //前序遍历的第一个节点是根节点，根后面的(leftLength=rootIndex) - 1个节点是左子树。因此是i+1
                preLeftNodes.add(i, preNodes.get(i + 1));
                //中序遍历前（leftLength = rootIndex ） - 1节点是左子树，第rootIndex是根节点。
                inLeftNodes.add(i, inNodes.get(i));
            } else if (i > rootIndex) {
                //前序遍历的第一个节点是根节点，根后面的(leftLength=rootIndex) - 1个节点是左子树,后面是右子树
                preRightNodes.add(i - rootIndex - 1,preNodes.get(i));
                //中序遍历同理
                inRightNodes.add(i - rootIndex - 1, inNodes.get(i));
            }
        }

        //递归重建根节点的左右子树
        rootNode.leftNode = reconstructBinaryTree(preLeftNodes, inLeftNodes);
        rootNode.rightNode = reconstructBinaryTree(preRightNodes, inRightNodes);

        return rootNode;
    }

    public static void main(String[] args) {
        RebuildBinaryTreeClazz instance = new RebuildBinaryTreeClazz();
        List<Integer> preOrder = Arrays.asList(1,2,4,7,3,5,6,8);
        List<Integer> inOrder = Arrays.asList(4,7,2,1,5,3,8,6);

        TreeNode treeNode = instance.reconstructBinaryTree(preOrder,inOrder);

        System.out.println("pre order :");

        instance.preOrder(treeNode);

        System.out.println("\n middle order:");
        instance.inOrder(treeNode);

        System.out.println("\n 遍历结束！");

    }
}
