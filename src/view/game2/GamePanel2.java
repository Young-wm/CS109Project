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
                // 添加动画检查
                if (isAnimating()) {
                    return;
                }
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

        // 使用双缓冲绘制，防止闪烁
        if (offscreenBuffer == null || offscreenBuffer.getWidth() != getWidth() || offscreenBuffer.getHeight() != getHeight()) {
            offscreenBuffer = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
            if (offscreenGraphics != null) {
                offscreenGraphics.dispose(); // 释放旧的图形上下文
            }
            offscreenGraphics = offscreenBuffer.createGraphics();
            // 开启抗锯齿
            offscreenGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            offscreenGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        }

        // 清空缓冲区背景（透明）
        offscreenGraphics.setComposite(AlphaComposite.Clear);
        offscreenGraphics.fillRect(0, 0, getWidth(), getHeight());
        offscreenGraphics.setComposite(AlphaComposite.SrcOver);

        // 绘制棋盘背景（如果GameImageManager提供）
        Image boardImage = GameImageManager.getBoardImage();
        if (boardImage != null) {
            offscreenGraphics.drawImage(boardImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            // 如果没有棋盘背景图片，绘制一个简单的颜色背景
            offscreenGraphics.setColor(new Color(230, 230, 230)); // 淡灰色背景
            offscreenGraphics.fillRect(0, 0, getWidth(), getHeight());
        }
        
        // 动态计算单元格大小和偏移量
        int cellSize = calculateCellSize();
        int boardPixelWidth = board2.getWidth() * cellSize;
        int boardPixelHeight = board2.getHeight() * cellSize;
        
        // 使棋盘在面板中居中绘制
        int actualOffsetX = offsetX + (getWidth() - 2 * offsetX - boardPixelWidth) / 2;
        int actualOffsetY = offsetY + (getHeight() - 2 * offsetY - boardPixelHeight) / 2;

        // 绘制网格线 (如果启用)
        if (showGridLines) {
            offscreenGraphics.setColor(gridColor);
            for (int i = 0; i <= board2.getHeight(); i++) { // 横线
                offscreenGraphics.drawLine(actualOffsetX, actualOffsetY + i * cellSize, actualOffsetX + boardPixelWidth, actualOffsetY + i * cellSize);
            }
            for (int i = 0; i <= board2.getWidth(); i++) { // 竖线
                offscreenGraphics.drawLine(actualOffsetX + i * cellSize, actualOffsetY, actualOffsetX + i * cellSize, actualOffsetY + boardPixelHeight);
            }
        }

        // 绘制棋子
        if (board2.getBlocksCopy() != null && board2.getBlocksCopy().values() != null) {
            for (Block2 block : board2.getBlocksCopy().values()) { // Iterating over values of the map
                if (block == null) continue;

                // 获取动画计算后的棋子边界
                Rectangle blockBounds = blockAnimator.getAnimatedBlockBounds(block, cellSize, actualOffsetX, actualOffsetY);
                if (blockBounds == null || blockBounds.width <= 0 || blockBounds.height <= 0) { // 增加对无效宽高的检查
                    // 如果获取失败或尺寸无效，则使用静态位置和默认大小
                    blockBounds = new Rectangle(
                        actualOffsetX + block.getX() * cellSize,
                        actualOffsetY + block.getY() * cellSize,
                        block.getWidth() * cellSize,
                        block.getHeight() * cellSize
                    );
                    if (blockBounds.width <= 0 || blockBounds.height <= 0) continue; // 再次检查，如果还是无效则跳过此棋子
                }

                Image pieceImage = null;
                Image finalImageToDraw = null;

                // 尝试从BlockAnimator的AnimationState获取预取的图像 (如果正在动画中且有预取)
                BlockAnimator.AnimationState animState = blockAnimator.getAnimationState(block.getId());
                if (animState != null && animState.pieceImageForAnimation != null && blockAnimator.isBlockAnimating(block.getId())) {
                    // 如果动画状态中有预取图像，并且该图像已经适应了动画的某种特定尺寸需求，则直接使用
                    // 假设 animState.pieceImageForAnimation 已经是正确缩放的，或者 BlockAnimator 内部会处理
                    finalImageToDraw = animState.pieceImageForAnimation;
                } else {
                    // 否则，正常从GameImageManager获取原始图片
                    pieceImage = GameImageManager.getPieceImage(block.getId());
                    if (pieceImage != null) {
                        // 手动缩放图片以适应当前 blockBounds 的尺寸
                        finalImageToDraw = pieceImage.getScaledInstance(blockBounds.width, blockBounds.height, Image.SCALE_SMOOTH);
                    }
                }

                if (finalImageToDraw != null) {
                    offscreenGraphics.drawImage(finalImageToDraw, blockBounds.x, blockBounds.y, blockBounds.width, blockBounds.height, this);
                } else {
                    // 图片缺失或纯色模式下的回退绘制逻辑
                    drawFallbackPiece(offscreenGraphics, block, blockBounds.x, blockBounds.y, blockBounds.width, blockBounds.height);
                }

                // 绘制选中边框
                if (block.equals(gameLogic2.getSelectedBlock())) {
                    offscreenGraphics.setColor(selectedBlockBorderColor);
                    offscreenGraphics.setStroke(new BasicStroke(3)); // 设置边框粗细
                    offscreenGraphics.drawRect(blockBounds.x, blockBounds.y, blockBounds.width, blockBounds.height);
                }
            }
        }
        
        // 将离屏缓冲区内容绘制到屏幕
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
            
            if (stringWidth < width - 4) {
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
        if (blockAnimator == null || block == null || direction == null) {
            return;
        }
        // 从GameImageManager获取当前棋子应该显示的图片（GameImageManager应处理缩放）
        // 为了动画，我们可能需要原始未缩放的图像，或者一个适合动画初始状态的尺寸
        // 暂时我们假设GameImageManager能提供一个合适的图像，或者BlockAnimator内部会处理
        Image pieceImage = GameImageManager.getPieceImage(block.getId()); // 获取基础图片

        // 如果用纯色，可以传递null或者一个预渲染的纯色块图像
        if (GameImageManager.getSkinMode() == 1) { // 1 代表纯色模式
            // 对于纯色模式，可以创建一个临时的纯色图像传递给动画器，或者让动画器根据ID处理
            // 为简化，这里可以传null，让BlockAnimator的getAnimatedBlockBounds在绘制时再决定颜色
            pieceImage = null; 
        }

        blockAnimator.animateBlockMove(block, direction, pieceImage);
    }
    
    /**
     * 检查是否有动画正在进行
     * @return 是否有动画正在进行
     */
    public boolean isAnimating() {
        return blockAnimator != null && blockAnimator.isAnimating();
    }
    
    /**
     * 取消所有正在进行的动画
     */
    public void cancelAnimations() {
        if (blockAnimator != null) {
            blockAnimator.cancelAnimations();
        }
        repaint();
    }
    
    /**
     * 设置动画完成后的回调
     * @param callback 回调函数
     */
    public void setAnimationCompleteCallback(Runnable callback) {
        if (blockAnimator != null) {
            blockAnimator.setAnimationCompleteCallback(callback);
        }
    }
    
    /**
     * 获取棋子动画管理器
     * @return 棋子动画管理器实例
     */
    public BlockAnimator getBlockAnimator() {
        return blockAnimator;
    }
    
    /**
     * 获取当前的单元格大小
     * @return 当前计算出的单元格大小
     */
    public int getCellSize() {
        return calculateCellSize();
    }
    
    /**
     * 获取棋盘绘制区域的实际X轴偏移量 (考虑面板居中)
     * @return X轴偏移量
     */
    public int getActualOffsetX() {
        Board2 board2 = gameLogic2.getGameState().getBoard();
        if (board2 == null) return offsetX; // 默认值
        int cellSize = calculateCellSize();
        int boardPixelWidth = board2.getWidth() * cellSize;
        return offsetX + (getWidth() - 2 * offsetX - boardPixelWidth) / 2;
    }

    /**
     * 获取棋盘绘制区域的实际Y轴偏移量 (考虑面板居中)
     * @return Y轴偏移量
     */
    public int getActualOffsetY() {
        Board2 board2 = gameLogic2.getGameState().getBoard();
        if (board2 == null) return offsetY; // 默认值
        int cellSize = calculateCellSize();
        int boardPixelHeight = board2.getHeight() * cellSize;
        return offsetY + (getHeight() - 2 * offsetY - boardPixelHeight) / 2;
    }
}
