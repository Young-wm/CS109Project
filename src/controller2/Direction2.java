package controller2;

//枚举类，定义了四个移动方向
public enum Direction2 {
    UP(0,-1), DOWN(0,1), LEFT(-1,0), RIGHT(1,0);

    private final int dx;
    private final int dy;

    Direction2(int dx, int dy){
        this.dx = dx;
        this.dy = dy;
    }

    public int getDx() {
        return dx;
    }

    public int getDy() {
        return dy;
    }
}
