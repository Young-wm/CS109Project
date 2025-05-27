package view.game2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * 技能动画面板，用于显示技能（如大挪移、炸弹）的动画效果
 * 实现了淡入淡出效果
 */
public class SkillAnimationPanel extends JPanel {
    // 动画图片
    private ImageIcon animationIcon;
    // 当前透明度 (0.0f - 1.0f)
    private float currentAlpha = 0.0f;
    // 动画状态：0=淡入，1=显示，2=淡出，3=结束
    private int animationState = 0;
    // 定时器用于控制动画
    private Timer animationTimer;
    // 淡入淡出速度
    private static final float FADE_SPEED = 0.05f;
    // 显示时间 (毫秒)
    private static final int DISPLAY_TIME = 2000;
    // 刷新率 (毫秒)
    private static final int REFRESH_RATE = 30;
    // 回调接口，动画结束时调用
    private Runnable onAnimationEnd;

    /**
     * 构造函数
     */
    public SkillAnimationPanel() {
        setOpaque(false);
        setLayout(null);
        setVisible(false);
    }

    /**
     * 设置动画结束回调
     * @param callback 回调函数
     */
    public void setOnAnimationEnd(Runnable callback) {
        this.onAnimationEnd = callback;
    }

    /**
     * 播放技能动画
     * @param gifPath GIF图片路径
     */
    public void playAnimation(String gifPath) {
        // 检查文件是否存在
        File gifFile = new File(gifPath);
        if (!gifFile.exists()) {
            System.err.println("技能动画文件不存在: " + gifPath);
            if (onAnimationEnd != null) {
                onAnimationEnd.run();
            }
            return;
        }

        try {
            // 加载GIF图片
            animationIcon = new ImageIcon(gifPath);
            if (animationIcon.getIconWidth() <= 0 || animationIcon.getIconHeight() <= 0) {
                System.err.println("无法加载技能动画: " + gifPath);
                if (onAnimationEnd != null) {
                    onAnimationEnd.run();
                }
                return;
            }

            // 重置动画状态
            currentAlpha = 0.0f;
            animationState = 0;
            setVisible(true);

            // 如果已有计时器，停止它
            if (animationTimer != null && animationTimer.isRunning()) {
                animationTimer.stop();
            }

            // 创建新的计时器来控制动画
            animationTimer = new Timer(REFRESH_RATE, new ActionListener() {
                private long displayStartTime = 0;

                @Override
                public void actionPerformed(ActionEvent e) {
                    switch (animationState) {
                        case 0: // 淡入
                            currentAlpha += FADE_SPEED;
                            if (currentAlpha >= 1.0f) {
                                currentAlpha = 1.0f;
                                animationState = 1;
                                displayStartTime = System.currentTimeMillis();
                            }
                            break;
                        case 1: // 显示
                            long currentTime = System.currentTimeMillis();
                            if (currentTime - displayStartTime >= DISPLAY_TIME) {
                                animationState = 2;
                            }
                            break;
                        case 2: // 淡出
                            currentAlpha -= FADE_SPEED;
                            if (currentAlpha <= 0.0f) {
                                currentAlpha = 0.0f;
                                animationState = 3;
                                // 停止计时器
                                ((Timer) e.getSource()).stop();
                                setVisible(false);
                                // 调用回调
                                if (onAnimationEnd != null) {
                                    onAnimationEnd.run();
                                }
                            }
                            break;
                    }
                    repaint();
                }
            });
            animationTimer.start();
        } catch (Exception e) {
            System.err.println("播放技能动画时出错: " + e.getMessage());
            e.printStackTrace();
            if (onAnimationEnd != null) {
                onAnimationEnd.run();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (animationIcon != null && currentAlpha > 0) {
            Graphics2D g2d = (Graphics2D) g.create();
            try {
                // 设置透明度
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, currentAlpha));
                
                // 计算图片居中位置
                int x = (getWidth() - animationIcon.getIconWidth()) / 2;
                int y = (getHeight() - animationIcon.getIconHeight()) / 2;
                
                // 如果图片太大，则缩放到面板大小
                if (animationIcon.getIconWidth() > getWidth() || animationIcon.getIconHeight() > getHeight()) {
                    double scaleX = (double) getWidth() / animationIcon.getIconWidth();
                    double scaleY = (double) getHeight() / animationIcon.getIconHeight();
                    double scale = Math.min(scaleX, scaleY);
                    
                    int newWidth = (int) (animationIcon.getIconWidth() * scale);
                    int newHeight = (int) (animationIcon.getIconHeight() * scale);
                    
                    x = (getWidth() - newWidth) / 2;
                    y = (getHeight() - newHeight) / 2;
                    
                    g2d.drawImage(animationIcon.getImage(), x, y, newWidth, newHeight, this);
                } else {
                    // 正常绘制
                    animationIcon.paintIcon(this, g2d, x, y);
                }
            } finally {
                g2d.dispose();
            }
        }
    }

    /**
     * 停止当前动画
     */
    public void stopAnimation() {
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }
        setVisible(false);
        if (onAnimationEnd != null) {
            onAnimationEnd.run();
        }
    }
} 