package view.frontend.level.menu;

import view.frontend.controller.Logic;
import view.frontend.level.game.Frame;
import view.game.GameFrame;
import view.game2.GameFrame2;
import view.game3.GameFrame3;
import view.frontend.LoginFrame.AuthFrame;
import view.frontend.controller.HoverButton;
import view.frontend.TextAnimator;
import view.audio.AudioManager;
import view.frontend.resourses.ResourceManager;
import view.game.MouseTrailLayer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.List;
import java.io.File;

public class LevelSelectionFrame extends JFrame implements ComponentListener {
    private int selectedLevel = 1; // 默认选中第一关
    private JPanel levelDetailPanel;
    private JLabel levelDescriptionLabel;
    private TextAnimator textAnimator;
    private Image backgroundImage;
    
    // 轮盘式选关界面组件
    private JPanel carouselPanel;
    private List<JPanel> levelPanels = new ArrayList<>();
    private int totalLevels;
    
    // 轮盘参数
    private static final int MAIN_PANEL_SIZE = 350; // 中心面板改为正方形
    private static final int SIDE_PANEL_WIDTH = 220;
    private static final int SIDE_PANEL_HEIGHT = 220;
    private static final float SIDE_PANEL_ALPHA = 0.7f; // 侧面板透明度
    
    // 梯形效果参数
    private static final double TRAPEZOID_TOP_RATIO = 0.8; // 梯形顶部宽度比例
    private static final double TRAPEZOID_BOTTOM_RATIO = 1.0; // 梯形底部宽度比例
    
