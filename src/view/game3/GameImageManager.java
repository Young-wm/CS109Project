package view.game3;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
//import java.awt.image.GraphicsConfiguration;
//import java.awt.image.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * 游戏3棋盘和棋子图片资源管理器
 * 用于加载和管理游戏3中棋盘和棋子的图片资源
 */
public class GameImageManager {
    // 棋子图片缓存
    private static final Map<Integer, Image> pieceImageCache = new HashMap<>();
    // 棋盘背景图片
    private static Image boardBackgroundImage;
    // 空白格子图片
    private static Image emptyCellImage;
    // 缓存调整大小后的图片
    private static final Map<String, Image> resizedImageCache = new HashMap<>();
    
    // 皮肤模式：0 - 图片皮肤, 1 - 纯色皮肤
    private static int skinMode = 0;
    
    // 图片加载路径
    private static final String IMAGE_PATH = "src/view/game3/images/";
    private static final String BOARD_IMAGE_NAME = "board_background.jpg";
    private static final String EMPTY_CELL_IMAGE_NAME = "empty_cell.jpg";
    private static final String PIECE_IMAGE_PREFIX = "piece_";
    
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
        // 清空缩放图片缓存，以便使用新的皮肤
        resizedImageCache.clear();
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
                URL resource = GameImageManager.class.getResource("/view/game3/images/" + imageName);
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
        
        // 生成缓存键（源图片哈希码+目标宽高）
        String cacheKey = image.hashCode() + "_" + targetWidth + "x" + targetHeight;
        
        // 检查缓存中是否已有调整大小的图片
        if (resizedImageCache.containsKey(cacheKey)) {
            return resizedImageCache.get(cacheKey);
        }
        
        int imgWidth = image.getWidth(null);
        int imgHeight = image.getHeight(null);
        
        if (imgWidth <= 0 || imgHeight <= 0) {
            image = ensureImageLoaded(image);
            imgWidth = image.getWidth(null);
            imgHeight = image.getHeight(null);
            
            if (imgWidth <= 0 || imgHeight <= 0) {
                return image; // 无法获取尺寸，返回原图
            }
        }
        
        // 计算缩放比例
        double widthRatio = (double) targetWidth / imgWidth;
        double heightRatio = (double) targetHeight / imgHeight;
        double ratio = Math.min(widthRatio, heightRatio);
        
        int newWidth = (int) (imgWidth * ratio);
        int newHeight = (int) (imgHeight * ratio);
        
        // 使用更高质量的图像缩放方法
        Image resizedImage;
        if (newWidth > 0 && newHeight > 0) {
            // 创建一个高质量的缓冲图像
            GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice().getDefaultConfiguration();
            BufferedImage bufferedImage = gc.createCompatibleImage(newWidth, newHeight, Transparency.TRANSLUCENT);
            
            Graphics2D g2d = bufferedImage.createGraphics();
            try {
                // 设置高质量的图像渲染提示
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
                
                // 绘制图像
                g2d.drawImage(image, 0, 0, newWidth, newHeight, null);
            } finally {
                g2d.dispose();
            }
            
            resizedImage = bufferedImage;
        } else {
            // 如果计算出的尺寸无效，使用原始缩放方法
            resizedImage = image.getScaledInstance(Math.max(1, newWidth), Math.max(1, newHeight), Image.SCALE_SMOOTH);
        }
        
        // 缓存调整大小后的图片
        // 如果缓存过大，可以考虑限制缓存大小或定期清理缓存
        if (resizedImageCache.size() > 100) {
            resizedImageCache.clear(); // 简单的缓存管理策略，当缓存过大时清空
        }
        resizedImageCache.put(cacheKey, resizedImage);
        
        return resizedImage;
    }
    
    /**
     * 清理图片缓存
     * 在不需要图片资源时调用此方法释放内存
     */
    public static void clearCache() {
        pieceImageCache.clear();
        resizedImageCache.clear();
        boardBackgroundImage = null;
        emptyCellImage = null;
        System.gc(); // 建议JVM进行垃圾回收
    }
} 