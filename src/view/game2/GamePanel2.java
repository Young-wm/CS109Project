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
* 游戏支持自适应缩放和图片背景
*/

public class GamePanel2 extends JPanel {

    private GameLogic2 gameLogic2;
    //这里必须加上一个gameLogic的field来找到里面对应的东西，
    // 但是在创建GamePanel相关对象的时候，千万不能创建新的，要传入GameFrame当中的gameLogic

    private int baseCellSize = 80; // 基础单元格大小
    private int cellSize = baseCellSize; // 当前使用的单元格大小，会根据面板大小动态调整
    private int offsetX = 20;
    private int offsetY = 20;
    //同样为了后面方便美化页面,这里定义出这个panel边界的间距

    private Map<Integer, Color> blockColors = new HashMap<>();
    //前面的Map的每一个ID对应一个Block，所以这里也通过一个Map的形式来储存所有的颜色
    private Color selectedBlockBorderColor = Color.YELLOW;
    // 选中方块的边框颜色，定义为黄色
    private Color gridColor = Color.DARK_GRAY;
    // 网格线颜色，定义为深灰色
    private Color emptyCellColor = Color.LIGHT_GRAY;
    // 空格颜色定义为亮灰色

    // 是否显示网格线
    private boolean showGridLines = false;
    // 是否显示棋子名称（纯色模式下显示，图片模式下不显示）
    private boolean showPieceNames = true;

    // 皮肤切换按钮
    private JButton skinToggleButton = new JButton("切换皮肤");

    // 记录当前面板尺寸，用于检测实际变化
    private Dimension lastPanelSize = new Dimension(0, 0);
    private boolean forceUpdate = false;

