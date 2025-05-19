package view.frontend.LoginFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginPanel extends JPanel {
    private AuthFrame authFrame;
    private UserManager userManager;

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton switchToRegisterButton;
    private JButton guestModeButton;

    public LoginPanel(AuthFrame frame, UserManager manager) {
        this.authFrame = frame;
        this.userManager = manager;
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel titleLabel = new JLabel("用户登录", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 20));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        add(titleLabel, gbc);

        // Username
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.2;
        add(new JLabel("用户名:"), gbc);

        usernameField = new JTextField(15);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 0.8;
        add(usernameField, gbc);

        // Password
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.2;
        add(new JLabel("密码:"), gbc);

        passwordField = new JPasswordField(15);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 0.8;
        add(passwordField, gbc);

        // Login Button
        loginButton = new JButton("登录");
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        add(loginButton, gbc);

        // Switch to Register Button
        switchToRegisterButton = new JButton("没有账户？点击注册");
        gbc.gridx = 0;
        gbc.gridy = 4;
        add(switchToRegisterButton, gbc);

        // Guest Mode Button
        guestModeButton = new JButton("游客模式");
        gbc.gridx = 0;
        gbc.gridy = 5;
        add(guestModeButton, gbc);

        // Action Listeners
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());

                if (username.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(LoginPanel.this,
                            "用户名和密码不能为空。",
                            "登录错误",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                User user = userManager.loginUser(username, password);
                if (user != null) {
                    JOptionPane.showMessageDialog(LoginPanel.this, "登录成功!");
                    authFrame.showMainAppPanel(user);
                    // Clear fields after successful login
                    usernameField.setText("");
                    passwordField.setText("");
                } else {
                    JOptionPane.showMessageDialog(LoginPanel.this,
                            "用户名或密码错误。",
                            "登录失败",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        switchToRegisterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                authFrame.showRegisterPanel();
                usernameField.setText("");
                passwordField.setText("");
            }
        });

        guestModeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                authFrame.showGuestModePanel();
                usernameField.setText("");
                passwordField.setText("");
            }
        });
    }
}