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
import view.frontend.resourses.ImageTransformer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
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
    private static final int MAIN_PANEL_WIDTH = 400;
    private static final int MAIN_PANEL_HEIGHT = 400; // 正方形布局
    private static final int SIDE_PANEL_WIDTH = 250;
    private static final int SIDE_PANEL_HEIGHT = 300;
    private static final float MAIN_PANEL_ALPHA = 1.0f; // 中间面板完全不透明
    private static final float SIDE_PANEL_ALPHA = 0.6f; // 增加侧面板透明度
    private static final double TRAPEZOID_FACTOR = 0.3; // 梯形变换程度
    
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
            titleLabel.setForeground(Color.BLACK);
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
            
            // 播放背景音乐
            try {
                AudioManager.getInstance().playDefaultBGM();
            } catch (Exception e) {
                System.err.println("播放背景音乐失败");
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println("初始化关卡选择界面失败");
            e.printStackTrace();
            // 创建一个简单的错误界面
            getContentPane().removeAll();
            setLayout(new BorderLayout());
            JPanel errorPanel = new JPanel(new BorderLayout());
            errorPanel.setBackground(new Color(30, 30, 30));
            JLabel errorLabel = new JLabel("加载关卡选择界面失败，请检查资源文件", SwingConstants.CENTER);
            errorLabel.setForeground(Color.BLACK);
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
            
            // 创建左侧切换按钮
            HoverButton prevButton = new HoverButton("<");
            prevButton.setFont(new Font("Arial", Font.BOLD, 24));
            prevButton.addActionListener(e -> {
                try {
                    AudioManager.getInstance().playDefaultButtonClickSound();
                } catch (Exception ex) {
                    System.err.println("播放按钮点击音效失败");
                }
                // 修改旋转逻辑为 "123"->"231"
                rotateCarousel(false);
            });
            
            // 创建右侧切换按钮
            HoverButton nextButton = new HoverButton(">");
            nextButton.setFont(new Font("Arial", Font.BOLD, 24));
            nextButton.addActionListener(e -> {
                try {
                    AudioManager.getInstance().playDefaultButtonClickSound();
                } catch (Exception ex) {
                    System.err.println("播放按钮点击音效失败");
                }
                // 旋转逻辑为 "123"->"312"
                rotateCarousel(true);
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
            errorLabel.setForeground(Color.BLACK);
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
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        titleLabel.setForeground(Color.BLACK);
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
            imageLabel = new JLabel(new ImageIcon(levelImage));
        } else {
            // 如果没有图片，使用纯色面板
            JPanel colorPanel = new JPanel();
            colorPanel.setBackground(new Color(30, 30, 30 + levelNum * 20));
            colorPanel.setPreferredSize(new Dimension(MAIN_PANEL_WIDTH - 50, MAIN_PANEL_HEIGHT - 100));
            imageLabel = new JLabel();
            imageLabel.add(colorPanel);
        }
        panel.add(imageLabel, BorderLayout.CENTER);
        
        // 保存关卡图像引用，供梯形变换使用
        panel.putClientProperty("originalImage", levelImage);
        
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
            int centerY = carouselPanel.getHeight() / 2; // 正中央
            
            // 确保selectedLevel在有效范围内
            if (selectedLevel < 1 || selectedLevel > totalLevels) {
                selectedLevel = 1;
            }
            
            // 确定当前选中关卡的索引
            int currentIndex = selectedLevel - 1;
            
            // 计算前一个和后一个关卡的索引
            int prevIndex = (currentIndex - 1 + totalLevels) % totalLevels;
            int nextIndex = (currentIndex + 1) % totalLevels;
            
            // 计算按钮位置
            Component[] components = carouselPanel.getComponents();
            for (Component comp : components) {
                if (comp instanceof HoverButton) {
                    HoverButton button = (HoverButton) comp;
                    if ("<".equals(button.getText())) {
                        // 左侧按钮
                        button.setBounds(20, centerY - 25, 50, 50);
                    } else if (">".equals(button.getText())) {
                        // 右侧按钮
                        button.setBounds(carouselPanel.getWidth() - 70, centerY - 25, 50, 50);
                    }
                }
            }
            
            // 重新布局所有关卡面板
            for (int i = 0; i < levelPanels.size(); i++) {
                JPanel panel = levelPanels.get(i);
                Image originalImage = (Image) panel.getClientProperty("originalImage");
                
                if (i == currentIndex) {
                    // 当前选中关卡居中显示，设置为正方形
                    panel.setBounds(
                        centerX - MAIN_PANEL_WIDTH / 2,
                        centerY - MAIN_PANEL_HEIGHT / 2,
                        MAIN_PANEL_WIDTH,
                        MAIN_PANEL_HEIGHT
                    );
                    panel.setVisible(true);
                    
                    // 当前关卡不需要变形和透明度处理
                    updatePanelWithOriginalImage(panel, originalImage);
                    
                } else if (i == prevIndex) {
                    // 前一个关卡显示在左侧，设置为梯形效果（长边在右侧）
                    panel.setBounds(
                        centerX - MAIN_PANEL_WIDTH / 2 - SIDE_PANEL_WIDTH,
                        centerY - SIDE_PANEL_HEIGHT / 2,
                        SIDE_PANEL_WIDTH,
                        SIDE_PANEL_HEIGHT
                    );
                    panel.setVisible(true);
                    
                    // 应用梯形变换（左侧梯形）
                    updatePanelWithTrapezoidImage(panel, originalImage, false, SIDE_PANEL_ALPHA);
                    
                } else if (i == nextIndex) {
                    // 后一个关卡显示在右侧，设置为梯形效果（长边在左侧）
                    panel.setBounds(
                        centerX + MAIN_PANEL_WIDTH / 2,
                        centerY - SIDE_PANEL_HEIGHT / 2,
                        SIDE_PANEL_WIDTH,
                        SIDE_PANEL_HEIGHT
                    );
                    panel.setVisible(true);
                    
                    // 应用梯形变换（右侧梯形）
                    updatePanelWithTrapezoidImage(panel, originalImage, true, SIDE_PANEL_ALPHA);
                    
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
     * 更新面板中的图像为原始图像
     * @param panel 要更新的面板
     * @param originalImage 原始图像
     */
    private void updatePanelWithOriginalImage(JPanel panel, Image originalImage) {
        if (originalImage == null) return;
        
        // 查找面板中的图像标签
        for (Component c : panel.getComponents()) {
            if (c instanceof JLabel && ((JLabel) c).getIcon() instanceof ImageIcon) {
                JLabel imgLabel = (JLabel) c;
                imgLabel.setIcon(new ImageIcon(originalImage));
                break;
            }
        }
    }
    
    /**
     * 更新面板中的图像为梯形图像
     * @param panel 要更新的面板
     * @param originalImage 原始图像
     * @param isRightSide 是否是右侧梯形
     * @param alpha 透明度
     */
    private void updatePanelWithTrapezoidImage(JPanel panel, Image originalImage, boolean isRightSide, float alpha) {
        if (originalImage == null) return;
        
        try {
            // 创建梯形图像
            BufferedImage trapezoidImage = ImageTransformer.createTrapezoidImage(
                originalImage, isRightSide, TRAPEZOID_FACTOR);
            
            // 应用透明度
            BufferedImage finalImage = ImageTransformer.setImageAlpha(trapezoidImage, alpha);
            
            // 查找面板中的图像标签并更新
            for (Component c : panel.getComponents()) {
                if (c instanceof JLabel && ((JLabel) c).getIcon() instanceof ImageIcon) {
                    JLabel imgLabel = (JLabel) c;
                    imgLabel.setIcon(new ImageIcon(finalImage));
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("创建梯形图像失败");
            e.printStackTrace();
        }
    }
    
    /**
     * 旋转轮盘
     * @param clockwise 是否顺时针旋转
     */
    private void rotateCarousel(boolean clockwise) {
        if (clockwise) {
            // 修改为 "123"->"312" 的效果
            selectedLevel = (selectedLevel % totalLevels) + 1;
        } else {
            // 修改为 "123"->"231" 的效果
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
        levelDescriptionLabel.setForeground(Color.BLACK);
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
        if (textAnimator != null && levelDescriptionLabel != null) {
            String description = getDescriptionForLevel(levelNum);
            textAnimator.animateText(description);
        }
    }
    
    /**
     * 获取关卡描述
     */
    private String getDescriptionForLevel(int levelNum) {
        switch (levelNum) {
            case 1:
                return "关卡1：简单难度 - 学习游戏的基本操作";
            case 2:
                return "关卡2：困难难度 - 经典\"横刀立马\"布局，需更复杂的移动组合";
            case 3:
                return "关卡3：限时模式 - 在关卡2的基础上增加时间限制，挑战更高的效率";
            default:
                return "关卡" + levelNum + "：挑战等待着你！";
        }
    }
    
    // 实现ComponentListener接口的方法
    @Override
    public void componentResized(ComponentEvent e) {
        updateCarouselLayout();
    }

    @Override
    public void componentMoved(ComponentEvent e) {
        // 窗口移动时无需特殊处理
    }

    @Override
    public void componentShown(ComponentEvent e) {
        // 窗口显示时无需特殊处理
    }

    @Override
    public void componentHidden(ComponentEvent e) {
        // 窗口隐藏时无需特殊处理
    }
}