package view.frontend.level.menu;

import view.frontend.LoginFrame.AuthFrame;
import view.frontend.TextAnimator;
import view.frontend.controller.HoverButton;
import view.frontend.resourses.ResourceManager;
import view.audio.AudioManager;
import view.game.GameFrame; // 经典模式
import view.game2.GameFrame2; // 技能模式 (对应您的 幻影模式)
import view.game3.GameFrame3; // 极限模式
import view.game.MouseTrailLayer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.awt.image.BufferedImage;
import java.awt.AlphaComposite;

public class LevelSelectionFrame extends JFrame {
    private int currentModeIndex = 0; // 0: 技能/幻影, 1: 经典, 2: 极限
    private final int TOTAL_MODES = 3;

    private JPanel levelDetailPanel;
    private JLabel levelDescriptionLabel;
    private TextAnimator textAnimator;
    private Image backgroundImage;
    
    private JPanel mainDisplayPanel; // 新的中央显示区域
    private JPanel modesDisplayPanel; // 显示所有模式的面板
    // private List<JPanel> modePanels = new ArrayList<>(); // Replaced by direct ModePanel references
    private ModePanel leftModePanel, centerModePanel, rightModePanel;


    // 模式面板的大小和透明度设置
    private static final int MAIN_MODE_SIZE = 280; // 中间主模式的大小
    private static final int SIDE_MODE_SIZE = 180; // 两侧模式的大小
    private static final float SIDE_MODE_ALPHA = 0.7f; // 两侧模式的透明度
    private static final float CENTER_MODE_ALPHA = 1.0f; // 中间模式的透明度

    private final String[] MODE_INTERNAL_NAMES = {"SKILL_MODE", "CLASSIC_MODE", "EXTREME_MODE"};
    private final String[] MODE_DISPLAY_NAMES = {"技能模式", "经典模式", "极限模式"}; // 与截图对应
    private final String[] MODE_IMAGE_PATHS = {
            "images/skill_mode_display.jpg",
            "images/classic_mode_display.jpg",
            "images/extreme_mode_display.jpg"
    };

    private HoverButton bgmToggleButton; // 背景音乐切换按钮
    private boolean isBgmPlaying = true; // 假设BGM开始时处于播放状态

    // 动画相关字段
    private Timer panelAnimator;
    private final int ANIMATION_TOTAL_FRAMES = 20; // 动画总帧数
    private int currentAnimationFrame;
    private Rectangle[] targetPanelBounds = new Rectangle[3];
    private float[] targetPanelAlphas = new float[3];
    private int[] targetPanelDisplaySizes = new int[3]; // 图片的显示大小
    private boolean isAnimating = false;
    private boolean shiftDirectionIsLeft; // true if new center comes from left, false if from right
    
