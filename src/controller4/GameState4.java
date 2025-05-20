package controller4;


import java.io.Serializable;
import java.util.Objects;
import java.util.Stack;

/*
*这个类提供了一下好用的方法：
* 1.它的几个field（board4,steps,moveHistory,elapsedTimeInSeconds,gameWon)的getter，注意gameWon的getter叫做isGameWon()；
* 2.对于steps这个给了增加步数的方法incrementSteps()，以及减少步数的方法decrementSteps()
* 3.对于elapsedTimeInSeconds这个方法给出了增加时间的方法incrementElapsedTime()
* 4.对于moveHistory这个field给出了addMoveToHistory()来添加记录到Stack里面，popMoveFromHistory()来移除Stack最上面的记录，clearMoveHistory()用来清楚Stack里面的全部记录
* 5.还有一个resetGame()方法可以用来把棋盘重置
* 6.补充一点：
* 要完成撤销工作，Stack<>是一个非常好的选择，有先入后出的性质
* Stack常用的与撤销相关的方法：.push()用来把moveRecord填入最上面， .pop()用来移除最上面的moveRecord,
* .peek()用来查看最上面的moveRecord， .empty()用来检测Stack是不是空的 .clear()用来清除Stack
 */

public class GameState4 implements Serializable {
    private static final long serialVersionUID = 1L;
    //同样定义序列号
    private Board4 board4;
    private int steps;
    Stack<MoveRecord4> moveHistory;
    // 用于撤销操作
    //要完成撤销工作，Stack<>是一个非常好的选择，有先入后出的性质
    //Stack常用的与撤销相关的方法：.push()用来把moveRecord填入最上面， .pop()用来移除最上面的moveRecord,
    //.peek()用来查看最上面的moveRecord， .empty()用来检测Stack是不是空的 .clear()用来清除Stack
    private int elapsedTimeInSeconds;
    // 游戏已进行的时间（秒）
    private boolean gameWon;
    // 标记游戏是否胜利


    public GameState4() {
        this.board4 = new Board4();
        this.steps = 0;
        this.moveHistory = new Stack<>();
        this.elapsedTimeInSeconds = 0;
        this.gameWon = false;
    }

    public Board4 getBoard() {
        return board4;
    }

    public int getSteps() {
        return steps;
    }

    public Stack<MoveRecord4> getMoveHistory() {
        return moveHistory;
    }

    public long getElapsedTimeInSeconds() {
        return elapsedTimeInSeconds;
    }

    public boolean isGameWon() {
        return gameWon;
    }

    public void setBoard(Board4 board4) {
        this.board4 = board4;
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


    public void addMoveToHistory(MoveRecord4 record) {
        this.moveHistory.push(record);
    }
    //把某个记录添加到Stack里面的方法

    public MoveRecord4 popMoveFromHistory() {
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


    public void resetGame() {
        this.board4 = new Board4();
        this.steps = 0;
        this.moveHistory.clear();
        this.elapsedTimeInSeconds = 0;
        this.gameWon = false;
    }
    //重置游戏的方法


    @Override
    public boolean equals(Object o) {
        if (!(o instanceof GameState4 gameState4)) return false;
        return steps == gameState4.steps && elapsedTimeInSeconds == gameState4.elapsedTimeInSeconds && gameWon == gameState4.gameWon && Objects.equals(board4, gameState4.board4) && Objects.equals(moveHistory, gameState4.moveHistory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(board4, steps, moveHistory, elapsedTimeInSeconds, gameWon);
    }

    //同样顺手重写了equals和hashCode方法
}
