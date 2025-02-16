package com.swj.ics.dataStructure.array;

/**
 * author shiweijie
 * date 2018/10/7 下午11:31
 * [二维数组中的查找]
 * [题目]
 * 在一个二维数组中，每一行都按照从左到右递增的顺序排序，每一列都按照从上到下递增的顺序排序。
 * 请完成一个函数，输入这样的一个二维数组和一个整数，判断数组中是否含有该整数。
 * [解析]
 * 假设数组 array 有 m 行 n 列
 * 1. 可以直接遍历一遍二维数组，则时间复杂度为 O(m*n)
 * 2. 利用有序的性质，我们从右上角或者左下角开始查找，每次可以减少一行或一列，时间复杂度 O(m+n)
 */
public class FindNumberIn2Dimension {
    public static void main(String[] args) {
        Integer[][] sourceArray = {{1, 2, 3, 4}, {5, 6, 7, 8}, {9, 10, 11, 12}};
        Position pos = findTargetNumber(sourceArray, 11);
        if (pos != null) {
            System.out.println(pos.getX() + "," + pos.getY());
        } else {
            System.out.println("null");
        }
    }

    static class Position {
        Integer x;
        Integer y;

        public Integer getX() {
            return x;
        }

        public void setX(Integer x) {
            this.x = x;
        }

        public Integer getY() {
            return y;
        }

        public void setY(Integer y) {
            this.y = y;
        }

        public Position(Integer x, Integer y) {
            this.x = x;
            this.y = y;
        }

        public Position() {
        }
    }

    //从右上方开始查找
    static Position findTargetNumber(Integer[][] sourceArray, Integer targetNum) {
        int i = 0, j = sourceArray[0].length - 1;
        while (i < sourceArray.length && j >= 0) {
            if (sourceArray[i][j].equals(targetNum)) {
                return new Position(i, j);
            }
            if (sourceArray[i][j] < targetNum) {
                i++;
            }
            if (sourceArray[i][j] > targetNum) {
                j--;
            }
        }
        return null;
    }
}
