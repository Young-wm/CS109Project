package view.frontend.level.menu;

import view.frontend.LoginFrame.User;
import view.frontend.controller.Logic;
import view.frontend.level.game.Frame;
import view.game.GameFrame;
import view.game2.GameFrame2;
import view.game3.GameFrame3;
import view.frontend.LoginFrame.AuthFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LevelSelectionFrame extends JFrame {
    private int selectedLevel = -1;
    private JPanel levelDetailPanel;
    private JLabel levelDescriptionLabel;
    private User currentUser;
    
    public LevelSelectionFrame(User user) {
        this.currentUser = user;
        setTitle("选择关卡");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("请选择关卡", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 22));
        add(titleLabel, BorderLayout.NORTH);

        // 创建左侧关卡选择面板
        int levelCount = Logic.getNumberOfLevels();
        JPanel levelPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        levelPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        for (int i = 1; i <= levelCount; i++) {
            JButton levelButton = new JButton("关卡 " + i);
            int levelNum = i;
            levelButton.addActionListener(e -> {
                selectedLevel = levelNum;
                updateLevelDetailPanel(levelNum);
            });
            levelPanel.add(levelButton);
        }
        
        // 创建右侧关卡详情面板
        levelDetailPanel = new JPanel(new BorderLayout());
        levelDetailPanel.setBorder(BorderFactory.createTitledBorder("关卡详情"));
        
        levelDescriptionLabel = new JLabel("请选择一个关卡查看详情", SwingConstants.CENTER);
        levelDescriptionLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        levelDetailPanel.add(levelDescriptionLabel, BorderLayout.CENTER);
        
        JButton confirmButton = new JButton("确认关卡");
        confirmButton.setEnabled(false);
        confirmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedLevel > 0) {
                    Logic.loadLevel(selectedLevel);
                    dispose();
                    
                    // 根据不同关卡连接到不同模块
                    switch (selectedLevel) {
                        case 1:
                            // 关卡1连接到control2, game2
                            new GameFrame2().setVisible(true);
                            break;
                        case 2:
                            // 关卡2连接到control, game
                            new GameFrame().setVisible(true);
                            break;
                        case 3:
                            // 关卡3连接到control3, game3
                            new GameFrame3().setVisible(true);
                            break;
                        default:
                            // 默认使用普通Frame
                            new Frame().setVisible(true);
                            break;
                    }
                }
            }
        });
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(confirmButton);
        levelDetailPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // 创建分割面板
        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(levelPanel),
                levelDetailPanel);
        splitPane.setDividerLocation(250);
        add(splitPane, BorderLayout.CENTER);

        JButton backButton = new JButton("返回主菜单");
        backButton.addActionListener(e -> {
            dispose();
            new AuthFrame().setVisible(true);
        });
        add(backButton, BorderLayout.SOUTH);
    }
    
    /**
     * 更新关卡详情面板
     * @param levelNum 关卡编号
     */
    private void updateLevelDetailPanel(int levelNum) {
        // 更新关卡描述
        String description = "<html><body>";
        description += "<h2>关卡 " + levelNum + " 详情</h2>";
        description += "<p>这是关卡" + levelNum + "的详细说明，待补充。</p>";
        
        switch (levelNum) {
            case 1:
                description += "<p>难度：Easy</p>";
                description += "<p>模块：control2, game2</p>（调试用）";
                break;
            case 2:
                description += "<p>难度：Hard</p>";
                description += "<p>模块：control, game</p>（调试用）";
                break;
            case 3:
                description += "<p>难度：Limit Time</p>";
                description += "<p>模块：control3, game3</p>（调试用）";
                break;
        }
        
        description += "</body></html>";
        levelDescriptionLabel.setText(description);
        
        // 启用确认按钮
        Component buttonComponent = levelDetailPanel.getComponent(1);
        if (buttonComponent instanceof Container) {
            Container buttonContainer = (Container) buttonComponent;
            for (Component comp : buttonContainer.getComponents()) {
                if (comp instanceof JButton) {
                    comp.setEnabled(true);
                }
            }
        }
        
        // 刷新界面
        levelDetailPanel.revalidate();
        levelDetailPanel.repaint();
    }
}