package view.frontend.resourses;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

/**
 * 图像变换工具类
 * 用于对图像进行各种几何变换，如梯形变换
 */
public class ImageTransformer {
    
    /**
     * 将图像转换为梯形
     * 可以生成靠近中心边长，远离中心边短的梯形效果
     *
     * @param image 原始图像
     * @param isRightSide 是否是右侧（决定梯形方向）
     * @param trapezoidFactor 梯形程度因子（0.0-1.0），值越大梯形效果越明显
     * @return 变换后的梯形图像
     */
    public static BufferedImage createTrapezoidImage(Image image, boolean isRightSide, double trapezoidFactor) {
        if (image == null) return null;
        
        // 确保图像完全加载
        image = ResourceManager.ensureImageLoaded(image);
        
        int width = image.getWidth(null);
        int height = image.getHeight(null);
        
        if (width <= 0 || height <= 0) {
            return null;
        }
        
        // 创建目标图像
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = result.createGraphics();
        
        try {
            // 设置高质量绘图
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            
            // 计算梯形变换参数
            double shortEdgeFactor = 1.0 - trapezoidFactor;
            
            // 定义源图像的四个角点
            Point2D[] srcPoints = new Point2D[4];
            srcPoints[0] = new Point2D.Double(0, 0);             // 左上
            srcPoints[1] = new Point2D.Double(width, 0);         // 右上
            srcPoints[2] = new Point2D.Double(width, height);    // 右下
            srcPoints[3] = new Point2D.Double(0, height);        // 左下
            
            // 定义目标梯形的四个角点
            Point2D[] dstPoints = new Point2D[4];
            
            if (isRightSide) {
                // 右侧梯形：左边为长边
                dstPoints[0] = new Point2D.Double(0, 0);                             // 左上
                dstPoints[1] = new Point2D.Double(width * shortEdgeFactor, 0);       // 右上
                dstPoints[2] = new Point2D.Double(width * shortEdgeFactor, height);  // 右下
                dstPoints[3] = new Point2D.Double(0, height);                        // 左下
            } else {
                // 左侧梯形：右边为长边
                dstPoints[0] = new Point2D.Double(width * (1.0 - shortEdgeFactor), 0);   // 左上
                dstPoints[1] = new Point2D.Double(width, 0);                             // 右上
                dstPoints[2] = new Point2D.Double(width, height);                        // 右下
                dstPoints[3] = new Point2D.Double(width * (1.0 - shortEdgeFactor), height); // 左下
            }
            
            // 创建仿射变换
            AffineTransform transform = createTransform(srcPoints, dstPoints);
            
            // 应用变换
            g2d.drawImage(image, transform, null);
            
            return result;
        } finally {
            g2d.dispose();
        }
    }
    
    /**
     * 创建基于四对角点的仿射变换
     * 
     * @param src 源图像的四个角点
     * @param dst 目标图像的四个角点
     * @return 仿射变换对象
     */
    private static AffineTransform createTransform(Point2D[] src, Point2D[] dst) {
        AffineTransform transform = new AffineTransform();
        
        // 使用三个点来确定仿射变换
        double[] srcMatrix = {
            src[0].getX(), src[0].getY(),
            src[1].getX(), src[1].getY(),
            src[3].getX(), src[3].getY()
        };
        
        double[] dstMatrix = {
            dst[0].getX(), dst[0].getY(),
            dst[1].getX(), dst[1].getY(),
            dst[3].getX(), dst[3].getY()
        };
        
        transform.setTransform(
            createTransformMatrix(srcMatrix, dstMatrix)
        );
        
        return transform;
    }
    
    /**
     * 根据三个点创建变换矩阵
     */
    private static AffineTransform createTransformMatrix(double[] src, double[] dst) {
        double[][] matrix = new double[3][3];
        
        // 使用三个点来确定仿射变换
        double dx1 = src[2] - src[0];
        double dy1 = src[3] - src[1];
        double dx2 = src[4] - src[0];
        double dy2 = src[5] - src[1];
        
        double sx1 = dst[2] - dst[0];
        double sy1 = dst[3] - dst[1];
        double sx2 = dst[4] - dst[0];
        double sy2 = dst[5] - dst[1];
        
        double determinant = dx1 * dy2 - dx2 * dy1;
        
        if (determinant == 0) {
            return new AffineTransform(); // 返回恒等变换
        }
        
        double r = 1.0 / determinant;
        
        matrix[0][0] = (dy2 * sx1 - dy1 * sx2) * r;
        matrix[1][0] = (dx1 * sx2 - dx2 * sx1) * r;
        matrix[2][0] = dst[0] - (matrix[0][0] * src[0] + matrix[1][0] * src[1]);
        
        matrix[0][1] = (dy2 * sy1 - dy1 * sy2) * r;
        matrix[1][1] = (dx1 * sy2 - dx2 * sy1) * r;
        matrix[2][1] = dst[1] - (matrix[0][1] * src[0] + matrix[1][1] * src[1]);
        
        matrix[0][2] = 0.0;
        matrix[1][2] = 0.0;
        matrix[2][2] = 1.0;
        
        AffineTransform transform = new AffineTransform(
            matrix[0][0], matrix[0][1],
            matrix[1][0], matrix[1][1],
            matrix[2][0], matrix[2][1]
        );
        
        return transform;
    }
    
    /**
     * 设置图像的透明度
     * 
     * @param image 原始图像
     * @param alpha 透明度 (0.0-1.0)
     * @return 调整透明度后的图像
     */
    public static BufferedImage setImageAlpha(Image image, float alpha) {
        if (image == null) return null;
        
        // 确保图像完全加载
        image = ResourceManager.ensureImageLoaded(image);
        
        int width = image.getWidth(null);
        int height = image.getHeight(null);
        
        if (width <= 0 || height <= 0) {
            return null;
        }
        
        // 检查透明度范围
        alpha = Math.max(0.0f, Math.min(1.0f, alpha));
        
        // 创建目标图像
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = result.createGraphics();
        
        try {
            // 设置透明度
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2d.drawImage(image, 0, 0, null);
            
            return result;
        } finally {
            g2d.dispose();
        }
    }
} 