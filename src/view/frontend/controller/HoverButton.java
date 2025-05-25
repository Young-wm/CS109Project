package view.frontend.controller;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import view.audio.AudioManager;

/**
 * 带有悬停效果的自定义按钮
 * 鼠标悬停时改变颜色、字体，并播放音效
 */
public class HoverButton extends JButton {
    // 原始颜色和字体
    private Color originalBackground;
    private Color originalForeground;
    private Font originalFont;
    
    // 悬停时的颜色和字体
    private Color hoverBackground;
    private Color hoverForeground;
    private Font hoverFont;
    
    // 是否播放音效
    private boolean playSoundEffect = true;
    
    /**
     * 构造函数
     * @param text 按钮文本
     */
    public HoverButton(String text) {
        super(text);
        initialize();
    }
    
    /**
     * 构造函数
     * @param icon 按钮图标
     */
    public HoverButton(Icon icon) {
        super(icon);
        initialize();
    }
    
    /**
     * 构造函数
     * @param text 按钮文本
     * @param icon 按钮图标
     */
    public HoverButton(String text, Icon icon) {
        super(text, icon);
        initialize();
    }
    
    /**
     * 初始化按钮
     */
    private void initialize() {
        // 保存原始设置
        originalBackground = getBackground();
        originalForeground = getForeground();
        originalFont = getFont();
        
        // 设置悬停时的颜色和字体
        hoverBackground = new Color(70, 130, 180); // 钢蓝色，更明显的悬停颜色
        hoverForeground = Color.WHITE;
        hoverFont = new Font(originalFont.getName(), Font.BOLD, originalFont.getSize());
        
        // 添加鼠标事件监听器
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (isEnabled()) {
                    setBackground(hoverBackground);
                    setForeground(hoverForeground);
                    setFont(hoverFont);
                    setBorderPainted(true);
                    setContentAreaFilled(true);
                    
                    // 播放悬停音效
                    if (playSoundEffect) {
                        AudioManager.getInstance().playDefaultButtonHoverSound();
                    }
                }
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                // 恢复原始设置
                setBackground(originalBackground);
                setForeground(originalForeground);
                setFont(originalFont);
            }
            
            @Override
            public void mousePressed(MouseEvent e) {
                if (isEnabled() && playSoundEffect) {
                    // 播放点击音效
                    AudioManager.getInstance().playDefaultButtonClickSound();
                }
            }
        });
    }
    
    /**
     * 设置悬停时的背景颜色
     * @param color 颜色
     */
    public void setHoverBackground(Color color) {
        this.hoverBackground = color;
    }
    
    /**
     * 设置悬停时的前景颜色（文本颜色）
     * @param color 颜色
     */
    public void setHoverForeground(Color color) {
        this.hoverForeground = color;
    }
    
    /**
     * 设置悬停时的字体
     * @param font 字体
     */
    public void setHoverFont(Font font) {
        this.hoverFont = font;
    }
    
    /**
     * 设置是否播放音效
     * @param playSoundEffect 是否播放
     */
    public void setPlaySoundEffect(boolean playSoundEffect) {
        this.playSoundEffect = playSoundEffect;
    }
    
    /**
     * 重写设置背景色方法，保存原始背景色
     */
    @Override
    public void setBackground(Color bg) {
        super.setBackground(bg);
        if (!bg.equals(hoverBackground)) {
            originalBackground = bg;
        }
    }
    
    /**
     * 重写设置前景色方法，保存原始前景色
     */
    @Override
    public void setForeground(Color fg) {
        super.setForeground(fg);
        if (!fg.equals(hoverForeground)) {
            originalForeground = fg;
        }
    }
    
    /**
     * 重写设置字体方法，保存原始字体
     */
    @Override
    public void setFont(Font font) {
        super.setFont(font);
        if (!font.equals(hoverFont)) {
            originalFont = font;
        }
    }
} 