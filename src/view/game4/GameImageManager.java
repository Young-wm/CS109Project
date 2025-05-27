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
import java.util.Objects;

/**
 * 游戏棋盘和棋子图片资源管理器
 * 用于加载和管理游戏中棋盘和棋子的图片资源
 */
public class GameImageManager {
    // 原始棋子图片缓存
    private static final Map<Integer, Image> originalPieceImageCache = new HashMap<>();
    // 缩放后的棋子图片缓存, Key为 PieceImageCacheKey 对象
    private static final Map<PieceImageCacheKey, Image> scaledPieceImageCache = new HashMap<>();
    // 棋盘背景图片
    private static Image boardBackgroundImage;
    // 空白格子图片
    private static Image emptyCellImage;

    // 图片加载路径
    private static final String IMAGE_PATH = "src/view/game/images/";
    private static final String BOARD_IMAGE_NAME = "board_background.jpg";
    private static final String EMPTY_CELL_IMAGE_NAME = "empty_cell.png";
    private static final String PIECE_IMAGE_PREFIX = "piece_";

    // 皮肤模式：0 - 图片皮肤, 1 - 纯色皮肤
    private static int skinMode = 0;

    // 是否已初始化
    private static boolean initialized = false;

    // 内部类作为缩放图片缓存的Key
    private static class PieceImageCacheKey {
        final int pieceId;
        final int targetWidth;
        final int targetHeight;

        public PieceImageCacheKey(int pieceId, int targetWidth, int targetHeight) {
            this.pieceId = pieceId;
            this.targetWidth = targetWidth;
            this.targetHeight = targetHeight;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PieceImageCacheKey that = (PieceImageCacheKey) o;
            return pieceId == that.pieceId &&
                    targetWidth == that.targetWidth &&
                    targetHeight == that.targetHeight;
        }

        @Override
        public int hashCode() {
            return Objects.hash(pieceId, targetWidth, targetHeight);
        }
    }

