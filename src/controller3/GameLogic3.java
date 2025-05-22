package controller3;

import java.io.File;

/*
*这个类给出了几个主要的方法：
* 1.selectBlockAt()输入一个grid的坐标，后续可能要和鼠标点击事件结合起来，返回true则有效地点击了某个方块，把方块传输给了变量selectedBlock
* 2.moveSelectedBlock()这个方法要传入一个Direction，返回true则有效地将某个块进行了移动，并且增加了步数，记录下来这次移动
* 3.undoLastMove()用来撤销最近的一次移动，返回ture则步数减1，撤销记录里面的最后一次，并且把selectedBlock定为被移动的这个
* 4.resetGame()，重置整个游戏
* 5.checkWinCondition(),返回true则表示胜利
* 6.留了两个接口，一个AISolver的和一个SoundPlayer的，后续可以补上
 */
public class GameLogic3 {
    private GameState3 gameState3;
    //创建的gameState里面有steps,moveHistory,elapsedTimeInSeconds,gameWon,board这几个field，
    //另外，其中的board又有isValidCoordinate()、getBlockIdAt()、getBlockById()、getBlocksCopy()、getGridCopy()、moveBlockOnBoard()这几个可能用到的方法
    private Block3 selectedBlock3;
    // 当前选中的方块

    public GameLogic3() {
        this.gameState3 = new GameState3();
        this.selectedBlock3 = null;
    }

    public GameState3 getGameState() {
        return gameState3;
    }

    public Block3 getSelectedBlock() {
        return selectedBlock3;
    }

    public boolean selectBlockAt(int gridX, int gridY) {
        Board3 board3 = gameState3.getBoard();
        if (!board3.isValidCoordinate(gridX, gridY)) {
            this.selectedBlock3 = null;
            return false;
        }
        //如果输入棋盘的位置是invalid会返回false
        int blockId = board3.getBlockIdAt(gridX, gridY);
        if (blockId != Board3.EMPTY_CELL_ID) {
            this.selectedBlock3 = board3.getBlockById(blockId);
            return true;
        } else {
            this.selectedBlock3 = null;
            return false;
        }
        //不是空的话就成功点击，并且把点击的block传入selectedBlock
    }
    //这个方法的作用是当你输入一个坐标值的时候，如果这个地方不是空的，且坐标是有效的话，就返回true，而且把这个地方的方块传入selectedBlock
    //后面可能需要把这个地方的输入坐标与一个鼠标点击事件关联起来


    public boolean moveSelectedBlock(Direction3 direction3) {
        if (selectedBlock3 == null || gameState3.isGameWon()) {
            return false;
        }

        if (canMove(selectedBlock3, direction3.getDx(), direction3.getDy())) {
            Board3 board3 = gameState3.getBoard();
            MoveRecord3 record = new MoveRecord3(selectedBlock3.getId(), selectedBlock3.getX(), selectedBlock3.getY(), selectedBlock3.getX() + direction3.getDx(), selectedBlock3.getY() + direction3.getDy());
            board3.moveBlockOnBoard(selectedBlock3, selectedBlock3.getX() + direction3.getDx(), selectedBlock3.getY() + direction3.getDy());
            gameState3.incrementSteps();
            gameState3.addMoveToHistory(record);

            if (checkWinCondition()) {
                gameState3.setGameWon(true);
            }
            return true;
        }
        return false;
    }
    //这个方法实现的是块的移动，如果可以正常移动会返回true，并且把块移动过去，然后把步数增加1，再记录这次移动
    //另外，注意这个方法里面有一个selectedBlock的选中，所以这个方法再后面的使用的时候需要先用一次selectedBlockAt()这个方法，更新一下selectedBlock

