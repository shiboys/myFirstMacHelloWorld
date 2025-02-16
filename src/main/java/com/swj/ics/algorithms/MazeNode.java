package com.swj.ics.algorithms;

public class MazeNode {
    private MazeNodePosition position; //当前节点的位置

    private int recordIndex;//当前节点的当前顺序

    private MazeDirectionEnum directionType;// 当前节点的方向


    public MazeNode(int x,int y, int recordIndex, MazeDirectionEnum directionType) {
        this.position = new MazeNodePosition(x,y);
        this.recordIndex = recordIndex;
        this.directionType = directionType;
    }

    public MazeNode() {
    }

    public MazeNodePosition getPosition() {
        return position;
    }

    public void setPosition(MazeNodePosition position) {
        this.position = position;
    }

    public int getRecordIndex() {
        return recordIndex;
    }

    public void setRecordIndex(int recordIndex) {
        this.recordIndex = recordIndex;
    }

    public MazeDirectionEnum getDirectionType() {
        return directionType;
    }

    public void setDirectionType(MazeDirectionEnum directionType) {
        this.directionType = directionType;
    }
}
