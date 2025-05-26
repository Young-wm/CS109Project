package view.game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
// import java.awt.event.MouseMotionAdapter; // 这个导入似乎未使用，可以移除
import java.util.LinkedList;
import java.util.List; // 明确导入 List

public class MouseTrailLayer extends JPanel {
    private final List<Point> trailPoints = new LinkedList<>(); // 使用接口 List 声明

    public MouseTrailLayer() {
        setOpaque(false);

        // 添加 mouseMotionListener 到全局事件分发器（或窗口）
        Toolkit.getDefaultToolkit().addAWTEventListener(e -> {
            if (e instanceof MouseEvent mouseEvent && mouseEvent.getID() == MouseEvent.MOUSE_MOVED) {
                SwingUtilities.invokeLater(() -> {
                    Point p = SwingUtilities.convertPoint(
                            ((MouseEvent) e).getComponent(),
                            mouseEvent.getPoint(),
                            this
                    );
                    trailPoints.add(p);
                    if (trailPoints.size() > 15) { // 稍微调整一下条件判断的括号
                        trailPoints.remove(0);
                    }
                    repaint();
                });
            }
        }, AWTEvent.MOUSE_MOTION_EVENT_MASK);

        new Timer(30, e -> repaint()).start();
    }

    @Override
    public boolean contains(int x, int y) {
        return false; // 关键：让鼠标事件透过此层
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        try { // 使用 try-finally 确保 g2d 被 dispose
            for (int i = 0; i < trailPoints.size(); i++) {
                Point p = trailPoints.get(i);
                float alpha = (float) i / trailPoints.size();
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                g2d.setColor(Color.CYAN); // 可以考虑让颜色可配置
                g2d.fillOval(p.x - 5, p.y - 5, 10, 10); // 轨迹点的大小也可以考虑可配置
            }
        } finally {
            g2d.dispose();
        }
    }
} 