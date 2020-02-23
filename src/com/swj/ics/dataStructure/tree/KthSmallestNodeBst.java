package com.swj.ics.dataStructure.tree;

/**
 * Created by swj on 2020/2/22.
 * 给定一个二叉搜索树（BST），找到书中第K小的节点。
 */

/**
 * 
     * 5
     / \
     3   6
     / \
     2   4
     /
    1

 * 说明：保证输入的K满足1<=k<=节点数目
 * 解题思路：树相关的题目，第一眼就想到递归求解，左右子树分别遍历。联想到二叉搜索树的性质，root大于左子树，小于右子树
 * 如果左子树的节点数目等于K-1，那么当前的root就是结果，否则如果左子树的节点数目小于K-1，那么结果必然在右子树，否则就在
 * 左子树。因此，在搜索的时候，同时返回节点的数目，跟K作比较，就能得出结论。这句话很重要。
 * todo:这个没有写DEMO，比较抽象
 */
public class KthSmallestNodeBst {

    public static class TreeNode {
        Integer val;
        private TreeNode left;
        private TreeNode right;

        TreeNode(Integer val) {
            this.val = val;
        }

        private class ResultType {
            //是否找到
            private boolean found;
            private int nodeCounter;// 从左子树的叶子节点开始计算的到当前节点的路径节点数。
            private Integer nodeVal;//节点的值

            public ResultType(boolean found, int nodeCounter,Integer nodeVal) {
                this.found = found;
                this.nodeCounter = nodeCounter;
                this.nodeVal = nodeVal;
            }
        }

        public int kthSmallestNode(TreeNode root, int k) {
            return kthSmallestHelper(root, k).nodeCounter;
        }

        /**
         * 查找BST第K小元素的核心算法
         *
         * @param root
         * @param k
         * @return
         */
        private ResultType kthSmallestHelper(TreeNode root, int k) {
            if (root == null) {
                return new ResultType(false, 0,null);
            }
            //进行递归遍历，先递归到左子树的叶子节点
            ResultType leftResultType = kthSmallestHelper(root.left,k);
            if(leftResultType.found) {
                //return new ResultType(false,resultType.nodeCounter,resultType.nodeVal);
                return leftResultType;
            }
            //如果是当前根节点，则返回找到的信息
            if(k-1 == leftResultType.nodeCounter) {
                return new ResultType(true,k-1,root.val);
            }
            //
            ResultType rightResult = kthSmallestHelper(root.right,k);
            if(rightResult.found) {
                return new ResultType(true,k-1,root.right.val);
            }
            //没有找到，返回节点总数
            return new ResultType(false,leftResultType.nodeCounter+rightResult.nodeCounter+1,root.val);
        }

    }

}