    public GamePanel2(GameLogic2 logic) {
        this.gameLogic2 = logic;
        //这里传入的就是GameFrame里面的gameLogic
        setPreferredSizeBasedOnBoard();
        //设置好panel的边界条件
        initializeBlockColors();
        //设置好block的颜色

        // 初始化图片资源管理器
        GameImageManager.initialize();

        // 根据当前皮肤模式设置是否显示棋子名称
        updatePieceNameVisibility();

        // 设置布局为null，以便自定义放置按钮
        setLayout(null);

        // 添加皮肤切换按钮
        skinToggleButton.setBounds(10, 10, 100, 30);
        add(skinToggleButton);
        skinToggleButton.addActionListener(e -> toggleSkin());

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleMouseClick(e.getX(), e.getY());
            }
            //补充：mousePressed指的是鼠标点击下去这个事件，mouseClicked指的是鼠标点下去再回弹的事件
        });
        //这里用适配器，来只实现我想要实现的方法

        // 添加组件监听器，处理大小变化
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                // 只有当大小真正改变时才更新，避免不必要的计算
                Dimension currentSize = getSize();
                if (!currentSize.equals(lastPanelSize) || forceUpdate) {
                    lastPanelSize = currentSize;
                    updateCellSizeBasedOnPanel();
                    forceUpdate = false;
                }
            }
        });
    }

    /**
     * 根据面板大小更新单元格大小
     */
    public void updateCellSizeBasedOnPanel() {
        if (getWidth() <= 0 || getHeight() <= 0 || gameLogic2 == null || gameLogic2.getGameState() == null || gameLogic2.getGameState().getBoard() == null)
            return;

        Board2 board2 = gameLogic2.getGameState().getBoard();

        // 计算可用的棋盘区域大小
        int availableWidth = getWidth() - (2 * offsetX);
        int availableHeight = getHeight() - (2 * offsetY);

        // 计算每个方向上的单元格大小
        int cellSizeFromWidth = availableWidth / board2.getWidth();
        int cellSizeFromHeight = availableHeight / board2.getHeight();

        // 使用较小的值作为单元格大小，确保完整显示
        cellSize = Math.min(cellSizeFromWidth, cellSizeFromHeight);

        // 确保单元格大小不小于最小值（但不限制最大值）
        cellSize = Math.max(cellSize, 10);

        // 重新计算偏移量，使棋盘居中
        offsetX = (getWidth() - (cellSize * board2.getWidth())) / 2;
        offsetY = (getHeight() - (cellSize * board2.getHeight())) / 2;

        // 清除图片缓存，避免使用过时的尺寸
        clearImageCache();

        repaint(); // 重绘棋盘
    }

    /**
     * 设置初始首选大小，但不限制后续可以扩展到的大小
     */
    private void setPreferredSizeBasedOnBoard() {
        Board2 board2 = gameLogic2.getGameState().getBoard();
        int panelWidth = board2.getWidth() * baseCellSize + 2 * offsetX;
        // 总宽度 = 列数 * 单元格大小 + 2 * 偏移量 (左右)
        int panelHeight = board2.getHeight() * baseCellSize + 2 * offsetY;
        // 总高度 = 行数 * 单元格大小 + 2 * 偏移量 (上下)
        setPreferredSize(new Dimension(panelWidth, panelHeight));

        // 设置最小大小，防止棋盘过小导致显示问题
        int minCellSize = 30; // 最小单元格大小
        int minWidth = board2.getWidth() * minCellSize + 2 * offsetX;
        int minHeight = board2.getHeight() * minCellSize + 2 * offsetY;
        setMinimumSize(new Dimension(minWidth, minHeight));
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
            JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
        }
    }
    //这个方法实现了将鼠标点击的位置转化为坐标的形式，同时将被点击的坐标高亮，点击位置无效还会出现提示框报错

    /**
     * 窗口大小变化时调用此方法更新棋盘大小
     */
    public void updateSizeForWindowChange() {
        // 立即更新并重绘
        updateCellSizeBasedOnPanel();
        repaint();
    }

    /**
     * 当窗口调整完成后调用此方法
     */
    public void finalizeResize() {
        // 强制更新和重绘
        forceUpdate = true;
        clearImageCache();
        updateCellSizeBasedOnPanel();
        repaint();
    }

    /**
     * 清除图片缓存
     */
    private Map<Integer, Image> scaledPieceImageCache = new HashMap<>();

    public void clearImageCache() {
        scaledPieceImageCache.clear();
    }

    /**
     * 获取棋子图片，优先从缓存获取
     *
     * @param pieceId 棋子ID
     * @return 对应的棋子图片
     */
    private Image getPieceImage(int pieceId, int width, int height) {
        // 检查当前皮肤模式
        if (GameImageManager.getSkinMode() == 1) {
            // 纯色皮肤模式下返回null，让调用者使用纯色绘制
            return null;
        }

        // 图片皮肤模式
        // 生成缓存键，使用pieceId和尺寸
        String cacheKey = pieceId + "_" + width + "_" + height;
        int cacheKeyInt = cacheKey.hashCode();

        // 首先尝试从缓存获取已缩放的图像
        if (scaledPieceImageCache.containsKey(cacheKeyInt)) {
            return scaledPieceImageCache.get(cacheKeyInt);
        }

        // 获取原始图像
        Image image = GameImageManager.getPieceImage(pieceId);
        if (image != null) {
            // 调整图片大小以适应当前单元格大小
            Image scaledImage = GameImageManager.resizeImageToFit(image, width, height);

            // 缓存缩放后的图像
            scaledPieceImageCache.put(cacheKeyInt, scaledImage);
            return scaledImage;
        }
        return null;
    }

    /**
     * 重写paintComponent方法，确保在每次绘制时都考虑当前面板大小
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 检查面板大小变化，确保棋盘大小随窗口变化
        Dimension currentSize = getSize();
        if (!currentSize.equals(lastPanelSize) || forceUpdate) {
            lastPanelSize = currentSize;
            updateCellSizeBasedOnPanel();
            forceUpdate = false;
        }

        // 清空背景
        Graphics2D g2d = (Graphics2D) g;

        // 设置抗锯齿，提高图片质量
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        GameState2 gameState2 = gameLogic2.getGameState();
        if (gameState2 == null) {
            return;
        }
        Board2 board2 = gameState2.getBoard();
        if (board2 == null) {
            return;
        }

        // 绘制棋盘背景（如果有）
        Image boardBackground = GameImageManager.getBoardImage();
        if (boardBackground != null) {
            int panelWidth = getWidth();
            int panelHeight = getHeight();
            g2d.drawImage(boardBackground, 0, 0, panelWidth, panelHeight, this);
        }

        // 绘制所有空格和网格线
        for (int row = 0; row < board2.getHeight(); row++) {
            for (int col = 0; col < board2.getWidth(); col++) {
                int x = offsetX + col * cellSize;
                int y = offsetY + row * cellSize;

                // 绘制空格背景（如果此处没有棋子）
                if (board2.getBlockIdAt(col, row) == Board2.EMPTY_CELL_ID) {
                    Image emptyCellImage = GameImageManager.getEmptyCellImage();
                    if (emptyCellImage != null) {
                        // 缩放空白格子图片
                        g2d.drawImage(GameImageManager.resizeImageToFit(emptyCellImage, cellSize, cellSize),
                                x, y, cellSize, cellSize, this);
                    } else {
                        g2d.setColor(emptyCellColor);
                        g2d.fillRect(x, y, cellSize, cellSize);
                    }
                }

                // 绘制网格线（如果需要）
                if (showGridLines) {
                    g2d.setColor(gridColor);
                    g2d.drawRect(x, y, cellSize, cellSize);
                }
            }
        }

        // 绘制所有方块
        Map<Integer, Block2> allBlocks = board2.getBlocksCopy();
        for (Block2 block2 : allBlocks.values()) {
            int blockPixelX = offsetX + block2.getX() * cellSize;
            int blockPixelY = offsetY + block2.getY() * cellSize;
            int blockPixelWidth = block2.getWidth() * cellSize;
            int blockPixelHeight = block2.getHeight() * cellSize;

            // 获取当前皮肤模式
            int skinMode = GameImageManager.getSkinMode();

            if (skinMode == 0) {
                // 图片皮肤模式
                // 尝试加载并绘制棋子图片
                Image pieceImage = getPieceImage(block2.getId(), blockPixelWidth, blockPixelHeight);
                if (pieceImage != null) {
                    g2d.drawImage(pieceImage, blockPixelX, blockPixelY, blockPixelWidth, blockPixelHeight, this);
                } else {
                    // 如果没有找到图片，则使用纯色填充
                    g2d.setColor(blockColors.get(block2.getId()));
                    g2d.fillRect(blockPixelX, blockPixelY, blockPixelWidth, blockPixelHeight);

                    g2d.setColor(blockColors.get(block2.getId()).darker());
                    g2d.drawRect(blockPixelX, blockPixelY, blockPixelWidth - 1, blockPixelHeight - 1);
                }
            } else {
                // 纯色皮肤模式
                Color blockColor = blockColors.getOrDefault(block2.getId(), new Color(200, 200, 200));
                g2d.setColor(blockColor);
                g2d.fillRect(blockPixelX, blockPixelY, blockPixelWidth, blockPixelHeight);

                g2d.setColor(blockColor.darker());
                g2d.drawRect(blockPixelX, blockPixelY, blockPixelWidth - 1, blockPixelHeight - 1);

                // 在纯色模式下显示棋子名称
                if (showPieceNames) {
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
            }
        }

        // 绘制选中方块的高亮边框
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

    /**
     * 切换皮肤模式
     */
    private void toggleSkin() {
        // 使用GameImageManager切换皮肤模式
        GameImageManager.toggleSkinMode();

        // 根据当前皮肤模式更新是否显示棋子名称
        updatePieceNameVisibility();

        // 强制重绘
        forceUpdate = true;
        repaint();
    }

    /**
     * 根据当前皮肤模式更新是否显示棋子名称
     */
    private void updatePieceNameVisibility() {
        // 获取当前皮肤模式：0-图片皮肤，1-纯色皮肤
        int currentMode = GameImageManager.getSkinMode();
        // 在纯色模式下显示名称，图片模式下不显示
        showPieceNames = (currentMode == 1);
    }
}