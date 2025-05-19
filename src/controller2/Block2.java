package controller2;

import java.io.Serializable;
import java.util.Objects;

/*
*这个类的主要目的是定义了Block是的属性，有规定了就不能动的属性id，name，width（横的），height（竖的），
*以及一个block的左上角的那个最小单元在后面创建的grid当中的位置x，y
*能动的属性有getter和setter；不能动的属性只有getter
*/

public class Block2 implements Serializable {
    private static final long serialVersionUID = 1L;
    //定义了一个版本序列号，后面储存数据时，这个版本的Block只能储存在第一版的游戏当中
    private final int id;
    private final String name;
    private final int width;
    private final int height;
    private int x;
    private int y;
    //每一个block有它独特的id，名字，长，宽，再加上它现在的位置的属性

    //构造方法

    public Block2(int id, String name, int width, int height, int x, int y) {
        if (id <= 0) {
            System.out.println("Block2 ID must be positive.");
        }
        if (name == null || name.trim().isEmpty()) {
            System.out.println("Block2 name cannot be null or empty.");
        }
        if (width <= 0 || height <= 0) {
            System.out.println("Block2 width and height must be positive.");
        }
        this.id = id;
        this.name = name;
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    //所有field的getter


    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    //x,y的setter

    public void move(int dx, int dy) {
        this.x += dx;
        this.y += dy;
    }
    //写一个move方法来后续控制方块的移动


    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Block2 block2)) return false;
        return id == block2.id && width == block2.width && height == block2.height && x == block2.x && y == block2.y && Objects.equals(name, block2.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, width, height, x, y);
    }
    //重写了equals和hashCode方法，方便后面比较两个对象是不是相同（可能会修改为只要位置一样的同类型block就相同，先放在这里）。


    @Override
    public String toString() {
        return String.format("The width of block%d (%s) is %d and its height is %d.It is located at (%d , %d)", this.getId(), this.getName(), this.getWidth(), this.getHeight(), this.getX(), this.getY());
    }
}
