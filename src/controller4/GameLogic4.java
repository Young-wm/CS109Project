package controller4;

/*
*这个类给出了几个主要的方法：
* 1.selectBlockAt()输入一个grid的坐标，后续可能要和鼠标点击事件结合起来，返回true则有效地点击了某个方块，把方块传输给了变量selectedBlock
* 2.moveSelectedBlock()这个方法要传入一个Direction，返回true则有效地将某个块进行了移动，并且增加了步数，记录下来这次移动
* 3.undoLastMove()用来撤销最近的一次移动，返回ture则步数减1，撤销记录里面的最后一次，并且把selectedBlock定为被移动的这个
* 4.resetGame()，重置整个游戏
* 5.checkWinCondition(),返回true则表示胜利
* 6.留了两个接口，一个AISolver的和一个SoundPlayer的，后续可以补上
 */
public class GameLogic4 {
    private GameState4 gameState4;
    //创建的gameState里面有steps,moveHistory,elapsedTimeInSeconds,gameWon,board这几个field，
    //另外，其中的board又有isValidCoordinate()、getBlockIdAt()、getBlockById()、getBlocksCopy()、getGridCopy()、moveBlockOnBoard()这几个可能用到的方法
    private Block4 selectedBlock4;
    // 当前选中的方块

    public GameLogic4() {
        this.gameState4 = new GameState4();
        this.selectedBlock4 = null;
    }

    public GameState4 getGameState() {
        return gameState4;
    }

    public Block4 getSelectedBlock() {
        return selectedBlock4;
    }

    public boolean selectBlockAt(int gridX, int gridY) {
        Board4 board4 = gameState4.getBoard();
        if (!board4.isValidCoordinate(gridX, gridY)) {
            this.selectedBlock4 = null;
            return false;
        }
        //如果输入棋盘的位置是invalid会返回false
        int blockId = board4.getBlockIdAt(gridX, gridY);
        if (blockId != Board4.EMPTY_CELL_ID) {
            this.selectedBlock4 = board4.getBlockById(blockId);
            return true;
        } else {
            this.selectedBlock4 = null;
            return false;
        }
        //不是空的话就成功点击，并且把点击的block传入selectedBlock
    }
    //这个方法的作用是当你输入一个坐标值的时候，如果这个地方不是空的，且坐标是有效的话，就返回true，而且把这个地方的方块传入selectedBlock
    //后面可能需要把这个地方的输入坐标与一个鼠标点击事件关联起来


    public boolean moveSelectedBlock(Direction4 direction4) {
        if (selectedBlock4 == null || gameState4.isGameWon()) {
            return false;
        }

        if (canMove(selectedBlock4, direction4.getDx(), direction4.getDy())) {
            Board4 board4 = gameState4.getBoard();
            MoveRecord4 record = new MoveRecord4(selectedBlock4.getId(), selectedBlock4.getX(), selectedBlock4.getY(), selectedBlock4.getX() + direction4.getDx(), selectedBlock4.getY() + direction4.getDy());
            board4.moveBlockOnBoard(selectedBlock4, selectedBlock4.getX() + direction4.getDx(), selectedBlock4.getY() + direction4.getDy());
            gameState4.incrementSteps();
            gameState4.addMoveToHistory(record);

            if (checkWinCondition()) {
                gameState4.setGameWon(true);
            }
            return true;
        }
        return false;
    }
    //这个方法实现的是块的移动，如果可以正常移动会返回true，并且把块移动过去，然后把步数增加1，再记录这次移动
    //另外，注意这个方法里面有一个selectedBlock的选中，所以这个方法再后面的使用的时候需要先用一次selectedBlockAt()这个方法，更新一下selectedBlock

