package view.game2;

import javax.swing.SwingUtilities;

/**
 * 用于测试游戏2的窗口自适应调整功能
 */
public class TestGame2 {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameFrame2 game = new GameFrame2();
            game.setVisible(true);
        });
    }
} 