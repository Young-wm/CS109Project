package controller2;

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
public class GameLogic2 {
    private GameState2 gameState2;
    //创建的gameState里面有steps,moveHistory,elapsedTimeInSeconds,gameWon,board这几个field，
    //另外，其中的board又有isValidCoordinate()、getBlockIdAt()、getBlockById()、getBlocksCopy()、getGridCopy()、moveBlockOnBoard()这几个可能用到的方法
    private Block2 selectedBlock2;
    // 当前选中的方块

    public GameLogic2() {
        this.gameState2 = new GameState2();
        this.selectedBlock2 = null;
    }

    public GameState2 getGameState() {
        return gameState2;
    }

    public Block2 getSelectedBlock() {
        return selectedBlock2;
    }

    public boolean selectBlockAt(int gridX, int gridY) {
        Board2 board2 = gameState2.getBoard();
        if (!board2.isValidCoordinate(gridX, gridY)) {
            this.selectedBlock2 = null;
            return false;
        }
        //如果输入棋盘的位置是invalid会返回false
        int blockId = board2.getBlockIdAt(gridX, gridY);
        if (blockId != Board2.EMPTY_CELL_ID) {
            this.selectedBlock2 = board2.getBlockById(blockId);
            return true;
        } else {
            this.selectedBlock2 = null;
            return false;
        }
        //不是空的话就成功点击，并且把点击的block传入selectedBlock
    }
    //这个方法的作用是当你输入一个坐标值的时候，如果这个地方不是空的，且坐标是有效的话，就返回true，而且把这个地方的方块传入selectedBlock
    //后面可能需要把这个地方的输入坐标与一个鼠标点击事件关联起来


    public boolean moveSelectedBlock(Direction2 direction2) {
        if (selectedBlock2 == null || gameState2.isGameWon()) {
            return false;
        }

        if (canMove(selectedBlock2, direction2.getDx(), direction2.getDy())) {
            Board2 board2 = gameState2.getBoard();
            MoveRecord2 record = new MoveRecord2(selectedBlock2.getId(), selectedBlock2.getX(), selectedBlock2.getY(), selectedBlock2.getX() + direction2.getDx(), selectedBlock2.getY() + direction2.getDy());
            board2.moveBlockOnBoard(selectedBlock2, selectedBlock2.getX() + direction2.getDx(), selectedBlock2.getY() + direction2.getDy());
            gameState2.incrementSteps();
            gameState2.addMoveToHistory(record);

            if (checkWinCondition()) {
                gameState2.setGameWon(true);
            }
            return true;
        }
        return false;
    }
    //这个方法实现的是块的移动，如果可以正常移动会返回true，并且把块移动过去，然后把步数增加1，再记录这次移动
    //另外，注意这个方法里面有一个selectedBlock的选中，所以这个方法再后面的使用的时候需要先用一次selectedBlockAt()这个方法，更新一下selectedBlock

    private boolean canMove(Block2 block2, int dx, int dy) {
        if (block2 == null || (dx == 0 && dy == 0)) {
            return false;
        }

        Board2 board2 = gameState2.getBoard();
        int newX = block2.getX() + dx;
        int newY = block2.getY() + dy;


        if (newX < 0 || (newX + block2.getWidth()) > board2.getWidth() ||
                newY < 0 || (newY + block2.getHeight()) > board2.getHeight()) {
            return false;
        }
        //检查是否超越了grid的边界

        if (dx > 0) {
            for (int y = 0; y < block2.getHeight(); y++) {
                int checkX = block2.getX() + block2.getWidth(); // 原方块右边界的右边一格
                int checkY = block2.getY() + y;
                int targetCellId = board2.getBlockIdAt(checkX, checkY);
                if (targetCellId != Board2.EMPTY_CELL_ID) {
                    return false;
                }
            }
        } else if (dx < 0) {
            for (int y = 0; y < block2.getHeight(); y++) {
                int checkX = block2.getX() - 1;
                int checkY = block2.getY() + y;
                int targetCellId = board2.getBlockIdAt(checkX, checkY);
                if (targetCellId != Board2.EMPTY_CELL_ID) {
                    return false;
                }
            }
        } else if (dy > 0) {
            for (int x = 0; x < block2.getWidth(); x++) {
                int checkX = block2.getX() + x;
                int checkY = block2.getY() + block2.getHeight();
                int targetCellId = board2.getBlockIdAt(checkX, checkY);
                if (targetCellId != Board2.EMPTY_CELL_ID) {
                    return false;
                }
            }
        } else if (dy < 0) {
            for (int x = 0; x < block2.getWidth(); x++) {
                int checkX = block2.getX() + x;
                int checkY = block2.getY() - 1;
                int targetCellId = board2.getBlockIdAt(checkX, checkY);
                if (targetCellId != Board2.EMPTY_CELL_ID) {
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
        if (gameState2.isGameWon()) {
            return false;
        }

        MoveRecord2 lastMove = gameState2.popMoveFromHistory();
        //把stack里面的最上面的记录撤销
        if (lastMove != null) {
            Block2 block2ToMove = gameState2.getBoard().getBlockById(lastMove.getBlockId());
            if (block2ToMove != null) {
                gameState2.getBoard().moveBlockOnBoard(block2ToMove, lastMove.getFromX(), lastMove.getFromY());
                gameState2.decrementSteps();

                if (gameState2.isGameWon()) {
                    if (!checkWinConditionInternal()) {
                        gameState2.setGameWon(false);
                    }
                }
                this.selectedBlock2 = block2ToMove;
                return true;
            }
        }
        return false;
    }
    //这个类是用来撤销移动的，如果返回true则能够清除Stack里面最上层的记录，再将选中的方块设定为这个被撤销过的方块


    public void resetGame() {
        gameState2.resetGame();
        this.selectedBlock2 = null;
    }

    public boolean checkWinCondition() {
        if (gameState2.isGameWon()) {
            return true;
        }
        return checkWinConditionInternal();
    }

    public static final int CAO_CAO_ID = 1;
    public static final int WIN_TARGET_X = 1;
    public static final int WIN_TARGET_Y = 3;
    //这个是胜利条件的判断，对应曹操方块在（1，3）的位置

    private boolean checkWinConditionInternal() {
        Block2 caoCao = gameState2.getBoard().getBlockById(CAO_CAO_ID);
        if (caoCao != null) {
            return caoCao.getX() == WIN_TARGET_X && caoCao.getY() == WIN_TARGET_Y;
        }
        return false;
    }
    //这两个方法联合起来实现是否胜利的判断


    public boolean saveGameStateToFile(File file) {
        boolean saved = GameDataStorage2.saveGameToFile(this.gameState2, file);
        if (saved) {
            System.out.println("Successful: Game state saved to " + file.getName());
        } else {
            System.out.println("Error: Failed to save game state to " + file.getName());
        }
        return saved;
    }

    public boolean loadGameStateFromFile(File file) {
        GameState2 loadedState = GameDataStorage2.loadGameFromFile(file);
        if (loadedState != null) {
            this.gameState2 = loadedState;
            this.selectedBlock2 = null;
            //重载游戏以后清空被选中的方块

            if (checkWinConditionInternal()) {
                this.gameState2.setGameWon(true);
            } else {
                this.gameState2.setGameWon(false);
            }
            //这里以防万一，判断一下是否重载的游戏是胜利了的状态

            System.out.println("Successful: Game state loaded from " + file.getName());
            return true;
        } else {
            System.err.println("GameLogic2: Failed to load game state from " + file.getName());
            return false;
        }
    }




}
