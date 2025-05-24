package view.game;

import controller.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;
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
public class GameFrame extends JFrame {

    private GameLogic gameLogic;
    //gameLogic是controller里面我写的最后一个类，里面包含了所有我需要的东西
    //具体关系如下：gameLogic里面有gameState，gameState里面有steps,moveHistory,elapsedTimeInSeconds,gameWon,board这几个field
    //而board又有isValidCoordinate()、getBlockIdAt()、getBlockById()、getBlocksCopy()、getGridCopy()、moveBlockOnBoard()这几个可能用到的方法
    private GamePanel gamePanel;
    private ControlPanel controlPanel;
    private StatusPanel statusPanel;
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
    private Timer autoSaveTimer;
    private boolean isAISolving = false;
    private Timer animationTimer;
    private List<MoveRecord> solutionMoves;
    private int currentMoveIndex;

    private static final String KLOTSKI_FILE_EXTENSION = "klotski";
    //定义存档文件的推荐扩展名，在储存时就定义好，后面找的时候更加方便
    private static final String KLOTSKI_FILE_DESCRIPTION = "(*.klotski)";
    //在文件过滤器中显示的描述，方便后面在实现load时查找自己的游戏文件
    private static final String AUTO_SAVE_FILE_NAME = "1.klotski";

    public GameFrame() {
        gameLogic = new GameLogic();
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


        gamePanel = new GamePanel(gameLogic);
        statusPanel = new StatusPanel();
        controlPanel = new ControlPanel(this);//这里传入this是一个比较妙的点，如果忘记为什么这么写的，后面的controlPanel里面有具体讲解
        // 初始化各个面板

        add(gamePanel, BorderLayout.CENTER); // 游戏棋盘在中间
        add(statusPanel, BorderLayout.NORTH);  // 状态信息在顶部
        add(controlPanel, BorderLayout.SOUTH); // 控制按钮在底部
        //排版布局准备将整个游戏放在正中间，然后把控制按钮放在底部，状态信息放在顶部


        // 初始化并启动游戏计时器
        setupGameTimer();

        // 设置并启动自动保存计时器
        setupAutoSaveTimer();
        autoSaveTimer.start();

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

    private void promptToSaveOnExit() {
        if (autoSaveTimer != null) {
            autoSaveTimer.stop();
        }
        gameTimer.stop();
        //点击×的时候会实现自动保存功能的停止

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
                if (!gameLogic.getGameState().isGameWon()) {
                    gameLogic.getGameState().incrementElapsedTime(1);
                    updateStatus();
                } else {
                    gameTimer.stop();
                    if (autoSaveTimer != null) {
                        autoSaveTimer.stop();
                    }
                    // 游戏胜利时也停止自动保存
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
                        moved = gameLogic.moveSelectedBlock(controller.Direction.UP);
                        break;
                    case KeyEvent.VK_DOWN:
                    case KeyEvent.VK_S:
                        moved = gameLogic.moveSelectedBlock(controller.Direction.DOWN);
                        break;
                    case KeyEvent.VK_LEFT:
                    case KeyEvent.VK_A:
                        moved = gameLogic.moveSelectedBlock(controller.Direction.LEFT);
                        break;
                    case KeyEvent.VK_RIGHT:
                    case KeyEvent.VK_D:
                        moved = gameLogic.moveSelectedBlock(controller.Direction.RIGHT);
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

    private void setupAutoSaveTimer() {
        autoSaveTimer = new Timer(30000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (gameLogic != null && !gameLogic.getGameState().isGameWon()) {
                    gameLogic.saveGameStateToFile(new File(AUTO_SAVE_FILE_NAME));
                } else if (gameLogic != null && gameLogic.getGameState().isGameWon()) {
                    if(autoSaveTimer != null) autoSaveTimer.stop();
                    // 游戏胜利则停止
                }
            }
        });
    }
    //这个类实现了自动保存的逻辑，实现了每秒自动保存一次的逻辑

    public void refreshGameView() {
        gamePanel.repaint();
        //更新游戏界面的状态
        updateStatus();
        //更新状态栏里面的状态
        this.requestFocusInWindow();
        //确保窗口能够进行下一次操作
    }
    //这个方法非常重要，每当你进行了一次移动都要引用一次这个方法来更新游戏界面和状态栏里面的状态


    private void updateStatus() {
        statusPanel.updateStatus(gameLogic.getGameState().getSteps(),
                gameLogic.getGameState().getElapsedTimeInSeconds());
    }
    //这个方法别的地方用不到，只是在这个类当中方便用来写状态栏状态的


    public void checkAndShowWinDialog() {
        if (gameLogic.getGameState().isGameWon()) {
            gameTimer.stop();
            
            // 使用新的VictoryFrame来显示胜利界面
            String formattedTime = formatTime(gameLogic.getGameState().getElapsedTimeInSeconds());
            int steps = gameLogic.getGameState().getSteps();
            
            // 第三个参数是当前关卡，这里暂时设为1，可以根据实际情况调整
            // 第四个参数是胜利背景图片路径，这里使用"victory_background.jpg"，请确保该文件存在于resources目录
            view.frontend.resourses.VictoryFrame.showVictory(
                    formattedTime, 
                    steps, 
                    1, 
                    "victory_background.jpg");
        }
    }
    //这里使用了新的胜利界面，需要在resources文件夹中放入名为victory_background.jpg的图片


    public void handleUndo() {
        if (gameLogic.undoLastMove()) {
            refreshGameView();
        }
    }
    //每次撤销操作以后需要引用一次这个方法来更新整个JFrame

    public void handleReset() {
        gameLogic.resetGame();
        gameTimer.restart();
        if (autoSaveTimer != null) {
            autoSaveTimer.restart();
        }
        // 重置游戏时也重启自动保存
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

            //调用 GameLogic 中的方法来实际保存游戏状态到这个文件
            if (gameLogic.saveGameStateToFile(fileToSave)) {
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

            //调用 GameLogic 中的方法来实际加载游戏状态
            if (gameLogic.loadGameStateFromFile(fileToLoad)) {
                // 加载成功后的处理
                gameTimer.stop();

                if (autoSaveTimer != null) autoSaveTimer.stop();


                if (!gameLogic.getGameState().isGameWon()) {
                    gameTimer.start();
                    if (autoSaveTimer != null) {
                        autoSaveTimer.restart(); // 为新加载的游戏重启自动保存
                    }
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

    public GameLogic getGameLogic() {
        return gameLogic;
    }
    //顺便写了一个gameLogic的getter以防其它开发的时候要用到GameFrame中的gameLogic

    // 新增方法：处理AI求解请求
    public void handleAISolve() {
        if (isAISolving) {
            JOptionPane.showMessageDialog(this, "AI 正在计算中，请稍候...", "AI 忙碌", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (gameLogic.getGameState().isGameWon()) { //
            JOptionPane.showMessageDialog(this, "游戏已经胜利！无需 AI 求解。", "游戏结束", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        controlPanel.setAllButtonsEnabled(false);
        isAISolving = true;
        // 可选: 更新状态栏提示AI正在工作
        // statusPanel.showTemporaryMessage("AI 计算中...");

        SwingWorker<List<MoveRecord>, String> worker = new SwingWorker<List<MoveRecord>, String>() {
            private long startTime;

            @Override
            protected List<MoveRecord> doInBackground() throws Exception {
                publish("AI 开始求解...");
                startTime = System.currentTimeMillis();
                AISolver solver = new AISolver();
                GameState currentStateForAI = new GameState(gameLogic.getGameState()); //
                List<MoveRecord> solution = solver.solve(currentStateForAI);
                long endTime = System.currentTimeMillis();
                publish(String.format("AI 求解耗时: %.2f 秒", (endTime - startTime) / 1000.0));
                return solution;
            }

            @Override
            protected void process(List<String> chunks) {
                for (String message : chunks) {
                    System.out.println(message); // 或更新到状态栏
                }
            }

            @Override
            protected void done() {
                try {
                    List<MoveRecord> solution = get();
                    if (solution != null && !solution.isEmpty()) {
                        publish("AI 找到解法，共 " + solution.size() + " 步。准备演示...");
                        animateSolution(solution);
                    } else {
                        publish("AI 未能找到解法或被中断。");
                        JOptionPane.showMessageDialog(GameFrame.this, "AI 未能找到解法。", "AI 求解结果", JOptionPane.INFORMATION_MESSAGE);
                        finishAISession(); // 恢复UI
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    publish("AI 求解被中断。");
                    JOptionPane.showMessageDialog(GameFrame.this, "AI 求解过程被中断。", "AI 错误", JOptionPane.ERROR_MESSAGE);
                    finishAISession(); // 恢复UI
                } catch (java.util.concurrent.ExecutionException e) {
                    publish("AI 求解过程中发生错误。");
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(GameFrame.this, "AI 求解时发生错误: " + e.getCause().getMessage(), "AI 错误", JOptionPane.ERROR_MESSAGE);
                    finishAISession(); // 恢复UI
                }
                // 如果动画没有启动（例如，没有解），确保按钮已恢复
                if (animationTimer == null || !animationTimer.isRunning()) {
                    if (isAISolving) { // 只有在确实是AI结束后才调用
                        finishAISession();
                    }
                }
            }
        };
        worker.execute();
    }

    // 新增方法：动画演示解题步骤
    private void animateSolution(List<MoveRecord> solution) {
        if (solution == null || solution.isEmpty()) {
            finishAISession();
            return;
        }
        this.solutionMoves = solution;
        this.currentMoveIndex = 0;

        this.setFocusable(false); // 动画期间禁用键盘操作

        // 动画开始前停止游戏相关计时器
        if (gameTimer != null) gameTimer.stop(); //
        if (autoSaveTimer != null) autoSaveTimer.stop(); //


        int delay = 700; // 动画每一步的延迟
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }
        animationTimer = new Timer(delay, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (gameLogic.getGameState().isGameWon()) { //
                    animationTimer.stop();
                    finishAISession(); // 使用新的清理方法
                    checkAndShowWinDialog(); //
                    return;
                }

                if (currentMoveIndex < solutionMoves.size()) {
                    MoveRecord move = solutionMoves.get(currentMoveIndex);
                    boolean selectionSuccess = gameLogic.selectBlockAt(move.getFromX(), move.getFromY()); //

                    if (!selectionSuccess || gameLogic.getSelectedBlock() == null || gameLogic.getSelectedBlock().getId() != move.getBlockId()) { //
                        System.err.println("AI 动画错误: 选择棋子失败 " + move);
                        animationTimer.stop();
                        finishAISession();
                        JOptionPane.showMessageDialog(GameFrame.this, "AI演示时选择棋子失败。", "AI演示错误", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    Direction moveDirection = determineDirection(move.getFromX(), move.getFromY(), move.getToX(), move.getToY());
                    if (moveDirection == null) {
                        System.err.println("AI 动画错误: 无法确定方向 " + move);
                        animationTimer.stop();
                        finishAISession();
                        JOptionPane.showMessageDialog(GameFrame.this, "AI演示时无法确定移动方向。", "AI演示错误", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    boolean moved = gameLogic.moveSelectedBlock(moveDirection); //
                    if (moved) {
                        refreshGameView(); //
                    } else {
                        System.err.println("AI 动画错误: 移动执行失败 " + move);
                        animationTimer.stop();
                        finishAISession();
                        JOptionPane.showMessageDialog(GameFrame.this, "AI演示的某一步未能执行。", "AI演示错误", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    currentMoveIndex++;
                } else {
                    animationTimer.stop();
                    finishAISession();
                    System.out.println("AI 动画: 演示完成。");
                    checkAndShowWinDialog(); //
                }
            }
        });
        animationTimer.start();
    }

    // 新增方法：统一处理AI会话结束（无论成功、失败、中断或动画完成）
    private void finishAISession() {
        controlPanel.setAllButtonsEnabled(true);
        this.setFocusable(true);
        this.requestFocusInWindow(); //
        isAISolving = false;

        // 仅当游戏未胜利时才重启计时器
        if (!gameLogic.getGameState().isGameWon()) { //
            if (gameTimer != null && !gameTimer.isRunning()) { //
                gameTimer.start(); //
            }
            if (autoSaveTimer != null && !autoSaveTimer.isRunning()) { //
                autoSaveTimer.start(); //
            }
        }
    }


    // 新增方法：根据坐标确定方向
    private Direction determineDirection(int fromX, int fromY, int toX, int toY) {
        int dx = toX - fromX;
        int dy = toY - fromY;
        // AI通常生成单位移动
        if (dx == 0 && dy == -1) return Direction.UP;
        if (dx == 0 && dy == 1) return Direction.DOWN;
        if (dx == -1 && dy == 0) return Direction.LEFT;
        if (dx == 1 && dy == 0) return Direction.RIGHT;
        // 如果AI可能生成多格移动（虽然BFS通常不会直接这么做，除非canMove支持）
        if (dx == 0 && dy < -1) return Direction.UP;
        if (dx == 0 && dy > 1) return Direction.DOWN;
        if (dx < -1 && dy == 0) return Direction.LEFT;
        if (dx > 1 && dy == 0) return Direction.RIGHT;
        return null;
    }

    @Override
    public void dispose() {
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }
        if (autoSaveTimer != null) {
            autoSaveTimer.stop(); // 确保在窗口销毁时停止自动保存计时器
        }
        if (gameTimer != null) { //
            gameTimer.stop(); //
        }
        super.dispose();
    }
    //确保窗口关闭时自动保存的计时器和游戏界面的主计时器都关闭，避免资源浪费

}
