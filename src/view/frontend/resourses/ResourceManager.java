package view.frontend.resourses;

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
 * 资源管理器
 * 负责加载和管理游戏中使用的各种资源，如图片等
 */
public class ResourceManager {
    // 图片资源缓存
    private static final Map<String, Image> imageCache = new HashMap<>();
    // 光标资源缓存
    private static final Map<String, Cursor> cursorCache = new HashMap<>();
    
    // 资源加载失败时的默认图片
    private static Image defaultImage = null;
    
    // 是否启用资源加载
    private static boolean resourceLoadingEnabled = true;
    
    /**
     * 加载图片资源
     * @param imagePath 图片路径（相对于resources文件夹）
     * @return 加载的图片，如果加载失败则返回默认图片或null
     */
    public static Image loadImage(String imagePath) {
        // 如果资源加载被禁用，直接返回默认图片或null
        if (!resourceLoadingEnabled) {
            return getDefaultImage();
        }
        
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
                // 返回默认图片
                image = getDefaultImage();
            }
        } catch (IOException e) {
            System.err.println("加载图片时出错: " + imagePath);
            e.printStackTrace();
            // 返回默认图片
            image = getDefaultImage();
        }
        
        return image;
    }
    
    /**
     * 获取默认图片（1x1像素的透明图片）
     * @return 默认图片
     */
    private static Image getDefaultImage() {
        if (defaultImage == null) {
            // 创建1x1像素的透明图片
            defaultImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        }
        return defaultImage;
    }
    
    /**
     * 加载图标资源
     * @param imagePath 图片路径
     * @return 图标对象，如果加载失败则返回空图标
     */
    public static ImageIcon loadIcon(String imagePath) {
        Image image = loadImage(imagePath);
        return new ImageIcon(image); // 即使image为null，ImageIcon也能处理
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
            return getDefaultImage();
        }
        return originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
    }
    
    /**
     * 清除图片缓存
     */
    public static void clearCache() {
        imageCache.clear();
        cursorCache.clear();
    }
    
    /**
     * 获取图片的原始尺寸
     * @param imagePath 图片路径
     * @return 图片尺寸，如果图片不存在则返回默认尺寸(1,1)
     */
    public static Dimension getImageDimension(String imagePath) {
        Image image = loadImage(imagePath);
        if (image == null) {
            return new Dimension(1, 1);
        }
        
        // 等待图片完全加载以获取正确尺寸
        MediaTracker tracker = new MediaTracker(new JPanel());
        tracker.addImage(image, 0);
        try {
            tracker.waitForID(0);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return new Dimension(1, 1);
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
            return getDefaultImage();
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
     * 加载自定义鼠标光标
     * @param imagePath 光标图片路径
     * @return 自定义光标，如果加载失败则返回默认光标
     */
    public static Cursor loadCursor(String imagePath) {
        return loadCursor(imagePath, 0, 0);
    }
    
    /**
     * 加载自定义鼠标光标
     * @param imagePath 光标图片路径
     * @param hotSpotX 热点X坐标
     * @param hotSpotY 热点Y坐标
     * @return 自定义光标，如果加载失败则返回默认光标
     */
    public static Cursor loadCursor(String imagePath, int hotSpotX, int hotSpotY) {
        // 如果资源加载被禁用，直接返回默认光标
        if (!resourceLoadingEnabled) {
            return Cursor.getDefaultCursor();
        }
        
        // 检查缓存
        String cacheKey = imagePath + "_" + hotSpotX + "_" + hotSpotY;
        if (cursorCache.containsKey(cacheKey)) {
            return cursorCache.get(cacheKey);
        }
        
        try {
            // 加载图片
            Image image = loadImage(imagePath);
            if (image == null || image == getDefaultImage()) {
                return Cursor.getDefaultCursor();
            }
            
            // 等待图片完全加载
            image = ensureImageLoaded(image);
            
            // 创建自定义光标
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Dimension size = getImageDimension(imagePath);
            
            // 检查图片尺寸，太大的图片可能不适合用作光标
            if (size != null && (size.width > 128 || size.height > 128)) {
                image = resizeImage(image, 
                    Math.min(128, size.width), 
                    Math.min(128, size.height));
                size = new Dimension(
                    Math.min(128, size.width), 
                    Math.min(128, size.height));
            }
            
            // 确保热点坐标在图片范围内
            hotSpotX = Math.max(0, Math.min(hotSpotX, size.width - 1));
            hotSpotY = Math.max(0, Math.min(hotSpotY, size.height - 1));
            
            // 创建光标
            Cursor cursor = toolkit.createCustomCursor(
                image, 
                new Point(hotSpotX, hotSpotY), 
                "customCursor");
            
            // 缓存光标
            cursorCache.put(cacheKey, cursor);
            
            return cursor;
        } catch (Exception e) {
            System.err.println("加载自定义光标失败: " + e.getMessage());
            e.printStackTrace();
            return Cursor.getDefaultCursor();
        }
    }
    
    /**
     * 为组件设置自定义光标
     * @param component 目标组件
     * @param imagePath 光标图片路径
     */
    public static void setCursor(Component component, String imagePath) {
        setCursor(component, imagePath, 0, 0);
    }
    
    /**
     * 为组件设置自定义光标
     * @param component 目标组件
     * @param imagePath 光标图片路径
     * @param hotSpotX 热点X坐标
     * @param hotSpotY 热点Y坐标
     */
    public static void setCursor(Component component, String imagePath, int hotSpotX, int hotSpotY) {
        if (component == null) return;
        
        try {
            Cursor cursor = loadCursor(imagePath, hotSpotX, hotSpotY);
            component.setCursor(cursor);
        } catch (Exception e) {
            System.err.println("设置自定义光标失败: " + e.getMessage());
            e.printStackTrace();
            // 发生异常时使用默认光标
            component.setCursor(Cursor.getDefaultCursor());
        }
    }
    
    /**
     * 设置是否启用资源加载
     * @param enabled 是否启用
     */
    public static void setResourceLoadingEnabled(boolean enabled) {
        resourceLoadingEnabled = enabled;
    }
    
    /**
     * 检查资源加载是否启用
     * @return 是否启用
     */
    public static boolean isResourceLoadingEnabled() {
        return resourceLoadingEnabled;
    }
} 