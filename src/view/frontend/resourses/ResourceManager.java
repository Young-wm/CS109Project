package view.frontend.resourses;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * 资源管理器
 * 负责加载和管理游戏中使用的各种资源，如图片等
 */
public class ResourceManager {
    // 图片资源缓存
    private static final Map<String, Image> imageCache = new HashMap<>();
    
    /**
     * 加载图片资源
     * @param imagePath 图片路径（相对于resources文件夹）
     * @return 加载的图片，如果加载失败则返回null
     */
    public static Image loadImage(String imagePath) {
        // 检查缓存
        if (imageCache.containsKey(imagePath)) {
            return imageCache.get(imagePath);
        }
        
        Image image = null;
        
        try {
            // 尝试从文件系统加载
            File file = new File("src/view/frontend/resourses/" + imagePath);
            if (file.exists()) {
                image = ImageIO.read(file);
            } else {
                // 尝试从类路径加载
                URL resource = ResourceManager.class.getResource("/view/frontend/resourses/" + imagePath);
                if (resource != null) {
                    image = ImageIO.read(resource);
                }
            }
            
            // 如果成功加载，添加到缓存
            if (image != null) {
                imageCache.put(imagePath, image);
            } else {
                System.err.println("无法加载图片: " + imagePath);
            }
        } catch (IOException e) {
            System.err.println("加载图片时出错: " + imagePath);
            e.printStackTrace();
        }
        
        return image;
    }
    
    /**
     * 加载图标资源
     * @param imagePath 图片路径
     * @return 图标对象
     */
    public static ImageIcon loadIcon(String imagePath) {
        Image image = loadImage(imagePath);
        if (image != null) {
            return new ImageIcon(image);
        }
        return null;
    }
    
    /**
     * 调整图片大小
     * @param originalImage 原始图片
     * @param width 目标宽度
     * @param height 目标高度
     * @return 调整大小后的图片
     */
    public static Image resizeImage(Image originalImage, int width, int height) {
        if (originalImage == null) {
            return null;
        }
        return originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
    }
    
    /**
     * 清除图片缓存
     */
    public static void clearCache() {
        imageCache.clear();
    }
    
    /**
     * 获取图片的原始尺寸
     * @param imagePath 图片路径
     * @return 图片尺寸，如果图片不存在则返回null
     */
    public static Dimension getImageDimension(String imagePath) {
        Image image = loadImage(imagePath);
        if (image == null) {
            return null;
        }
        
        // 等待图片完全加载以获取正确尺寸
        MediaTracker tracker = new MediaTracker(new JPanel());
        tracker.addImage(image, 0);
        try {
            tracker.waitForID(0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        return new Dimension(image.getWidth(null), image.getHeight(null));
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
} 