package view.game4;

import controller4.Direction4;
import controller4.GameLogic4;
import view.frontend.resourses.ResourceManager;
import view.audio.AudioManager;
import view.frontend.LoginFrame.AuthFrame;
import view.game.MouseTrailLayer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

/*
* 现在最需要注意的点是后面添加通过访客登录的功能时，需要将页面点击关闭换成结束程序运行而不是跳转保存
* 这个类这样用：直接new GameFrame就会出来一个全新的完整的游戏棋盘
* 这个类提供了以下几个方法可能会被用到：
* 1.refreshGameView()，整体更新整个JFrame，包括游戏界面和目前状态
* 2.checkAndShowWinDialog()检测是否胜利
* 3.handleUndo()撤销上一步以后的更新游戏界面
* 4.handleReset()重置以后的更新游戏界面
* 5.还需继续完成的部分：（1）handleSave()和handleLoad()还没写完，后续完成保存和重载逻辑以后需要重新写
*                    （2）checkAndShowWinDialog()胜利界面太丑了，看看队友有没有什么好的设计
 */
public class GameFrame4 extends JFrame {

    private GameLogic4 gameLogic4;
    //gameLogic是controller里面我写的最后一个类，里面包含了所有我需要的东西
    //具体关系如下：gameLogic里面有gameState，gameState里面有steps,moveHistory,elapsedTimeInSeconds,gameWon,board这几个field
    //而board又有isValidCoordinate()、getBlockIdAt()、getBlockById()、getBlocksCopy()、getGridCopy()、moveBlockOnBoard()这几个可能用到的方法

    private GamePanel4 gamePanel4;
    private ControlPanel4 controlPanel4;
    private StatusPanel4 statusPanel4;
    //这里的框架搭建预计在最底层的JFrame上面加三个具体Panel，一个是游戏界面的gamePanel，
    // 一个是上下左右按键和撤销重置按钮的controlPanel，最后一个是包含了步数和计时器的statePanel

    private Timer gameTimer;
    // 用于游戏计时,这个javax.swing.Timer包下的Timer专为GUI设计，不想util包下的Timer容易被干扰
    //这个Timer在使用时是通过以下形式，Timer(int delay, ActionListener listener)，第一个传入的参量是你希望多久触发一次第二个参量的监听
    //第一个参量的单位是ms，我的Board里面的时间是用秒作为单位，所以是1000ms
    //第二个监视的listener我准备设定为监测游戏是否胜利，如果没胜利则时间加1
    //几个常用的方法：
    //gameTimer.start();启动
    //gameTimer.stop();停止
    //gameTimer.restart();重启
    //这个项目中可能用到的就是这几个方法

    private Image backgroundImage;

    private static final String KLOTSKI_FILE_EXTENSION = "klotski";
    //定义存档文件的推荐扩展名，在储存时就定义好，后面找的时候更加方便
    private static final String KLOTSKI_FILE_DESCRIPTION = "(*.klotski)";
    //在文件过滤器中显示的描述，方便后面在实现load时查找自己的游戏文件

