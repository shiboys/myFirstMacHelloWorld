package com.swj.ics.algorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

/**
 * 迷宫类游戏
 */
public class MazeChess {

    static final int DEFAULT_NODE_BLOCK_SIZE = 18;

    static int[][] nodeArr;

    static void initMazeChess(int xsize,int ysize,List<MazeNodePosition> unPassNodeList) {

        nodeArr = new int[xsize][ysize];


        for(int i = 0;i < xsize;i++) {
            nodeArr[i][0] = 0;// 第i行，第0列
            nodeArr[i][ysize-1] = 0;//第i行，最后一列初始化为0
        }
        for(int j = 0;j<ysize;j++) {
            nodeArr[0][j] = 0;
            nodeArr[xsize-1][j] = 0;
        }

        //四周围上墙之后，将二维数组内部全部设为1，初始化为全部可通过

        for(int i = 1;i< xsize - 1; i++) {
            for(int y = 1;y < ysize - 1;y++) {
                nodeArr[i][y] = 1;
            }
        }

        if (unPassNodeList == null || unPassNodeList.size() < 1) {
            return;
        }
        //unpassList
        for(MazeNodePosition pos : unPassNodeList) {
            if (pos.getX() < xsize && pos.getY() < ysize) {
                nodeArr[pos.getX()][pos.getY()] = 0;//这些地方unpass
            }
        }



    }



    static void printf(String msg) {
        System.out.println(msg);
    }

    static void printMaze(int[][] mazeNodeArr) {
        for(int i =0 ; i < mazeNodeArr.length;i++) {
            for (int j = 0; j < mazeNodeArr[i].length ; j++){
                System.out.print(String.format("%3d",mazeNodeArr[i][j]));
            }


            printf("");
        }
    }

    static Stack<MazeNode> nodeStack = new Stack<MazeNode>();
    static  int currStepIndex = 1;//当前步
    static MazeNodePosition currNodePos;
    static MazeNode nodeTop = null;


    static boolean nodePass(MazeNodePosition nodePos) {
        return nodePos.getX() < nodeArr.length && nodePos.getX() >= 0
                && nodePos.getY() < nodeArr[0].length && nodePos.getY() >= 0
                && nodeArr[nodePos.getX()][nodePos.getY()] == 1;//1 表示路径可通过
    }

    static void setNextNodePos(MazeNodePosition nodePos,MazeDirectionEnum direction) {
        nodePos.setX(nodePos.getX() + direction.getX());
        nodePos.setY(nodePos.getY() + direction.getY());
    }