    public LevelSelectionFrame() {
        try {
            setTitle("华容道 选择关卡"); 
            setSize(800, 650); 
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);
            setLayout(new BorderLayout(0,0));

            loadBackgroundImage();
            initAudio();
            try {
                AudioManager.getInstance().playDefaultBGM();
                isBgmPlaying = true; 
            } catch (Exception audioEx) {
                System.err.println("初始BGM播放失败: " + audioEx.getMessage());
                isBgmPlaying = false;
            }

            JLabel titleLabel = new JLabel("关卡选择", SwingConstants.CENTER); 
            titleLabel.setFont(new Font("黑体", Font.BOLD, 38)); 
            titleLabel.setForeground(new Color(230, 220, 200)); 
            titleLabel.setBorder(BorderFactory.createEmptyBorder(25, 0, 10, 0)); 
            JPanel titlePanel = new JPanel(new BorderLayout()) { 
                 @Override
                protected void paintComponent(Graphics g) {}
            };
            titlePanel.setOpaque(false);
            titlePanel.add(titleLabel, BorderLayout.CENTER);
            add(titlePanel, BorderLayout.NORTH);

            createMainDisplayArea();
            add(mainDisplayPanel, BorderLayout.CENTER);
            
            createLevelDetailPanel();
            add(levelDetailPanel, BorderLayout.SOUTH);
            
            // 初始化时创建面板实例，但先不设置具体模式内容和动画
            leftModePanel = new ModePanel(); 
            centerModePanel = new ModePanel();
            rightModePanel = new ModePanel();

            modesDisplayPanel.add(leftModePanel);
            modesDisplayPanel.add(centerModePanel);
            modesDisplayPanel.add(rightModePanel);
            
            updateModeDisplayAndDetails(true); // 初始加载，可能不需要动画或使用特定初始动画
            updateBgmButtonIcon(); 

            setCustomCursor();
            
            MouseTrailLayer mouseTrailLayer = new MouseTrailLayer();
            setGlassPane(mouseTrailLayer);
            mouseTrailLayer.setVisible(true);

        } catch (Exception e) {
            System.err.println("初始化关卡选择界面失败: " + e.getMessage());
            e.printStackTrace();
            showErrorScreen();
        }
    }

    // ModePanel 内部类
    private static class ModePanel extends JPanel {
        JLabel imageLabel;
        JLabel nameLabel;
        Image originalModeImage; // 存储原始图片，用于高效重绘
        String currentDisplayName;

        final Color NAME_LABEL_BASE_BG_OPAQUE = new Color(60, 40, 30);
        final int NAME_LABEL_BASE_BG_ALPHA_COMPONENT = 220; // Color 的 alpha 分量 (0-255)

        public ModePanel() {
            super(new BorderLayout(0, 5));
            setOpaque(false); // ModePanel 本身透明

            imageLabel = new JLabel();
            imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
            add(imageLabel, BorderLayout.CENTER);

            nameLabel = new JLabel(" ", SwingConstants.CENTER); // 初始为空
            nameLabel.setForeground(new Color(210, 190, 160));
            nameLabel.setOpaque(true); // nameLabel 需要背景，所以不透明
            nameLabel.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
            add(nameLabel, BorderLayout.SOUTH);
        }

        public void setContent(String displayName, String imagePath) {
            this.currentDisplayName = displayName;
            this.nameLabel.setText(displayName);
            try {
                this.originalModeImage = ResourceManager.loadImage(imagePath);
            } catch (Exception e) {
                this.originalModeImage = null;
                System.err.println("加载模式图片失败 (ModePanel): " + imagePath + " - " + e.getMessage());
            }
        }

        public void updateAppearance(int displaySize, float contentAlpha, boolean isCenter) {
            // 更新图片
            if (originalModeImage != null) {
                Image scaledImg = originalModeImage.getScaledInstance(displaySize, displaySize, Image.SCALE_SMOOTH);
                if (contentAlpha < 1.0f) {
                    scaledImg = applyAlphaToImageHelper(scaledImg, contentAlpha);
                }
                imageLabel.setIcon(new ImageIcon(scaledImg));
            } else {
                imageLabel.setIcon(null);
                imageLabel.setText("图片缺失");
                imageLabel.setForeground(Color.RED);
            }

            // 更新名称标签的字体和背景透明度
            nameLabel.setFont(new Font("黑体", Font.BOLD, isCenter ? 26 : 20));
            int effectiveNameLabelBgAlpha = (int) (NAME_LABEL_BASE_BG_ALPHA_COMPONENT * contentAlpha);
            effectiveNameLabelBgAlpha = Math.max(0, Math.min(255, effectiveNameLabelBgAlpha));
            nameLabel.setBackground(new Color(NAME_LABEL_BASE_BG_OPAQUE.getRed(), NAME_LABEL_BASE_BG_OPAQUE.getGreen(), NAME_LABEL_BASE_BG_OPAQUE.getBlue(), effectiveNameLabelBgAlpha));
        }

        private static Image applyAlphaToImageHelper(Image image, float alpha) {
            if (image == null) return null;
            int width = image.getWidth(null);
            int height = image.getHeight(null);
            if (width <= 0 || height <= 0) return image; 

            BufferedImage buffImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = buffImg.createGraphics();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0f, Math.min(1f, alpha))));
            g2d.drawImage(image, 0, 0, null);
            g2d.dispose();
            return buffImg;
        }
    }

    private void loadBackgroundImage() {
        try {
            backgroundImage = ResourceManager.loadImage("images/level_selection_background.jpg");
        } catch (Exception e) {
            System.err.println("加载背景图片时出错: " + e.getMessage());
        }
    }

    private void initAudio() {
        try {
            AudioManager audioManager = AudioManager.getInstance();
            if (audioManager.getDefaultBgmPath() == null) {
                audioManager.setDefaultBgmPath("src/view/audio/resources/bgm.wav");
                audioManager.setDefaultButtonClickPath("src/view/audio/resources/button_click.wav");
                audioManager.setDefaultButtonHoverPath("src/view/audio/resources/button_hover.wav");
            }
        } catch (Exception e) {
            System.err.println("初始化音频系统失败: " + e.getMessage());
        }
    }

    private void createMainDisplayArea() {
        mainDisplayPanel = new JPanel(new BorderLayout(10, 0)) { 
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    if (backgroundImage != null) {
                        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                    } else {
                        Graphics2D g2d = (Graphics2D) g;
                        GradientPaint gradient = new GradientPaint(0, 0, new Color(0, 20, 40), 0, getHeight(), new Color(0, 5, 15));
                        g2d.setPaint(gradient);
                        g2d.fillRect(0, 0, getWidth(), getHeight());
                    }
                }
            };
        mainDisplayPanel.setOpaque(false); 
        mainDisplayPanel.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30)); 

        JPanel topControlsPanel = new JPanel(new BorderLayout());
        topControlsPanel.setOpaque(false);

        bgmToggleButton = new HoverButton("♫"); 
        bgmToggleButton.setFont(new Font("Arial Unicode MS", Font.BOLD, 20)); 
        bgmToggleButton.setToolTipText("播放/暂停背景音乐");
        bgmToggleButton.setPreferredSize(new Dimension(50, 40)); 
        
        bgmToggleButton.addActionListener(e -> {
            AudioManager.getInstance().toggleBGM();
            isBgmPlaying = !isBgmPlaying; 
            updateBgmButtonIcon(); 
        });

        JPanel bgmButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bgmButtonPanel.setOpaque(false);
        bgmButtonPanel.add(bgmToggleButton);
        topControlsPanel.add(bgmButtonPanel, BorderLayout.EAST);
        
        topControlsPanel.add(new JPanel(){{setOpaque(false);}}, BorderLayout.WEST);
        mainDisplayPanel.add(topControlsPanel, BorderLayout.NORTH);

        modesDisplayPanel = new JPanel(null) { 
            @Override
            protected void paintComponent(Graphics g) {}
        };
        modesDisplayPanel.setOpaque(false);
        mainDisplayPanel.add(modesDisplayPanel, BorderLayout.CENTER);

        // 左右箭头按钮 - 逻辑已交换
        HoverButton prevButton = new HoverButton("◀"); // 原来的"左"，现在"向右拨动"轮盘
        prevButton.setFont(new Font("Arial Unicode MS", Font.BOLD, 48)); 
        prevButton.setPreferredSize(new Dimension(80, 100)); 
            prevButton.addActionListener(e -> {
            if(isAnimating) return;
            currentModeIndex = (currentModeIndex + 1) % TOTAL_MODES; // 逻辑变为加1
            shiftDirectionIsLeft = false; // 新的中间项从右边过来
            updateModeDisplayAndDetails(false); 
        });

        HoverButton nextButton = new HoverButton("▶"); // 原来的"右"，现在"向左拨动"轮盘
        nextButton.setFont(new Font("Arial Unicode MS", Font.BOLD, 48));
        nextButton.setPreferredSize(new Dimension(80, 100)); 
            nextButton.addActionListener(e -> {
            if(isAnimating) return;
            currentModeIndex = (currentModeIndex - 1 + TOTAL_MODES) % TOTAL_MODES; // 逻辑变为减1
            shiftDirectionIsLeft = true; // 新的中间项从左边过来
            updateModeDisplayAndDetails(false); 
        });

        JPanel leftArrowPanel = new JPanel(new GridBagLayout()); 
        leftArrowPanel.setOpaque(false);
        leftArrowPanel.add(prevButton);
        mainDisplayPanel.add(leftArrowPanel, BorderLayout.WEST);

        JPanel rightArrowPanel = new JPanel(new GridBagLayout());
        rightArrowPanel.setOpaque(false);
        rightArrowPanel.add(nextButton);
        mainDisplayPanel.add(rightArrowPanel, BorderLayout.EAST);
    }

    private void updateModeDisplayAndDetails(boolean initialLoad) {
        if (isAnimating && !initialLoad) return; // 如果正在动画且不是强制刷新，则忽略

        if (panelAnimator != null && panelAnimator.isRunning()) {
            panelAnimator.stop(); // 停止当前可能正在运行的动画
        }
        isAnimating = true;

        int centerX = modesDisplayPanel.getWidth() / 2;
        int centerY = modesDisplayPanel.getHeight() / 2;
        
        int leftIndex = (currentModeIndex - 1 + TOTAL_MODES) % TOTAL_MODES;
        int centerIndex = currentModeIndex;
        int rightIndex = (currentModeIndex + 1) % TOTAL_MODES;

        leftModePanel.setContent(MODE_DISPLAY_NAMES[leftIndex], MODE_IMAGE_PATHS[leftIndex]);
        centerModePanel.setContent(MODE_DISPLAY_NAMES[centerIndex], MODE_IMAGE_PATHS[centerIndex]);
        rightModePanel.setContent(MODE_DISPLAY_NAMES[rightIndex], MODE_IMAGE_PATHS[rightIndex]);

        targetPanelDisplaySizes[0] = SIDE_MODE_SIZE;
        targetPanelAlphas[0] = SIDE_MODE_ALPHA;
        targetPanelBounds[0] = new Rectangle(centerX - MAIN_MODE_SIZE/2 - SIDE_MODE_SIZE - 30, centerY - (SIDE_MODE_SIZE+40)/2, SIDE_MODE_SIZE, SIDE_MODE_SIZE+40); 

        targetPanelDisplaySizes[1] = MAIN_MODE_SIZE;
        targetPanelAlphas[1] = CENTER_MODE_ALPHA;
        targetPanelBounds[1] = new Rectangle(centerX - MAIN_MODE_SIZE/2, centerY - (MAIN_MODE_SIZE+50)/2, MAIN_MODE_SIZE, MAIN_MODE_SIZE+50); 

        targetPanelDisplaySizes[2] = SIDE_MODE_SIZE;
        targetPanelAlphas[2] = SIDE_MODE_ALPHA;
        targetPanelBounds[2] = new Rectangle(centerX + MAIN_MODE_SIZE/2 + 30, centerY - (SIDE_MODE_SIZE+40)/2, SIDE_MODE_SIZE, SIDE_MODE_SIZE+40);
        
        if (initialLoad) { 
            leftModePanel.setBounds(targetPanelBounds[0]);
            leftModePanel.updateAppearance(targetPanelDisplaySizes[0], targetPanelAlphas[0], false);
            centerModePanel.setBounds(targetPanelBounds[1]);
            centerModePanel.updateAppearance(targetPanelDisplaySizes[1], targetPanelAlphas[1], true);
            rightModePanel.setBounds(targetPanelBounds[2]);
            rightModePanel.updateAppearance(targetPanelDisplaySizes[2], targetPanelAlphas[2], false);
            isAnimating = false;
                } else {
            currentAnimationFrame = 0;
            if (panelAnimator == null) {
                panelAnimator = new Timer(1000 / 60, e -> animatePanels()); // ~60 FPS
                panelAnimator.setInitialDelay(0);
            }
            panelAnimator.start();
        }
        
        updateLevelDetailPanelText(currentModeIndex);
    }

    private void animatePanels() {
        currentAnimationFrame++;
        float progress = (float) currentAnimationFrame / ANIMATION_TOTAL_FRAMES;
        progress = Math.min(1.0f, progress); // 确保 progress <= 1.0

        ModePanel[] panels = {leftModePanel, centerModePanel, rightModePanel};

        for (int i = 0; i < panels.length; i++) {
            ModePanel panel = panels[i];
            Rectangle targetBound = targetPanelBounds[i];
            float targetAlpha = targetPanelAlphas[i];
            int targetDisplaySize = targetPanelDisplaySizes[i];
            boolean isTargetCenter = (i == 1);

            float initialAlpha;
            int initialDisplaySize;
            Rectangle initialBound; 
            
            if (isTargetCenter) { 
                initialDisplaySize = SIDE_MODE_SIZE; 
                initialAlpha = SIDE_MODE_ALPHA;     
                // 根据 shiftDirectionIsLeft 决定中间面板的入场方向
                int initialX;
                if (shiftDirectionIsLeft) { // 新的中间项从左边过来 (即用户按下了"向左拨动"的按钮，即新的"左箭头")
                    initialX = targetPanelBounds[0].x + (targetPanelBounds[0].width - SIDE_MODE_SIZE)/2; // 从左侧目标位旁边开始
                } else { // 新的中间项从右边过来 (即用户按下了"向右拨动"的按钮，即新的"右箭头")
                    initialX = targetPanelBounds[2].x + (targetPanelBounds[2].width - SIDE_MODE_SIZE)/2; // 从右侧目标位旁边开始
                }
                initialBound = new Rectangle(initialX, targetBound.y + (targetBound.height - (SIDE_MODE_SIZE + 40))/2 , SIDE_MODE_SIZE, SIDE_MODE_SIZE+40);
            } else { 
                initialDisplaySize = (int)(SIDE_MODE_SIZE * 0.8f); 
                initialAlpha = 0.0f;                   
                initialBound = new Rectangle(
                    targetBound.x + (targetBound.width - initialDisplaySize) / 2,
                    targetBound.y + (targetBound.height - (initialDisplaySize + 40)) / 2,
                    initialDisplaySize, initialDisplaySize + 40
                );
            }
            // 如果面板已经有边界 (不是第一次绘制)，则从当前状态开始动画，使得过渡更平滑
            if(panel.getBounds().width > 0 && panel.getBounds().height > 0 && currentAnimationFrame ==1 ){
                initialBound = panel.getBounds();
                // 这里可以进一步获取面板当前的实际alpha和size作为动画起点，但为简化，先用预设值
                // 例如: initialAlpha = getCurrentAlphaFromPanel(panel); 
                // 但这需要 ModePanel 存储或能计算出它当前的视觉 alpha 和 size
            }

            int currentDisplaySize = (int) (initialDisplaySize + (targetDisplaySize - initialDisplaySize) * progress);
            float currentAlpha = initialAlpha + (targetAlpha - initialAlpha) * progress;
            
            int currentX = (int) (initialBound.x + (targetBound.x - initialBound.x) * progress);
            int currentY = (int) (initialBound.y + (targetBound.y - initialBound.y) * progress);
            int currentW = (int) (initialBound.width + (targetBound.width - initialBound.width) * progress);
            int currentH = (int) (initialBound.height + (targetBound.height - initialBound.height) * progress);
            
            panel.setBounds(currentX, currentY, currentW, currentH);
            panel.updateAppearance(currentDisplaySize, currentAlpha, isTargetCenter);
        }

        modesDisplayPanel.revalidate();
        modesDisplayPanel.repaint();

        if (progress >= 1.0f) {
            panelAnimator.stop();
            isAnimating = false;
            leftModePanel.setBounds(targetPanelBounds[0]);
            leftModePanel.updateAppearance(targetPanelDisplaySizes[0], targetPanelAlphas[0], false);
            centerModePanel.setBounds(targetPanelBounds[1]);
            centerModePanel.updateAppearance(targetPanelDisplaySizes[1], targetPanelAlphas[1], true);
            rightModePanel.setBounds(targetPanelBounds[2]);
            rightModePanel.updateAppearance(targetPanelDisplaySizes[2], targetPanelAlphas[2], false);
        }
    }

    @Override
    public void doLayout() {
        super.doLayout();
        // 当窗口大小改变时，如果modesDisplayPanel可见，则强制刷新布局和动画目标
        if (modesDisplayPanel != null && modesDisplayPanel.isVisible()) {
            SwingUtilities.invokeLater(() -> updateModeDisplayAndDetails(true)); // 重新计算目标并直接设置，或重启动画
        }
    }

    private void createLevelDetailPanel() {
        levelDetailPanel = new JPanel(new BorderLayout(10,5)); 
        levelDetailPanel.setOpaque(false);
        levelDetailPanel.setBorder(BorderFactory.createEmptyBorder(5, 30, 15, 30)); 

        levelDescriptionLabel = new JLabel("模式描述", SwingConstants.CENTER);
        levelDescriptionLabel.setFont(new Font("微软雅黑", Font.PLAIN, 15)); 
        levelDescriptionLabel.setForeground(Color.BLACK); 
        levelDescriptionLabel.setPreferredSize(new Dimension(700, 60)); 
        levelDetailPanel.add(levelDescriptionLabel, BorderLayout.NORTH);

        try {
            textAnimator = new TextAnimator(levelDescriptionLabel, 30); 
        } catch (Exception e) {
            System.err.println("初始化文本动画器失败: " + e.getMessage());
            textAnimator = new TextAnimator(levelDescriptionLabel, 30) { 
                @Override public void animateText(String text) { if (levelDescriptionLabel != null) levelDescriptionLabel.setText(text); }
            };
        }

        JPanel bottomButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 10)); 
        bottomButtonPanel.setOpaque(false);

        HoverButton confirmButton = new HoverButton("确定"); 
        HoverButton backToLoginButton = new HoverButton("归返登入");

        Font buttonFont = new Font("宋体", Font.BOLD, 18); 
        Dimension buttonSize = new Dimension(140, 45); 

        for (HoverButton btn : new HoverButton[]{confirmButton, backToLoginButton}) {
            btn.setFont(buttonFont);
            btn.setPreferredSize(buttonSize); 
        }
        
        confirmButton.addActionListener(e -> {
            try {
                        dispose();
                switch (currentModeIndex) {
                    case 0: new GameFrame2().setVisible(true); break;
                    case 1: new GameFrame().setVisible(true);  break;
                    case 2: new GameFrame3().setVisible(true); break;
                    default: new AuthFrame().setVisible(true); break;
                    }
                } catch (Exception ex) {
                    System.err.println("启动游戏关卡失败: " + ex.getMessage());
                    ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "启动游戏失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                try { new AuthFrame().setVisible(true); } catch (Exception ignored) {}
            }
        });

        backToLoginButton.addActionListener(e -> {
            try {
                dispose();
                new AuthFrame().setVisible(true);
            } catch (Exception ex) {
                System.err.println("返回主菜单失败: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        bottomButtonPanel.add(confirmButton);
        bottomButtonPanel.add(backToLoginButton);

        levelDetailPanel.add(bottomButtonPanel, BorderLayout.CENTER); 
    }

    private void updateLevelDetailPanelText(int modeIndex) {
        String description = "";
        switch (modeIndex) {
            case 0: description = "技能模式：方寸之间，变幻万千，利用技能解开华容道的迷局，体验策略与技能的完美结合。"; break;
            case 1: description = "经典模式：品味经典，在方寸棋盘上演绎经典故事。挑战纯粹的逻辑与耐心。"; break;
            case 2: description = "极限模式：限时挑战，每一步都关乎成败。在限定时间内考验极致操作与冷静判断。"; break;
            default: description = "请选择一个模式开始您的挑战。"; break;
        }
        try {
            String htmlText = "<html><body style='width: 550px; text-align: center;'>" + description.replace("\n", "<br>") + "</body></html>";
            if (textAnimator != null) textAnimator.animateText(htmlText);
            else if (levelDescriptionLabel != null) levelDescriptionLabel.setText(htmlText);
        } catch (Exception e) {
            System.err.println("显示关卡描述文本失败: " + e.getMessage());
            if (levelDescriptionLabel != null) levelDescriptionLabel.setText("描述加载失败");
        }
    }

    private void setCustomCursor() {
        try {
            setCursor(Cursor.getDefaultCursor()); 
        } catch (Exception e) {
            System.err.println("设置自定义光标失败: " + e.getMessage());
            setCursor(Cursor.getDefaultCursor());
        }
    }
    
    private void showErrorScreen() {
        getContentPane().removeAll();
        setLayout(new BorderLayout());
        JPanel errorPanel = new JPanel(new BorderLayout());
        errorPanel.setBackground(new Color(30, 30, 30));
        JLabel errorLabel = new JLabel("加载关卡选择界面失败，请检查资源文件或控制台输出。", SwingConstants.CENTER);
        errorLabel.setForeground(Color.WHITE);
        errorLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        errorPanel.add(errorLabel, BorderLayout.CENTER);

        JButton returnButton = new JButton("返回登录");
        returnButton.addActionListener(e -> {
            dispose();
            try { new AuthFrame().setVisible(true); } catch (Exception ex) { System.exit(0); }
        });
        JPanel buttonPane = new JPanel();
        buttonPane.setOpaque(false);
        buttonPane.add(returnButton);
        errorPanel.add(buttonPane, BorderLayout.SOUTH);
        add(errorPanel);
        revalidate();
        repaint();
    }

    private void updateBgmButtonIcon() {
        if (bgmToggleButton != null) {
            bgmToggleButton.setText(isBgmPlaying ? "暂停" : "播放"); 
            bgmToggleButton.setToolTipText(isBgmPlaying ? "暂停背景音乐" : "播放背景音乐");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.put("Button.select", new Color(80, 60, 50)); 
                UIManager.put("Button.focus", new Color(0,0,0,0)); 
                UIManager.put("Label.foreground", Color.BLACK); 
                
                LevelSelectionFrame frame = new LevelSelectionFrame();
                frame.setVisible(true);
            } catch (Exception e) {
                System.err.println("启动关卡选择界面主方法失败: " + e.getMessage());
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, 
                        "关键错误: 无法启动游戏界面。\n请查看日志了解详情。",
                        "启动失败", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}