    public GameFrame4() {
        try {
            gameLogic4 = new GameLogic4();
            //new出来的全部都是初始状态的"横刀立马"图
            setTitle("Klotski Puzzle");
            setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            
            // 添加窗口关闭监听器
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    try {
                        int response = JOptionPane.showConfirmDialog(
                            GameFrame4.this,
                            "确定要退出游戏吗？",
                            "退出游戏",
                            JOptionPane.YES_NO_OPTION);
                        
                        if (response == JOptionPane.YES_OPTION) {
                            dispose();
                            new AuthFrame().setVisible(true);
                        }
                    } catch (Exception ex) {
                        System.err.println("返回主菜单失败: " + ex.getMessage());
                        ex.printStackTrace();
                        dispose();
                    }
                }
            });

            // 尝试加载背景图片
            try {
                backgroundImage = ResourceManager.loadImage("images/game4_background.jpg");
                if (backgroundImage == null) {
                    System.err.println("警告: 无法加载游戏4背景图片，将使用默认背景");
                }
            } catch (Exception e) {
                System.err.println("加载背景图片时出错");
                e.printStackTrace();
            }
            
            // 初始化音频
            initAudio();

            setLayout(new BorderLayout());
            // 主窗口使用边界布局

            try {
                gamePanel4 = new GamePanel4(gameLogic4);
            } catch (Exception e) {
                System.err.println("初始化游戏面板失败");
                e.printStackTrace();
                // 创建一个简单的备用面板
                gamePanel4 = new GamePanel4(gameLogic4) {
                    @Override
                    public void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        // 如果没有背景图片，绘制渐变背景
                        if (backgroundImage != null) {
                            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                        } else {
                            Graphics2D g2d = (Graphics2D) g;
                            GradientPaint gradient = new GradientPaint(
                                    0, 0, new Color(0, 30, 60),
                                    0, getHeight(), new Color(0, 10, 30));
                            g2d.setPaint(gradient);
                            g2d.fillRect(0, 0, getWidth(), getHeight());
                        }
                    }
                };
            }
            
            try {
                statusPanel4 = new StatusPanel4();
            } catch (Exception e) {
                System.err.println("初始化状态面板失败");
                e.printStackTrace();
                statusPanel4 = new StatusPanel4();
            }
            
            try {
                controlPanel4 = new ControlPanel4(this);
            } catch (Exception e) {
                System.err.println("初始化控制面板失败");
                e.printStackTrace();
                controlPanel4 = new ControlPanel4(this);
            }
            // 初始化各个面板

            add(gamePanel4, BorderLayout.CENTER); // 游戏棋盘在中间
            add(statusPanel4, BorderLayout.NORTH);  // 状态信息在顶部
            add(controlPanel4, BorderLayout.SOUTH); // 控制按钮在底部
            //排版布局准备将整个游戏放在正中间，然后把控制按钮放在底部，状态信息放在顶部

            // 初始化并启动游戏计时器
            setupGameTimer();

            // 添加键盘监听器
            setupKeyboardControls();

            // 设置自定义鼠标光标
            setCustomCursor();

            pack();
            //一个非常好用的方法，它能够让内容自动调整窗口大小
            //这个方法的使用一般是在加完组件之后，setVisible之前
            setLocationRelativeTo(null); // 窗口居中显示
            setResizable(true);
            //使游戏窗口大小可调，如果后期不方便可以设为false
            setVisible(true);

            // 添加鼠标轨迹层
            MouseTrailLayer mouseTrailLayer = new MouseTrailLayer();
            setGlassPane(mouseTrailLayer);
            mouseTrailLayer.setVisible(true);

            updateStatus();
        } catch (Exception e) {
            System.err.println("初始化游戏界面失败");
            e.printStackTrace();
            // 创建一个简单的错误界面
            getContentPane().removeAll();
            setLayout(new BorderLayout());
            JPanel errorPanel = new JPanel(new BorderLayout());
            errorPanel.setBackground(new Color(30, 30, 30));
            JLabel errorLabel = new JLabel("加载游戏界面失败，请检查资源文件", SwingConstants.CENTER);
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

    private void setupGameTimer() {
        try {
            gameTimer = new Timer(1000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        if (!gameLogic4.getGameState().isGameWon()) {
                            gameLogic4.getGameState().incrementElapsedTime(1);
                            updateStatus();
                        } else {
                            gameTimer.stop();
                        }
                    } catch (Exception ex) {
                        System.err.println("更新游戏时间失败");
                        ex.printStackTrace();
                    }
                }
            });
            gameTimer.start();
        } catch (Exception e) {
            System.err.println("设置游戏计时器失败");
            e.printStackTrace();
        }
    }
    //这个类实现了计时器的构建，是一个每秒钟查看一次是否胜利的计时器，如果不胜利就加1秒
    //另外这个类的书写只是为了构造方法里面方便书写，后面不会用到它

    private void setupKeyboardControls() {
        try {
            this.setFocusable(true);
            // JFrame需要能获得焦点才能接收键盘事件
            this.requestFocusInWindow();
            //这里我采用主动请求，以确保JFrame能去找到这个聚焦

            this.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    try {
                        boolean moved = false;
                        switch (e.getKeyCode()) {
                            case KeyEvent.VK_UP:
                            case KeyEvent.VK_W:
                                moved = gameLogic4.moveSelectedBlock(Direction4.UP);
                                if (moved) {
                                    // 播放棋子移动音效
                                    view.audio.AudioManager.getInstance().playDefaultPieceMoveSound();
                                }
                                break;
                            case KeyEvent.VK_DOWN:
                            case KeyEvent.VK_S:
                                moved = gameLogic4.moveSelectedBlock(Direction4.DOWN);
                                if (moved) {
                                    // 播放棋子移动音效
                                    view.audio.AudioManager.getInstance().playDefaultPieceMoveSound();
                                }
                                break;
                            case KeyEvent.VK_LEFT:
                            case KeyEvent.VK_A:
                                moved = gameLogic4.moveSelectedBlock(Direction4.LEFT);
                                if (moved) {
                                    // 播放棋子移动音效
                                    view.audio.AudioManager.getInstance().playDefaultPieceMoveSound();
                                }
                                break;
                            case KeyEvent.VK_RIGHT:
                            case KeyEvent.VK_D:
                                moved = gameLogic4.moveSelectedBlock(Direction4.RIGHT);
                                if (moved) {
                                    // 播放棋子移动音效
                                    view.audio.AudioManager.getInstance().playDefaultPieceMoveSound();
                                }
                                break;
                        }
                        if (moved) {
                            refreshGameView();
                            checkAndShowWinDialog();
                        }
                    } catch (Exception ex) {
                        System.err.println("处理键盘输入失败");
                        ex.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            System.err.println("设置键盘控制失败");
            e.printStackTrace();
        }
    }
    //这个方法实现了键盘移动，它同样是对应构造方法里面，后面不会用到这个具体方法
    //这个实现的逻辑可以通过键盘上面的上下左右或者wsad来进行方块的移动


    public void refreshGameView() {
        try {
            gamePanel4.repaint();
            //更新游戏界面的状态
            updateStatus();
            //更新状态栏里面的状态
            this.requestFocusInWindow();
            //确保窗口能够进行下一次操作
        } catch (Exception e) {
            System.err.println("刷新游戏视图失败");
            e.printStackTrace();
        }
    }
    //这个方法非常重要，每当你进行了一次移动都要引用一次这个方法来更新游戏界面和状态栏里面的状态


    private void updateStatus() {
        try {
            statusPanel4.updateStatus(gameLogic4.getGameState().getSteps(),
                    gameLogic4.getGameState().getElapsedTimeInSeconds());
        } catch (Exception e) {
            System.err.println("更新状态面板失败");
            e.printStackTrace();
        }
    }
    //这个方法别的地方用不到，只是在这个类当中方便用来写状态栏状态的


    public void checkAndShowWinDialog() {
        try {
            if (gameLogic4.getGameState().isGameWon()) {
                gameTimer.stop();
                
                // 使用新的VictoryFrame来显示胜利界面
                String formattedTime = this.formatTime(180 - gameLogic4.getGameState().getElapsedTimeInSeconds());
                int steps = gameLogic4.getGameState().getSteps();
                
                try {
                    // 第三个参数是当前关卡，这里设为4
                    // 第四个参数是胜利背景图片路径，这里使用"victory_background4.jpg"
                    view.frontend.resourses.VictoryFrame.showVictory(
                            formattedTime, 
                            steps, 
                            4, 
                            "victory_background4.jpg");
                } catch (Exception e) {
                    System.err.println("显示胜利界面失败");
                    e.printStackTrace();
                    // 如果无法显示胜利界面，显示一个简单的对话框
                    JOptionPane.showMessageDialog(this,
                            "恭喜你赢了！\n用时: " + formattedTime + "\n步数: " + steps,
                            "游戏胜利",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (Exception e) {
            System.err.println("检查胜利状态失败");
            e.printStackTrace();
        }
    }
    //这里使用了新的胜利界面，需要在resources文件夹中放入名为victory_background4.jpg的图片


    public void handleUndo() {
        try {
            if (gameLogic4.undoLastMove()) {
                refreshGameView();
            }
        } catch (Exception e) {
            System.err.println("撤销操作失败");
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "撤销操作失败。\n错误信息: " + e.getMessage(), 
                "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    //每次撤销操作以后需要引用一次这个方法来更新整个JFrame

    public void handleReset() {
        try {
            gameLogic4.resetGame();
            gameTimer.restart();
            refreshGameView();
        } catch (Exception e) {
            System.err.println("重置游戏失败");
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "重置游戏失败。\n错误信息: " + e.getMessage(), 
                "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    //每次重置操作以后需要引用一次这个方法来更新整个JFrame

    public static String formatTime(long totalSeconds) {
        try {
            long hours = totalSeconds / 3600;
            long minutes = (totalSeconds % 3600) / 60;
            long seconds = totalSeconds % 60;
            
            if (hours > 0) {
                return String.format("%02d:%02d:%02d", hours, minutes, seconds);
            } else {
                return String.format("%02d:%02d", minutes, seconds);
            }
        } catch (Exception e) {
            System.err.println("格式化时间失败");
            e.printStackTrace();
            return "00:00"; // 返回默认时间格式
        }
    }
    //这个类用来使输出的时间以一个标准化的格式

    public GameLogic4 getGameLogic() {
        return gameLogic4;
    }
    //顺便写了一个gameLogic的getter以防其它开发的时候要用到GameFrame中的gameLogic

}
