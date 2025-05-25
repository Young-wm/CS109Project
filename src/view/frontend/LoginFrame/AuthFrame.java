package view.frontend.LoginFrame;

import javax.swing.*;
import java.awt.*;
import view.frontend.level.menu.LevelSelectionFrame;
import view.game4.GameFrame4;
import view.frontend.resourses.ResourceManager;
import view.frontend.TextAnimator;
import view.audio.AudioManager;
import java.io.File;
/*
* 这个类是整个初始界面，包含登录按钮，注册按钮，以及游客模式按钮
* 功能：主要认证界面框架，管理登录、注册和游客模式的不同面板
* 使用CardLayout在不同面板间切换
* 与LoginPanel、RegisterPanel、UserManager交互
* 使用了<br>作为html格式的换行符，防止因文本长度过长导致的显示不全的问题
* */
public class AuthFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private LoginPanel loginPanel;
    private RegisterPanel registerPanel;
    private UserManager userManager;
    private JPanel mainAppPanel; // Panel for the main application after login
    private JLabel mainAppWelcomeLabel; // Label for dynamic welcome message
    private Image backgroundImage; // 背景图片
    private TextAnimator textAnimator; // 文本动画器

    private static final String LOGIN_PANEL = "LoginPanel";
    private static final String REGISTER_PANEL = "RegisterPanel";
    private static final String GUEST_PANEL = "GuestPanel"; // Placeholder for guest/main app
    private static final String MAIN_APP_PANEL = "MainAppPanel"; // Placeholder for main application after login
    
    // 默认背景图片路径
    private static final String DEFAULT_BACKGROUND_PATH = "images/login_background.jpg";
    // 欢迎界面背景图片路径
    private static final String WELCOME_BACKGROUND_PATH = "images/welcome_background.jpg";


    public AuthFrame() {
        userManager = new UserManager();
        setTitle("用户认证");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null); // Center the window
        
        // 加载背景图片
        backgroundImage = ResourceManager.loadImage(DEFAULT_BACKGROUND_PATH);
        
        // 初始化音频管理器
        initAudio();

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // 绘制背景图片
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };
        mainPanel.setOpaque(false);

        loginPanel = new LoginPanel(this, userManager);
        registerPanel = new RegisterPanel(this, userManager);

        //游客模式
        JPanel guestModePanel = new JPanel(new BorderLayout());
        guestModePanel.setOpaque(false);
        
        // 使用HTML格式实现多行文本
        JLabel guestWelcomeLabel = new JLabel("<html><div style='text-align: center;'>" +
                "欢迎进入游客模式！<br><br>" +
                "请注意，在这个模式中，你<br>" +
                "只可体验游戏的其中一关，<br>" +
                "并且没有存档与读档功能" +
                "</div></html>", SwingConstants.CENTER);
        guestWelcomeLabel.setFont(new Font("Serif", Font.PLAIN, 18));
        guestWelcomeLabel.setForeground(Color.BLACK); // 修改为黑色，提高可视性
        guestModePanel.add(guestWelcomeLabel, BorderLayout.CENTER);

        JPanel guestButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        guestButtonsPanel.setOpaque(false);
        JButton startGameGuestButton = new JButton("开始游戏");
        startGameGuestButton.addActionListener(e -> {
            AudioManager.getInstance().playDefaultButtonClickSound();
            dispose(); // Close AuthFrame
            SwingUtilities.invokeLater(() -> new GameFrame4().setVisible(true)); // 直接打开GameFrame4，跳过选关界面
        });
        setupButtonHoverEffect(startGameGuestButton);
        guestButtonsPanel.add(startGameGuestButton);

        JButton backToLoginFromGuestButton = new JButton("返回登录");
        backToLoginFromGuestButton.addActionListener(e -> {
            AudioManager.getInstance().playDefaultButtonClickSound();
            showLoginPanel();
        });
        setupButtonHoverEffect(backToLoginFromGuestButton);
        guestButtonsPanel.add(backToLoginFromGuestButton);
        guestModePanel.add(guestButtonsPanel, BorderLayout.SOUTH);

        // 成功登录后主界面
        this.mainAppPanel = new JPanel(new BorderLayout());
        this.mainAppPanel.setOpaque(false);
        
        this.mainAppWelcomeLabel = new JLabel("", SwingConstants.CENTER);
        this.mainAppWelcomeLabel.setFont(new Font("Serif", Font.PLAIN, 18));
        this.mainAppWelcomeLabel.setForeground(Color.BLACK); // 修改为黑色，提高可视性
        this.mainAppPanel.add(this.mainAppWelcomeLabel, BorderLayout.CENTER);
        
        // 初始化文本动画器
        textAnimator = new TextAnimator(this.mainAppWelcomeLabel, 20); // 每秒输出20个字符

        JPanel appButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        appButtonsPanel.setOpaque(false);
        JButton startGameMainButton = new JButton("进入游戏");
        startGameMainButton.addActionListener(e -> {
            AudioManager.getInstance().playDefaultButtonClickSound();
            dispose(); // 这里直接关闭了初始登录界面
            SwingUtilities.invokeLater(() -> new LevelSelectionFrame().setVisible(true)); //打开选关界面
        });
        setupButtonHoverEffect(startGameMainButton);
        appButtonsPanel.add(startGameMainButton);

        JButton logoutButton = new JButton("登出");
        logoutButton.addActionListener(e -> {
            AudioManager.getInstance().playDefaultButtonClickSound();
            showLoginPanel(); //当用户点击"退出"时，回到一开始的登录界面
        });
        setupButtonHoverEffect(logoutButton);
        appButtonsPanel.add(logoutButton);
        this.mainAppPanel.add(appButtonsPanel, BorderLayout.SOUTH);


        mainPanel.add(loginPanel, LOGIN_PANEL);
        mainPanel.add(registerPanel, REGISTER_PANEL);
        mainPanel.add(guestModePanel, GUEST_PANEL);
        mainPanel.add(this.mainAppPanel, MAIN_APP_PANEL);

        add(mainPanel);
        showLoginPanel(); // Show login panel by default
        
        setVisible(true);
    }
    
    /**
     * 初始化音频设置
     */
    private void initAudio() {
        try {
            AudioManager audioManager = AudioManager.getInstance();
            
            // 检查音频文件是否存在
            File bgmFile = new File("src/view/audio/resources/bgm.wav");
            File clickFile = new File("src/view/audio/resources/button_click.wav");
            File hoverFile = new File("src/view/audio/resources/button_hover.wav");
            File pieceMoveFile = new File("src/view/audio/resources/piece_move.wav");
            
            // 如果任何一个文件不存在，给出警告但不崩溃
            if (!bgmFile.exists() || !clickFile.exists() || !hoverFile.exists() || !pieceMoveFile.exists()) {
                System.err.println("警告: 部分音频文件不存在，音频功能可能不完整");
            }
            
            // 设置默认音频路径（实际使用时需替换为真实路径）
            audioManager.setDefaultBgmPath("src/view/audio/resources/bgm.wav");
            audioManager.setDefaultButtonClickPath("src/view/audio/resources/button_click.wav");
            audioManager.setDefaultButtonHoverPath("src/view/audio/resources/button_hover.wav");
            audioManager.setDefaultPieceMovePath("src/view/audio/resources/piece_move.wav");
            
            // 设置音量
            audioManager.setMasterVolume(0.8f);
            audioManager.setBGMVolume(0.5f);
            audioManager.setUIVolume(0.7f);
        } catch (Exception e) {
            System.err.println("初始化音频系统失败");
            e.printStackTrace();
        }
    }
    
    /**
     * 为按钮设置悬停效果
     * @param button 要设置效果的按钮
     */
    private void setupButtonHoverEffect(JButton button) {
        // 保存原始颜色
        Color originalBackground = button.getBackground();
        Color originalForeground = button.getForeground();
        Font originalFont = button.getFont();
        
        // 悬停时改变按钮外观
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                // 播放悬停音效
                AudioManager.getInstance().playDefaultButtonHoverSound();
                
                // 改变按钮外观 - 使用更明显的颜色变化
                button.setBackground(new Color(70, 130, 180)); // 钢蓝色，更明显的悬停颜色
                button.setForeground(Color.WHITE);
                button.setFont(new Font(originalFont.getName(), Font.BOLD, originalFont.getSize()));
                button.setBorderPainted(true);
                button.setContentAreaFilled(true);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                // 恢复原始外观
                button.setBackground(originalBackground);
                button.setForeground(originalForeground);
                button.setFont(originalFont);
            }
        });
    }

    /**
     * 设置背景图片
     * @param imagePath 图片路径
     */
    public void setBackgroundImage(String imagePath) {
        try {
            backgroundImage = ResourceManager.loadImage(imagePath);
            if (backgroundImage != null) {
                mainPanel.repaint();
            } else {
                System.err.println("无法加载背景图片: " + imagePath);
            }
        } catch (Exception e) {
            System.err.println("加载背景图片时出错: " + imagePath);
            e.printStackTrace();
        }
    }

    public void showLoginPanel() {
        setBackgroundImage(DEFAULT_BACKGROUND_PATH);
        cardLayout.show(mainPanel, LOGIN_PANEL);
    }

    public void showRegisterPanel() {
        setBackgroundImage(DEFAULT_BACKGROUND_PATH);
        cardLayout.show(mainPanel, REGISTER_PANEL);
    }

    public void showGuestModePanel() {
        setBackgroundImage(WELCOME_BACKGROUND_PATH);
        cardLayout.show(mainPanel, GUEST_PANEL);
        // 游客模式不需要保存用户状态，游戏进度不与用户身份关联
        // The actual transition to LevelSelectionFrame is now handled by the "开始游戏" button on guestModePanel
    }

    public void showMainAppPanel(User user) {
        // 切换背景图片
        setBackgroundImage(WELCOME_BACKGROUND_PATH);
        
        // 设置欢迎文本
        String welcomeText;
        if (user != null && user.getUsername() != null) { 
            welcomeText = "登录成功! 欢迎回来, " + user.getUsername() + "!";
        } else {
            welcomeText = "登录成功! 欢迎来到主应用!"; // Fallback if user or username is null
        }
        
        // 使用动画显示文本
        cardLayout.show(mainPanel, MAIN_APP_PANEL);
        textAnimator.animateText(welcomeText);
        
        // 播放背景音乐
        try {
            AudioManager.getInstance().playDefaultBGM();
        } catch (Exception e) {
            System.err.println("播放背景音乐失败");
            e.printStackTrace();
        }
    }

    // 加载图片接口
    public ImageIcon loadImage(String path) {
        return ResourceManager.loadIcon(path);
    }

    // 播放音频接口
    public void playSound(String path) {
        AudioManager.getInstance().playButtonClickSound(path);
    }


    public static void main(String[] args) {
        // Ensure GUI updates are done on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> new AuthFrame());
    }
    
    /**
     * 创建一个背景图片README文件，用于指导用户如何添加背景图片
     * @return README文件内容
     */
    public static String getBackgroundImageReadmeContent() {
        return "### 背景图片使用说明\n\n" +
               "为了在游戏中添加自定义背景图片，请按照以下方式操作：\n\n" +
               "1. 将背景图片放在以下目录：\n" +
               "   - src/view/frontend/resourses/images/\n\n" +
               "2. 图片命名规则：\n" +
               "   - 登录/注册界面背景：login_background.jpg\n" +
               "   - 欢迎界面背景：welcome_background.jpg\n\n" +
               "3. 支持的图片格式：jpg, png, gif\n\n" +
               "4. 图片尺寸建议：\n" +
               "   - 宽度：至少600像素\n" +
               "   - 高度：至少400像素\n" +
               "   - 为了最佳效果，请使用适合窗口比例的图片\n\n" +
               "5. 在代码中设置自定义背景：\n" +
               "```java\n" +
               "// 在AuthFrame类中\n" +
               "setBackgroundImage(\"images/my_custom_background.jpg\");\n" +
               "```\n\n" +
               "注意：\n" +
               "- 如果找不到指定的背景图片，将使用默认的纯色背景\n" +
               "- 图片会自动缩放以适应窗口大小\n";
    }
}