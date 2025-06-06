package view.game2;

import controller2.*;
import view.game.MouseTrailLayer;
import view.frontend.resourses.VictoryFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import controller2.*;
import java.util.List;
import javax.swing.Timer;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
public class GameFrame2 extends JFrame {

    private GameLogic2 gameLogic2;
    //gameLogic是controller里面我写的最后一个类，里面包含了所有我需要的东西
    //具体关系如下：gameLogic里面有gameState，gameState里面有steps,moveHistory,elapsedTimeInSeconds,gameWon,board这几个field
    //而board又有isValidCoordinate()、getBlockIdAt()、getBlockById()、getBlocksCopy()、getGridCopy()、moveBlockOnBoard()这几个可能用到的方法

    private GamePanel2 gamePanel2;
    private ControlPanel2 controlPanel2;
    private StatusPanel2 statusPanel2;
    private SkillAnimationPanel skillAnimationPanel; // 添加技能动画面板
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

    private static final String KLOTSKI_FILE_EXTENSION = "klotski";
    //定义存档文件的推荐扩展名，在储存时就定义好，后面找的时候更加方便
    private static final String KLOTSKI_FILE_DESCRIPTION = "(*.klotski)";
    //在文件过滤器中显示的描述，方便后面在实现load时查找自己的游戏文件

    private boolean isAISolving = false;
    private Timer animationTimer;
    private List<MoveRecord2> solutionMoves;
    private int currentMoveIndex;

    // 技能动画文件路径
    private static final String TELEPORT_ANIMATION_PATH = "src/view/game2/resources/teleport_animation.gif";
    private static final String BOMB_ANIMATION_PATH = "src/view/game2/resources/bomb_animation.gif";
    
    // 音效文件路径
    private static final String TELEPORT_SOUND_PATH = "src/view/game2/resources/teleport_sound.wav";
    private static final String BOMB_SOUND_PATH = "src/view/game2/resources/bomb_sound.wav";
    
    // 炸弹目标高亮参数
    private static final int HIGHLIGHT_DURATION = 1000; // 高亮持续时间(毫秒)
    private static final Color HIGHLIGHT_COLOR = new Color(255, 0, 0, 150); // 半透明红色
    private Timer highlightTimer; // 高亮效果计时器

