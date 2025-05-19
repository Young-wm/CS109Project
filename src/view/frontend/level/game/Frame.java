package view.frontend.level.game;

import javax.swing.*;
import java.awt.*;

/**
 * 这个类是负责显示游戏主界面，并显示当前关卡的游戏内容
 */
public class Frame extends JFrame {
    private int currentLevel;
    
    public Frame() {
        setTitle("游戏界面");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        // 顶部信息面板
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel levelLabel = new JLabel("当前关卡: " + currentLevel);
        levelLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        infoPanel.add(levelLabel);
        add(infoPanel, BorderLayout.NORTH);
        
        // 游戏主内容区域
        JPanel gamePanel = new JPanel();
        gamePanel.setLayout(new BorderLayout());
        gamePanel.setBackground(new Color(240, 240, 240));
        
        // 临时显示内容,现在没用了
        JLabel gameContentLabel = new JLabel("游戏内容区域 - 关卡内容将在这里显示", SwingConstants.CENTER);
        gameContentLabel.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        gamePanel.add(gameContentLabel, BorderLayout.CENTER);
        
        add(gamePanel, BorderLayout.CENTER);
        
        // 底部控制面板
        JPanel controlPanel = new JPanel();
        JButton backButton = new JButton("返回关卡选择");
        backButton.addActionListener(e -> {
            dispose();
            new view.frontend.level.menu.LevelSelectionFrame().setVisible(true);
        });
        controlPanel.add(backButton);
        
        add(controlPanel, BorderLayout.SOUTH);
    }
    
    /**
     * 设置当前关卡
     * @param level 关卡编号
     */
    public void setCurrentLevel(int level) {
        this.currentLevel = level;
        // 更新界面显示
        // 这里可以根据关卡加载不同的游戏内容
    }
}