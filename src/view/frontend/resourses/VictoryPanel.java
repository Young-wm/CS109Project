package view.frontend.resourses;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * 游戏胜利界面面板
 * 用于在游戏胜利时显示带有背景图片的胜利界面
 */
public class VictoryPanel extends JPanel {
    private Image backgroundImage;
    private String victoryCongratulation;
    private String timeSpent;
    private String stepsCount;
    private JButton returnButton;
    private JButton nextLevelButton;

    /**
     * 创建胜利界面面板
     * @param timeSpent 花费时间
     * @param steps 移动步数
     */
    public VictoryPanel(String timeSpent, int steps) {
        this.victoryCongratulation = "恭喜你通过本关卡！";
        this.timeSpent = "花费时间: " + timeSpent;
        this.stepsCount = "步数: " + steps;
        
        setLayout(new BorderLayout());
        
        // 创建中央面板来放置文字信息
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false); // 设置为透明，以显示背景图片
        
        // 创建信息标签
        JLabel congratulationLabel = createStyledLabel(victoryCongratulation, 32);
        JLabel timeLabel = createStyledLabel(this.timeSpent, 24);
        JLabel stepsLabel = createStyledLabel(this.stepsCount, 24);
        
        // 添加到信息面板
        infoPanel.add(Box.createVerticalGlue());
        infoPanel.add(congratulationLabel);
        infoPanel.add(Box.createVerticalStrut(20));
        infoPanel.add(timeLabel);
        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(stepsLabel);
        infoPanel.add(Box.createVerticalGlue());
        
        // 创建按钮面板
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        
        returnButton = new JButton("返回关卡选择");
        nextLevelButton = new JButton("下一关");
        
        buttonPanel.add(returnButton);
        buttonPanel.add(nextLevelButton);
        
        // 添加面板到主面板
        add(infoPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // 设置首选大小
        setPreferredSize(new Dimension(600, 400));
    }
    
    /**
     * 设置胜利界面的背景图片
     * @param backgroundImage 背景图片
     */
    public void setBackgroundImage(Image backgroundImage) {
        this.backgroundImage = backgroundImage;
        repaint(); // 设置背景后重绘面板
    }
    
    /**
     * 添加返回按钮的动作监听器
     * @param listener 动作监听器
     */
    public void addReturnButtonListener(ActionListener listener) {
        returnButton.addActionListener(listener);
    }
    
    /**
     * 添加下一关按钮的动作监听器
     * @param listener 动作监听器
     */
    public void addNextLevelButtonListener(ActionListener listener) {
        nextLevelButton.addActionListener(listener);
    }
    
    /**
     * 创建样式化的标签
     * @param text 文本内容
     * @param fontSize 字体大小
     * @return 样式化的标签
     */
    private JLabel createStyledLabel(String text, int fontSize) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("微软雅黑", Font.BOLD, fontSize));
        label.setForeground(Color.WHITE); // 白色文字，便于在图片背景上显示
        // 修改为黑色文本，并添加文本阴影效果以确保在任何背景上都清晰可见
        label.setForeground(Color.BLACK);
        label.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(2, 2, 2, 2),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
            )
        ));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // 绘制背景图片
        if (backgroundImage != null) {
            Graphics2D g2d = (Graphics2D) g;
            
            int panelWidth = getWidth();
            int panelHeight = getHeight();
            int imgWidth = backgroundImage.getWidth(this);
            int imgHeight = backgroundImage.getHeight(this);
            
            // 计算等比例缩放后的尺寸
            double panelRatio = (double) panelWidth / panelHeight;
            double imgRatio = (double) imgWidth / imgHeight;
            
            int x = 0, y = 0;
            int drawWidth = panelWidth;
            int drawHeight = panelHeight;
            
            // 如果图片比面板更宽，按高度缩放，并截取中间部分
            if (imgRatio > panelRatio) {
                drawWidth = (int) (panelHeight * imgRatio);
                x = (panelWidth - drawWidth) / 2;
            } 
            // 如果图片比面板更高，按宽度缩放，并截取中间部分
            else if (imgRatio < panelRatio) {
                drawHeight = (int) (panelWidth / imgRatio);
                y = (panelHeight - drawHeight) / 2;
            }
            
            // 绘制缩放后的图片
            g2d.drawImage(backgroundImage, x, y, drawWidth, drawHeight, this);
            
            // 添加半透明黑色层，使文字更易读
            g2d.setColor(new Color(0, 0, 0, 150));
            g2d.fillRect(0, 0, panelWidth, panelHeight);
        } else {
            // 如果没有设置背景图片，则使用默认背景色
            g.setColor(new Color(50, 50, 100));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
} 