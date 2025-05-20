package view.game3;

import controller3.*;

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

public class GamePanel3 extends JPanel {

    private GameLogic3 gameLogic3;
    //这里传入的就是GameFrame里面的gameLogic
    private int baseCellSize = 80; // 基础单元格大小
    private int cellSize = baseCellSize; // 当前使用的单元格大小，会根据面板大小动态调整
    private int offsetX = 20;
    private int offsetY = 20;
    //同样为了后面方便美化页面,这里定义出这个panel边界的间距
    private Map<Integer, Color> blockColors = new HashMap<>();
    //前面的Map的每一个ID对应一个Block，所以这里也通过一个Map的形式来储存所有的颜色
    
    // 皮肤切换按钮
    private JButton skinToggleButton = new JButton("切换皮肤");
    private Color selectedBlockBorderColor = Color.YELLOW;
    // 选中方块的边框颜色，定义为黄色
    private Color gridColor = Color.DARK_GRAY;
    // 网格线颜色，定义为深灰色
    private Color emptyCellColor = Color.LIGHT_GRAY;
    // 空格颜色定义为亮灰色
    
    // 是否显示网格线
    private boolean showGridLines = false;
    // 是否显示棋子名称
    private boolean showPieceNames = true;

    public GamePanel3(GameLogic3 logic) {
        this.gameLogic3 = logic;
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
        
        // 启用双缓冲，减少闪烁，提高渲染性能
        setDoubleBuffered(true);

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
                updateCellSizeBasedOnPanel();
            }
        });
    }

    /**
     * 根据面板大小更新单元格大小
     */
    private void updateCellSizeBasedOnPanel() {
        if (getWidth() <= 0 || getHeight() <= 0) return;
        
        Board3 board3 = gameLogic3.getGameState().getBoard();
        
        // 计算可用的棋盘区域大小
        int availableWidth = getWidth() - (2 * offsetX);
        int availableHeight = getHeight() - (2 * offsetY);
        
        // 计算每个方向上的单元格大小
        int cellSizeFromWidth = availableWidth / board3.getWidth();
        int cellSizeFromHeight = availableHeight / board3.getHeight();
        
        // 使用较小的值作为单元格大小，确保完整显示
        int newCellSize = Math.min(cellSizeFromWidth, cellSizeFromHeight);
        
        // 确保单元格大小不会低于一个最小值（可以根据需要调整）
        int minCellSize = 20;
        newCellSize = Math.max(newCellSize, minCellSize);
        
        // 更新单元格大小（如果有变化）
        if (cellSize != newCellSize) {
            cellSize = newCellSize;
            
            // 重新计算偏移量，使棋盘居中
            offsetX = (getWidth() - (cellSize * board3.getWidth())) / 2;
            offsetY = (getHeight() - (cellSize * board3.getHeight())) / 2;
            
            repaint(); // 重绘棋盘
        }
    }

    private void setPreferredSizeBasedOnBoard() {
            Board3 board3 = gameLogic3.getGameState().getBoard();
            int panelWidth = board3.getWidth() * baseCellSize + 2 * offsetX;
            // 总宽度 = 列数 * 单元格大小 + 2 * 偏移量 (左右)
            int panelHeight = board3.getHeight() * baseCellSize + 2 * offsetY;
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
        if (gameLogic3.getGameState().isGameWon()) {
            return;
        }

        int gridX = (mouseX - offsetX) / cellSize;
        // (mouseX - offsetX) / cellSize 得到的是列号 gridX
        int gridY = (mouseY - offsetY) / cellSize;
        // (mouseY - offsetY) / cellSize 得到的是行号 gridY

        if (gridX >= 0 && gridX < gameLogic3.getGameState().getBoard().getWidth() && gridY >= 0 && gridY < gameLogic3.getGameState().getBoard().getHeight()) {
            boolean selectionChanged = gameLogic3.selectBlockAt(gridX, gridY);
             if (selectionChanged || gameLogic3.getSelectedBlock() != null) {
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
    //这个方法实现了将鼠标点击的位置转化为坐标的形式，同时将被点击的坐标高亮，点击位置无效还会出现提示框报错    /**
     /* 获取棋子图片，优先从缓存获取
     * @param pieceId 棋子ID
     * @return 对应的棋子图片
     */
    private Image getPieceImage(int pieceId, int width, int height) {
        // 检查当前皮肤模式
        if (GameImageManager.getSkinMode() == 1) {
            // 纯色皮肤模式下返回null，让调用者使用纯色绘制
            return null;
        }
        
        Image image = GameImageManager.getPieceImage(pieceId);
        if (image != null) {
            // 调整图片大小以适应当前单元格大小
            return GameImageManager.resizeImageToFit(image, width, height);
        }
        return null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Apply rendering hints
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        GameState3 gameState3 = this.gameLogic3.getGameState();
        if (gameState3 == null || gameState3.getBoard() == null) {
            String message = "Game data not available. Waiting for initialization...";
            FontMetrics fm = g2d.getFontMetrics();
            int stringWidth = fm.stringWidth(message);
            g2d.drawString(message, (getWidth() - stringWidth) / 2, getHeight() / 2);
            return;
        }
        Board3 board3 = gameState3.getBoard();

        if (getWidth() <= 0 || getHeight() <= 0) { // Panel not ready or not visible
            return;
        }
        
        if (this.cellSize <= 0) {
            updateCellSizeBasedOnPanel();
            if (this.cellSize <= 0) {
                 String message = "Error: Cell size invalid. Panel may not be properly sized.";
                 FontMetrics fm = g2d.getFontMetrics();
                 int stringWidth = fm.stringWidth(message);
                 g2d.drawString(message, (getWidth() - stringWidth) / 2, getHeight() / 2);
                return;
            }
        }

        int currentSkinMode = GameImageManager.getSkinMode();
        
        int boardPixelWidth = board3.getWidth() * this.cellSize;
        int boardPixelHeight = board3.getHeight() * this.cellSize;

        // 1. Draw Board Background
        if (currentSkinMode == 0) { // Image mode
            Image boardBackgroundImage = GameImageManager.getBoardImage();
            if (boardBackgroundImage != null) {
                Image scaledBoardBg = GameImageManager.resizeImageToFit(boardBackgroundImage, boardPixelWidth, boardPixelHeight);
                g2d.drawImage(scaledBoardBg != null ? scaledBoardBg : boardBackgroundImage, this.offsetX, this.offsetY, boardPixelWidth, boardPixelHeight, this);
            } else {
                g2d.setColor(Color.LIGHT_GRAY); 
                g2d.fillRect(this.offsetX, this.offsetY, boardPixelWidth, boardPixelHeight);
            }
        } else { // Solid color mode
            g2d.setColor(getBackground() != null ? getBackground().darker() : Color.DARK_GRAY); 
            g2d.fillRect(this.offsetX, this.offsetY, boardPixelWidth, boardPixelHeight);
        }

        // 2. Draw Empty Cells and Grid Lines
        for (int row = 0; row < board3.getHeight(); row++) {
            for (int col = 0; col < board3.getWidth(); col++) {
                int x = this.offsetX + col * this.cellSize;
                int y = this.offsetY + row * this.cellSize;

                if (board3.getBlockIdAt(col, row) == Board3.EMPTY_CELL_ID) {
                    if (currentSkinMode == 0) { // Image mode
                        Image emptyCellImg = GameImageManager.getEmptyCellImage();
                        if (emptyCellImg != null) {
                            Image scaledEmptyImg = GameImageManager.resizeImageToFit(emptyCellImg, this.cellSize, this.cellSize);
                            g2d.drawImage(scaledEmptyImg != null ? scaledEmptyImg : emptyCellImg, x, y, this.cellSize, this.cellSize, this);
                        } else {
                            g2d.setColor(this.emptyCellColor != null ? this.emptyCellColor.brighter() : Color.WHITE); 
                            g2d.fillRect(x, y, this.cellSize, this.cellSize);
                        }
                    } else { // Solid color mode
                        g2d.setColor(this.emptyCellColor != null ? this.emptyCellColor : Color.LIGHT_GRAY);
                        g2d.fillRect(x, y, this.cellSize, this.cellSize);
                    }
                }

                if (this.showGridLines) {
                    g2d.setColor(this.gridColor != null ? this.gridColor : Color.DARK_GRAY);
                    g2d.drawRect(x, y, this.cellSize, this.cellSize);
                }
            }
        }

        // 3. Draw Pieces
        Map<Integer, Block3> allBlocks = board3.getBlocksCopy();
        if (allBlocks != null) {
            for (Block3 block : allBlocks.values()) {
                if (block == null) continue;

                int blockPixelX = this.offsetX + block.getX() * this.cellSize;
                int blockPixelY = this.offsetY + block.getY() * this.cellSize;
                int blockPixelWidth = block.getWidth() * this.cellSize;
                int blockPixelHeight = block.getHeight() * this.cellSize;

                if (currentSkinMode == 0) { // Image mode
                    Image pieceImage = getPieceImage(block.getId(), blockPixelWidth, blockPixelHeight); 
                    if (pieceImage != null) {
                        g2d.drawImage(pieceImage, blockPixelX, blockPixelY, blockPixelWidth, blockPixelHeight, this);
                    } else {
                        Color fallbackColor = this.blockColors.getOrDefault(block.getId(), new Color(180, 180, 180));
                        g2d.setColor(fallbackColor);
                        g2d.fillRect(blockPixelX, blockPixelY, blockPixelWidth, blockPixelHeight);
                        g2d.setColor(Color.DARK_GRAY);
                        g2d.drawRect(blockPixelX, blockPixelY, blockPixelWidth - 1, blockPixelHeight - 1);
                    }
                } else { // Solid color mode (currentSkinMode == 1)
                    Color pieceColor = this.blockColors.getOrDefault(block.getId(), new Color(200, 200, 200));
                    g2d.setColor(pieceColor);
                    g2d.fillRect(blockPixelX, blockPixelY, blockPixelWidth, blockPixelHeight);
                    
                    g2d.setColor(pieceColor.darker()); 
                    g2d.drawRect(blockPixelX, blockPixelY, blockPixelWidth - 1, blockPixelHeight - 1);
                    
                    if (this.showPieceNames) { 
                        g2d.setColor(Color.WHITE); 
                        String blockText = block.getName();
                        if (blockText != null && !blockText.isEmpty()) {
                            FontMetrics fm = g2d.getFontMetrics();
                            int stringWidth = fm.stringWidth(blockText);
                            int textX = blockPixelX + (blockPixelWidth - stringWidth) / 2;
                            int textY = blockPixelY + (blockPixelHeight - fm.getHeight()) / 2 + fm.getAscent();
                            g2d.drawString(blockText, textX, textY);
                        }
                    }
                }
            }
        }

        // 4. Highlight Selected Block
        Block3 selected = this.gameLogic3.getSelectedBlock();
        if (selected != null) {
            int selX = this.offsetX + selected.getX() * this.cellSize;
            int selY = this.offsetY + selected.getY() * this.cellSize;
            int selWidth = selected.getWidth() * this.cellSize;
            int selHeight = selected.getHeight() * this.cellSize;

            g2d.setColor(this.selectedBlockBorderColor != null ? this.selectedBlockBorderColor : Color.YELLOW);
            g2d.setStroke(new BasicStroke(3)); 
            g2d.drawRect(selX + 1, selY + 1, selWidth - 2, selHeight - 2); 
            g2d.setStroke(new BasicStroke(1)); 
        }
    }

    /**
     * 设置是否显示网格线
     * @param show 是否显示
     */
    public void setShowGridLines(boolean show) {
        if (this.showGridLines != show) {
            this.showGridLines = show;
            repaint();
        }
    }
    
    /**
     * 设置是否显示棋子名称
     * @param show 是否显示
     */
    public void setShowPieceNames(boolean show) {
        if (this.showPieceNames != show) {
            this.showPieceNames = show;
            repaint();
        }
    }
    
    /**
     * 切换皮肤模式
     */
    private void toggleSkin() {
        // 使用GameImageManager切换皮肤模式
        GameImageManager.toggleSkinMode();
        
        // 根据当前皮肤模式更新是否显示棋子名称
        updatePieceNameVisibility();
        
        // 刷新缓存和重绘
        clearImageCache();
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
    
    /**
     * 清除图片缓存
     */
    public void clearImageCache() {
        // 清空图片缓存，强制重新加载所有图片
    }
}