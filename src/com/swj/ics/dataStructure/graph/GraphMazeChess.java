package com.swj.ics.dataStructure.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Stack;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/02/22 21:02
 * 基于图的迷宫，参考 MazeChess，可以说是重写
 * 迷宫棋盘的摆放如maze6.png 所示
 */
public class GraphMazeChess {

  private static final int BLOCKED = 0;
  private static final int PASSED = 1;
  // 进制通行，用于标识已经经过但是是死胡同的节点
  private static final int PROHIBITED = -1;

  private int currentStepIndex = 1;

  private NodePosition currentPosition;

  // 迷宫的辅助栈，主要用来做回退操作。
  private Stack<MazeNode> nodeStack;

  private int[][] mazeArray;

  public GraphMazeChess(int width, int height, int[] xBarrierArray, int[] yBarrierArray) {
    if (width <= 2 || height <= 2) {
      throw new IllegalArgumentException("width and height should be both more than 2.");
    }
    if (xBarrierArray.length == 0 || yBarrierArray.length == 0) {
      throw new IllegalArgumentException("the length of xBarrierArray or yBarrierArray should be more than 0");
    }
    if (xBarrierArray.length != yBarrierArray.length) {
      throw new IllegalArgumentException("the length of xBarrierArray should be equal");
    }
    nodeStack = new Stack<>();
    mazeArray = new int[width][height];
    List<NodePosition> posList = new ArrayList<>(xBarrierArray.length);
    for (int i = 0, len = xBarrierArray.length; i < len; i++) {
      posList.add(new NodePosition(xBarrierArray[i], yBarrierArray[i]));
    }
    initMazeChess(posList);
  }

  private void initMazeChess(List<NodePosition> blockedPosList) {
    /**
     * 迷宫初始化，1 表示为 不可通过，0 表示为通过。首先外墙是不可通过的，都初始化为 0
     */
    int lastColIndex = mazeArray[0].length - 1;
    int lastRowIndex = mazeArray.length - 1;
    for (int i = 0; i < mazeArray.length; i++) {
      // 第一列 block
      mazeArray[i][0] = BLOCKED;
      // 最后一列
      mazeArray[i][lastColIndex] = BLOCKED;
    }
    for (int j = 0; j <= lastColIndex; j++) {
      // 第一行 block
      mazeArray[0][j] = BLOCKED;
      // 最后一行
      mazeArray[lastRowIndex][j] = BLOCKED;
    }

    // 再将剩余地方标识为可通行
    for (int i = 1; i < lastRowIndex; i++) {
      for (int j = 1; j < lastColIndex; j++) {
        mazeArray[i][j] = PASSED;
      }
    }

    // 最后再初始化 传入参数的相关位置为 blocked
    if (blockedPosList == null || blockedPosList.isEmpty()) {
      return;
    }
    for (NodePosition nodePos : blockedPosList) {
      mazeArray[nodePos.X][nodePos.Y] = BLOCKED;
    }
  }

  /**
   * 核心算法我一遍写完，但是跑完发现有 bug 经过 大概 10 分钟的调试分析发现，有 3 处 bug：
   * 1、DirectionEnum 的 x 轴 和 Y 轴的值定义错了，导致向前跑失败
   * 2、计算下一个需要移动的位置的时候，不时有弹出的 节点位置算起，而是用 currentPosition 算起，currentPosition 代表了当前位置的下一个位置，当前位置变换，那么 currentPosition 必须跟着变化
   * 3、辅助栈弹出数据的时机不对，当一个节点发现 4 个方向都是无法通行的时候，需要立即将之标注为失效节点，然后从栈中弹出下一个元素进行判断，而不是先弹出在判断，这样就是判断的下一个节点。
   * @param startPos
   * @param endPos
   * @return
   */
  public boolean generateMazePath(NodePosition startPos, NodePosition endPos) {
    if (startPos == null || endPos == null) {
      throw new IllegalArgumentException("startPos and end position both can not be null");
    }
    currentPosition = startPos;
    do {
      if (nodeIsPass(currentPosition)) {
        // 标记第 currentStepIndex 步走过这个坐标
        markCurrentPosition(currentPosition);
        // 将当前位置生成一个新的节点压入栈中
        MazeNode mazeNode =
            new MazeNode(currentPosition.X, currentPosition.Y, currentStepIndex, StepDirectionEnum.RIGHT);
        nodeStack.push(mazeNode);
        // 统计步数索引更新
        currentStepIndex++;
        if (currentPosition.equals(endPos)) {
          // got it
          return true;
        }
        // 尝试走下一步
        moveNextStep(currentPosition, mazeNode);
      } else {
        // 如果下一个位置是不通的，如果当前节点的方向 如果不为 向上 UP 方向，则尝试变换当前节点的方向
        if (!nodeStack.isEmpty()) {
          MazeNode mazeNode = nodeStack.peek();
          if (mazeNode.stepDirection != StepDirectionEnum.UP) {
            mazeNode.stepDirection = StepDirectionEnum.getNextDirection(mazeNode.stepDirection);
            // 重新计算下一个位置
            moveNextStep(currentPosition, mazeNode);

          } else {
            while (mazeNode.stepDirection == StepDirectionEnum.UP && !nodeStack.isEmpty()) {
              markNodeProhibited(mazeNode);
              mazeNode = nodeStack.pop();
              currentStepIndex--;
            }
            // 如果最后一个弹出的不是因为栈空，则需要重新压回栈
            if (mazeNode.stepDirection != StepDirectionEnum.UP) {
              nodeStack.push(mazeNode);
              currentStepIndex++;
              mazeNode.stepDirection = StepDirectionEnum.getNextDirection(mazeNode.stepDirection);
              // 重新计算下一个位置
              moveNextStep(currentPosition, mazeNode);
            }
          }
        }
      }
    } while (!nodeStack.isEmpty());

    return false;
  }