    public GameFrame2() {
        gameLogic2 = new GameLogic2();
        //new出来的全部都是初始状态的"横刀立马"图
        setTitle("Klotski Puzzle");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        //这意味着当用户点击 'X' 时，Swing 不会自动做任何事。
        //我们将通过下面的 WindowListener 来接管关闭操作。

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                promptToSaveOnExit();
                // 调用我们自己定义的提示保存方法
            }
        });


        setLayout(new BorderLayout());
        // 主窗口使用边界布局


        gamePanel2 = new GamePanel2(gameLogic2);
        statusPanel2 = new StatusPanel2();
        controlPanel2 = new ControlPanel2(this);//这里传入this是一个比较妙的点，如果忘记为什么这么写的，后面的controlPanel里面有具体讲解
        // 初始化各个面板

        // 初始化技能动画面板
        skillAnimationPanel = new SkillAnimationPanel();
        skillAnimationPanel.setBounds(0, 0, 800, 600); // 设置一个初始大小，后续会自动调整

        // 创建一个JLayeredPane来管理层次关系
        JLayeredPane layeredPane = new JLayeredPane();
        // layeredPane.setLayout(null); // JLayeredPane应该使用null布局而不是BorderLayout -- 移除这行，让JLayeredPane使用默认布局或由我们明确设置
        // 为JLayeredPane设置一个布局管理器，例如BorderLayout，使其能够响应内部组件的preferredSize
        // 或者，更简单的方式是，直接给layeredPane设置preferredSize

        // 添加游戏面板到底层
        // gamePanel2.setBounds(0, 0, 800, 600); // 移除固定大小设置，让preferredSize生效
        layeredPane.add(gamePanel2, Integer.valueOf(JLayeredPane.DEFAULT_LAYER));
        
        // 添加技能动画面板到顶层
        // skillAnimationPanel.setBounds(0, 0, 800, 600); // 移除固定大小设置
        layeredPane.add(skillAnimationPanel, Integer.valueOf(JLayeredPane.POPUP_LAYER));
        
        // 设置layeredPane的preferredSize，使其与gamePanel2的preferredSize一致
        // 这样pack()方法就能正确计算窗口大小
        Dimension gamePanelPreferredSize = gamePanel2.getPreferredSize();
        layeredPane.setPreferredSize(gamePanelPreferredSize);

        // 添加层次面板到窗口中央
        add(layeredPane, BorderLayout.CENTER);
        add(statusPanel2, BorderLayout.NORTH);  // 状态信息在顶部
        add(controlPanel2, BorderLayout.SOUTH); // 控制按钮在底部
        //排版布局准备将整个游戏放在正中间，然后把控制按钮放在底部，状态信息放在顶部

        // 添加组件监听器以调整面板大小
        layeredPane.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                // 调整游戏面板和动画面板的大小以适应layeredPane
                // 当layeredPane大小改变时， gamePanel2 和 skillAnimationPanel 也需要调整大小
                // GamePanel2本身会根据其父容器（layeredPane）的大小通过calculateCellSize动态调整棋盘绘制
                // SkillAnimationPanel也应该覆盖整个layeredPane
                gamePanel2.setBounds(0, 0, layeredPane.getWidth(), layeredPane.getHeight());
                skillAnimationPanel.setBounds(0, 0, layeredPane.getWidth(), layeredPane.getHeight());
            }
        });

        // 初始化并启动游戏计时器
        setupGameTimer();

        // 添加键盘监听器
        setupKeyboardControls();

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
    }

    private void promptToSaveOnExit() {
        // 使用 JOptionPane 显示一个确认对话框，包含"是"、"否"、"取消"三个选项
        int response = JOptionPane.showConfirmDialog(
                this,
                "Do you want to save the current game before exiting?", // message: 对话框中显示的问题
                "exit", // title: 对话框的标题
                JOptionPane.YES_NO_CANCEL_OPTION, // optionType: 指定对话框有哪些按钮
                JOptionPane.QUESTION_MESSAGE // messageType: 指定对话框的图标类型
        );

        //根据用户的选择执行不同操作
        switch (response) {
            case JOptionPane.YES_OPTION:
                handleSave();
                System.exit(0);
                //这段代码是使系统结束运行
                break;
            case JOptionPane.NO_OPTION:
                System.exit(0);
                break;
            case JOptionPane.CANCEL_OPTION:
                break;
            case JOptionPane.CLOSED_OPTION:
                break;
        }
    }
    //一个辅助方法，帮助构造方法的书写，完成了点击关闭按钮以后选项的处理

    private void setupGameTimer() {
        gameTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!gameLogic2.getGameState().isGameWon()) {
                    gameLogic2.getGameState().incrementElapsedTime(1);
                    updateStatus();
                } else {
                    gameTimer.stop();
                }
            }
        });
        gameTimer.start();
    }
    //这个类实现了计时器的构建，是一个每秒钟查看一次是否胜利的计时器，如果不胜利就加1秒
    //另外这个类的书写只是为了构造方法里面方便书写，后面不会用到它

    private void setupKeyboardControls() {
        this.setFocusable(true);
        // JFrame需要能获得焦点才能接收键盘事件
        this.requestFocusInWindow();
        //这里我采用主动请求，以确保JFrame能去找到这个聚焦

        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // 如果动画正在进行，忽略键盘输入
                if (gamePanel2.isAnimating()) {
                    return;
                }
                
                boolean moved = false;
                Direction2 moveDirection = null;
                
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP:
                    case KeyEvent.VK_W:
                        moveDirection = Direction2.UP;
                        break;
                    case KeyEvent.VK_DOWN:
                    case KeyEvent.VK_S:
                        moveDirection = Direction2.DOWN;
                        break;
                    case KeyEvent.VK_LEFT:
                    case KeyEvent.VK_A:
                        moveDirection = Direction2.LEFT;
                        break;
                    case KeyEvent.VK_RIGHT:
                    case KeyEvent.VK_D:
                        moveDirection = Direction2.RIGHT;
                        break;
                }
                
                if (moveDirection != null) {
                    final Direction2 finalMoveDirection = moveDirection;
                    Block2 selectedBlock = gameLogic2.getSelectedBlock();
                    if (selectedBlock != null && gameLogic2.canMove(selectedBlock, finalMoveDirection.getDx(), finalMoveDirection.getDy())) {
                        // 开始动画
                        gamePanel2.animateBlockMove(selectedBlock, finalMoveDirection);
                        
                        // 设置动画完成后的回调
                        gamePanel2.setAnimationCompleteCallback(() -> {
                            // 动画完成后执行实际的棋子移动
                            boolean success = gameLogic2.moveSelectedBlock(finalMoveDirection);
                            
                            // 在模型更新后，通知BlockAnimator可以清理已完成的动画状态
                            if (gamePanel2.getBlockAnimator() != null) {
                                gamePanel2.getBlockAnimator().finalizeAllPendingAnimations();
                            }
                            
                            if (success) {
                                // 播放棋子移动音效 (确保AudioManager已初始化并有此方法)
                                // view.audio.AudioManager.getInstance().playDefaultPieceMoveSound();
                                refreshGameView();
                                checkAndShowWinDialog();
                            } else {
                                // 如果移动不成功（理论上在canMove检查后不应发生）
                                // 也应重绘以确保视图与模型一致。
                                gamePanel2.repaint();
                            }
                        });
                        
                        moved = true; // 标记已开始移动/动画
                    }
                }
                
                // 不需要在这里做if(moved)的判断和刷新，因为动画完成后回调会处理刷新
            }
        });
    }
    //这个方法实现了键盘移动，它同样是对应构造方法里面，后面不会用到这个具体方法
    //这个实现的逻辑可以通过键盘上面的上下左右或者wsad来进行方块的移动


    public void refreshGameView() {
        gamePanel2.repaint();
        //更新游戏界面的状态
        updateStatus();
        //更新状态栏里面的状态
        this.requestFocusInWindow();
        //确保窗口能够进行下一次操作
    }
    //这个方法非常重要，每当你进行了一次移动都要引用一次这个方法来更新游戏界面和状态栏里面的状态


    private void updateStatus() {
        statusPanel2.updateStatus(gameLogic2.getGameState().getSteps(),
                gameLogic2.getGameState().getElapsedTimeInSeconds());
    }
    //这个方法别的地方用不到，只是在这个类当中方便用来写状态栏状态的


    public void checkAndShowWinDialog() {
        if (gameLogic2.getGameState().isGameWon()) {
            gameTimer.stop();
            
            String formattedTime = GameFrame2.formatTime(gameLogic2.getGameState().getElapsedTimeInSeconds());
            int steps = gameLogic2.getGameState().getSteps();
            
            // 使用新的VictoryFrame来显示胜利界面
            VictoryFrame.showVictory(
                    formattedTime, 
                    steps, 
                    1, // 当前关卡为1
                    "victory_background1.jpg" // 关卡1的胜利背景图片路径
            );
        }
    }
    //这里使用了新的胜利界面，需要在resources文件夹中放入名为victory_background1.jpg的图片


    public void handleUndo() {
        if (gameLogic2.undoLastMove()) {
            refreshGameView();
        }
    }
    //每次撤销操作以后需要引用一次这个方法来更新整个JFrame

    public void handleReset() {
        gameLogic2.resetGame();
        gameTimer.restart();
        refreshGameView();
    }
    //每次重置操作以后需要引用一次这个方法来更新整个JFrame

    public void handleSave() {
        JFileChooser fileChooser = new JFileChooser();
        //这里使用JFileChooser这个GUI，能够非常高效地完成文件的读取和存储工作
        //补充：本次项目可能会用到的它的两个方法：一个是showSaveDialog()，另一个使showOpenDialog();
        fileChooser.setDialogTitle("Save Game");

        fileChooser.setCurrentDirectory(new File("."));
        //就存在当前项目的根目录 "." 代表当前工作目录

        //加了一个设置文件过滤器，优先显示和保存为我们定义的扩展名
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                KLOTSKI_FILE_DESCRIPTION,
                KLOTSKI_FILE_EXTENSION
        );
        fileChooser.setFileFilter(filter);

        //显示"保存文件"对话框，并获取用户的操作结果
        int userSelection = fileChooser.showSaveDialog(this);
        //对于用户的不同选择会传回不同的整数值

        //下面的if逻辑用来处理用户的选择
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            //如果用户点击了对话框中的"保存"按钮
            File fileToSave = fileChooser.getSelectedFile();

            String filePath = fileToSave.getAbsolutePath();
            // 获取文件的绝对路径字符串
            if (!filePath.toLowerCase().endsWith("." + KLOTSKI_FILE_EXTENSION)) {
                fileToSave = new File(filePath + "." + KLOTSKI_FILE_EXTENSION);
            }
            //无论文件是否以。klotski命名，都使文件以.klotski结尾

            //检查文件是否已存在，并提示用户是否覆盖
            if (fileToSave.exists()) {
                int overwriteResponse = JOptionPane.showConfirmDialog(
                        this,
                        "The file: \"" + fileToSave.getName() + "\" is already saved.\nAre you going to cover it?",
                        "Confirm",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );
                if (overwriteResponse != JOptionPane.YES_OPTION) {
                    // 用户选择不覆盖，则直接返回，不执行保存操作
                    return;
                }
            }

            //调用 GameLogic2 中的方法来实际保存游戏状态到这个文件
            if (gameLogic2.saveGameStateToFile(fileToSave)) {
                //保存成功，给用户一个提示
                JOptionPane.showMessageDialog(this,
                        "The game is successfully saved to:\n" + fileToSave.getName(),
                        "Saved successfully!", JOptionPane.INFORMATION_MESSAGE);
            } else {
                //保存失败，给用户一个错误提示
                JOptionPane.showMessageDialog(this,
                        "Error!\nPlease check the wrong reason on the consult",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            //用户在文件选择对话框中点击了"取消"或关闭了对话框，则什么也不做
            System.out.println("Cancelled");
        }
    }
    //这里的方法实现了保存游戏的逻辑


    public void handleLoad() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Load Game");
        fileChooser.setCurrentDirectory(new File("."));
        //默认从项目根目录开始找，因为我们的文档都是存储在项目的根目录里面

        //设置文件过滤器，只显示我们游戏的存档文件
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                KLOTSKI_FILE_DESCRIPTION, KLOTSKI_FILE_EXTENSION);
        fileChooser.setFileFilter(filter);

        //这里同样根据用户的不同选择会返回不同的整数值
        int userSelection = fileChooser.showOpenDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToLoad = fileChooser.getSelectedFile();
            // 获取用户选择的文件

            //调用 GameLogic2 中的方法来实际加载游戏状态
            if (gameLogic2.loadGameStateFromFile(fileToLoad)) {
                // 加载成功后的处理
                gameTimer.stop();


                if (!gameLogic2.getGameState().isGameWon()) {
                    gameTimer.start();
                }

                refreshGameView();
                // 刷新棋盘和状态显示 (步数、时间)
                checkAndShowWinDialog();
                // 检查加载的游戏是否直接是胜利状态

                JOptionPane.showMessageDialog(this,
                        "The game is load successfully from: \n" + fileToLoad.getName(),
                        "Load successfully", JOptionPane.INFORMATION_MESSAGE);
            } else {
                //加载失败
                JOptionPane.showMessageDialog(this,
                        "The loading goes wrong！",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            System.out.println("Cancelled");
        }
    }
    //这个方法实现了底层的load的逻辑，即将储存的GameState覆盖当前的GameState，再重新刷新屏幕

    public static String formatTime(long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }
    //这个类用来使输出的时间以一个标准化的格式

    public GameLogic2 getGameLogic() {
        return gameLogic2;
    }
    //顺便写了一个gameLogic的getter以防其它开发的时候要用到GameFrame中的gameLogic

    public void handleTeleport() {
        if (isAISolving) {
            JOptionPane.showMessageDialog(this, "AI正在运行，请稍后操作。", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        gamePanel2.cancelAnimations(); // 确保没有棋子动画在进行
        
        // 禁用控制面板按钮，防止动画播放期间再次点击
        controlPanel2.setAllButtonsEnabled(false);
        
        // 播放大挪移动画
        skillAnimationPanel.setOnAnimationEnd(() -> {
            // 动画结束后执行实际的大挪移逻辑
            gameLogic2.teleportBoard();
            refreshGameView();
            if (gameTimer != null) { // 重启计时器
                gameTimer.stop();
                if (!gameLogic2.getGameState().isGameWon()) {
                    gameTimer.start();
                }
            }
            // 重新启用控制面板按钮
            controlPanel2.setAllButtonsEnabled(true);
            // 播放音效
            playSkillSound(TELEPORT_SOUND_PATH);
        });
        
        // 开始播放动画
        skillAnimationPanel.playAnimation(TELEPORT_ANIMATION_PATH);
    }


    public void handleBomb() {
        if (isAISolving) {
            JOptionPane.showMessageDialog(this, "AI正在运行，请稍后操作。", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        gamePanel2.cancelAnimations(); // 确保没有棋子动画在进行

        // 检查"兵1"是否存在
        if (gameLogic2.getGameState().getBoard().getBlockById(GameLogic2.BOMB_TARGET_BLOCK_ID) == null) {
            JOptionPane.showMessageDialog(this, "\"炸弹\"目标（兵1）已不存在！", "炸弹", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // 获取炸弹目标位置，用于高亮显示
        Block2 targetBlock = gameLogic2.getGameState().getBoard().getBlockById(GameLogic2.BOMB_TARGET_BLOCK_ID);
        
        // 禁用控制面板按钮，防止动画播放期间再次点击
        controlPanel2.setAllButtonsEnabled(false);
        
        // 播放炸弹动画
        skillAnimationPanel.setOnAnimationEnd(() -> {
            // 先高亮显示目标棋子
            highlightBombTarget(targetBlock);
            
            // 播放炸弹音效
            playSkillSound(BOMB_SOUND_PATH);
            
            // 延迟一段时间后执行实际的炸弹逻辑，让玩家能看到高亮效果
            Timer delayTimer = new Timer(HIGHLIGHT_DURATION, e -> {
                boolean success = gameLogic2.useBomb();
                if (success) {
                    refreshGameView();
                    checkAndShowWinDialog(); // 检查使用炸弹后是否获胜
                }
                // 重新启用控制面板按钮
                controlPanel2.setAllButtonsEnabled(true);
            });
            delayTimer.setRepeats(false);
            delayTimer.start();
        });
        
        // 开始播放动画
        skillAnimationPanel.playAnimation(BOMB_ANIMATION_PATH);
    }
    
    /**
     * 高亮显示炸弹目标棋子
     * @param targetBlock 要高亮的目标棋子
     */
    private void highlightBombTarget(Block2 targetBlock) {
        if (targetBlock == null) return;
        
        // 创建一个半透明的红色覆盖层
        JPanel highlightPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setColor(HIGHLIGHT_COLOR);
                
                // 计算目标棋子在面板上的位置和大小
                int cellSize = gamePanel2.getCellSize();
                // 使用GamePanel2提供的实际偏移量进行精确定位
                int actualOffsetX = gamePanel2.getActualOffsetX();
                int actualOffsetY = gamePanel2.getActualOffsetY();
                
                int x = actualOffsetX + targetBlock.getX() * cellSize;
                int y = actualOffsetY + targetBlock.getY() * cellSize;
                int width = targetBlock.getWidth() * cellSize;
                int height = targetBlock.getHeight() * cellSize;
                
                // 绘制高亮矩形
                g2d.fillRect(x, y, width, height);
                g2d.dispose();
            }
        };
        
        // 设置高亮面板属性
        highlightPanel.setOpaque(false);
        // 高亮面板需要覆盖整个游戏面板区域，而不是整个layeredPane
        highlightPanel.setBounds(0, 0, gamePanel2.getWidth(), gamePanel2.getHeight());
        
        // 将高亮面板添加到gamePanel2的父容器（即layeredPane）的更高层
        // 注意：这里高亮面板是直接添加到layeredPane，其坐标是相对于layeredPane的。
        // 而highlightPanel内部的paintComponent绘制的矩形坐标是相对于highlightPanel自身的。
        // 我们需要确保highlightPanel的大小和位置与gamePanel2一致，这样内部绘制逻辑才能正确。
        // 已经通过 setBounds(0,0, gamePanel2.getWidth(), gamePanel2.getHeight()) 设置了，
        // 但因为layeredPane是null布局，所以添加子组件时，子组件的位置和大小需要明确指定。
        // 此处高亮面板是作为一个覆盖层添加到layeredPane，并且其绘制内容是基于gamePanel2的内部坐标。

        // 将高亮面板添加到layeredPane的最顶层
        Container parent = gamePanel2.getParent();
        if (parent instanceof JLayeredPane) {
            JLayeredPane layeredPane = (JLayeredPane) parent;
            // 先移除旧的（如果有），避免重复添加
            for (Component comp : layeredPane.getComponentsInLayer(JLayeredPane.DRAG_LAYER)) {
                if (comp.getClass().isAnonymousClass() && comp instanceof JPanel) { // 简易判断是否是之前的高亮面板
                    layeredPane.remove(comp);
                }
            }
            layeredPane.add(highlightPanel, Integer.valueOf(JLayeredPane.DRAG_LAYER)); 
            highlightPanel.setBounds(gamePanel2.getBounds()); // <--- 关键：确保高亮面板与游戏面板完全重叠
            layeredPane.repaint(); // 重绘 layeredPane 以显示高亮
        } else {
            System.err.println("错误：GamePanel2的父容器不是JLayeredPane，无法添加高亮层。");
            return; // 如果父容器不对，无法继续
        }
        
        // 设置计时器在一段时间后移除高亮效果
        if (highlightTimer != null && highlightTimer.isRunning()) {
            highlightTimer.stop();
        }
        
        highlightTimer = new Timer(HIGHLIGHT_DURATION, e -> {
            // 移除高亮面板
            if (parent instanceof JLayeredPane) {
                JLayeredPane layeredPane = (JLayeredPane) parent;
                layeredPane.remove(highlightPanel);
                layeredPane.repaint();
            }
        });
        highlightTimer.setRepeats(false);
        highlightTimer.start();
    }
    
    /**
     * 播放技能音效
     * @param soundPath 音效文件路径
     */
    private void playSkillSound(String soundPath) {
        try {
            // 检查文件是否存在
            File soundFile = new File(soundPath);
            if (soundFile.exists()) {
                // 使用AudioManager播放自定义音效
                view.audio.AudioManager.getInstance().playPieceMoveSound(soundPath);
            } else {
                // 如果文件不存在，播放默认音效
                view.audio.AudioManager.getInstance().playDefaultPieceMoveSound();
                System.err.println("技能音效文件不存在: " + soundPath + "，使用默认音效替代");
            }
        } catch (Exception e) {
            // 出错时播放默认音效
            view.audio.AudioManager.getInstance().playDefaultPieceMoveSound();
            System.err.println("播放技能音效时出错: " + e.getMessage());
        }
    }

    public GamePanel2 getGamePanel() {
        return this.gamePanel2;
    }

    public void handleAISolve() {
        if (isAISolving) {
            JOptionPane.showMessageDialog(this, "AI 正在计算中...", "AI 忙碌", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (gameLogic2.getGameState().isGameWon()) {
            JOptionPane.showMessageDialog(this, "游戏已经胜利！", "游戏结束", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        isAISolving = true;
        controlPanel2.setAllButtonsEnabled(false); // 禁用按钮
        System.out.println("AI: 请求已接收，开始求解...");

        SwingWorker<List<MoveRecord2>, String> worker = new SwingWorker<List<MoveRecord2>, String>() {
            @Override
            protected List<MoveRecord2> doInBackground() throws Exception {
                publish("AI 开始求解...");
                AISolver2 solver = new AISolver2();
                GameState2 currentStateForAI = new GameState2(gameLogic2.getGameState());
                List<MoveRecord2> solution = solver.solve(currentStateForAI);
                publish("AI 求解完成。");
                return solution;
            }

            @Override
            protected void process(List<String> chunks) {
                for (String message : chunks) {
                    System.out.println(message); // 在控制台显示进度
                }
            }

            @Override
            protected void done() {
                try {
                    List<MoveRecord2> solution = get();
                    if (solution != null && !solution.isEmpty()) {
                        System.out.println("AI 找到解法，共 " + solution.size() + " 步。准备演示...");
                        animateSolution(solution);
                    } else {
                        JOptionPane.showMessageDialog(GameFrame2.this, "AI 未能找到解法。", "AI 结果", JOptionPane.INFORMATION_MESSAGE);
                        finishAISession();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(GameFrame2.this, "AI 求解时发生错误: " + e.getMessage(), "AI 错误", JOptionPane.ERROR_MESSAGE);
                    finishAISession();
                }
            }
        };

        worker.execute();
    }

    private void animateSolution(List<MoveRecord2> solution) {
        if (solution == null || solution.isEmpty()) {
            finishAISession();
            return;
        }
        this.solutionMoves = solution;
        this.currentMoveIndex = 0;
        this.setFocusable(false); // 禁用键盘
        if (gameTimer != null) gameTimer.stop(); // 停止游戏计时器

        int delay = 500; // 动画速度 (毫秒)

        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }

        animationTimer = new Timer(delay, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (gameLogic2.getGameState().isGameWon()) {
                    animationTimer.stop();
                    finishAISession();
                    checkAndShowWinDialog();
                    return;
                }

                if (currentMoveIndex < solutionMoves.size()) {
                    MoveRecord2 move = solutionMoves.get(currentMoveIndex);

                    boolean selectionSuccess = gameLogic2.selectBlockAt(move.getFromX(), move.getFromY());

                    if (!selectionSuccess || gameLogic2.getSelectedBlock() == null || gameLogic2.getSelectedBlock().getId() != move.getBlockId()) {
                        System.err.println("AI 动画错误: 无法选择棋子 " + move.getBlockId());
                        animationTimer.stop();
                        finishAISession();
                        return;
                    }

                    Direction2 moveDirection = determineDirection(move.getFromX(), move.getFromY(), move.getToX(), move.getToY());

                    if (moveDirection == null) {
                        System.err.println("AI 动画错误: 无法确定方向 " + move);
                        animationTimer.stop();
                        finishAISession();
                        return;
                    }

                    boolean moved = gameLogic2.moveSelectedBlock(moveDirection);

                    if (moved) {
                        refreshGameView();
                    } else {
                        System.err.println("AI 动画错误: 移动失败 " + move);
                        animationTimer.stop();
                        finishAISession();
                        return;
                    }
                    currentMoveIndex++;
                } else {
                    animationTimer.stop();
                    finishAISession();
                    checkAndShowWinDialog();
                }
            }
        });
        animationTimer.start();
    }

    private Direction2 determineDirection(int fromX, int fromY, int toX, int toY) {
        int dx = toX - fromX;
        int dy = toY - fromY;

        if (dx == 0 && dy == -1) return Direction2.UP;
        if (dx == 0 && dy == 1) return Direction2.DOWN;
        if (dx == -1 && dy == 0) return Direction2.LEFT;
        if (dx == 1 && dy == 0) return Direction2.RIGHT;

        return null;
    }


    private void finishAISession() {
        controlPanel2.setAllButtonsEnabled(true);
        this.setFocusable(true);
        this.requestFocusInWindow();
        isAISolving = false;

        if (!gameLogic2.getGameState().isGameWon() ) {
            if (gameTimer != null && !gameTimer.isRunning()) {
                gameTimer.start();
            }
        }
    }

    @Override
    public void dispose() {
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }
        if (gameTimer != null) {
            gameTimer.stop();
        }
        if (highlightTimer != null && highlightTimer.isRunning()) {
            highlightTimer.stop();
        }
        // 停止技能动画
        if (skillAnimationPanel != null) {
            skillAnimationPanel.stopAnimation();
        }
        super.dispose();
    }

}