    /**
     * 初始化图片资源管理器
     * 加载棋盘背景、空白格和所有棋子图片
     */
    public static void initialize() {
        if (initialized) return;

        File imageDir = new File(IMAGE_PATH);
        if (!imageDir.exists()) {
            imageDir.mkdirs();
        }

        boardBackgroundImage = ensureImageLoaded(loadImage(BOARD_IMAGE_NAME));
        emptyCellImage = ensureImageLoaded(loadImage(EMPTY_CELL_IMAGE_NAME));

        for (int i = 1; i <= 10; i++) {
            Image pieceImage = ensureImageLoaded(loadImage(PIECE_IMAGE_PREFIX + i + ".png"));
            if (pieceImage != null) {
                originalPieceImageCache.put(i, pieceImage);
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
        clearScaledImageCache(); // 切换皮肤时清除缩放缓存
        return skinMode;
    }

    /**
     * 清除缩放图片缓存 (例如，在面板大小改变或切换皮肤时调用)
     */
    public static void clearScaledImageCache() {
        scaledPieceImageCache.clear();
        // 注意：棋盘背景和空格图片如果也需要针对不同尺寸缓存，也应在此处处理
        // 目前的实现是每次getBoardImage/getEmptyCellImage时都可能重新缩放，可进一步优化
    }

    /**
     * 获取当前皮肤模式
     * @return 当前皮肤模式（0-图片皮肤，1-纯色皮肤）
     */
    public static int getSkinMode() {
        return skinMode;
    }

    /**
     * 获取原始棋子图片
     * @param pieceId 棋子ID
     * @return 对应的原始棋子图片，如果不存在则返回null
     */
    private static Image getOriginalPieceImage(int pieceId) {
        if (!initialized) initialize();
        return originalPieceImageCache.get(pieceId);
    }

    /**
     * 获取指定尺寸的棋子图片，优先从缓存获取，否则生成并缓存
     * @param pieceId 棋子ID
     * @param targetWidth 目标宽度
     * @param targetHeight 目标高度
     * @return 调整后的棋子图片
     */
    public static Image getScaledPieceImage(int pieceId, int targetWidth, int targetHeight) {
        if (!initialized) initialize();
        if (skinMode == 1) {
            return null; // 纯色模式不使用棋子图片
        }
        if (targetWidth <= 0 || targetHeight <= 0) {
            return getOriginalPieceImage(pieceId); // 尺寸无效，返回原始图片
        }

        PieceImageCacheKey key = new PieceImageCacheKey(pieceId, targetWidth, targetHeight);
        if (scaledPieceImageCache.containsKey(key)) {
            return scaledPieceImageCache.get(key);
        }

        Image originalImage = getOriginalPieceImage(pieceId);
        if (originalImage == null) {
            return null;
        }

        Image scaledImage = resizeImage(originalImage, targetWidth, targetHeight, Image.SCALE_SMOOTH);
        if (scaledImage != null) {
            scaledPieceImageCache.put(key, scaledImage);
        }
        return scaledImage;
    }

    /**
     * 获取棋盘背景图片 (可能需要根据面板大小调整)
     * @param panelWidth 面板宽度
     * @param panelHeight 面板高度
     * @return 棋盘背景图片
     */
    public static Image getBoardImage(int panelWidth, int panelHeight) {
        if (!initialized) initialize();
        if (skinMode == 1 || boardBackgroundImage == null) {
            return null;
        }
        // 简单示例：直接缩放。更优方案是也缓存缩放后的背景图。
        return resizeImage(boardBackgroundImage, panelWidth, panelHeight, Image.SCALE_SMOOTH);
    }

    /**
     * 获取空白格子图片 (可能需要根据单元格大小调整)
     * @param cellSize 单元格大小
     * @return 空白格子图片
     */
    public static Image getEmptyCellImage(int cellSize) {
        if (!initialized) initialize();
        if (skinMode == 1 || emptyCellImage == null) {
            return null;
        }
        // 简单示例：直接缩放。更优方案是也缓存缩放后的空格图。
        return resizeImage(emptyCellImage, cellSize, cellSize, Image.SCALE_SMOOTH);
    }

    /**
     * 加载图片资源
     * @param imageName 图片名称
     * @return 加载的图片，如果加载失败则返回null
     */
    private static Image loadImage(String imageName) {
        Image image = null;
        String fullPath = IMAGE_PATH + imageName;
        try {
            File file = new File(fullPath);
            if (file.exists()) {
                image = ImageIO.read(file);
            } else {
                URL resource = GameImageManager.class.getResource("/view/game/images/" + imageName);
                if (resource != null) {
                    image = ImageIO.read(resource);
                } else {
                    System.err.println("资源未找到: /view/game/images/" + imageName + " (尝试绝对路径: " + fullPath + ")");
                }
            }

            if (image == null) {
                System.err.println("无法加载图片: " + imageName + " (路径: " + fullPath + ")");
            }
        } catch (IOException e) {
            System.err.println("加载图片时出错: " + imageName + " (路径: " + fullPath + ")");
            e.printStackTrace();
        }
        return image;
    }

    /**
     * 确保图片完全加载，并在加载后返回一个VolatileImage以提高性能（如果适用）
     * @param image 要加载的图片
     * @return 加载完成的图片 (可能是VolatileImage)
     */
    public static Image ensureImageLoaded(Image image) {
        if (image == null) return null;

        // 使用MediaTracker确保图片数据完全加载
        MediaTracker tracker = new MediaTracker(new JPanel());
        tracker.addImage(image, 0);
        try {
            tracker.waitForID(0);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupted status
            System.err.println("图片加载被中断: " + e.getMessage());
            return image; // 返回原始图片，即使可能未完全加载
        }
        if (tracker.isErrorID(0)) {
            System.err.println("加载图片时发生错误");
            return image; // 加载出错，返回原始图片
        }
        return image; // 明确返回已加载的Image
    }

    /**
     * 调整图片大小
     * @param image 原始图片
     * @param targetWidth 目标宽度
     * @param targetHeight 目标高度
     * @param hints 缩放算法提示 (例如 Image.SCALE_SMOOTH)
     * @return 调整后的图片
     */
    public static Image resizeImage(Image image, int targetWidth, int targetHeight, int hints) {
        if (image == null || targetWidth <= 0 || targetHeight <= 0) {
            return null;
        }

        Image loadedImage = ensureImageLoaded(image);
        if (loadedImage.getWidth(null) <= 0 || loadedImage.getHeight(null) <= 0) {
            System.err.println("无法获取原始图片尺寸，缩放失败。");
            return loadedImage;
        }

        // 使用getScaledInstance进行缩放
        Image scaledImage = loadedImage.getScaledInstance(targetWidth, targetHeight, hints);

        // 为了确保图像完全渲染并解决潜在的异步绘制问题（如残影），
        // 将缩放后的图像绘制到一个新的BufferedImage上。
        if (scaledImage instanceof java.awt.image.BufferedImage) {
            return scaledImage; // 如果已经是BufferedImage，直接返回
        }

        // 创建一个与目标尺寸相符的BufferedImage，并支持透明度
        BufferedImage bufferedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bufferedImage.createGraphics();

        // 绘制缩放后的图像到BufferedImage
        // MediaTracker可以再次用于确保scaledImage在这里是完全可用的，但通常绘制到BufferedImage已足够
        boolean drawn = g2d.drawImage(scaledImage, 0, 0, null);
        g2d.dispose(); // 释放Graphics2D资源

        if (!drawn) {
            System.err.println("将scaledImage绘制到BufferedImage失败。");
            // 发生错误，可以考虑返回原始loadedImage或scaledImage，或者null
            // 为简单起见，我们仍返回尝试创建的bufferedImage，即使它可能是空白的
        }

        // 确保新创建的BufferedImage也被完全加载/准备好（尽管对于BufferedImage通常不是必须的）
        return ensureImageLoaded(bufferedImage);
    }
} 