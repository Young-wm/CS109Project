package view.game2;

import controller2.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

/*
* 这个类实现了整个完整的游戏panel
* 后续要用到的方法：每当游戏界面需要发生改变的时候（包括了鼠标点击的高亮，以及方块发生移动等等），都直接调用一次repaint()即可
* 后续还需要改进的：给整个游戏加上背景，再给游戏的每一个角色加上背景图片
* 另外如果加上背景图片以底面上每个角色的名字还想要保留，则再paintComponent方法里面修改写名字时画笔的颜色使其更加清晰即可
 */

public class GamePanel2 extends JPanel {

    private GameLogic2 gameLogic2;
    //这里必须加上一个gameLogic的field来找到里面对应的东西，
    // 但是在创建GamePanel相关对象的时候，千万不能创建新的，要传入GameFrame当中的gameLogic

    int cellSize = 80;
    // 这里定义出每个格子的像素大小这个变量，方便后面在调整具体大小美化时候可以直接更改
    private int offsetX = 20;
    private int offsetY = 20;
    //同样为了后面方便美化页面,这里定义出这个panel边界的间距

    private Map<Integer, Color> blockColors;
    //前面的Map的每一个ID对应一个Block，所以这里也通过一个Map的形式来储存所有的颜色，后面会非常好对应
    private Color selectedBlockBorderColor = Color.YELLOW;
    // 选中方块的边框颜色，定义为黄色
    private Color gridColor = Color.DARK_GRAY;
    // 网格线颜色，定义为深灰色
    private Color emptyCellColor = Color.LIGHT_GRAY;
    // 空格颜色定义为亮灰色