    static boolean getMazePath(MazeNodePosition startNodePos,MazeNodePosition endNodePos) {
       // currNodePos = new MazeNodePosition( startNodePos.getX(),startNodePos.getY());
        currNodePos = startNodePos;
        /*
        * 算法思想
        *
        * do{
        * 若当前位可通
        *   则 {
        *       标记当前位置
        *      将当前位置插入栈顶，
        *      判断当前位置是否是出口位置，如果是，则结束
        *      否则 切换当前位置的右邻居为当前位置
        *   } else {
        *       若栈不空且栈顶尚有其他位置未经探索
        *           则设定新的当前位置为沿顺时针方向旋转找到一个栈顶位置的下一个相邻块。
        *       否则
        *       while（如果栈不空且当前位置四周均不通，为死胡同）{
        *           弹出栈顶元素，标记当前路径为不可达或者删除当前通道块。
        *       }
        *       若栈不空，则重新尝试新的栈顶位置，直到找出一个可通的相邻块或出至栈空。
        *
        *
        *   }
        *
        * } while(栈不空）
        *
        * */

        do {
            if(nodePass(currNodePos)) {
                markNodePass(currNodePos);
                nodeTop = new MazeNode(currNodePos.getX(),currNodePos.getY(),currStepIndex,MazeDirectionEnum.RIGHT);
                nodeStack.push(nodeTop);
                currStepIndex++;
                if(currNodePos.getY() == endNodePos.getY() && currNodePos.getX() == endNodePos.getX()) {
                    return true;
                }
                //继续下一个节点位置
                setNextNodePos(currNodePos,MazeDirectionEnum.RIGHT);
            } else {
                if(!nodeStack.isEmpty()) {
                    nodeTop = nodeStack.pop();
                    currStepIndex --;
                    while (nodeTop.getDirectionType() == MazeDirectionEnum.UP && !nodeStack.isEmpty()) {
                        markNodeUnPass(nodeTop.getPosition());
                        nodeTop = nodeStack.pop();
                        currStepIndex --;
                    }
                    if (nodeTop.getDirectionType() != MazeDirectionEnum.UP) {

                        nodeStack.push(nodeTop);
                        currStepIndex++;
                        nodeTop.setDirectionType(MazeDirectionEnum.getEnumByValue(nodeTop.getDirectionType().getValue()
                                + 1));
                        currNodePos.setY(nodeTop.getPosition().getY());
                        currNodePos.setX(nodeTop.getPosition().getX());

                        setNextNodePos(currNodePos,nodeTop.getDirectionType());
                    }
                }
            }
        } while (!nodeStack.empty());

        return false;

        /*
        * 总结，算法思想是对的，但是因为根据面向过程和面向对象，因为Java是纯面向对象的，因此当前元素是一个指针，
        * 栈中的元素也是一个指针，不能相互赋值，相互赋值的外部引用的的时候，等于是直接对栈中元素的属性值更改
        *
        * */

        /*
        * if(nodePass(currNodePos)) {
                markNodePass(currNodePos);
                nodeTop = new MazeNode(currNodePos.getX(),currNodePos.getY(),currStepIndex,MazeDirectionEnum.RIGHT);
                nodeStack.push(nodeTop);
                currStepIndex ++;
                if (currNodePos.getX() == endNodePos.getX()
                        && currNodePos.getY() == endNodePos.getY()) { //找到了终点
                    return true;
                }
                //否则将当前节点移动到下一个位置
                setNextNodePos(currNodePos,MazeDirectionEnum.RIGHT);
            } else {
                if (!nodeStack.isEmpty()) {
                    nodeTop = nodeStack.pop();
                    currStepIndex --;
                    while (nodeTop.getDirectionType() == MazeDirectionEnum.UP && !nodeStack.isEmpty()) {
                        //将当前nodeTop节点标记为无效的路径块
                        markNodeUnPass(nodeTop.getPosition());
                        nodeTop = nodeStack.pop();
                        currStepIndex --;
                    }
                    if (nodeTop.getDirectionType().getValue() < MazeDirectionEnum.UP.getValue()) {
                        //说明当前被弹出的节点还有微遍历的方向，需要将当前节点重新压入栈中
                        nodeStack.push(nodeTop);
                        currStepIndex ++;
                        //更改当前节点的前进方向
                        nodeTop.setDirectionType(MazeDirectionEnum.getEnumByValue(nodeTop.getDirectionType().getValue() + 1));
                        //设置当前位置为当前节点
                        currNodePos.setX(nodeTop.getPosition().getX());
                        currNodePos.setY(nodeTop.getPosition().getY());
                        //将当前节点移动一个位置
                        setNextNodePos(currNodePos,nodeTop.getDirectionType());

                    }
                }
            }
        * */

        /**
         * if (nodePass(currNodePos)) {

         markNodePass(currNodePos);

         nodeTop = new MazeNode(currNodePos.getX(),currNodePos.getY(),currStepIndex,MazeDirectionEnum.RIGHT);
         nodeStack.push(nodeTop);
         currStepIndex ++;

         if(currNodePos.getY() == endNodePos.getY()
         && currNodePos.getX() == endNodePos.getX()) {
         return true;
         }

         setNextNodePos(currNodePos,nodeTop.getDirectionType());

         } else {
         if (!nodeStack.empty()) {
         nodeTop = nodeStack.pop();
         currStepIndex --;
         while (nodeTop.getDirectionType() == MazeDirectionEnum.UP && !nodeStack.isEmpty()) {
         //将当前块标记为-1，表示此块不通
         markNodePassUnPass(nodeTop.getPosition());
         nodeTop = nodeStack.pop();
         currStepIndex --;
         }

         if(nodeTop.getDirectionType().getValue() < MazeDirectionEnum.UP.getValue()) {

         //顺时针方向旋转
         nodeTop.setDirectionType(MazeDirectionEnum.getEnumByValue(
         nodeTop.getDirectionType().getValue() + 1));

         currNodePos.setX(nodeTop.getPosition().getX());
         currNodePos.setY(nodeTop.getPosition().getY());

         nodeStack.push(nodeTop);//将当前满足条件的元素，需要重新押回栈顶，以便寻找其他方向的
         currStepIndex ++;
         setNextNodePos(currNodePos,nodeTop.getDirectionType());
         }

         }
         }
         */
    }

