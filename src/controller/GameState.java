package controller;


import java.io.Serializable;
import java.util.Objects;
import java.util.Stack;

/*
*这个类提供了一下好用的方法：
* 1.它的几个field（board,steps,moveHistory,elapsedTimeInSeconds,gameWon)的getter，注意gameWon的getter叫做isGameWon()；
* 2.对于steps这个给了增加步数的方法incrementSteps()，以及减少步数的方法decrementSteps()
* 3.对于elapsedTimeInSeconds这个方法给出了增加时间的方法incrementElapsedTime()
* 4.对于moveHistory这个field给出了addMoveToHistory()来添加记录到Stack里面，popMoveFromHistory()来移除Stack最上面的记录，clearMoveHistory()用来清楚Stack里面的全部记录
* 5.还有一个resetGame()方法可以用来把棋盘重置
* 6.补充一点：
* 要完成撤销工作，Stack<>是一个非常好的选择，有先入后出的性质
* Stack常用的与撤销相关的方法：.push()用来把moveRecord填入最上面， .pop()用来移除最上面的moveRecord,
* .peek()用来查看最上面的moveRecord， .empty()用来检测Stack是不是空的 .clear()用来清除Stack
 */

public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;
    //同样定义序列号
    private Board board;
    private int steps;
    Stack<MoveRecord> moveHistory;
    // 用于撤销操作
    //要完成撤销工作，Stack<>是一个非常好的选择，有先入后出的性质
    //Stack常用的与撤销相关的方法：.push()用来把moveRecord填入最上面， .pop()用来移除最上面的moveRecord,
    //.peek()用来查看最上面的moveRecord， .empty()用来检测Stack是不是空的 .clear()用来清除Stack
    private int elapsedTimeInSeconds;
    // 游戏已进行的时间（秒）
    private boolean gameWon;
    // 标记游戏是否胜利


    public GameState() {
        this.board = new Board();
        this.steps = 0;
        this.moveHistory = new Stack<>();
        this.elapsedTimeInSeconds = 0;
        this.gameWon = false;
    }

    public Board getBoard() {
        return board;
    }

    public int getSteps() {
        return steps;
    }

    public Stack<MoveRecord> getMoveHistory() {
        return moveHistory;
    }

    public long getElapsedTimeInSeconds() {
        return elapsedTimeInSeconds;
    }

    public boolean isGameWon() {
        return gameWon;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public void incrementSteps() {
        this.steps++;
    }
    //增加步数的方法

    public void decrementSteps() {
        if (this.steps > 0) {
            this.steps--;
        }
    }
    //减少步数的方法

    public void resetSteps() {
        this.steps = 0;
    }
    //重置步数的方法


    public void addMoveToHistory(MoveRecord record) {
        this.moveHistory.push(record);
    }
    //把某个记录添加到Stack里面的方法

    public MoveRecord popMoveFromHistory() {
        if (!this.moveHistory.isEmpty()) {
            return this.moveHistory.pop();
        }
        return null;
    }
    //用来移除Stack里面最上层的记录的方法

    public void clearMoveHistory() {
        this.moveHistory.clear();
    }
    //用来清除Stack里面记录的方法

    public void setElapsedTimeInSeconds(int elapsedTimeInSeconds) {
        this.elapsedTimeInSeconds = elapsedTimeInSeconds;
    }

    public void incrementElapsedTime(int secondsToAdd) {
        this.elapsedTimeInSeconds += secondsToAdd;
    }
    //增加时间的方法


    public void setGameWon(boolean gameWon) {
        this.gameWon = gameWon;
    }

    public void setSteps(int steps) {
        if (steps >= 0) {
            this.steps = steps;
        } else {
            this.steps = 0;
        }
    }


    public void resetGame() {
        this.board = new Board();
        this.steps = 0;
        this.moveHistory.clear();
        this.elapsedTimeInSeconds = 0;
        this.gameWon = false;
    }
    //重置游戏的方法


    @Override
    public boolean equals(Object o) {
        if (!(o instanceof GameState gameState)) return false;
        return steps == gameState.steps && elapsedTimeInSeconds == gameState.elapsedTimeInSeconds && gameWon == gameState.gameWon && Objects.equals(board, gameState.board) && Objects.equals(moveHistory, gameState.moveHistory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, steps, moveHistory, elapsedTimeInSeconds, gameWon);
    }

    //同样顺手重写了equals和hashCode方法


    public GameState(GameState other) {
        this.board = new Board(other.board); // 使用 Board 的拷贝构造函数进行深拷贝
        this.steps = other.steps;
        this.elapsedTimeInSeconds = other.elapsedTimeInSeconds;
        this.gameWon = other.gameWon;
        // AI搜索中的路径是独立生成的，所以 moveHistory 通常不需要在这里深拷贝。
        // 但对于我们的AI SearchNode，路径是单独维护的。
        this.moveHistory = new Stack<>();
        // AI的SearchNode会自己管理路径，这里给个空的就好
    }
    // 为了实现ai功能，这里也增加了一个拷贝构造函数，拷贝了所有GameState里面的内容

}
