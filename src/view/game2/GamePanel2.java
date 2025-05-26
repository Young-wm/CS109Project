package view.game2;

import controller2.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import java.awt.image.BufferedImage;
import java.awt.FontMetrics;

/*
* 这个类实现了整个完整的游戏panel
* 后续要用到的方法：每当游戏界面需要发生改变的时候（包括了鼠标点击的高亮，以及方块发生移动等等），都直接调用一次repaint()即可
* 游戏支持自适应缩放和图片背景
*/

public class GamePanel2 extends JPanel {

    private GameLogic2 gameLogic2;
    //这里必须加上一个gameLogic的field来找到里面对应的东西，
    // 但是在创建GamePanel相关对象的时候，千万不能创建新的，要传入GameFrame当中的gameLogic

    // 移除固定的cellSize，动态计算
    // int cellSize = 80;
    
    private int offsetX = 20;
    private int offsetY = 20;
    //同样为了后面方便美化页面,这里定义出这个panel边界的间距

    // 选中方块的边框颜色，定义为黄色
    private Color selectedBlockBorderColor = Color.YELLOW;
    // 网格线颜色，定义为深灰色
    private Color gridColor = Color.DARK_GRAY;
    
    // 定义棋子的纯色填充颜色
    private Map<Integer, Color> pieceColors = new HashMap<>();
    
    // 图片缓存，避免重复加载
    private Map<Integer, Image> pieceImageCache = new HashMap<>();
    // 是否显示网格线
    private boolean showGridLines = false;
    // 是否显示棋子名称（纯色模式下显示，图片模式下不显示）
    private boolean showPieceNames = true;

    // 皮肤切换按钮
    private JButton skinToggleButton = new JButton("切换皮肤");
    
    // 棋子动画管理器
    private BlockAnimator blockAnimator;
    
    // 离屏缓冲区
    private BufferedImage offscreenBuffer;
    // 缓冲区绘图对象
    private Graphics2D offscreenGraphics;
    
