package view.frontend.level.game;

import javax.swing.*;
import java.awt.*;
import view.frontend.resourses.ResourceManager;
import view.audio.AudioManager;
import java.io.File;

/**
 * 这个类是负责显示游戏主界面，并显示当前关卡的游戏内容
 */
public class Frame extends JFrame {
    private int currentLevel;
    private Image backgroundImage;
    
    public Frame() {
        try {
            setTitle("游戏界面");
            setSize(800, 600);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);
            
            // 尝试加载背景图片
            try {
                backgroundImage = ResourceManager.loadImage("images/game_background.jpg");
                if (backgroundImage == null) {
                    System.err.println("警告: 无法加载游戏背景图片，将使用默认背景");
                }
            } catch (Exception e) {
                System.err.println("加载背景图片时出错");
                e.printStackTrace();
            }
            
            // 初始化音频
            initAudio();
            
            initComponents();
        } catch (Exception e) {
            System.err.println("初始化游戏界面失败");
            e.printStackTrace();
            // 创建一个简单的错误界面
            getContentPane().removeAll();
            setLayout(new BorderLayout());
            JPanel errorPanel = new JPanel(new BorderLayout());
            errorPanel.setBackground(new Color(30, 30, 30));
            JLabel errorLabel = new JLabel("加载游戏界面失败，请检查资源文件", SwingConstants.CENTER);
            errorLabel.setForeground(Color.BLACK);
            errorLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
            errorPanel.add(errorLabel, BorderLayout.CENTER);
            
            JButton returnButton = new JButton("返回关卡选择");
            returnButton.addActionListener(e2 -> {
                dispose();
                try {
                    new view.frontend.level.menu.LevelSelectionFrame().setVisible(true);
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
    
    private void initComponents() {
        try {
            setLayout(new BorderLayout());
            
            // 顶部信息面板
            JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JLabel levelLabel = new JLabel("当前关卡: " + currentLevel);
            levelLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
            infoPanel.add(levelLabel);
            add(infoPanel, BorderLayout.NORTH);
            
            // 游戏主内容区域
            JPanel gamePanel = new JPanel() {
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
            gamePanel.setLayout(new BorderLayout());
            
            // 临时显示内容,现在没用了
            JLabel gameContentLabel = new JLabel("游戏内容区域 - 关卡内容将在这里显示", SwingConstants.CENTER);
            gameContentLabel.setFont(new Font("微软雅黑", Font.PLAIN, 18));
            gameContentLabel.setForeground(Color.BLACK);
            gameContentLabel.setOpaque(true);
            gameContentLabel.setBackground(new Color(255, 255, 255, 180)); // 半透明白色背景
            gamePanel.add(gameContentLabel, BorderLayout.CENTER);
            
            add(gamePanel, BorderLayout.CENTER);
            
            // 底部控制面板
            JPanel controlPanel = new JPanel();
            JButton backButton = new JButton("返回关卡选择");
            backButton.addActionListener(e -> {
                try {
                    dispose();
                    new view.frontend.level.menu.LevelSelectionFrame().setVisible(true);
                } catch (Exception ex) {
                    System.err.println("返回关卡选择失败: " + ex.getMessage());
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, 
                        "返回关卡选择失败。\n错误信息: " + ex.getMessage(), 
                        "错误", JOptionPane.ERROR_MESSAGE);
                    // 尝试强制关闭当前窗口
                    dispose();
                }
            });
            controlPanel.add(backButton);
            
            add(controlPanel, BorderLayout.SOUTH);
        } catch (Exception e) {
            System.err.println("初始化组件失败");
            e.printStackTrace();
        }
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
     * 设置当前关卡
     * @param level 关卡编号
     */
    public void setCurrentLevel(int level) {
        this.currentLevel = level;
        // 更新界面显示
        // 这里可以根据关卡加载不同的游戏内容
    }
}