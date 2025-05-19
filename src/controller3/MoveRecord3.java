package controller3;

import java.io.Serializable;
/*
*这个类主要起到一个记录作用，有blockID,fromX,fromY,toX,toY这几个field，以及它们对应的getter和setter
 */
public class MoveRecord3 implements Serializable {
    private static final long serialVersionUID = 1L;
    //同样记录一个版本号

    private int blockId;
    // 移动的方块ID
    private int fromX;
    // 移动前的x坐标
    private int fromY;
    // 移动前的y坐标
    private int toX;
    // 移动后的x坐标
    private int toY;
    // 移动后的y坐标

    public MoveRecord3(int blockId, int fromX, int fromY, int toX, int toY) {
        this.blockId = blockId;
        this.fromX = fromX;
        this.fromY = fromY;
        this.toX = toX;
        this.toY = toY;
    }

    public int getBlockId() {
        return blockId;
    }

    public int getFromX() {
        return fromX;
    }

    public int getFromY() {
        return fromY;
    }

    public int getToX() {
        return toX;
    }

    public int getToY() {
        return toY;
    }

    @Override
    public String toString() {
        return String.format("The block %d moves from (%d, %d) to (%d, %d).",this.getBlockId(),this.getFromX(),this.getFromY(),this.toX,this.toY);
    }
}
