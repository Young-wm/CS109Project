package controller2;

import java.io.Serializable;
import java.util.*;

/*这个类中的几个后面非常有用的方法：
*1.直接去new Board2()得到的就是一个初始化的“横刀立马”的图:
*        //3 1 1 5
         //3 1 1 5
         //4 2 2 6
         //4 7 8 6
         //0 9 X 0
        //X指的是10（为了对齐）
*2.用board去引用isValidCoordinate()，传入两个参数，一个是x，一个是y，判断这个参数再grid里面是不是合理的，最终返回的是boolean
*3.用board去引用getBlockIdAt()，传入x，y两个参数，判断grid的这个位置被id是什么的方块给占据了，最终返回的是这个地方的id
*4.用board去引用getBlockById()，传入一个id就能返回这个地方的是哪个方块，如果你想查找一下是谁，就接一个.toString()，或者直接用找对应哪一个
*5.两个特殊的getter：
*      （1）用board去引用getBlocksCopy()，由于公用一个地址能够有效防止更改内部数据
*      （2）用board去引用getGridCopy()，由于公用一个地址能够有效防止更改内部数据
*6.两个普通的getter：
*      （1）getWidth()
*      （2）getHeight()
*7.（最重要方法）用board去引用moveBlockOnBoard()，传入哪一个方块block，移动到x，和y
*8.补充一个点：曹操对应1，关羽对应2，张飞对应3，赵云对应4，马超对应5，黄忠对应6，小兵1对应7，小兵2对应8，小兵3对应9，小兵4对应10
*/

public class Board2 implements Serializable {
    private static final long serialVersionUID = 1L;
    //同样的，这个是第一版游戏的Board，定义一个版本序列号，方便后面存档，以免报错

