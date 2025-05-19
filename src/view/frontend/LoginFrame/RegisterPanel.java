package view.frontend.LoginFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RegisterPanel extends JPanel {
    private AuthFrame authFrame;
    private UserManager userManager;

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JButton registerButton;
    private JButton switchToLoginButton;

    public RegisterPanel(AuthFrame frame, UserManager manager) {
        this.authFrame = frame;
        this.userManager = manager;
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel titleLabel = new JLabel("用户注册", SwingConstants.CENTER);
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

        // Confirm Password
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.2;
        add(new JLabel("确认密码:"), gbc);

        confirmPasswordField = new JPasswordField(15);
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.weightx = 0.8;
        add(confirmPasswordField, gbc);

        // Register Button
        registerButton = new JButton("注册");
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        add(registerButton, gbc);

        // Switch to Login Button
        switchToLoginButton = new JButton("已有账户？点击登录");
        gbc.gridx = 0;
        gbc.gridy = 5;
        add(switchToLoginButton, gbc);

        // Action Listeners
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                String confirmPassword = new String(confirmPasswordField.getPassword());

                if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    JOptionPane.showMessageDialog(RegisterPanel.this,
                            "所有字段均为必填项。",
                            "注册错误",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    JOptionPane.showMessageDialog(RegisterPanel.this,
                            "两次输入的密码不匹配。",
                            "注册错误",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (userManager.userExists(username)) {
                    JOptionPane.showMessageDialog(RegisterPanel.this,
                            "用户名已存在。",
                            "注册错误",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (userManager.registerUser(username, password)) {
                    JOptionPane.showMessageDialog(RegisterPanel.this, "注册成功！请登录。");
                    authFrame.showLoginPanel();
                    // Clear fields after successful registration
                    usernameField.setText("");
                    passwordField.setText("");
                    confirmPasswordField.setText("");
                } else {
                    // This case should ideally not be reached if userExists check is done properly
                    JOptionPane.showMessageDialog(RegisterPanel.this,
                            "注册失败，未知错误。",
                            "注册错误",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        switchToLoginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                authFrame.showLoginPanel();
                usernameField.setText("");
                passwordField.setText("");
                confirmPasswordField.setText("");
            }
        });
    }
}