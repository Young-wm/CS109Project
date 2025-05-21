package view.game4;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * 游戏4棋盘和棋子图片资源管理器
 * 用于加载和管理游戏4中棋盘和棋子的图片资源
 */
public class GameImageManager {
    // 棋子图片缓存
    private static final Map<Integer, Image> pieceImageCache = new HashMap<>();
    // 棋盘背景图片
    private static Image boardBackgroundImage;
    // 空白格子图片
    private static Image emptyCellImage;
    
    // 图片加载路径
    private static final String IMAGE_PATH = "src/view/game4/images/";
    private static final String BOARD_IMAGE_NAME = "board_background.jpg";
    private static final String EMPTY_CELL_IMAGE_NAME = "empty_cell.jpg";
    private static final String PIECE_IMAGE_PREFIX = "piece_";
    
    // 皮肤模式：0 - 图片皮肤, 1 - 纯色皮肤
    private static int skinMode = 0;
    
    // 是否已初始化
    private static boolean initialized = false;
    
    /**
     * 初始化图片资源管理器
     * 加载棋盘背景、空白格和所有棋子图片
     */
    public static void initialize() {
        if (initialized) return;
        
        // 创建images目录（如果不存在）
        File imageDir = new File(IMAGE_PATH);
        if (!imageDir.exists()) {
            imageDir.mkdirs();
        }
        
        // 加载棋盘背景
        boardBackgroundImage = loadImage(BOARD_IMAGE_NAME);
        
        // 加载空白格子图片
        emptyCellImage = loadImage(EMPTY_CELL_IMAGE_NAME);
        
        // 预加载棋子图片（ID 1-10）
        for (int i = 1; i <= 10; i++) {
            Image pieceImage = loadImage(PIECE_IMAGE_PREFIX + i + ".jpg");
            if (pieceImage != null) {
                pieceImageCache.put(i, pieceImage);
            }
        }
        
        initialized = true;
    }
    
    /**
     * 切换皮肤模式
     * @return 返回切换后的皮肤模式（0-图片皮肤，1-纯色皮肤）
     */
    public static int toggleSkinMode() {
        skinMode = (skinMode + 1) % 2;  // 在0和1之间切换
        return skinMode;
    }
    
    /**
     * 获取当前皮肤模式
     * @return 当前皮肤模式（0-图片皮肤，1-纯色皮肤）
     */
    public static int getSkinMode() {
        return skinMode;
    }
    
    /**
     * 获取棋子图片
     * @param pieceId 棋子ID
     * @return 对应的棋子图片，如果不存在则返回null
     */
    public static Image getPieceImage(int pieceId) {
        if (!initialized) initialize();
        if (skinMode == 1) {
            return null;  // 纯色模式不使用棋子图片
        }
        return pieceImageCache.get(pieceId);
    }
    
    /**
     * 获取棋盘背景图片
     * @return 棋盘背景图片
     */
    public static Image getBoardImage() {
        if (!initialized) initialize();
        if (skinMode == 1) {
            return null;  // 纯色模式不使用背景图片
        }
        return boardBackgroundImage;
    }
    
    /**
     * 获取空白格子图片
     * @return 空白格子图片
     */
    public static Image getEmptyCellImage() {
        if (!initialized) initialize();
        if (skinMode == 1) {
            return null;  // 纯色模式不使用空格图片
        }
        return emptyCellImage;
    }
    
    /**
     * 加载图片资源
     * @param imageName 图片名称
     * @return 加载的图片，如果加载失败则返回null
     */
    private static Image loadImage(String imageName) {
        Image image = null;
        
        try {
            // 尝试从文件系统加载
            File file = new File(IMAGE_PATH + imageName);
            if (file.exists()) {
                image = ImageIO.read(file);
            } else {
                // 尝试从类路径加载
                URL resource = GameImageManager.class.getResource("/view/game4/images/" + imageName);
                if (resource != null) {
                    image = ImageIO.read(resource);
                }
            }
            
            // 如果图片加载失败，打印错误信息
            if (image == null) {
                System.err.println("无法加载图片: " + imageName + "，图片文件可能不存在");
            }
        } catch (IOException e) {
            System.err.println("加载图片时出错: " + imageName);
            e.printStackTrace();
        }
        
        return image;
    }
    
    /**
     * 确保图片完全加载
     * @param image 要加载的图片
     * @return 加载完成的图片
     */
    public static Image ensureImageLoaded(Image image) {
        if (image == null) {
            return null;
        }
        
        MediaTracker tracker = new MediaTracker(new JPanel());
        tracker.addImage(image, 0);
        try {
            tracker.waitForID(0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        return image;
    }
    
    /**
     * 调整图片大小，适应指定区域，保持原比例
     * @param image 原始图片
     * @param targetWidth 目标宽度
     * @param targetHeight 目标高度
     * @return 调整后的图片
     */
    public static Image resizeImageToFit(Image image, int targetWidth, int targetHeight) {
        if (image == null) {
            return null;
        }
        
        // 确保图片已加载，避免尺寸获取问题
        image = ensureImageLoaded(image);
        
        int imgWidth = image.getWidth(null);
        int imgHeight = image.getHeight(null);
        
        if (imgWidth <= 0 || imgHeight <= 0) {
            return image; // 无法获取尺寸，返回原图
        }
        
        // 计算缩放比例
        double widthRatio = (double) targetWidth / imgWidth;
        double heightRatio = (double) targetHeight / imgHeight;
        double ratio = Math.min(widthRatio, heightRatio);
        
        int newWidth = (int) (imgWidth * ratio);
        int newHeight = (int) (imgHeight * ratio);
        
        // 确保新尺寸至少为1像素
        newWidth = Math.max(1, newWidth);
        newHeight = Math.max(1, newHeight);
        
        // 使用更高质量的缩放方式
        if (image instanceof BufferedImage) {
            // 对于BufferedImage，使用更高质量的缩放算法
            BufferedImage bufferedImage = (BufferedImage) image;
            BufferedImage scaledImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = scaledImage.createGraphics();
            
            // 设置高质量缩放选项
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            g2d.drawImage(bufferedImage, 0, 0, newWidth, newHeight, null);
            g2d.dispose();
            
            return scaledImage;
        } else {
            // 对于普通Image，使用getScaledInstance
            return image.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        }
    }
} 