    static void markNodePass(MazeNodePosition nodePos) {
        if( nodePos.getX() < nodeArr.length && nodePos.getX() >= 0
                && nodePos.getY() < nodeArr[0].length && nodePos.getY() >= 0 ) {

            nodeArr[nodePos.getX()][nodePos.getY()] = currStepIndex;//currStepIndex 表示此路径块为第stepIndex块。
        }
    }
    static void markNodeUnPass(MazeNodePosition nodePos) {
        if( nodePos.getX() < nodeArr.length && nodePos.getX() >= 0
                && nodePos.getY() < nodeArr[0].length && nodePos.getY() >= 0 ) {

             nodeArr[nodePos.getX()][nodePos.getY()] = -1;//-1 表示此路径经证实不可通
        }

    }


    static List<MazeNodePosition> getBarrierNodeList () {
        List<MazeNodePosition> unPassPosList = new ArrayList<>();
        unPassPosList.add(new MazeNodePosition(1,3));
        unPassPosList.add(new MazeNodePosition(1,7));
        
        unPassPosList.add(new MazeNodePosition(2,3));
        unPassPosList.add(new MazeNodePosition(2,7));
        
        unPassPosList.add(new MazeNodePosition(3,5));
        unPassPosList.add(new MazeNodePosition(3,6));
        
        unPassPosList.add(new MazeNodePosition(4,2));
        unPassPosList.add(new MazeNodePosition(4,3));
        unPassPosList.add(new MazeNodePosition(4,4));
        
        unPassPosList.add(new MazeNodePosition(5,4));
        
        unPassPosList.add(new MazeNodePosition(6,2));
        unPassPosList.add(new MazeNodePosition(6,6));
        
        unPassPosList.add(new MazeNodePosition(7,2));
        unPassPosList.add(new MazeNodePosition(7,3));
        unPassPosList.add(new MazeNodePosition(7,4));
        unPassPosList.add(new MazeNodePosition(7,6));
        unPassPosList.add(new MazeNodePosition(7,7));

        unPassPosList.add(new MazeNodePosition(8,1));
        return unPassPosList;
    };
    public static void main(String[] args) {

        System.out.println("迷宫开始。。。");
        printf("请输入迷宫横坐标点数：");
        Scanner scanner = new Scanner(System.in);

         int xsize = scanner.nextInt();
         printf("请输入迷宫纵坐标点数：");
         int ysize = scanner.nextInt();

        String[] posArr = null;


        initMazeChess(xsize,ysize,getBarrierNodeList());

        printMaze(nodeArr);

        printf("请输入起点位置坐标，逗号分割:");

        posArr = scanner.next().split(",");
        MazeNodePosition startNodePos = new MazeNodePosition(Integer.valueOf(posArr[0]),Integer.valueOf(posArr[1]));

        printf("请输入终点位置坐标，逗号分割:");

        posArr = scanner.next().split(",");
        MazeNodePosition endNodePos = new MazeNodePosition(Integer.valueOf(posArr[0]),Integer.valueOf(posArr[1]));

        boolean isReached = getMazePath(startNodePos,endNodePos);
        if (isReached) {
            printf("success! path is ");
            // 打印 1.。。。n 的 stepIndex
            printMaze(nodeArr);
        } else {
            printf("the maze is unreachable!");
        }

    }
}
