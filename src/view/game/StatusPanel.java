package view.game;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
/*
* 1.这个类是最终的JFrame上表示游戏状态的JPanel, 有stepsLabel以及timeLabel这两个标签，并且设置好了它们的字体和位置
* 2.这个类提供了一个好用的方法，即updateStatus(int steps, long totalSeconds)，
* 而且这两个变量都是在前面的程序里面就实现了自动的更新了的，后面再用时只需要传入目前的数据即可
 */
public class StatusPanel extends JPanel {

    private JLabel stepsLabel;
    private JLabel timeLabel;
    //在状态栏准备添加两个JLabel，一个是表示步数的，一个是计时器这个Label

    public StatusPanel() {
        setLayout(new FlowLayout(FlowLayout.CENTER, 20, 5));
        // 使用流式布局，居中对齐，组件间水平间距20，垂直间距5
        setBorder(new EmptyBorder(5, 10, 5, 10));
        // 添加一些内边距

        stepsLabel = new JLabel("Steps: 0");
        timeLabel = new JLabel("Timer: 00:00");


        Font labelFont = new Font("SansSerif", Font.BOLD, 14);
        //设置了字体
        stepsLabel.setFont(labelFont);
        timeLabel.setFont(labelFont);

        add(stepsLabel);
        add(timeLabel);
    }

    public void updateStatus(int steps, long totalSeconds) {
        stepsLabel.setText("Steps: " + steps);
        timeLabel.setText("Timer: " + GameFrame.formatTime(totalSeconds));
    }

}
