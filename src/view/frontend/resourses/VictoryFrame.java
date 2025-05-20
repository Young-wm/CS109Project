package view.frontend.resourses;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import view.frontend.level.menu.LevelSelectionFrame;
import view.game.GameFrame;
import view.game2.GameFrame2;
import view.game3.GameFrame3;
import view.game4.GameFrame4;

/**
 * 游戏胜利窗口
 * 显示游戏胜利信息的独立窗口
 */
public class VictoryFrame extends JFrame {
    private VictoryPanel victoryPanel;
    private int currentLevel;

    /**
     * 创建胜利窗口
     * @param timeSpent 花费时间的格式化字符串
     * @param steps 移动步数
     * @param level 当前关卡号
     * @param victoryImagePath 胜利背景图片路径（可为null使用默认背景）
     */
    public VictoryFrame(String timeSpent, int steps, int level, String victoryImagePath) {
        this.currentLevel = level;
        
        // 设置窗口属性
        setTitle("胜利");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        
        // 创建胜利面板
        victoryPanel = new VictoryPanel(timeSpent, steps);
        
        // 加载胜利背景图片
        if (victoryImagePath != null && !victoryImagePath.isEmpty()) {
            Image backgroundImage = ResourceManager.loadImage(victoryImagePath);
            if (backgroundImage != null) {
                // 确保图片完全加载，以获取正确的尺寸信息
                backgroundImage = ResourceManager.ensureImageLoaded(backgroundImage);
                victoryPanel.setBackgroundImage(backgroundImage);
            }
        }
        
        // 添加按钮事件监听器
        addButtonListeners();
        
        // 添加面板到窗口
        add(victoryPanel);
        
        // 调整窗口大小并居中显示
        pack();
        setLocationRelativeTo(null);
        
        // 添加窗口关闭事件
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
    }
    
    /**
     * 添加按钮事件监听器
     */
    private void addButtonListeners() {
        // 返回按钮事件
        victoryPanel.addReturnButtonListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // 关闭胜利窗口
                
                // 返回关卡选择界面
                SwingUtilities.invokeLater(() -> {
                    new LevelSelectionFrame().setVisible(true);
                });
            }
        });
        
        // 下一关按钮事件
        victoryPanel.addNextLevelButtonListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // 关闭胜利窗口
                
                // 根据当前关卡打开下一关
                SwingUtilities.invokeLater(() -> {
                    openNextLevel(currentLevel + 1);
                });
            }
        });
    }
    
    /**
     * 打开指定关卡
     * @param level 关卡编号
     */
    private void openNextLevel(int level) {
        // 根据关卡编号打开对应关卡
        if (level > 4) {
            // 如果已经是最后一关，则返回关卡选择界面
            new LevelSelectionFrame().setVisible(true);
            JOptionPane.showMessageDialog(null, 
                    "恭喜你已通关所有关卡！", 
                    "游戏通关", 
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        switch (level) {
            case 1:
                new GameFrame().setVisible(true);
                break;
            case 2:
                new GameFrame2().setVisible(true);
                break;
            case 3:
                new GameFrame3().setVisible(true);
                break;
            case 4:
                new GameFrame4().setVisible(true);
                break;
            default:
                new LevelSelectionFrame().setVisible(true);
                break;
        }
    }
    
    /**
     * 显示胜利窗口
     * @param timeSpent 花费时间
     * @param steps 移动步数
     * @param level 当前关卡
     * @param victoryImagePath 胜利背景图片路径（可为null）
     */
    public static void showVictory(String timeSpent, int steps, int level, String victoryImagePath) {
        SwingUtilities.invokeLater(() -> {
            VictoryFrame victoryFrame = new VictoryFrame(timeSpent, steps, level, victoryImagePath);
            victoryFrame.setVisible(true);
        });
    }
} 