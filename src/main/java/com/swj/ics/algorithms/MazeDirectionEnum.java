package com.swj.ics.algorithms;

public enum MazeDirectionEnum {
    //顺时针方向 右 0,下1,左2 上 3

    UP(-1,0,3),
    DOWN(1,0,1),
    LEFT(0,-1,2),
    RIGHT(0,1,0)
    ;

    private int x;

    private int y;
    
    int value ;

    private MazeDirectionEnum(int x, int y,int value) {
        this.x = x;
        this.y = y;
        this.value = value;
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

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public static MazeDirectionEnum getEnumByValue(int value) {
        MazeDirectionEnum[] enums = MazeDirectionEnum.values();
        for(int i=0,len = enums.length;i < len ;i++) {
            if (enums[i].value == value) {
                return enums[i];
            }
        }
        return MazeDirectionEnum.RIGHT;
    }
}
