package view.frontend;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * 流式文本输出效果类
 * 实现文本逐字显示的"打字机"效果
 */
public class TextAnimator {
    // 默认每秒输出字符数
    private static final int DEFAULT_CHARS_PER_SECOND = 20;
    
    // 目标文本组件
    private JComponent textComponent;
    // 完整文本
    private String targetText;
    // 每秒输出字符数
    private int charsPerSecond;
    // 当前已显示的字符索引
    private int currentIndex;
    // 动画计时器
    private Timer animationTimer;
    // 是否正在播放动画
    private boolean isAnimating = false;
    // 动画完成回调
    private Runnable onCompleteCallback;
    // HTML前缀和后缀
    private String htmlPrefix = "";
    private String htmlSuffix = "";
    // 实际需要动画显示的内容
    private String contentText = "";
    
    /**
     * 构造函数
     */
    public TextAnimator(JComponent textComponent) {
        this(textComponent, DEFAULT_CHARS_PER_SECOND);
    }
    
    /**
     * 构造函数
     */
    public TextAnimator(JComponent textComponent, int charsPerSecond) {
        this.textComponent = textComponent;
        this.charsPerSecond = charsPerSecond;
        
        // 添加鼠标点击监听器，点击时跳过动画
        textComponent.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                skipAnimation();
            }
        });
    }
    
    /**
     * 开始文本动画
     */
    public void animateText(String text) {
        animateText(text, null);
    }
    
    /**
     * 开始文本动画
     */
    public void animateText(String text, Runnable onComplete) {
        // 如果正在播放动画，先停止
        stopAnimation();
        
        this.targetText = text;
        this.onCompleteCallback = onComplete;
        this.currentIndex = 0;
        
        // 如果文本为空，直接返回
        if (text == null || text.isEmpty()) {
            updateComponentText();
            if (onCompleteCallback != null) {
                onCompleteCallback.run();
            }
            return;
        }
        
        // 处理HTML标签
        processHtmlText(text);
        
        // 计算每个字符的延迟时间（毫秒）
        int delayPerChar = 1000 / charsPerSecond;
        
        // 创建动画计时器
        isAnimating = true;
        animationTimer = new Timer(delayPerChar, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 增加当前索引
                currentIndex++;
                
                // 更新文本
                updateComponentText();
                
                // 如果已经显示完整文本，停止动画
                if (currentIndex >= contentText.length()) {
                    stopAnimation();
                    if (onCompleteCallback != null) {
                        onCompleteCallback.run();
                    }
                }
            }
        });
        
        // 开始动画
        updateComponentText();
        animationTimer.start();
    }
    
    /**
     * 处理HTML文本，提取前缀、内容和后缀
     */
    private void processHtmlText(String text) {
        // 检查是否是HTML格式
        if (text.toLowerCase().startsWith("<html")) {
            // 查找HTML内容的开始和结束位置
            int bodyStart = text.toLowerCase().indexOf("<body");
            int bodyEnd = text.toLowerCase().indexOf("</body>");
            
            if (bodyStart >= 0 && bodyEnd > bodyStart) {
                // 找到body标签的结束位置
                int contentStart = text.indexOf('>', bodyStart);
                if (contentStart >= 0) {
                    contentStart++; // 移到>之后
                    
                    // 提取HTML前缀、内容和后缀
                    htmlPrefix = text.substring(0, contentStart);
                    contentText = text.substring(contentStart, bodyEnd);
                    htmlSuffix = text.substring(bodyEnd);
                    return;
                }
            }
            
            // 如果没有找到body标签，尝试查找html标签
            int htmlEndTag = text.toLowerCase().indexOf("</html>");
            if (htmlEndTag > 0) {
                int contentStart = text.indexOf('>', 0);
                if (contentStart >= 0) {
                    contentStart++; // 移到>之后
                    
                    // 提取HTML前缀、内容和后缀
                    htmlPrefix = text.substring(0, contentStart);
                    contentText = text.substring(contentStart, htmlEndTag);
                    htmlSuffix = text.substring(htmlEndTag);
                    return;
                }
            }
        }
        
        // 如果不是HTML格式或无法解析，直接使用原文本
        htmlPrefix = "";
        contentText = text;
        htmlSuffix = "";
    }
    
    /**
     * 停止动画
     */
    public void stopAnimation() {
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }
        isAnimating = false;
    }
    
    /**
     * 跳过动画，直接显示完整文本
     */
    public void skipAnimation() {
        if (isAnimating && targetText != null) {
            stopAnimation();
            // 直接显示完整文本
            if (textComponent instanceof JLabel) {
                ((JLabel) textComponent).setText(targetText);
            } else if (textComponent instanceof JTextArea) {
                ((JTextArea) textComponent).setText(targetText);
            } else if (textComponent instanceof JTextField) {
                ((JTextField) textComponent).setText(targetText);
            } else if (textComponent instanceof JTextPane) {
                ((JTextPane) textComponent).setText(targetText);
            } else if (textComponent instanceof JEditorPane) {
                ((JEditorPane) textComponent).setText(targetText);
            }
            
            if (onCompleteCallback != null) {
                onCompleteCallback.run();
            }
        }
    }
    
    /**
     * 更新组件文本
     */
    private void updateComponentText() {
        // 构建当前应该显示的文本
        String displayText;
        if (htmlPrefix.isEmpty() && htmlSuffix.isEmpty()) {
            // 非HTML文本
            displayText = contentText.substring(0, Math.min(currentIndex, contentText.length()));
        } else {
            // HTML文本
            displayText = htmlPrefix + contentText.substring(0, Math.min(currentIndex, contentText.length())) + htmlSuffix;
        }
        
        // 根据组件类型更新文本
        if (textComponent instanceof JLabel) {
            ((JLabel) textComponent).setText(displayText);
        } else if (textComponent instanceof JTextArea) {
            ((JTextArea) textComponent).setText(displayText);
        } else if (textComponent instanceof JTextField) {
            ((JTextField) textComponent).setText(displayText);
        } else if (textComponent instanceof JTextPane) {
            ((JTextPane) textComponent).setText(displayText);
        } else if (textComponent instanceof JEditorPane) {
            ((JEditorPane) textComponent).setText(displayText);
        } else {
            System.err.println("不支持的组件类型: " + textComponent.getClass().getName());
        }
    }
    
    /**
     * 设置每秒输出字符数
     */
    public void setCharsPerSecond(int charsPerSecond) {
        this.charsPerSecond = Math.max(1, charsPerSecond); // 确保至少每秒输出1个字符
        
        // 如果动画正在进行，更新计时器的延迟
        if (isAnimating && animationTimer != null && animationTimer.isRunning()) {
            int delayPerChar = 1000 / this.charsPerSecond;
            animationTimer.setDelay(delayPerChar);
        }
    }
    
    /**
     * 获取当前每秒输出字符数
     */
    public int getCharsPerSecond() {
        return charsPerSecond;
    }
    
    /**
     * 检查动画是否正在播放
     */
    public boolean isAnimating() {
        return isAnimating;
    }
} 