    public GamePanel2(GameLogic2 logic) {
        this.gameLogic2 = logic;
        //这里传入的就是GameFrame里面的gameLogic
        setPreferredSizeBasedOnBoard();
        //设置好panel的边界条件
        initializeBlockColors();
        //设置好block的颜色

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleMouseClick(e.getX(), e.getY());
            }
            //补充：mousePressed指的是鼠标点击下去这个事件，mouseClicked指的是鼠标点下去再回弹的事件
        });
        //这里用适配器，来只实现我想要实现的方法
    }

    private void setPreferredSizeBasedOnBoard() {
            Board2 board2 = gameLogic2.getGameState().getBoard();
            int panelWidth = board2.getWidth() * cellSize + 2 * offsetX;
            // 总宽度 = 列数 * 单元格大小 + 2 * 偏移量 (左右)
            int panelHeight = board2.getHeight() * cellSize + 2 * offsetY;
            // 总高度 = 行数 * 单元格大小 + 2 * 偏移量 (上下)
            setPreferredSize(new Dimension(panelWidth, panelHeight));
    }
    //这个方法这样写后面可以非常容易地更改地图
    //可以自动地根据给出的地图去渲染出图形

    private void initializeBlockColors() {
        blockColors = new HashMap<>();
        // 曹操
        blockColors.put(1, new Color(220, 20, 60));

        // 关羽
        blockColors.put(2, new Color(0, 128, 0));

        // 张飞，赵云，马超，黄忠
        blockColors.put(3, new Color(70, 130, 180));
        blockColors.put(4, new Color(70, 130, 180));
        blockColors.put(5, new Color(70, 130, 180));
        blockColors.put(6, new Color(70, 130, 180));

        // 小兵
        blockColors.put(7, new Color(255, 165, 0));
        blockColors.put(8, new Color(255, 165, 0));
        blockColors.put(9, new Color(255, 165, 0));
        blockColors.put(10, new Color(255, 165, 0));

    }
    //这里调整颜色可能需要后期慢慢去调整和修改，或者后面需要调整为图片的形式可能会更好一点

    private void handleMouseClick(int mouseX, int mouseY) {
        if (gameLogic2.getGameState().isGameWon()) {
            return;
        }

        int gridX = (mouseX - offsetX) / cellSize;
        // (mouseX - offsetX) / cellSize 得到的是列号 gridX
        int gridY = (mouseY - offsetY) / cellSize;
        // (mouseY - offsetY) / cellSize 得到的是行号 gridY

        if (gridX >= 0 && gridX < gameLogic2.getGameState().getBoard().getWidth() && gridY >= 0 && gridY < gameLogic2.getGameState().getBoard().getHeight()) {
            boolean selectionChanged = gameLogic2.selectBlockAt(gridX, gridY);
             if (selectionChanged || gameLogic2.getSelectedBlock() != null) {
                 repaint();
             }
            repaint();
             // 无论是否成功选中，都重绘以更新可能的选择高亮
        } else {
            String message = "It is invalid to click here.";
            String title = "Error";
            JOptionPane.showMessageDialog(this,message,title, JOptionPane.INFORMATION_MESSAGE);
        }
    }
    //这个方法实现了将鼠标点击的位置转化为坐标的形式，同时将被点击的坐标高亮，点击位置无效还会出现提示框报错

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // 清空背景
        Graphics2D g2d = (Graphics2D) g;
        // 补充知识点：这里使用Graphics2D，它有更多绘图功能

        GameState2 gameState2 = gameLogic2.getGameState();
        if (gameState2 == null){
            return;
        }
        Board2 board2 = gameState2.getBoard();
        if (board2 == null){
            return;
        }

        // 这个for循环完成了棋盘网格和空格的绘制
        for (int row = 0; row < board2.getHeight(); row++) {
            for (int col = 0; col < board2.getWidth(); col++) {
                int x = offsetX + col * cellSize;
                int y = offsetY + row * cellSize;

                // 这里绘制了空格背景
                if (board2.getBlockIdAt(col, row) == Board2.EMPTY_CELL_ID) {
                    g2d.setColor(emptyCellColor);
                    //这个可以理解为设置一支画笔，它的颜色是空白所对应的颜色
                    g2d.fillRect(x, y, cellSize, cellSize);
                    //现在将这个矩形的部分全部涂上空白区域对应的颜色
                }

                // 这里绘制网格线
                g2d.setColor(gridColor);
                //这里的理解可以理解为将画笔的颜色换为网格线对应的颜色
                g2d.drawRect(x, y, cellSize, cellSize);
                //只给矩形的边界画上，即对应着网格线
            }
        }

        //接下来要开始绘制所有方块
        Map<Integer, Block2> allBlocks = board2.getBlocksCopy();
        for (Block2 block2 : allBlocks.values()) {
            int blockPixelX = offsetX + block2.getX() * cellSize;
            int blockPixelY = offsetY + block2.getY() * cellSize;
            int blockPixelWidth = block2.getWidth() * cellSize;
            int blockPixelHeight = block2.getHeight() * cellSize;

            g2d.setColor(blockColors.get(block2.getId()));
            //这里已经在前面的initializeBlockColors这个地方对应好了
            g2d.fillRect(blockPixelX, blockPixelY, blockPixelWidth, blockPixelHeight);
            //这里用更新过了的画笔给矩形部分画上颜色

            g2d.setColor(blockColors.get(block2.getId()).darker());
            g2d.drawRect(blockPixelX, blockPixelY, blockPixelWidth -1, blockPixelHeight -1);
            //这里用稍暗一点的颜色涂在边框区分清楚每一个块

            //下面给每一个块上写上它的名字
            g2d.setColor(Color.WHITE);
            String blockText = block2.getName();
            FontMetrics fm = g2d.getFontMetrics();
            int stringWidth = fm.stringWidth(blockText);
            int stringHeight = fm.getAscent() - fm.getDescent(); // 更准确的文本高度
            //这里计算一下文本居中位置
            int textX = blockPixelX + (blockPixelWidth - stringWidth) / 2;
            int textY = blockPixelY + (blockPixelHeight - stringHeight) / 2 + fm.getAscent();
            //用新的画笔给每个块的中心位置写上它的名字
            g2d.drawString(blockText, textX, textY);
        }

        //下面的代码用来高亮显示选中的方块
        Block2 selected = gameLogic2.getSelectedBlock();
        if (selected != null) {
            int selX = offsetX + selected.getX() * cellSize;
            int selY = offsetY + selected.getY() * cellSize;
            int selWidth = selected.getWidth() * cellSize;
            int selHeight = selected.getHeight() * cellSize;

            g2d.setColor(selectedBlockBorderColor);
            g2d.setStroke(new BasicStroke(3));
            //这里把画笔的像素设置为3，能凸显出高亮的边框
            g2d.drawRect(selX + 1, selY + 1, selWidth - 2, selHeight - 2);
            g2d.setStroke(new BasicStroke(1));
            //最后再恢复默认画笔宽度
        }
    }
    //这个方法覆盖了全部的需要被绘制的部分，每当游戏界面需要发生变化时，用一次repaint()就能使界面做出对应改变
}