package view.game4;

import controller4.Direction4;
import controller4.GameLogic4;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

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

    private static final String KLOTSKI_FILE_EXTENSION = "klotski";
    //定义存档文件的推荐扩展名，在储存时就定义好，后面找的时候更加方便
    private static final String KLOTSKI_FILE_DESCRIPTION = "(*.klotski)";
    //在文件过滤器中显示的描述，方便后面在实现load时查找自己的游戏文件

    public GameFrame4() {
        gameLogic4 = new GameLogic4();
        //new出来的全部都是初始状态的"横刀立马"图
        setTitle("Klotski Puzzle");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());
        // 主窗口使用边界布局


        gamePanel4 = new GamePanel4(gameLogic4);
        statusPanel4 = new StatusPanel4();
        controlPanel4 = new ControlPanel4(this);//这里传入this是一个比较妙的点，如果忘记为什么这么写的，后面的controlPanel里面有具体讲解
        // 初始化各个面板

        add(gamePanel4, BorderLayout.CENTER); // 游戏棋盘在中间
        add(statusPanel4, BorderLayout.NORTH);  // 状态信息在顶部
        add(controlPanel4, BorderLayout.SOUTH); // 控制按钮在底部
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
    }

    private void setupGameTimer() {
        gameTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!gameLogic4.getGameState().isGameWon()) {
                    gameLogic4.getGameState().incrementElapsedTime(1);
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
                boolean moved = false;
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP:
                    case KeyEvent.VK_W:
                        moved = gameLogic4.moveSelectedBlock(Direction4.UP);
                        break;
                    case KeyEvent.VK_DOWN:
                    case KeyEvent.VK_S:
                        moved = gameLogic4.moveSelectedBlock(Direction4.DOWN);
                        break;
                    case KeyEvent.VK_LEFT:
                    case KeyEvent.VK_A:
                        moved = gameLogic4.moveSelectedBlock(Direction4.LEFT);
                        break;
                    case KeyEvent.VK_RIGHT:
                    case KeyEvent.VK_D:
                        moved = gameLogic4.moveSelectedBlock(Direction4.RIGHT);
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
        gamePanel4.repaint();
        //更新游戏界面的状态
        updateStatus();
        //更新状态栏里面的状态
        this.requestFocusInWindow();
        //确保窗口能够进行下一次操作
    }
    //这个方法非常重要，每当你进行了一次移动都要引用一次这个方法来更新游戏界面和状态栏里面的状态


    private void updateStatus() {
        statusPanel4.updateStatus(gameLogic4.getGameState().getSteps(),
                gameLogic4.getGameState().getElapsedTimeInSeconds());
    }
    //这个方法别的地方用不到，只是在这个类当中方便用来写状态栏状态的


    public void checkAndShowWinDialog() {
        if (gameLogic4.getGameState().isGameWon()) {
            gameTimer.stop();
            
            // 使用新的VictoryFrame来显示胜利界面
            String formattedTime = this.formatTime(180 - gameLogic4.getGameState().getElapsedTimeInSeconds());
            int steps = gameLogic4.getGameState().getSteps();
            
            // 第三个参数是当前关卡，这里设为4
            // 第四个参数是胜利背景图片路径，这里使用"victory_background4.jpg"
            view.frontend.resourses.VictoryFrame.showVictory(
                    formattedTime, 
                    steps, 
                    4, 
                    "victory_background4.jpg");
        }
    }
    //这里使用了新的胜利界面，需要在resources文件夹中放入名为victory_background4.jpg的图片


    public void handleUndo() {
        if (gameLogic4.undoLastMove()) {
            refreshGameView();
        }
    }
    //每次撤销操作以后需要引用一次这个方法来更新整个JFrame

    public void handleReset() {
        gameLogic4.resetGame();
        gameTimer.restart();
        refreshGameView();
    }
    //每次重置操作以后需要引用一次这个方法来更新整个JFrame

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

    public GameLogic4 getGameLogic() {
        return gameLogic4;
    }
    //顺便写了一个gameLogic的getter以防其它开发的时候要用到GameFrame中的gameLogic

}