    public GamePanel2(GameLogic2 logic) {
        this.gameLogic2 = logic;
        //这里传入的就是GameFrame里面的gameLogic
        setPreferredSizeBasedOnBoard();
        //设置好panel的边界条件
        
        // 初始化颜色映射
        initPieceColors();
        
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
        
        // 初始化棋子动画管理器
        blockAnimator = new BlockAnimator(() -> repaint());
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleMouseClick(e.getX(), e.getY());
            }
            //补充：mousePressed指的是鼠标点击下去这个事件，mouseClicked指的是鼠标点下去再回弹的事件
        });
        //这里用适配器，来只实现我想要实现的方法
        
        // 添加组件监听器，在面板大小改变时触发重绘
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                // 面板大小变化时，清空离屏缓冲区和缓冲区绘图对象，以便重新创建
                offscreenBuffer = null;
                offscreenGraphics = null;
                
                clearImageCache(); // 清空图片缓存
                repaint(); // 重绘面板
                
                // 调整按钮位置
                skinToggleButton.setBounds(10, 10, 100, 30);
            }
        });
    }
    
    /**
     * 初始化棋子颜色映射（用于纯色模式）
     */
    private void initPieceColors() {
        // 为每个棋子分配一个颜色
        pieceColors.put(1, new Color(220, 20, 60));    // 曹操 - 红色
        pieceColors.put(2, new Color(0, 128, 0));      // 关羽 - 绿色
        pieceColors.put(3, new Color(70, 130, 180));   // 张飞 - 钢蓝色
        pieceColors.put(4, new Color(70, 130, 180));   // 赵云 - 钢蓝色
        pieceColors.put(5, new Color(70, 130, 180));   // 马超 - 钢蓝色
        pieceColors.put(6, new Color(70, 130, 180));   // 黄忠 - 钢蓝色
        pieceColors.put(7, new Color(255, 165, 0));    // 小兵 - 橙色
        pieceColors.put(8, new Color(255, 165, 0));    // 小兵 - 橙色
        pieceColors.put(9, new Color(255, 165, 0));    // 小兵 - 橙色
        pieceColors.put(10, new Color(255, 165, 0));   // 小兵 - 橙色
    }
    
    /**
     * 切换皮肤模式
     */
    private void toggleSkin() {
        // 使用GameImageManager切换皮肤模式
        GameImageManager.toggleSkinMode();
        
        // 根据当前皮肤模式更新是否显示棋子名称
        updatePieceNameVisibility();
        
        // 重绘面板
        clearImageCache();  // 清除图片缓存
        repaint();
        
        // 请求窗口焦点，确保键盘事件能够被正确捕获
        SwingUtilities.getWindowAncestor(this).requestFocusInWindow();
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

    private void setPreferredSizeBasedOnBoard() {
            Board2 board2 = gameLogic2.getGameState().getBoard();
            // 使用初始cellSize=80作为参考值来设置首选大小
            int initialCellSize = 80;
            int panelWidth = board2.getWidth() * initialCellSize + 2 * offsetX;
            // 总宽度 = 列数 * 单元格大小 + 2 * 偏移量 (左右)
            int panelHeight = board2.getHeight() * initialCellSize + 2 * offsetY;
            // 总高度 = 行数 * 单元格大小 + 2 * 偏移量 (上下)
            setPreferredSize(new Dimension(panelWidth, panelHeight));
    }
    //这个方法这样写后面可以非常容易地更改地图
    //可以自动地根据给出的地图去渲染出图形
    
    /**
     * 计算当前的单元格大小，基于面板当前尺寸和棋盘大小
     * @return 当前计算出的单元格大小
     */
    private int calculateCellSize() {
        Board2 board2 = gameLogic2.getGameState().getBoard();
        if (board2 == null) return 80; // 默认值
        
        // 计算可用于绘制的区域尺寸
        int panelWidth = getWidth() - 2 * offsetX;
        int panelHeight = getHeight() - 2 * offsetY;
        
        // 计算单元格大小，确保棋盘能完整显示
        int cellWidthBasedOnPanel = panelWidth / board2.getWidth();
        int cellHeightBasedOnPanel = panelHeight / board2.getHeight();
        
        // 取较小值，确保棋盘不会超出面板，并且保持正方形单元格
        int cellSize = Math.max(10, Math.min(cellWidthBasedOnPanel, cellHeightBasedOnPanel));
        
        return cellSize;
    }

    /**
     * 获取棋子图片，优先从缓存获取
     * @param pieceId 棋子ID
     * @return 对应的棋子图片
     */
    private Image getPieceImage(int pieceId) {
        // 首先尝试从缓存获取
        if (pieceImageCache.containsKey(pieceId)) {
            return pieceImageCache.get(pieceId);
        }
        
        // 从资源管理器获取图片
        Image pieceImage = GameImageManager.getPieceImage(pieceId);
        
        // 如果图片存在，则缓存并返回
        if (pieceImage != null) {
            pieceImageCache.put(pieceId, pieceImage);
            return pieceImage;
        }
        
        return null;
    }

    private void handleMouseClick(int mouseX, int mouseY) {
        if (gameLogic2.getGameState().isGameWon()) {
            return;
        }
        
        // 使用动态计算的单元格大小
        int cellSize = calculateCellSize();
        
        Board2 board2 = gameLogic2.getGameState().getBoard();
        if (board2 == null) return;
        
        // 计算棋盘实际绘制区域的偏移量，与paintComponent保持一致
        int boardPixelWidth = board2.getWidth() * cellSize;
        int boardPixelHeight = board2.getHeight() * cellSize;
        int actualOffsetX = offsetX + (getWidth() - 2 * offsetX - boardPixelWidth) / 2;
        int actualOffsetY = offsetY + (getHeight() - 2 * offsetY - boardPixelHeight) / 2;
        
        int gridX = (mouseX - actualOffsetX) / cellSize;
        int gridY = (mouseY - actualOffsetY) / cellSize;

        if (gridX >= 0 && gridX < board2.getWidth() && gridY >= 0 && gridY < board2.getHeight()) {
            boolean selectionChanged = gameLogic2.selectBlockAt(gridX, gridY);
            if (selectionChanged || gameLogic2.getSelectedBlock() != null) {
                repaint();
            }
            // 无论是否成功选中，都重绘以更新可能的选择高亮
        } else {
            String message = "It is invalid to click here.";
            String title = "Error";
            JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
        }
    }
    //这个方法实现了将鼠标点击的位置转化为坐标的形式，同时将被点击的坐标高亮，点击位置无效还会出现提示框报错

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // 确保游戏状态和棋盘有效
        GameState2 gameState2 = gameLogic2.getGameState();
        if (gameState2 == null){
            return;
        }
        Board2 board2 = gameState2.getBoard();
        if (board2 == null){
            return;
        }
        
        // 动态计算单元格大小
        int cellSize = calculateCellSize();
        
        // 计算棋盘实际绘制区域，使其在面板中居中
        int boardPixelWidth = board2.getWidth() * cellSize;
        int boardPixelHeight = board2.getHeight() * cellSize;
        int actualOffsetX = offsetX + (getWidth() - 2 * offsetX - boardPixelWidth) / 2;
        int actualOffsetY = offsetY + (getHeight() - 2 * offsetY - boardPixelHeight) / 2;
        
        // 如果缓冲区不存在或尺寸不匹配，创建新的缓冲区
        if (offscreenBuffer == null || offscreenBuffer.getWidth() != getWidth() || 
            offscreenBuffer.getHeight() != getHeight()) {
            offscreenBuffer = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
            offscreenGraphics = offscreenBuffer.createGraphics();
            
            // 设置抗锯齿和渲染提示
            offscreenGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            offscreenGraphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            offscreenGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        }
        
        // 清空缓冲区背景
        offscreenGraphics.setColor(getBackground());
        offscreenGraphics.fillRect(0, 0, getWidth(), getHeight());
        
        // 绘制棋盘背景（如果有）
        Image boardBackground = GameImageManager.getBoardImage(boardPixelWidth, boardPixelHeight);
        if (boardBackground != null) {
            offscreenGraphics.drawImage(boardBackground, actualOffsetX, actualOffsetY, boardPixelWidth, boardPixelHeight, this);
        } else {
            offscreenGraphics.setColor(Color.LIGHT_GRAY); // 画一个默认背景
            offscreenGraphics.fillRect(actualOffsetX, actualOffsetY, boardPixelWidth, boardPixelHeight);
        }

        // 绘制所有空格和网格线
        for (int row = 0; row < board2.getHeight(); row++) {
            for (int col = 0; col < board2.getWidth(); col++) {
                int x = actualOffsetX + col * cellSize;
                int y = actualOffsetY + row * cellSize;

                // 绘制空格（如果此处没有棋子）
                if (board2.getBlockIdAt(col, row) == Board2.EMPTY_CELL_ID) {
                    Image emptyCellImg = GameImageManager.getEmptyCellImage(cellSize);
                    if (emptyCellImg != null) {
                        offscreenGraphics.drawImage(emptyCellImg, x, y, cellSize, cellSize, this);
                    } else {
                        offscreenGraphics.setColor(new Color(200,200,200)); // 默认空格颜色
                        offscreenGraphics.fillRect(x, y, cellSize, cellSize);
                    }
                }

                // 绘制网格线（如果需要）
                if (showGridLines) {
                    offscreenGraphics.setColor(gridColor);
                    offscreenGraphics.drawRect(x, y, cellSize, cellSize);
                }
            }
        }

        // 绘制所有棋子
        Map<Integer, Block2> allBlocks = board2.getBlocksCopy();
        for (Block2 block : allBlocks.values()) {
            // 获取棋子的动画位置和大小
            Rectangle blockBounds = blockAnimator.getAnimatedBlockBounds(
                block, cellSize, actualOffsetX, actualOffsetY);
            
            if (blockBounds == null) continue;
            
            int blockPixelX = blockBounds.x;
            int blockPixelY = blockBounds.y;
            int blockPixelWidth = blockBounds.width;
            int blockPixelHeight = blockBounds.height;

            // 获取当前皮肤模式
            int skinMode = GameImageManager.getSkinMode();
            
            Image imageToDraw = null;

            // 尝试从AnimationState获取预取的动画专用图像
            BlockAnimator.AnimationState animState = blockAnimator.getAnimationState(block.getId());
            if (animState != null && animState.pieceImageForAnimation != null && blockAnimator.isBlockAnimating(block.getId())) {
                imageToDraw = animState.pieceImageForAnimation;
            }
            
            if (skinMode == 0) {
                if (imageToDraw == null) { // 如果没有预取的动画图像，或者不是在动画中，则正常获取
                    imageToDraw = getPieceImage(block.getId());
                    if (imageToDraw != null) {
                        // 缩放图片以适应当前单元格大小
                        imageToDraw = imageToDraw.getScaledInstance(blockPixelWidth, blockPixelHeight, Image.SCALE_SMOOTH);
                    }
                }

                if (imageToDraw != null) {
                    offscreenGraphics.drawImage(imageToDraw, blockPixelX, blockPixelY, blockPixelWidth, blockPixelHeight, this);
                } else {
                    // 图片加载失败或尺寸无效，使用纯色填充作为后备
                    drawFallbackPiece(offscreenGraphics, block, blockPixelX, blockPixelY, blockPixelWidth, blockPixelHeight);
                }
            } else {
                // 纯色皮肤模式
                drawFallbackPiece(offscreenGraphics, block, blockPixelX, blockPixelY, blockPixelWidth, blockPixelHeight);
            }
        }

        // 高亮显示选中的棋子
        Block2 selected = gameLogic2.getSelectedBlock();
        if (selected != null) {
            // 获取选中棋子的动画位置和大小
            Rectangle selBounds = blockAnimator.getAnimatedBlockBounds(
                selected, cellSize, actualOffsetX, actualOffsetY);
            
            if (selBounds != null) {
                int selX = selBounds.x;
                int selY = selBounds.y;
                int selWidth = selBounds.width;
                int selHeight = selBounds.height;
    
                offscreenGraphics.setColor(selectedBlockBorderColor);
                offscreenGraphics.setStroke(new BasicStroke(3)); // 边框粗细可以考虑随cellSize调整
                offscreenGraphics.drawRect(selX + 1, selY + 1, selWidth - 2, selHeight - 2);
                offscreenGraphics.setStroke(new BasicStroke(1));
            }
        }
        
        // 完成所有绘制后，将缓冲区内容一次性绘制到屏幕
        g.drawImage(offscreenBuffer, 0, 0, this);
    }
    
    /**
     * 绘制棋子的后备方案（纯色填充和名称）
     */
    private void drawFallbackPiece(Graphics2D g2d, Block2 block, int x, int y, int width, int height) {
        Color pieceColor = pieceColors.getOrDefault(block.getId(), new Color(200, 200, 200));
        g2d.setColor(pieceColor);
        g2d.fillRect(x, y, width, height);
        g2d.setColor(Color.DARK_GRAY);
        g2d.drawRect(x, y, width - 1, height - 1);
        
        if (showPieceNames) {
            g2d.setColor(Color.WHITE);
            String blockText = block.getName();
            FontMetrics fm = g2d.getFontMetrics();
            int stringWidth = fm.stringWidth(blockText);
            // 考虑文本可能比棋子宽的情况，避免裁剪
            if (stringWidth > width - 4) { // 留一点边距
                 // 可以选择缩小字体或截断文本，这里简单地不绘制如果太宽
            } else {
                int textX = x + (width - stringWidth) / 2;
                int textY = y + (height - fm.getHeight()) / 2 + fm.getAscent();
                 g2d.drawString(blockText, textX, textY);
            }
        }
    }

    /**
     * 设置是否显示网格线
     * @param show 是否显示
     */
    public void setShowGridLines(boolean show) {
        this.showGridLines = show;
        repaint();
    }
    
    /**
     * 清除图片缓存
     */
    public void clearImageCache() {
        pieceImageCache.clear();
    }
    
    /**
     * 设置自定义光标
     * @param imagePath 图像路径
     * @param hotspotX 热点X坐标
     * @param hotspotY 热点Y坐标
     */
    public void setCustomCursor(String imagePath, int hotspotX, int hotspotY) {
        // 空实现
    }
    
    /**
     * 恢复默认光标
     */
    public void restoreDefaultCursor() {
        // 空实现
    }
    
    /**
     * 设置是否显示鼠标轨迹
     * @param show 是否显示
     */
    public void setShowMouseTrack(boolean show) {
        // 空实现
    }
    
    /**
     * 设置鼠标轨迹颜色
     * @param color 颜色
     */
    public void setMouseTrackColor(Color color) {
        // 空实现
    }
    
    /**
     * 设置鼠标轨迹线宽
     * @param width 线宽
     */
    public void setMouseTrackWidth(float width) {
        // 空实现
    }

    /**
     * 开始棋子移动动画
     * @param block 要移动的棋子
     * @param direction 移动方向
     */
    public void animateBlockMove(Block2 block, Direction2 direction) {
        if (block != null && direction != null) {
            // 在动画开始前，为该棋子预取一个固定尺寸的图像
            int cellSize = calculateCellSize(); // 获取当前单元格大小
            int blockPixelWidth = block.getWidth() * cellSize;
            int blockPixelHeight = block.getHeight() * cellSize;
            
            Image animationImage = null;
            if (GameImageManager.getSkinMode() == 0) { // 仅在图片模式下预取
                animationImage = getPieceImage(block.getId());
                if (animationImage != null) {
                    animationImage = animationImage.getScaledInstance(blockPixelWidth, blockPixelHeight, Image.SCALE_SMOOTH);
                }
            }
            // 如果animationImage为null (例如纯色模式，或图片加载失败)，BlockAnimator的AnimationState中对应图像将为null
            // paintComponent中绘制时会进行判断

            blockAnimator.animateBlockMove(block, direction, animationImage);
        }
    }
    
    /**
     * 检查是否有动画正在进行
     * @return 是否有动画正在进行
     */
    public boolean isAnimating() {
        return blockAnimator.isAnimating();
    }
    
    /**
     * 取消所有正在进行的动画
     */
    public void cancelAnimations() {
        blockAnimator.cancelAnimations();
    }
    
    /**
     * 设置动画完成后的回调
     * @param callback 回调函数
     */
    public void setAnimationCompleteCallback(Runnable callback) {
        blockAnimator.setAnimationCompleteCallback(callback);
    }
    
    /**
     * 获取棋子动画管理器
     * @return 棋子动画管理器实例
     */
    public BlockAnimator getBlockAnimator() {
        return this.blockAnimator;
    }
}
