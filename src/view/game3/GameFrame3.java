package view.game3;

import controller3.Direction3;
import controller3.GameLogic3;
import controller3.GameState3;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
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
public class GameFrame3 extends JFrame {

    private GameLogic3 gameLogic3;
    //gameLogic是controller里面我写的最后一个类，里面包含了所有我需要的东西
    //具体关系如下：gameLogic里面有gameState，gameState里面有steps,moveHistory,elapsedTimeInSeconds,gameWon,board这几个field
    //而board又有isValidCoordinate()、getBlockIdAt()、getBlockById()、getBlocksCopy()、getGridCopy()、moveBlockOnBoard()这几个可能用到的方法
    private GamePanel3 gamePanel3;
    private ControlPanel3 controlPanel3;
    private StatusPanel3 statusPanel3;
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
    private boolean timeUpDialogShown = false;

    private static final String KLOTSKI_FILE_EXTENSION = "klotski";
    //定义存档文件的推荐扩展名，在储存时就定义好，后面找的时候更加方便
    private static final String KLOTSKI_FILE_DESCRIPTION = "(*.klotski)";
    //在文件过滤器中显示的描述，方便后面在实现load时查找自己的游戏文件

    public GameFrame3() {
        gameLogic3 = new GameLogic3();
        //new出来的全部都是初始状态的“横刀立马”图
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


        gamePanel3 = new GamePanel3(gameLogic3);
        statusPanel3 = new StatusPanel3();
        controlPanel3 = new ControlPanel3(this);//这里传入this是一个比较妙的点，如果忘记为什么这么写的，后面的controlPanel里面有具体讲解
        // 初始化各个面板

        add(gamePanel3, BorderLayout.CENTER); // 游戏棋盘在中间
        add(statusPanel3, BorderLayout.NORTH);  // 状态信息在顶部
        add(controlPanel3, BorderLayout.SOUTH); // 控制按钮在底部
        //排版布局准备将整个游戏放在正中间，然后把控制按钮放在底部，状态信息放在顶部


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

        updateStatus();
        gameTimer.start();
    }