  public int[][] getMazeArray() {
    return mazeArray;
  }

  private void markNodeProhibited(MazeNode mazeNode) {
    if (basicCheckPos(mazeNode.X, mazeNode.Y)) {
      mazeArray[mazeNode.X][mazeNode.Y] = PROHIBITED;
    }
  }

  private void moveNextStep(NodePosition currentPosition, MazeNode mazeNode) {
    StepDirectionEnum direction = mazeNode.stepDirection;
    currentPosition.X = mazeNode.X;
    currentPosition.Y = mazeNode.Y;


    currentPosition.X += direction.xShfit;
    currentPosition.Y += direction.yShfit;
  }

  private void markCurrentPosition(NodePosition currentPosition) {
    mazeArray[currentPosition.X][currentPosition.Y] = currentStepIndex;
  }



  private boolean nodeIsPass(NodePosition currentPosition) {
    if (basicCheckPos(currentPosition)) {
      return mazeArray[currentPosition.X][currentPosition.Y] == 1;
    }
    return false;
  }

  private boolean basicCheckPos(NodePosition currentPosition) {
    return currentPosition.X >= 0 && currentPosition.X < mazeArray.length
        && currentPosition.Y >= 0 && currentPosition.Y < mazeArray[0].length;
  }

  private boolean basicCheckPos(int posX, int posY) {
    return posX >= 0 && posX < mazeArray.length
        && posY >= 0 && posY < mazeArray[0].length;
  }

  private static class NodePosition {
    public int X;
    public int Y;

    public NodePosition(int x, int y) {
      X = x;
      Y = y;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      NodePosition that = (NodePosition) o;
      return X == that.X &&
          Y == that.Y;
    }

    @Override
    public int hashCode() {
      return Objects.hash(X, Y);
    }
  }


  public enum StepDirectionEnum {
    // 按照顺时针方向
    RIGHT(0, 1),
    DOWN(1, 0),
    LEFT(0, -1),
    UP(-1, 0);
    public int xShfit;
    public int yShfit;

    StepDirectionEnum(int x, int y) {
      this.xShfit = x;
      this.yShfit = y;
    }

    public static StepDirectionEnum getNextDirection(StepDirectionEnum currDirection) {
      if (currDirection == StepDirectionEnum.UP) {
        return RIGHT;
      } else {
        int index = currDirection.ordinal();
        int nextOrdinal = index + 1;
        return Arrays.stream(StepDirectionEnum.values()).filter(x -> x.ordinal() == nextOrdinal).findFirst().get();
      }
    }
  }


  private static class MazeNode {
    public int X;
    public int Y;
    // 第 n 步
    public int stepIndex;
    public StepDirectionEnum stepDirection;

    public MazeNode(int x, int y, int stepIndex,
        StepDirectionEnum stepDirection) {
      X = x;
      Y = y;
      this.stepIndex = stepIndex;
      this.stepDirection = stepDirection;
    }
  }

  public static void main(String[] args) {
    int mazeWidth = 10;
    int mazeHeight = 10;
    NodePosition startPos = new NodePosition(1, 1);
    NodePosition endPos = new NodePosition(8, 8);

    String[] blockXYArray = {
        "1:3", "1:7",
        "2:3", "2:7",
        "3:5", "3:6",
        "4:2", "4:3", "4:4",
        "5:4",
        "6:2", "6:6",
        "7:2", "7:3", "7:4", "7:6", "7:7",
        "8:1"
    };
    int[] xArray = new int[blockXYArray.length];
    int[] yArray = new int[blockXYArray.length];

    for (int i = 0, length = blockXYArray.length; i < length; i++) {
      String blockStr = blockXYArray[i];
      String[] xyArr = blockStr.split(":");
      xArray[i] = Integer.parseInt(xyArr[0]);
      yArray[i] = Integer.parseInt(xyArr[1]);
    }
    GraphMazeChess mazeChess = new GraphMazeChess(mazeWidth, mazeHeight, xArray, yArray);
    boolean success = mazeChess.generateMazePath(startPos, endPos);
    if (success) {
      System.out.println("find the path and print the path:");
      for (int i = 0; i < mazeChess.getMazeArray().length; i++) {
        System.out.println(Arrays.toString(mazeChess.getMazeArray()[i]));
      }
    } else {
      System.out.println("can not find the path from startPos to endPps");
    }
  }
}
