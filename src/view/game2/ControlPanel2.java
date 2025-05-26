package view.game2;

import controller2.Direction2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/*
* 这个类完成了ControlPanel的构建，里面有上下左右，撤销，重置，保存，重新加载按键，且实现的它们的全部功能。
* 这个类没有提供其它可用方法
 */
public class ControlPanel2 extends JPanel implements ActionListener {

    private GameFrame2 mainFrame;
    //这里创建了一个GameFrame本人认为是一个非常精妙绝伦的点，一方面后面的调度需要一个GameFrame对象，
    //因为它是最底层的JFrame，里面包含了所有的members，另一方面看似这个controlPanel理应在GameFrame之下，
    //但是又创建一个GameFrame显得逻辑混乱，但是只需要在最终创建controlPanel时传入GameFrame的参数用this即可完美化解循环嵌套

    private JButton upButton, downButton, leftButton, rightButton;
    private JButton undoButton, resetButton;
    private JButton saveButton, loadButton;
    //这里创建了游戏中的所有Button

    public ControlPanel2(GameFrame2 frame) {
        this.mainFrame = frame;

        setLayout(new GridBagLayout());
        /*
        *这里补充几种常见的setLayout：
        * 1.BorderLayout：将容器划分为五个区域：NORTH (北, 上)、SOUTH (南, 下)、
        * WEST (西, 左)、EAST (东, 右) 和 CENTER (中)。
        * 添加组件时需要指定要放入哪个区域，即.add(myButton, BorderLayout.NORTH);。
        * 2.FlowLayout：像文字排版一样，将组件从左到右、从上到下地依次排列。
        * 如果当前行放不下，它会自动"流"到下一行。
        * 3.GridBagLayout：最复杂但是也最自由的排列形式，
        * 由于ControlPanel里面有上面8个JLabel，所以我决定使用这种setLayout
        * 这里简单介绍一下：
        *   （1）每一个组件都有它的一个约束GridBagConstraints，约束了组件的以下性质：gridx, gridy：组件左上角所在的网格单元坐标；
        *       gridwidth, gridheight: 组件横跨的列数和行数等
        *   （2）anchor,即分配到的区域，且比BorderLayout更加复杂，它还有西南，东北这样的方向
        *   （3）inserts，即设定边距
        *   （4）fill，设定某个组件在其所在区域是否要拉伸
        *   （5）……
         */
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        // 组件间的间距

        upButton = new JButton("↑ (W)");
        downButton = new JButton("↓ (S)");
        leftButton = new JButton("← (A)");
        rightButton = new JButton("→ (D)");
        undoButton = new JButton("Undo");
        resetButton = new JButton("Reset");
        saveButton = new JButton("Save");
        loadButton = new JButton("Load");
        // 创建按钮

        // 上按钮 (第0行，第1列)
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 1; // 占1列
        add(upButton, gbc);

        // 左按钮 (第1行，第0列)
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(leftButton, gbc);

        // 下按钮 (第1行，第1列)
        gbc.gridx = 1;
        gbc.gridy = 1;
        add(downButton, gbc);

        // 右按钮 (第1行，第2列)
        gbc.gridx = 2;
        gbc.gridy = 1;
        add(rightButton, gbc);

        // 功能按钮 - 放在移动按钮下方或旁边
        // 撤销按钮 (第2行，第0列)
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(undoButton, gbc);

        // 重置按钮 (第2行，第1列)
        gbc.gridx = 1;
        gbc.gridy = 2;
        add(resetButton, gbc);

        // 保存按钮 (第2行，第2列)
        gbc.gridx = 2;
        gbc.gridy = 2;
        add(saveButton, gbc);

        // 加载按钮 (第2行，第3列)
        gbc.gridx = 3;
        gbc.gridy = 2;
        add(loadButton, gbc);

        upButton.addActionListener(this);
        downButton.addActionListener(this);
        leftButton.addActionListener(this);
        rightButton.addActionListener(this);
        undoButton.addActionListener(this);
        resetButton.addActionListener(this);
        saveButton.addActionListener(this);
        loadButton.addActionListener(this);
        // 为所有按钮添加动作监听器
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        // 根据不同的按钮执行不同的动作
        if (source == upButton) {
            handleMovement(Direction2.UP);
        } else if (source == downButton) {
            handleMovement(Direction2.DOWN);
        } else if (source == leftButton) {
            handleMovement(Direction2.LEFT);
        } else if (source == rightButton) {
            handleMovement(Direction2.RIGHT);
        } else if (source == undoButton) {
            mainFrame.handleUndo();
        } else if (source == resetButton) {
            mainFrame.handleReset();
        } else if (source == saveButton) {
            mainFrame.handleSave();
        } else if (source == loadButton) {
            mainFrame.handleLoad();
        }
        mainFrame.requestFocusInWindow();
    }
    // 这个方法实现了 ActionListener 接口

    private void handleMovement(Direction2 direction2) {
        // 如果动画正在进行，忽略移动操作
        if (mainFrame.getGamePanel().isAnimating()) {
            return;
        }
        
        // 获取当前选中的棋子
        controller2.Block2 selectedBlock = mainFrame.getGameLogic().getSelectedBlock();
        if (selectedBlock == null) {
            String message = "No block selected";
            String title = "Error";
            JOptionPane.showMessageDialog(mainFrame, message, title, JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // 检查是否可以移动
        if (mainFrame.getGameLogic().canMove(selectedBlock, direction2.getDx(), direction2.getDy())) {
            // 开始动画
            mainFrame.getGamePanel().animateBlockMove(selectedBlock, direction2);
            
            // 设置动画完成后的回调
            final Direction2 finalDirection = direction2;
            mainFrame.getGamePanel().setAnimationCompleteCallback(() -> {
                // 动画完成后执行实际的棋子移动
                boolean success = mainFrame.getGameLogic().moveSelectedBlock(finalDirection);
                
                // 在模型更新后，通知BlockAnimator可以清理已完成的动画状态
                if (mainFrame.getGamePanel().getBlockAnimator() != null) {
                    mainFrame.getGamePanel().getBlockAnimator().finalizeAllPendingAnimations();
                }
                
                if (success) {
                    mainFrame.refreshGameView();
                    mainFrame.checkAndShowWinDialog();
                } else {
                    // 如果移动不成功（理论上在canMove检查后不应发生）
                    // 也应重绘以确保视图与模型一致。
                    mainFrame.getGamePanel().repaint();
                }
            });
        } else {
            String message = String.format("Cannot move %swards", direction2);
            String title = "Irremovable";
            JOptionPane.showMessageDialog(mainFrame, message, title, JOptionPane.WARNING_MESSAGE);
        }
    }
    //这里写一个方法来帮助上面actionPerformed的书写，别的地方不能用
}