    private void promptToSaveOnExit() {
        gameTimer.stop();
        // 使用 JOptionPane 显示一个确认对话框，包含“是”、“否”、“取消”三个选项
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
                if (!gameLogic3.getGameState().isGameWon() && !gameLogic3.getGameState().isTimeUp()) {
                    gameTimer.start();
                }
                break;
            case JOptionPane.CLOSED_OPTION:
                if (!gameLogic3.getGameState().isGameWon() && !gameLogic3.getGameState().isTimeUp()) {
                    gameTimer.start();
                }
                break;
        }
    }
    //一个辅助方法，帮助构造方法的书写，完成了点击关闭按钮以后选项的处理

    private void setupGameTimer() {
        gameTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GameState3 currentGameState3 = gameLogic3.getGameState();
                if (!currentGameState3.isGameWon() && !currentGameState3.isTimeUp()) {
                    currentGameState3.decrementTime(1);
                    updateStatus();
                    if (currentGameState3.isTimeUp()) {
                        // 再次检查，因为decrementTime可能导致时间用完
                        gameTimer.stop();
                        if (!currentGameState3.isGameWon()) {
                            //确保不是刚赢了时间又恰好结束
                            showTimeUpDialog();
                        }
                    }
                } else {
                    gameTimer.stop();
                    if (currentGameState3.isTimeUp() && !currentGameState3.isGameWon() && !timeUpDialogShown) {
                        showTimeUpDialog();
                    }
                }
            }
        });
    }
    //这个类实现了计时器的构建，是一个每秒钟查看一次是否胜利的计时器，如果不胜利就倒计时就减1秒
    //另外这个类的书写只是为了构造方法里面方便书写，后面不会用到它

    private void showTimeUpDialog() {
        if (timeUpDialogShown) {
            return;
        }// 防止重复显示
        timeUpDialogShown = true;

        String message = "Time's up! Challenge failed!";
        String title = "Failed";
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.WARNING_MESSAGE);
    }

    private void setupKeyboardControls() {
        this.setFocusable(true);
        // JFrame需要能获得焦点才能接收键盘事件
        this.requestFocusInWindow();
        //这里我采用主动请求，以确保JFrame能去找到这个聚焦

        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                boolean moved = false;
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP:
                    case KeyEvent.VK_W:
                        moved = gameLogic3.moveSelectedBlock(Direction3.UP);
                        break;
                    case KeyEvent.VK_DOWN:
                    case KeyEvent.VK_S:
                        moved = gameLogic3.moveSelectedBlock(Direction3.DOWN);
                        break;
                    case KeyEvent.VK_LEFT:
                    case KeyEvent.VK_A:
                        moved = gameLogic3.moveSelectedBlock(Direction3.LEFT);
                        break;
                    case KeyEvent.VK_RIGHT:
                    case KeyEvent.VK_D:
                        moved = gameLogic3.moveSelectedBlock(Direction3.RIGHT);
                        break;
                }
                if (moved) {
                    refreshGameView();
                    checkAndShowWinDialog();
                }
            }
        });
    }
    //这个方法实现了键盘移动，它同样是对应构造方法里面，后面不会用到这个具体方法
    //这个实现的逻辑可以通过键盘上面的上下左右或者wsad来进行方块的移动


    public void refreshGameView() {
        gamePanel3.repaint();
        //更新游戏界面的状态
        updateStatus();
        //更新状态栏里面的状态
        this.requestFocusInWindow();
        //确保窗口能够进行下一次操作
    }
    //这个方法非常重要，每当你进行了一次移动都要引用一次这个方法来更新游戏界面和状态栏里面的状态


    private void updateStatus() {
        statusPanel3.updateStatus(gameLogic3.getGameState().getSteps(),
                gameLogic3.getGameState().getRemainingTimeInSecond());
    }
    //这个方法别的地方用不到，只是在这个类当中方便用来写状态栏状态的


    public void checkAndShowWinDialog() {
        if (gameLogic3.getGameState().isGameWon()) {
            gameTimer.stop();
            String message = String.format("Congratulations! You have won the game. " +
                    "Your total time taken is %s seconds and the total number of steps is %d.",
                    this.formatTime(180 - gameLogic3.getGameState().getRemainingTimeInSecond()),
                    gameLogic3.getGameState().getSteps());
            String title = "Victory";
            JOptionPane.showMessageDialog(this,message,title, JOptionPane.INFORMATION_MESSAGE);
        }
    }
    //这里只是简单随便写了一个胜利信息界面的庆祝，后面应该要重新更换为队友精心设计的界面


    public void handleUndo() {
        if (gameLogic3.undoLastMove()) {
            refreshGameView();
        }
    }
    //每次撤销操作以后需要引用一次这个方法来更新整个JFrame

    public void handleReset() {
        gameLogic3.resetGame();
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

        //显示“保存文件”对话框，并获取用户的操作结果
        int userSelection = fileChooser.showSaveDialog(this);
        //对于用户的不同选择会传回不同的整数值

        //下面的if逻辑用来处理用户的选择
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            //如果用户点击了对话框中的“保存”按钮
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

            //调用 GameLogic3 中的方法来实际保存游戏状态到这个文件
            if (gameLogic3.saveGameStateToFile(fileToSave)) {
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
            //用户在文件选择对话框中点击了“取消”或关闭了对话框，则什么也不做
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

            //调用 GameLogic3 中的方法来实际加载游戏状态
            if (gameLogic3.loadGameStateFromFile(fileToLoad)) {
                // 加载成功后的处理
                gameTimer.stop();


                if (!gameLogic3.getGameState().isGameWon()) {
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

    public GameLogic3 getGameLogic() {
        return gameLogic3;
    }
    //顺便写了一个gameLogic的getter以防其它开发的时候要用到GameFrame中的gameLogic

}
