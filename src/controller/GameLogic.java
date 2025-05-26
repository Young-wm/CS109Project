package controller;

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
public class GameLogic {
    private GameState gameState;
    //创建的gameState里面有steps,moveHistory,elapsedTimeInSeconds,gameWon,board这几个field，
    //另外，其中的board又有isValidCoordinate()、getBlockIdAt()、getBlockById()、getBlocksCopy()、getGridCopy()、moveBlockOnBoard()这几个可能用到的方法
    private Block selectedBlock;
    // 当前选中的方块

    public GameLogic() {
        this.gameState = new GameState();
        this.selectedBlock = null;
    }

    public GameState getGameState() {
        return gameState;
    }

    public Block getSelectedBlock() {
        return selectedBlock;
    }

    public boolean selectBlockAt(int gridX, int gridY) {
        Board board = gameState.getBoard();
        if (!board.isValidCoordinate(gridX, gridY)) {
            this.selectedBlock = null;
            return false;
        }
        //如果输入棋盘的位置是invalid会返回false
        int blockId = board.getBlockIdAt(gridX, gridY);
        if (blockId != Board.EMPTY_CELL_ID) {
            this.selectedBlock = board.getBlockById(blockId);
            return true;
        } else {
            this.selectedBlock = null;
            return false;
        }
        //不是空的话就成功点击，并且把点击的block传入selectedBlock
    }
    //这个方法的作用是当你输入一个坐标值的时候，如果这个地方不是空的，且坐标是有效的话，就返回true，而且把这个地方的方块传入selectedBlock
    //后面可能需要把这个地方的输入坐标与一个鼠标点击事件关联起来


    public boolean moveSelectedBlock(Direction direction) {
        if (selectedBlock == null || gameState.isGameWon()) {
            return false;
        }

        if (canMove(selectedBlock, direction.getDx(), direction.getDy())) {
            Board board = gameState.getBoard();
            MoveRecord record = new MoveRecord(selectedBlock.getId(), selectedBlock.getX(), selectedBlock.getY(), selectedBlock.getX() + direction.getDx(), selectedBlock.getY() + direction.getDy());
            board.moveBlockOnBoard(selectedBlock, selectedBlock.getX() + direction.getDx(), selectedBlock.getY() + direction.getDy());
            gameState.incrementSteps();
            gameState.addMoveToHistory(record);

            if (checkWinCondition()) {
                gameState.setGameWon(true);
            }
            return true;
        }
        return false;
    }
    //这个方法实现的是块的移动，如果可以正常移动会返回true，并且把块移动过去，然后把步数增加1，再记录这次移动
    //另外，注意这个方法里面有一个selectedBlock的选中，所以这个方法再后面的使用的时候需要先用一次selectedBlockAt()这个方法，更新一下selectedBlock

    public boolean canMove(Block block, int dx, int dy) {
        if (block == null || (dx == 0 && dy == 0)) {
            return false;
        }

        Board board = gameState.getBoard();
        int newX = block.getX() + dx;
        int newY = block.getY() + dy;


        if (newX < 0 || (newX + block.getWidth()) > board.getWidth() ||
                newY < 0 || (newY + block.getHeight()) > board.getHeight()) {
            return false;
        }
        //检查是否超越了grid的边界

        if (dx > 0) {
            for (int y = 0; y < block.getHeight(); y++) {
                int checkX = block.getX() + block.getWidth(); // 原方块右边界的右边一格
                int checkY = block.getY() + y;
                int targetCellId = board.getBlockIdAt(checkX, checkY);
                if (targetCellId != Board.EMPTY_CELL_ID) {
                    return false;
                }
            }
        } else if (dx < 0) {
            for (int y = 0; y < block.getHeight(); y++) {
                int checkX = block.getX() - 1;
                int checkY = block.getY() + y;
                int targetCellId = board.getBlockIdAt(checkX, checkY);
                if (targetCellId != Board.EMPTY_CELL_ID) {
                    return false;
                }
            }
        } else if (dy > 0) {
            for (int x = 0; x < block.getWidth(); x++) {
                int checkX = block.getX() + x;
                int checkY = block.getY() + block.getHeight();
                int targetCellId = board.getBlockIdAt(checkX, checkY);
                if (targetCellId != Board.EMPTY_CELL_ID) {
                    return false;
                }
            }
        } else if (dy < 0) {
            for (int x = 0; x < block.getWidth(); x++) {
                int checkX = block.getX() + x;
                int checkY = block.getY() - 1;
                int targetCellId = board.getBlockIdAt(checkX, checkY);
                if (targetCellId != Board.EMPTY_CELL_ID) {
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
        if (gameState.isGameWon()) {
            return false;
        }

        MoveRecord lastMove = gameState.popMoveFromHistory();
        //把stack里面的最上面的记录撤销
        if (lastMove != null) {
            Block blockToMove = gameState.getBoard().getBlockById(lastMove.getBlockId());
            if (blockToMove != null) {
                gameState.getBoard().moveBlockOnBoard(blockToMove, lastMove.getFromX(), lastMove.getFromY());
                gameState.decrementSteps();

                if (gameState.isGameWon()) {
                    if (!checkWinConditionInternal()) {
                        gameState.setGameWon(false);
                    }
                }
                this.selectedBlock = blockToMove;
                return true;
            }
        }
        return false;
    }
    //这个类是用来撤销移动的，如果返回true则能够清除Stack里面最上层的记录，再将选中的方块设定为这个被撤销过的方块


    public void resetGame() {
        gameState.resetGame();
        this.selectedBlock = null;
    }

    public boolean checkWinCondition() {
        if (gameState.isGameWon()) {
            return true;
        }
        return checkWinConditionInternal();
    }

    public static final int CAO_CAO_ID = 1;
    public static final int WIN_TARGET_X = 1;
    public static final int WIN_TARGET_Y = 3;
    //这个是胜利条件的判断，对应曹操方块在（1，3）的位置

    private boolean checkWinConditionInternal() {
        Block caoCao = gameState.getBoard().getBlockById(CAO_CAO_ID);
        if (caoCao != null) {
            return caoCao.getX() == WIN_TARGET_X && caoCao.getY() == WIN_TARGET_Y;
        }
        return false;
    }
    //这两个方法联合起来实现是否胜利的判断


    public boolean saveGameStateToFile(File file) {
        boolean saved = GameDataStorage.saveGameToFile(this.gameState, file);
        if (saved) {
            System.out.println("Successful: Game state saved to " + file.getName());
        } else {
            System.out.println("Error: Failed to save game state to " + file.getName());
        }
        return saved;
    }

    public boolean loadGameStateFromFile(File file) {
        GameState loadedState = GameDataStorage.loadGameFromFile(file);
        if (loadedState != null) {
            this.gameState = loadedState;
            this.selectedBlock = null;
            //重载游戏以后清空被选中的方块

            if (checkWinConditionInternal()) {
                this.gameState.setGameWon(true);
            } else {
                this.gameState.setGameWon(false);
            }
            //这里以防万一，判断一下是否重载的游戏是胜利了的状态

            System.out.println("Successful: Game state loaded from " + file.getName());
            return true;
        } else {
            System.err.println("GameLogic: Failed to load game state from " + file.getName());
            return false;
        }
    }


}