    public LevelSelectionFrame() {
        try {
            setTitle("选择关卡");
            setSize(800, 600);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);
            setLayout(new BorderLayout());
            addComponentListener(this); // Add component listener
            
            // 加载背景图片
            try {
                backgroundImage = ResourceManager.loadImage("images/level_selection_background.jpg");
                if (backgroundImage == null) {
                    System.err.println("警告: 无法加载关卡选择背景图片，将使用默认背景");
                }
            } catch (Exception e) {
                System.err.println("加载背景图片时出错");
                e.printStackTrace();
            }
            
            // 初始化音频
            initAudio();

            JLabel titleLabel = new JLabel("请选择关卡", SwingConstants.CENTER);
            titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
            titleLabel.setForeground(Color.WHITE);
            add(titleLabel, BorderLayout.NORTH);
            
            // 获取关卡数量，确保至少有一个关卡
            try {
                totalLevels = Logic.getNumberOfLevels();
                if (totalLevels <= 0) {
                    System.err.println("警告: 获取关卡数量失败，设置为默认值3");
                    totalLevels = 3;
                }
            } catch (Exception e) {
                System.err.println("获取关卡数量失败，设置为默认值3");
                e.printStackTrace();
                totalLevels = 3;
            }
            
            // 创建关卡详情面板（先创建这个，因为轮盘面板会引用它）
            createLevelDetailPanel();
            add(levelDetailPanel, BorderLayout.SOUTH);
            
            // 创建轮盘式选关面板
            createCarouselPanel();
            add(carouselPanel, BorderLayout.CENTER);
            
            // 更新关卡详情
            updateLevelDetailPanel(selectedLevel);
            
            // 设置自定义鼠标光标
            setCustomCursor();
            
            // 播放背景音乐
            try {
                AudioManager.getInstance().playDefaultBGM();
            } catch (Exception e) {
                System.err.println("播放背景音乐失败");
                e.printStackTrace();
            }

            // 添加鼠标轨迹层
            MouseTrailLayer mouseTrailLayer = new MouseTrailLayer();
            setGlassPane(mouseTrailLayer);
            mouseTrailLayer.setVisible(true);

        } catch (Exception e) {
            System.err.println("初始化关卡选择界面失败");
            e.printStackTrace();
            // 创建一个简单的错误界面
            getContentPane().removeAll();
            setLayout(new BorderLayout());
            JPanel errorPanel = new JPanel(new BorderLayout());
            errorPanel.setBackground(new Color(30, 30, 30));
            JLabel errorLabel = new JLabel("加载关卡选择界面失败，请检查资源文件", SwingConstants.CENTER);
            errorLabel.setForeground(Color.WHITE);
            errorLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
            errorPanel.add(errorLabel, BorderLayout.CENTER);
            
            JButton returnButton = new JButton("返回主菜单");
            returnButton.addActionListener(e2 -> {
                dispose();
                try {
                    new AuthFrame().setVisible(true);
                } catch (Exception ex) {
                    System.exit(0); // 如果连返回都失败，直接退出程序
                }
            });
            JPanel buttonPanel = new JPanel();
            buttonPanel.setOpaque(false);
            buttonPanel.add(returnButton);
            errorPanel.add(buttonPanel, BorderLayout.SOUTH);
            
            add(errorPanel);
        }
    }
    
    /**
     * 初始化音频设置
     */
    private void initAudio() {
        try {
            AudioManager audioManager = AudioManager.getInstance();
            
            // 如果没有设置默认音频路径，则设置
            if (audioManager.getDefaultBgmPath() == null) {
                // 检查音频文件是否存在
                File bgmFile = new File("src/view/audio/resources/bgm.wav");
                File clickFile = new File("src/view/audio/resources/button_click.wav");
                File hoverFile = new File("src/view/audio/resources/button_hover.wav");
                File pieceMoveFile = new File("src/view/audio/resources/piece_move.wav");
                
                // 如果任何一个文件不存在，给出警告但不崩溃
                if (!bgmFile.exists() || !clickFile.exists() || !hoverFile.exists() || !pieceMoveFile.exists()) {
                    System.err.println("警告: 部分音频文件不存在，音频功能可能不完整");
                }
                
                audioManager.setDefaultBgmPath("src/view/audio/resources/bgm.wav");
                audioManager.setDefaultButtonClickPath("src/view/audio/resources/button_click.wav");
                audioManager.setDefaultButtonHoverPath("src/view/audio/resources/button_hover.wav");
                audioManager.setDefaultPieceMovePath("src/view/audio/resources/piece_move.wav");
            }
            
            // 尝试播放背景音乐，如果失败则禁用音频
            try {
                audioManager.playDefaultBGM();
            } catch (Exception e) {
                System.err.println("播放背景音乐失败，音频功能将被禁用");
                e.printStackTrace();
                audioManager.setAudioEnabled(false);
            }
        } catch (Exception e) {
            System.err.println("初始化音频系统失败");
            e.printStackTrace();
        }
    }
    
    /**
     * 创建轮盘式选关面板
     */
    private void createCarouselPanel() {
        try {
            carouselPanel = new JPanel(null) { // 使用null布局以便精确定位
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    // 绘制背景图片
                    if (backgroundImage != null) {
                        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                    } else {
                        // 如果没有背景图片，绘制渐变背景
                        Graphics2D g2d = (Graphics2D) g;
                        GradientPaint gradient = new GradientPaint(
                                0, 0, new Color(0, 30, 60),
                                0, getHeight(), new Color(0, 10, 30));
                        g2d.setPaint(gradient);
                        g2d.fillRect(0, 0, getWidth(), getHeight());
                    }
                }
            };
            
            // 确保totalLevels至少为1
            if (totalLevels <= 0) {
                System.err.println("警告: 关卡数量为0或负数，设置为默认值1");
                totalLevels = 1;
            }
            
            // 创建各个关卡面板
            for (int i = 1; i <= totalLevels; i++) {
                JPanel levelPanel = createLevelPanel(i);
                levelPanels.add(levelPanel);
                carouselPanel.add(levelPanel);
            }
            
            // 创建左右切换按钮
            HoverButton prevButton = new HoverButton("<");
            prevButton.setFont(new Font("Arial", Font.BOLD, 24));
            prevButton.setBounds(20, carouselPanel.getHeight() / 2 - 25, 50, 50);
            prevButton.addActionListener(e -> {
                try {
                    AudioManager.getInstance().playDefaultButtonClickSound();
                } catch (Exception ex) {
                    System.err.println("播放按钮点击音效失败");
                }
                rotateCarousel(false); // 向左旋转
            });
            
            HoverButton nextButton = new HoverButton(">");
            nextButton.setFont(new Font("Arial", Font.BOLD, 24));
            nextButton.setBounds(carouselPanel.getWidth() - 70, carouselPanel.getHeight() / 2 - 25, 50, 50);
            nextButton.addActionListener(e -> {
                try {
                    AudioManager.getInstance().playDefaultButtonClickSound();
                } catch (Exception ex) {
                    System.err.println("播放按钮点击音效失败");
                }
                rotateCarousel(true); // 向右旋转
            });
            
            carouselPanel.add(prevButton);
            carouselPanel.add(nextButton);
            
            // 初始化轮盘位置
            updateCarouselLayout();
        } catch (Exception e) {
            System.err.println("创建轮盘面板失败");
            e.printStackTrace();
            
            // 创建一个简单的备用面板
            carouselPanel = new JPanel(new BorderLayout());
            carouselPanel.setBackground(new Color(0, 30, 60));
            JLabel errorLabel = new JLabel("无法加载关卡选择界面，请检查资源文件", SwingConstants.CENTER);
            errorLabel.setForeground(Color.WHITE);
            errorLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
            carouselPanel.add(errorLabel, BorderLayout.CENTER);
        }
    }
    
    /**
     * 创建单个关卡面板
     * @param levelNum 关卡编号
     * @return 关卡面板
     */
    private JPanel createLevelPanel(int levelNum) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        
        // 关卡标题
        JLabel titleLabel = new JLabel("关卡 " + levelNum, SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // 关卡图片（可以根据关卡编号加载不同图片）
        String imagePath = "images/level" + levelNum + ".jpg";
        Image levelImage = null;
        
        try {
            levelImage = ResourceManager.loadImage(imagePath);
        } catch (Exception e) {
            System.err.println("加载关卡图片失败: " + imagePath);
            e.printStackTrace();
        }
        
        JLabel imageLabel;
        if (levelImage != null) {
            // 预缩放图像以适应面板大小
            Image scaledImage = levelImage.getScaledInstance(
                MAIN_PANEL_SIZE - 50, 
                MAIN_PANEL_SIZE - 100, 
                Image.SCALE_SMOOTH
            );
            imageLabel = new JLabel(new ImageIcon(scaledImage));
        } else {
            // 如果没有图片，使用纯色面板
            JPanel colorPanel = new JPanel();
            colorPanel.setBackground(new Color(30, 30, 30 + levelNum * 20));
            colorPanel.setPreferredSize(new Dimension(MAIN_PANEL_SIZE - 50, MAIN_PANEL_SIZE - 100));
            colorPanel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
            
            // 在颜色面板中添加关卡信息
            colorPanel.setLayout(new BorderLayout());
            JLabel infoLabel = new JLabel("<html><div style='text-align: center;'>" +
                "<h2>关卡 " + levelNum + "</h2>" +
                "<p>华容道谜题</p>" +
                "</div></html>", SwingConstants.CENTER);
            infoLabel.setForeground(Color.WHITE);
            infoLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            colorPanel.add(infoLabel, BorderLayout.CENTER);
            
            imageLabel = new JLabel();
            imageLabel.add(colorPanel);
        }
        panel.add(imageLabel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 更新轮盘布局
     */
    private void updateCarouselLayout() {
        try {
            if (carouselPanel == null || levelPanels == null || levelPanels.isEmpty()) {
                System.err.println("警告: 无法更新轮盘布局，组件未初始化");
                return;
            }
            
            int centerX = carouselPanel.getWidth() / 2;
            int centerY = carouselPanel.getHeight() / 2;
            
            // 确保selectedLevel在有效范围内
            if (selectedLevel < 1 || selectedLevel > totalLevels) {
                selectedLevel = 1;
            }
            
            // 确定当前选中关卡的索引
            int currentIndex = selectedLevel - 1;
            
            // 计算前一个和后一个关卡的索引
            int prevIndex = (currentIndex - 1 + totalLevels) % totalLevels;
            int nextIndex = (currentIndex + 1) % totalLevels;
            
            // 重新布局所有关卡面板
            for (int i = 0; i < levelPanels.size(); i++) {
                JPanel panel = levelPanels.get(i);
                
                if (i == currentIndex) {
                    // 当前选中关卡居中显示 - 方形且完全不透明
                    panel.setBounds(
                        centerX - MAIN_PANEL_SIZE / 2,
                        centerY - MAIN_PANEL_SIZE / 2,
                        MAIN_PANEL_SIZE,
                        MAIN_PANEL_SIZE
                    );
                    panel.setVisible(true);
                    // 重置透明度为完全不透明
                    setComponentAlpha(panel, 1.0f);
                } else if (i == prevIndex) {
                    // 前一个关卡显示在左侧 - 梯形效果
                    panel.setBounds(
                        centerX - MAIN_PANEL_SIZE / 2 - SIDE_PANEL_WIDTH + 20,
                        centerY - SIDE_PANEL_HEIGHT / 2,
                        SIDE_PANEL_WIDTH,
                        SIDE_PANEL_HEIGHT
                    );
                    panel.setVisible(true);
                    // 应用梯形和透明度效果
                    applyTrapezoidEffect(panel, true); // true = 左侧梯形
                } else if (i == nextIndex) {
                    // 后一个关卡显示在右侧 - 梯形效果
                    panel.setBounds(
                        centerX + MAIN_PANEL_SIZE / 2 - 20,
                        centerY - SIDE_PANEL_HEIGHT / 2,
                        SIDE_PANEL_WIDTH,
                        SIDE_PANEL_HEIGHT
                    );
                    panel.setVisible(true);
                    // 应用梯形和透明度效果
                    applyTrapezoidEffect(panel, false); // false = 右侧梯形
                } else {
                    // 其他关卡隐藏
                    panel.setVisible(false);
                }
            }
            
            // 更新关卡详情
            updateLevelDetailPanel(selectedLevel);
            
            // 重绘界面
            carouselPanel.revalidate();
            carouselPanel.repaint();
        } catch (Exception e) {
            System.err.println("更新轮盘布局失败");
            e.printStackTrace();
        }
    }
    
    /**
     * 设置组件透明度
     * @param component 组件
     * @param alpha 透明度 (0.0-1.0)
     */
    private void setComponentAlpha(JComponent component, float alpha) {
        component.setOpaque(false);
        for (Component child : component.getComponents()) {
            if (child instanceof JComponent) {
                ((JComponent) child).setOpaque(false);
            }
        }
    }
    
    /**
     * 应用梯形效果到面板
     * @param panel 要应用效果的面板
     * @param isLeftSide 是否为左侧面板（影响梯形方向）
     */
    private void applyTrapezoidEffect(JPanel panel, boolean isLeftSide) {
        try {
            // 设置面板为透明
            panel.setOpaque(false);
            
            // 递归处理所有子组件
            applyTrapezoidEffectRecursive(panel, isLeftSide);
            
        } catch (Exception e) {
            System.err.println("应用梯形效果失败: " + e.getMessage());
            e.printStackTrace();
            // 如果梯形效果失败，至少应用透明度效果
            setComponentAlpha(panel, SIDE_PANEL_ALPHA);
        }
    }
    
    /**
     * 递归应用梯形效果到组件及其子组件
     * @param component 要处理的组件
     * @param isLeftSide 是否为左侧面板
     */
    private void applyTrapezoidEffectRecursive(Component component, boolean isLeftSide) {
        if (component instanceof JLabel) {
            JLabel label = (JLabel) component;
            Icon icon = label.getIcon();
            
            if (icon instanceof ImageIcon) {
                ImageIcon imageIcon = (ImageIcon) icon;
                Image originalImage = imageIcon.getImage();
                
                // 根据是左侧还是右侧调整梯形参数
                double topRatio, bottomRatio;
                if (isLeftSide) {
                    // 左侧梯形：左宽右窄
                    topRatio = TRAPEZOID_BOTTOM_RATIO;
                    bottomRatio = TRAPEZOID_TOP_RATIO;
                } else {
                    // 右侧梯形：左窄右宽
                    topRatio = TRAPEZOID_TOP_RATIO;
                    bottomRatio = TRAPEZOID_BOTTOM_RATIO;
                }
                
                // 应用梯形和透明度效果
                Image processedImage = ResourceManager.createTrapezoidAlphaImage(
                    originalImage,
                    SIDE_PANEL_WIDTH,
                    SIDE_PANEL_HEIGHT,
                    topRatio,
                    bottomRatio,
                    SIDE_PANEL_ALPHA
                );
                
                if (processedImage != null) {
                    label.setIcon(new ImageIcon(processedImage));
                }
            }
        } else if (component instanceof JPanel) {
            JPanel childPanel = (JPanel) component;
            childPanel.setOpaque(false);
            
            // 如果是颜色面板，应用透明度效果
            Color originalColor = childPanel.getBackground();
            if (originalColor != null) {
                Color transparentColor = new Color(
                    originalColor.getRed(),
                    originalColor.getGreen(),
                    originalColor.getBlue(),
                    (int) (255 * SIDE_PANEL_ALPHA)
                );
                childPanel.setBackground(transparentColor);
            }
            
            // 递归处理子组件
            for (Component child : childPanel.getComponents()) {
                applyTrapezoidEffectRecursive(child, isLeftSide);
            }
        }
        
        // 设置组件透明
        if (component instanceof JComponent) {
            ((JComponent) component).setOpaque(false);
        }
    }
    
    /**
     * 旋转轮盘
     * @param clockwise 是否顺时针旋转
     */
    private void rotateCarousel(boolean clockwise) {
        if (clockwise) {
            selectedLevel = (selectedLevel % totalLevels) + 1;
        } else {
            selectedLevel = (selectedLevel - 1 == 0) ? totalLevels : selectedLevel - 1;
        }
        
        // 更新轮盘布局
        updateCarouselLayout();
    }
    
    /**
     * 创建关卡详情面板
     */
    private void createLevelDetailPanel() {
        levelDetailPanel = new JPanel(new BorderLayout());
        levelDetailPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        levelDetailPanel.setOpaque(false);
        
        levelDescriptionLabel = new JLabel("", SwingConstants.CENTER);
        levelDescriptionLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        levelDescriptionLabel.setForeground(Color.WHITE);
        levelDetailPanel.add(levelDescriptionLabel, BorderLayout.CENTER);
        
        // 初始化文本动画器
        try {
            textAnimator = new TextAnimator(levelDescriptionLabel, 20); // 每秒输出20个字符
        } catch (Exception e) {
            System.err.println("初始化文本动画器失败");
            e.printStackTrace();
            // 如果文本动画器初始化失败，创建一个空实现
            textAnimator = new TextAnimator(levelDescriptionLabel, 20) {
                @Override
                public void animateText(String text) {
                    // 简单实现：直接设置文本，不做动画
                    if (levelDescriptionLabel != null) {
                        levelDescriptionLabel.setText(text);
                    }
                }
            };
        }
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        
        HoverButton confirmButton = new HoverButton("开始游戏");
        confirmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
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
                } catch (Exception ex) {
                    System.err.println("启动游戏关卡失败: " + ex.getMessage());
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(LevelSelectionFrame.this, 
                        "启动游戏关卡失败。\n错误信息: " + ex.getMessage(), 
                        "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        buttonPanel.add(confirmButton);
        
        HoverButton backButton = new HoverButton("返回主菜单");
        backButton.addActionListener(e -> {
            try {
                dispose();
                new AuthFrame().setVisible(true);
            } catch (Exception ex) {
                System.err.println("返回主菜单失败: " + ex.getMessage());
                ex.printStackTrace();
                JOptionPane.showMessageDialog(LevelSelectionFrame.this, 
                    "返回主菜单失败。\n错误信息: " + ex.getMessage(), 
                    "错误", JOptionPane.ERROR_MESSAGE);
                // 尝试强制关闭当前窗口
                dispose();
            }
        });
        buttonPanel.add(backButton);
        
        levelDetailPanel.add(buttonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * 更新关卡详情面板
     * @param levelNum 关卡编号
     */
    private void updateLevelDetailPanel(int levelNum) {
        // 更新关卡描述
        String description;
        
        switch (levelNum) {
            case 1:
                description = "关卡1：初级难度\n这是一个入门级别的华容道谜题，适合新手玩家。\n目标：将曹操移动到底部出口。";
                break;
            case 2:
                description = "关卡2：中级难度\n这个关卡难度适中，需要一定的思考能力。\n目标：将曹操移动到底部出口。";
                break;
            case 3:
                description = "关卡3：高级难度\n这是一个挑战性的关卡，需要深思熟虑的策略。\n目标：在限定时间内完成谜题。";
                break;
            default:
                description = "关卡" + levelNum + "：\n这是一个标准难度的华容道谜题。\n目标：将曹操移动到底部出口。";
                break;
        }
        
        // 使用动画显示文本，添加空指针检查
        try {
            if (textAnimator != null) {
                textAnimator.animateText(description);
            } else {
                // 如果textAnimator为null，直接设置文本
                if (levelDescriptionLabel != null) {
                    levelDescriptionLabel.setText(description);
                }
            }
        } catch (Exception e) {
            System.err.println("显示关卡描述文本失败");
            e.printStackTrace();
            // 直接设置文本作为备选方案
            if (levelDescriptionLabel != null) {
                levelDescriptionLabel.setText(description);
            }
        }
        
        // 刷新界面
        if (levelDetailPanel != null) {
            levelDetailPanel.revalidate();
            levelDetailPanel.repaint();
        }
    }
    
    /**
     * 重写组件调整大小方法，更新轮盘布局
     */
    @Override
    public void componentResized(java.awt.event.ComponentEvent e) {
        if (carouselPanel != null) {
            updateCarouselLayout();
        }
    }
    
    // Add other required methods for ComponentListener
    @Override
    public void componentMoved(ComponentEvent e) {
        // Not used
    }

    @Override
    public void componentShown(ComponentEvent e) {
        // Not used
    }

    @Override
    public void componentHidden(ComponentEvent e) {
        // Not used
    }
    
    /**
     * 设置自定义鼠标光标
     */
    private void setCustomCursor() {
        try {
            // 尝试设置自定义光标
            ResourceManager.setCursor(this, "images/cursor.png", 0, 0);
        } catch (Exception e) {
            System.err.println("设置自定义光标失败，将使用默认光标");
            e.printStackTrace();
            // 失败时使用默认光标
            setCursor(Cursor.getDefaultCursor());
        }
    }
    
    /**
     * 主方法，用于测试
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                LevelSelectionFrame frame = new LevelSelectionFrame();
                frame.setCustomCursor(); // 设置自定义光标
                frame.setVisible(true);
            } catch (Exception e) {
                System.err.println("启动关卡选择界面失败");
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, 
                    "启动游戏界面失败。\n错误信息: " + e.getMessage(), 
                    "错误", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}