    public static final int EMPTY_CELL_ID = 0;
    //我们设空白格的id为0
    //这里这样子定义board的宽高后面如果想要增加关卡会方便很多
    private int width; //棋盘的宽
    private int height;//棋盘的高
    public int[][] grid;
    public Map<Integer, Block2> blocks;
    //grid这个数组我准备用来储存每个小格的状态，即每个小格被id为什么的方块占领了
    //Map<>则是用来将每个方块和它的唯一的id对应起来，用Map<>后面要好查找很多


    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Board2(int width, int height) {
        if (width <= 0 || height <= 0) {
            System.out.println("The width and height must be positive.");
        }
        this.width = width;
        this.height = height;
        this.grid = new int[height][width];
        this.blocks = new HashMap<>();

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                grid[i][j] = 0;
            }
        }
        //构建一个矩阵，把它的所有位置先放上空格
    }

    public void initialize() {
        this.blocks.clear();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                grid[i][j] = 0;
            }
        }
        //这两步把Map中的对应关系清空，把grid网格也全部放上0

        //接下来我要将所有block初始化：
        List<Block2> initialBlock2s = new ArrayList<>();
        initialBlock2s.add(new Block2(1, "曹操", 2, 2, 2, 1));//1号对应大boss曹操
        initialBlock2s.add(new Block2(2, "关羽", 2, 1, 2, 3));//2号对应二弟关羽
        initialBlock2s.add(new Block2(3, "张飞", 1, 2, 1, 1)); // 3号对应三弟张飞
        initialBlock2s.add(new Block2(4, "赵云", 1, 2, 1, 3)); // 4号对应赵子龙
        initialBlock2s.add(new Block2(5, "马超", 1, 2, 0, 1)); // 5号对应马超
        initialBlock2s.add(new Block2(6, "黄忠", 1, 2, 0, 3)); // 6号对应老将军黄忠
        initialBlock2s.add(new Block2(7, "兵1", 1, 1, 0, 0));
        initialBlock2s.add(new Block2(8, "兵2", 1, 1, 2, 4));
        initialBlock2s.add(new Block2(9, "兵3", 1, 1, 3, 4));
        initialBlock2s.add(new Block2(10, "兵4", 1, 1, 1, 0)); // 4个小兵按照顺序摆放好

        for (Block2 block2 : initialBlock2s) {
            addBlockToBoard(block2);
        }

    }

    private void addBlockToBoard(Block2 block2) {
        if (block2 == null) {
            return;
        }
        blocks.put(block2.getId(), block2);
        //在添加到棋盘之前，顺便先让block在创建的Map里面与自己的ID对应起来，现在Map里面的序号和block是对应的
        for (int y = 0; y < block2.getHeight(); y++) {
            for (int j = 0; j < block2.getWidth(); j++) {
                int boardX = block2.getX() + j;
                int boardY = block2.getY() + y;
                if (isValidCoordinate(boardX, boardY)) {
                    grid[boardY][boardX] = block2.getId();
                } else {
                    System.out.printf("The block2 %s can not be put here.", block2.getName());
                }
            }
        }
    }
    //这里写一个addBlockToBoard方法，一方面是把block添加到board里面，另一方面，顺便把block的id在Map blocks里面确定下来
    //千万千万注意，这个方法不是随便加到棋盘中的，我只是为了在初始化棋盘的方法里面加块更加方便一点而已，其它任何地方都不能用这个方法，切记切

    public boolean isValidCoordinate(int x, int y) {
        return x >= 0 && x < this.width && y >= 0 && y < this.height;
    }
    //这里写一个方法来判断这个坐标是不是valid的，后面应该很有用


    public int getBlockIdAt(int x, int y) {
        if (!isValidCoordinate(x, y)) {
            System.out.println("The coordinate you input is invalid.");
        }
        return grid[y][x];
    }
    //输入某个grid的坐标，返回这个位置被哪个id占据

    public Block2 getBlockById(int blockId) {
        return blocks.get(blockId);
    }
    //这个方法用来查找哪个ID对应哪个Block

    public Map<Integer, Block2> getBlocksCopy() {
        // 返回一个深拷贝或不可变视图会更安全，但对于简单情况，HashMap的浅拷贝也行
        // 这里为了简单，返回一个新的HashMap，但Block对象本身是共享的
        return new HashMap<>(blocks);
    }
    //相当于blocks的getter，由于共享同一个地址，通过返回copy的形式来有效解决

    public int[][] getGridCopy() {
        int[][] gridCopy = new int[height][width];
        for (int i = 0; i < height; i++) {
            gridCopy[i] = Arrays.copyOf(grid[i], width);
        }
        return gridCopy;
    }
    //相当于grid的getter，由于共享同一个地址，通过返回copy的形式来有效解决


    public void moveBlockOnBoard(Block2 block2, int newX, int newY) {
        if (block2 == null) {
            System.out.println("Block2 to move cannot be null.");
        }

        //先清除旧位置
        for (int i = 0; i < block2.getHeight(); i++) {
            for (int j = 0; j < block2.getWidth(); j++) {
                if (isValidCoordinate(block2.getX() + j, block2.getY() + i)) {
                    grid[block2.getY() + i][block2.getX() + j] = EMPTY_CELL_ID;
                }
            }
        }

        block2.setX(newX);
        block2.setY(newY);

        //再标记新位置
        for (int i = 0; i < block2.getHeight(); i++) {
            for (int j = 0; j < block2.getWidth(); j++) {
                if (isValidCoordinate(newX + j, newY + i)) {
                    grid[newY + i][newX + j] = block2.getId();
                } else {
                    System.out.println("It is a invalid move.");
                }
            }
        }
    }
    //这个方法非常重要，它是用来移动一个方块的，将方块从某个地方移动到另一个坐标


    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Board2 board2)) return false;
        return width == board2.width && height == board2.height && Objects.deepEquals(grid, board2.grid) && Objects.equals(blocks, board2.blocks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(width, height, Arrays.deepHashCode(grid), blocks);
    }
    //重写了equals方法和hashCode方法方便后面实现撤销功能和AI算法中的判断


    public static final int DEFAULT_WIDTH = 4;
    public static final int DEFAULT_HEIGHT = 5;
    //这里先定义了默认宽高，即“横刀立马”棋盘的宽，高，准备更加简洁地来一个构造方法

    public Board2() {
        this(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        initialize();
    }
    //可以通过这个构造方法非常简洁快速地构造一个横刀立马的图，且所有东西都是初始化的

    public Board2(Board2 other) {
        this.width = other.width;
        this.height = other.height;
        this.grid = new int[this.height][this.width];
        this.blocks = new HashMap<>();

        // 深拷贝棋子 (Block3)
        for (Map.Entry<Integer, Block2> entry : other.blocks.entrySet()) {
            Block2 originalBlock = entry.getValue();
            Block2 copiedBlock = new Block2(originalBlock.getId(), originalBlock.getName(),
                    originalBlock.getWidth(), originalBlock.getHeight(),
                    originalBlock.getX(), originalBlock.getY());
            this.blocks.put(copiedBlock.getId(), copiedBlock);
        }

        // 深拷贝棋盘网格 (grid)
        for (int i = 0; i < this.height; i++) {
            this.grid[i] = Arrays.copyOf(other.grid[i], this.width);
        }
    }


    // 这里加了一个类实现大挪移逻辑
    public void setTeleportState() {
        this.blocks.clear();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                grid[i][j] = EMPTY_CELL_ID;
            }
        }

        List<Block2> teleportBlocks = new ArrayList<>();
        // 经典的“横刀立马”布局定义
        // 曹操 (ID 1, 2x2) at (1,0)
        // 关羽 (ID 2, 2x1) at (1,2)
        // 张飞 (ID 3, 1x2) at (0,0)
        // 赵云 (ID 4, 1x2) at (3,0)
        // 马超 (ID 5, 1x2) at (0,2)
        // 黄忠 (ID 6, 1x2) at (3,2)
        // 兵1 (ID 7, 1x1) at (1,3)
        // 兵2 (ID 8, 1x1) at (2,3)
        // 兵3 (ID 9, 1x1) at (1,4)
        // 兵4 (ID 10, 1x1) at (2,4)
        teleportBlocks.add(new Block2(1, "曹操", 2, 2, 0, 2));
        teleportBlocks.add(new Block2(2, "关羽", 2, 1, 2, 3));
        teleportBlocks.add(new Block2(3, "张飞", 1, 2, 3, 0));
        teleportBlocks.add(new Block2(4, "赵云", 1, 2, 2, 0));
        teleportBlocks.add(new Block2(5, "马超", 1, 2, 1, 0));
        teleportBlocks.add(new Block2(6, "黄忠", 1, 2, 0, 0));
        teleportBlocks.add(new Block2(7, "兵1", 1, 1, 2, 2)); // 大挪移后的兵1位置
        teleportBlocks.add(new Block2(8, "兵2", 1, 1, 0, 4));
        teleportBlocks.add(new Block2(9, "兵3", 1, 1, 1, 4));
        teleportBlocks.add(new Block2(10, "兵4", 1, 1, 3, 2));


        for (Block2 block : teleportBlocks) {
            addBlockToBoard(block);
        }
        System.out.println("棋盘发生“大挪移”");
    }


    // 这个方法是为了实现炸弹功能
    public boolean removeBlockById(int blockId) {
        Block2 blockToRemove = blocks.get(blockId);
        if (blockToRemove == null) {
            System.out.println("未找到ID为 " + blockId + " 的棋子，无法移除。");
            return false;
        }

        // 从grid中清除该棋子占据的位置
        for (int r = 0; r < blockToRemove.getHeight(); r++) {
            for (int c = 0; c < blockToRemove.getWidth(); c++) {
                int currentX = blockToRemove.getX() + c;
                int currentY = blockToRemove.getY() + r;
                if (isValidCoordinate(currentX, currentY) && grid[currentY][currentX] == blockId) {
                    grid[currentY][currentX] = EMPTY_CELL_ID;
                }
            }
        }
        // 从blocks映射中移除该棋子
        blocks.remove(blockId);
        System.out.println("ID为 " + blockId + " 的棋子已被移除。");
        return true;
    }


}
