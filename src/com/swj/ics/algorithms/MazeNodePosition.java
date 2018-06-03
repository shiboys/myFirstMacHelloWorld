package com.swj.ics.algorithms;

/**
 * 迷宫各个点的坐标
 *
 */
public class MazeNodePosition {
    private int x;
    private int y;

    public MazeNodePosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public MazeNodePosition() {
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}
