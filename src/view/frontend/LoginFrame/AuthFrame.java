package view.frontend.LoginFrame;

import javax.swing.*;
import java.awt.*;
import view.frontend.level.menu.LevelSelectionFrame;
import view.game4.GameFrame4;
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

    private static final String LOGIN_PANEL = "LoginPanel";
    private static final String REGISTER_PANEL = "RegisterPanel";
    private static final String GUEST_PANEL = "GuestPanel"; // Placeholder for guest/main app
    private static final String MAIN_APP_PANEL = "MainAppPanel"; // Placeholder for main application after login


    public AuthFrame() {
        userManager = new UserManager();
        setTitle("用户认证");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null); // Center the window

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        loginPanel = new LoginPanel(this, userManager);
        registerPanel = new RegisterPanel(this, userManager);

        //游客模式
        JPanel guestModePanel = new JPanel(new BorderLayout());
        
        // 使用HTML格式实现多行文本
        JLabel guestWelcomeLabel = new JLabel("<html><div style='text-align: center;'>" +
                "欢迎进入游客模式！<br><br>" +
                "请注意，在这个模式中，你<br>" +
                "只可体验游戏的其中一关，<br>" +
                "并且没有存档与读档功能" +
                "</div></html>", SwingConstants.CENTER);
        guestWelcomeLabel.setFont(new Font("Serif", Font.PLAIN, 18));
        guestModePanel.add(guestWelcomeLabel, BorderLayout.CENTER);

        JPanel guestButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton startGameGuestButton = new JButton("开始游戏");
        startGameGuestButton.addActionListener(e -> {
            dispose(); // Close AuthFrame
            SwingUtilities.invokeLater(() -> new GameFrame4().setVisible(true)); // 直接打开GameFrame4，跳过选关界面
        });
        guestButtonsPanel.add(startGameGuestButton);

        JButton backToLoginFromGuestButton = new JButton("返回登录");
        backToLoginFromGuestButton.addActionListener(e -> showLoginPanel());
        guestButtonsPanel.add(backToLoginFromGuestButton);
        guestModePanel.add(guestButtonsPanel, BorderLayout.SOUTH);

        // 成功登录后主界面
        this.mainAppPanel = new JPanel(new BorderLayout());
        this.mainAppWelcomeLabel = new JLabel("登录成功! 准备进入主应用...", SwingConstants.CENTER);
        this.mainAppWelcomeLabel.setFont(new Font("Serif", Font.PLAIN, 18));
        this.mainAppPanel.add(this.mainAppWelcomeLabel, BorderLayout.CENTER);

        JPanel appButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton startGameMainButton = new JButton("进入游戏");
        startGameMainButton.addActionListener(e -> {
            dispose(); // 这里直接关闭了初始登录界面
            SwingUtilities.invokeLater(() -> new LevelSelectionFrame().setVisible(true)); //打开选关界面
        });
        appButtonsPanel.add(startGameMainButton);

        JButton logoutButton = new JButton("登出");
        logoutButton.addActionListener(e -> showLoginPanel()); //当用户点击"退出"时，回到一开始的登录界面
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

    public void showLoginPanel() {
        cardLayout.show(mainPanel, LOGIN_PANEL);
    }

    public void showRegisterPanel() {
        cardLayout.show(mainPanel, REGISTER_PANEL);
    }

    public void showGuestModePanel() {
        cardLayout.show(mainPanel, GUEST_PANEL);
        // 游客模式不需要保存用户状态，游戏进度不与用户身份关联
        // The actual transition to LevelSelectionFrame is now handled by the "开始游戏" button on guestModePanel
    }

    public void showMainAppPanel(User user) {
        // Assuming User class has getUsername() method and is in the same package or imported
        if (user != null && user.getUsername() != null) { 
            this.mainAppWelcomeLabel.setText("登录成功! 欢迎回来, " + user.getUsername() + "!");
        } else {
            this.mainAppWelcomeLabel.setText("登录成功! 欢迎来到主应用!"); // Fallback if user or username is null
        }
        cardLayout.show(mainPanel, MAIN_APP_PANEL);
        // 这里可以保存用户登录状态，以便在游戏中使用
        // 用户凭证可以临时存储，游戏进度可以单独保存和加载，与用户身份关联
        // The actual transition to LevelSelectionFrame is now handled by the "进入游戏" button on mainAppPanel
    }

    // Placeholder for image interface
    public ImageIcon loadImage(String path) {
        // In a real app, load image from path
        // For now, returns null or a placeholder
        System.out.println("Image loading API called for: " + path);
        return null;
    }

    // Placeholder for audio interface
    public void playSound(String path) {
        // In a real app, load and play sound from path
        System.out.println("Audio playing API called for: " + path);
    }


    public static void main(String[] args) {
        // Ensure GUI updates are done on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> new AuthFrame());
    }
}