    private boolean canMove(Block4 block4, int dx, int dy) {
        if (block4 == null || (dx == 0 && dy == 0)) {
            return false;
        }

        Board4 board4 = gameState4.getBoard();
        int newX = block4.getX() + dx;
        int newY = block4.getY() + dy;


        if (newX < 0 || (newX + block4.getWidth()) > board4.getWidth() ||
                newY < 0 || (newY + block4.getHeight()) > board4.getHeight()) {
            return false;
        }
        //检查是否超越了grid的边界

        if (dx > 0) {
            for (int y = 0; y < block4.getHeight(); y++) {
                int checkX = block4.getX() + block4.getWidth(); // 原方块右边界的右边一格
                int checkY = block4.getY() + y;
                int targetCellId = board4.getBlockIdAt(checkX, checkY);
                if (targetCellId != Board4.EMPTY_CELL_ID) {
                    return false;
                }
            }
        } else if (dx < 0) {
            for (int y = 0; y < block4.getHeight(); y++) {
                int checkX = block4.getX() - 1;
                int checkY = block4.getY() + y;
                int targetCellId = board4.getBlockIdAt(checkX, checkY);
                if (targetCellId != Board4.EMPTY_CELL_ID) {
                    return false;
                }
            }
        } else if (dy > 0) {
            for (int x = 0; x < block4.getWidth(); x++) {
                int checkX = block4.getX() + x;
                int checkY = block4.getY() + block4.getHeight();
                int targetCellId = board4.getBlockIdAt(checkX, checkY);
                if (targetCellId != Board4.EMPTY_CELL_ID) {
                    return false;
                }
            }
        } else if (dy < 0) {
            for (int x = 0; x < block4.getWidth(); x++) {
                int checkX = block4.getX() + x;
                int checkY = block4.getY() - 1;
                int targetCellId = board4.getBlockIdAt(checkX, checkY);
                if (targetCellId != Board4.EMPTY_CELL_ID) {
                    return false;
                }
            }
        }
        //检查要移动到的位置是不是有其他方块
        return true;
    }
    //这个方法只是为了moveSelectedBlock()这个方法的实现更加好写而写的方法，后面不能再使用这个
    //这个方法实现能不能移动的判断

    public boolean undoLastMove() {
        if (gameState4.isGameWon()) {
            return false;
        }

        MoveRecord4 lastMove = gameState4.popMoveFromHistory();
        //把stack里面的最上面的记录撤销
        if (lastMove != null) {
            Block4 block4ToMove = gameState4.getBoard().getBlockById(lastMove.getBlockId());
            if (block4ToMove != null) {
                gameState4.getBoard().moveBlockOnBoard(block4ToMove, lastMove.getFromX(), lastMove.getFromY());
                gameState4.decrementSteps();

                if (gameState4.isGameWon()) {
                    if (!checkWinConditionInternal()) {
                        gameState4.setGameWon(false);
                    }
                }
                this.selectedBlock4 = block4ToMove;
                return true;
            }
        }
        return false;
    }
    //这个类是用来撤销移动的，如果返回true则能够清除Stack里面最上层的记录，再将选中的方块设定为这个被撤销过的方块


    public void resetGame() {
        gameState4.resetGame();
        this.selectedBlock4 = null;
    }

    public boolean checkWinCondition() {
        if (gameState4.isGameWon()) {
            return true;
        }
        return checkWinConditionInternal();
    }

    public static final int CAO_CAO_ID = 1;
    public static final int WIN_TARGET_X = 1;
    public static final int WIN_TARGET_Y = 3;
    //这个是胜利条件的判断，对应曹操方块在（1，3）的位置

    private boolean checkWinConditionInternal() {
        Block4 caoCao = gameState4.getBoard().getBlockById(CAO_CAO_ID);
        if (caoCao != null) {
            return caoCao.getX() == WIN_TARGET_X && caoCao.getY() == WIN_TARGET_Y;
        }
        return false;
    }
    //这两个方法联合起来实现是否胜利的判断


    private AISolver4 aiSolver4;
    // AI解决器接口实例
    private SoundPlayer4 soundPlayer4;
    // 声音播放器接口实例

    public void setAiSolver(AISolver4 solver) {
        this.aiSolver4 = solver;
    }

    public void setSoundPlayer(SoundPlayer4 player) {
        this.soundPlayer4 = player;
    }

}
