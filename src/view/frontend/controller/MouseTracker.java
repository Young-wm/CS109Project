package view.frontend.controller;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.LinkedList;
import java.util.Queue;

/**
 * 鼠标轨迹追踪器
 * 记录鼠标移动轨迹并绘制在界面上
 */
public class MouseTracker {
    // 轨迹点队列
    private Queue<Point> trailPoints = new LinkedList<>();
    
    // 轨迹最大点数
    private int maxPoints = 5; // 默认值，可根据需要调整
    
    // 轨迹颜色
    private Color trailColor = new Color(0, 120, 255, 150);
    
    // 轨迹宽度
    private float trailWidth = 3.0f;
    
    // 采样间隔（毫秒）
    private int samplingInterval = 100; // 默认值，可根据需要调整
    
    // 上一次采样时间
    private long lastSamplingTime = 0;
    
    // 是否启用
    private boolean enabled = true;
    
    // 目标JComponent（要在其上绘制轨迹的组件）
    private JComponent targetComponent;
    
    // 鼠标监听器
    private MouseMotionListener mouseMotionListener;
    
    /**
     * 构造函数
     * @param targetComponent 要在其上绘制轨迹的组件
     */
    public MouseTracker(JComponent targetComponent) {
        this.targetComponent = targetComponent;
        
        // 创建鼠标监听器
        mouseMotionListener = new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                updateTrail(e.getPoint());
            }
            
            @Override
            public void mouseMoved(MouseEvent e) {
                updateTrail(e.getPoint());
            }
        };
        
        // 添加鼠标监听器
        targetComponent.addMouseMotionListener(mouseMotionListener);
        
        // 创建重绘定时器
        Timer timer = new Timer(16, e -> {
            if (enabled && !trailPoints.isEmpty()) {
                targetComponent.repaint();
            }
        });
        timer.start();
    }
    
    /**
     * 更新轨迹
     * @param point 当前鼠标位置
     */
    private void updateTrail(Point point) {
        if (!enabled) return;
        
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSamplingTime >= samplingInterval) {
            trailPoints.add(new Point(point));
            lastSamplingTime = currentTime;
            
            // 限制轨迹点数量
            while (trailPoints.size() > maxPoints) {
                trailPoints.poll();
            }
        }
    }
    
    /**
     * 绘制轨迹
     * @param g2d Graphics2D对象
     */
    public void drawTrail(Graphics2D g2d) {
        if (!enabled || trailPoints.isEmpty()) return;
        
        // 保存原始设置
        Stroke originalStroke = g2d.getStroke();
        Color originalColor = g2d.getColor();
        Composite originalComposite = g2d.getComposite();
        
        // 设置绘制属性
        g2d.setColor(trailColor);
        g2d.setStroke(new BasicStroke(trailWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        // 转换点队列为数组，方便操作
        Point[] points = trailPoints.toArray(new Point[0]);
        
        // 绘制轨迹线段
        for (int i = 1; i < points.length; i++) {
            // 设置当前线段的透明度
            float alpha = (float) i / points.length;
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            
            // 绘制线段
            g2d.drawLine(
                points[i-1].x, points[i-1].y,
                points[i].x, points[i].y
            );
        }
        
        // 恢复原始设置
        g2d.setStroke(originalStroke);
        g2d.setColor(originalColor);
        g2d.setComposite(originalComposite);
    }
    
    /**
     * 设置轨迹最大点数
     * @param maxPoints 最大点数
     */
    public void setMaxPoints(int maxPoints) {
        this.maxPoints = Math.max(2, maxPoints);
    }
    
    /**
     * 设置采样间隔
     * @param interval 间隔（毫秒）
     */
    public void setSamplingInterval(int interval) {
        this.samplingInterval = Math.max(10, interval);
    }
    
    /**
     * 设置轨迹颜色
     * @param color 颜色
     */
    public void setTrailColor(Color color) {
        this.trailColor = color;
    }
    
    /**
     * 设置轨迹宽度
     * @param width 宽度
     */
    public void setTrailWidth(float width) {
        this.trailWidth = Math.max(1.0f, width);
    }
    
    /**
     * 启用或禁用轨迹
     * @param enabled 是否启用
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    /**
     * 清除轨迹
     */
    public void clearTrail() {
        trailPoints.clear();
    }
} 