    private boolean canMove(Block3 block3, int dx, int dy) {
        if (block3 == null || (dx == 0 && dy == 0)) {
            return false;
        }

        Board3 board3 = gameState3.getBoard();
        int newX = block3.getX() + dx;
        int newY = block3.getY() + dy;


        if (newX < 0 || (newX + block3.getWidth()) > board3.getWidth() ||
                newY < 0 || (newY + block3.getHeight()) > board3.getHeight()) {
            return false;
        }
        //检查是否超越了grid的边界

        if (dx > 0) {
            for (int y = 0; y < block3.getHeight(); y++) {
                int checkX = block3.getX() + block3.getWidth(); // 原方块右边界的右边一格
                int checkY = block3.getY() + y;
                int targetCellId = board3.getBlockIdAt(checkX, checkY);
                if (targetCellId != Board3.EMPTY_CELL_ID) {
                    return false;
                }
            }
        } else if (dx < 0) {
            for (int y = 0; y < block3.getHeight(); y++) {
                int checkX = block3.getX() - 1;
                int checkY = block3.getY() + y;
                int targetCellId = board3.getBlockIdAt(checkX, checkY);
                if (targetCellId != Board3.EMPTY_CELL_ID) {
                    return false;
                }
            }
        } else if (dy > 0) {
            for (int x = 0; x < block3.getWidth(); x++) {
                int checkX = block3.getX() + x;
                int checkY = block3.getY() + block3.getHeight();
                int targetCellId = board3.getBlockIdAt(checkX, checkY);
                if (targetCellId != Board3.EMPTY_CELL_ID) {
                    return false;
                }
            }
        } else if (dy < 0) {
            for (int x = 0; x < block3.getWidth(); x++) {
                int checkX = block3.getX() + x;
                int checkY = block3.getY() - 1;
                int targetCellId = board3.getBlockIdAt(checkX, checkY);
                if (targetCellId != Board3.EMPTY_CELL_ID) {
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
        if (gameState3.isGameWon()) {
            return false;
        }

        MoveRecord3 lastMove = gameState3.popMoveFromHistory();
        //把stack里面的最上面的记录撤销
        if (lastMove != null) {
            Block3 block3ToMove = gameState3.getBoard().getBlockById(lastMove.getBlockId());
            if (block3ToMove != null) {
                gameState3.getBoard().moveBlockOnBoard(block3ToMove, lastMove.getFromX(), lastMove.getFromY());
                gameState3.decrementSteps();

                if (gameState3.isGameWon()) {
                    if (!checkWinConditionInternal()) {
                        gameState3.setGameWon(false);
                    }
                }
                this.selectedBlock3 = block3ToMove;
                return true;
            }
        }
        return false;
    }
    //这个类是用来撤销移动的，如果返回true则能够清除Stack里面最上层的记录，再将选中的方块设定为这个被撤销过的方块


    public void resetGame() {
        gameState3.resetGame();
        this.selectedBlock3 = null;
    }

    public boolean checkWinCondition() {
        if (gameState3.isGameWon()) {
            return true;
        }
        return checkWinConditionInternal();
    }

    public static final int CAO_CAO_ID = 1;
    public static final int WIN_TARGET_X = 1;
    public static final int WIN_TARGET_Y = 3;
    //这个是胜利条件的判断，对应曹操方块在（1，3）的位置

    private boolean checkWinConditionInternal() {
        Block3 caoCao = gameState3.getBoard().getBlockById(CAO_CAO_ID);
        if (caoCao != null) {
            return caoCao.getX() == WIN_TARGET_X && caoCao.getY() == WIN_TARGET_Y;
        }
        return false;
    }
    //这两个方法联合起来实现是否胜利的判断


    public boolean saveGameStateToFile(File file) {
        boolean saved = GameDataStorage3.saveGameToFile(this.gameState3, file);
        if (saved) {
            System.out.println("Successful: Game state saved to " + file.getName());
        } else {
            System.out.println("Error: Failed to save game state to " + file.getName());
        }
        return saved;
    }

    public boolean loadGameStateFromFile(File file) {
        GameState3 loadedState = GameDataStorage3.loadGameFromFile(file);
        if (loadedState != null) {
            this.gameState3 = loadedState;
            this.selectedBlock3 = null;
            //重载游戏以后清空被选中的方块

            if (checkWinConditionInternal()) {
                this.gameState3.setGameWon(true);
            } else {
                this.gameState3.setGameWon(false);
            }
            //这里以防万一，判断一下是否重载的游戏是胜利了的状态

            System.out.println("Successful: Game state loaded from " + file.getName());
            return true;
        } else {
            System.err.println("GameLogic3: Failed to load game state from " + file.getName());
            return false;
        }
    }




}
