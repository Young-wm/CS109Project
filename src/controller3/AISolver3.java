package controller3;

import java.util.*;
/*
* 双向的BFS相比较于单向的有以下三个变化：
* 1.新增一个方法，规定出一个最终的胜利状态时的棋盘布局(由单项的BFS推得）
* 2.写了一个把正向和反向推得的结果合起来的方法
* 3.写solve时用到两个队列，且找到的最终目标从找到胜利状态变为了找到两个队列推出来重合的节点
 */

public class AISolver3 {

    private static class SearchNode {
        GameState3 gameState;
        List<MoveRecord3> path; // 从其各自的搜索起点（真实起点或目标点）到此节点的路径

        public SearchNode(GameState3 gameState, List<MoveRecord3> path) {
            this.gameState = gameState;
            this.path = path; // 这个路径已经是完整的，从其搜索源头开始
        }

        public GameState3 getGameState() {
            return gameState;
        }

        public List<MoveRecord3> getPath() {
            return path;
        }
    }

    // 构造函数
    public AISolver3() {
    }

    private String boardToString(Board3 board) {
        StringBuilder sb = new StringBuilder();
        int[][] grid = board.getGridCopy();
        for (int i = 0; i < board.getHeight(); i++) {
            for (int j = 0; j < board.getWidth(); j++) {
                sb.append(grid[i][j]);
                sb.append(',');
            }
        }
        return sb.toString();
    }

    private boolean isWinState(Board3 board) {
        Block3 caoCao = board.getBlockById(GameLogic3.CAO_CAO_ID);
        if (caoCao != null) {
            return caoCao.getX() == GameLogic3.WIN_TARGET_X && caoCao.getY() == GameLogic3.WIN_TARGET_Y;
        }
        return false;
    }

    private boolean canMoveOnBoard(Board3 board, Block3 block, int dx, int dy) {
        if (block == null || (dx == 0 && dy == 0)) {
            return false;
        }

        int currentBlockX = block.getX();
        int currentBlockY = block.getY();
        int blockWidth = block.getWidth();
        int blockHeight = block.getHeight();
        int boardWidth = board.getWidth();
        int boardHeight = board.getHeight();

        // 1. 检查移动方向：一次只能移动一格
        if (Math.abs(dx) > 1 || Math.abs(dy) > 1 || (Math.abs(dx) + Math.abs(dy) != 1)) {
            if (Math.abs(dx) + Math.abs(dy) == 0) return false; // No move
        }

        int newX = currentBlockX + dx;
        int newY = currentBlockY + dy;

        // 2. 检查边界
        if (newX < 0 || (newX + blockWidth) > boardWidth ||
                newY < 0 || (newY + blockHeight) > boardHeight) {
            return false;
        }

        // 3. 检查目标位置是否为空
        // 遍历将要移动到的区域，但只检查 *新* 占据的格子
        Board3 tempBoard = board; // Use the provided board

        if (dx > 0) { // 向右移动
            for (int y = 0; y < blockHeight; y++) {
                if (tempBoard.getBlockIdAt(newX + blockWidth - 1, currentBlockY + y) != Board3.EMPTY_CELL_ID) {
                    return false;
                }
            }
        } else if (dx < 0) { // 向左移动
            for (int y = 0; y < blockHeight; y++) {
                if (tempBoard.getBlockIdAt(newX, currentBlockY + y) != Board3.EMPTY_CELL_ID) {
                    return false;
                }
            }
        } else if (dy > 0) { // 向下移动
            for (int x = 0; x < blockWidth; x++) {
                if (tempBoard.getBlockIdAt(currentBlockX + x, newY + blockHeight - 1) != Board3.EMPTY_CELL_ID) {
                    return false;
                }
            }
        } else if (dy < 0) { // 向上移动
            for (int x = 0; x < blockWidth; x++) {
                if (tempBoard.getBlockIdAt(currentBlockX + x, newY) != Board3.EMPTY_CELL_ID) {
                    return false;
                }
            }
        }

        return true;
    }


    private List<SearchNode> generateSuccessorStates(SearchNode parentNode) {
        List<SearchNode> successors = new ArrayList<>();
        GameState3 currentParentState = parentNode.getGameState();
        Board3 currentBoard = currentParentState.getBoard();

        for (Block3 originalBlock : currentBoard.getBlocksCopy().values()) {
            if (originalBlock.getId() == Board3.EMPTY_CELL_ID) {
                continue;
            }

            for (Direction3 direction : Direction3.values()) {
                // Try moving 1 step
                int dx = direction.getDx();
                int dy = direction.getDy();

                if (canMoveOnBoard(currentBoard, originalBlock, dx, dy)) {
                    Board3 nextBoard = new Board3(currentBoard);
                    Block3 blockToMoveOnNextBoard = nextBoard.getBlockById(originalBlock.getId());

                    int newX = originalBlock.getX() + dx;
                    int newY = originalBlock.getY() + dy;

                    nextBoard.moveBlockOnBoard(blockToMoveOnNextBoard, newX, newY);

                    GameState3 nextGameState = new GameState3(currentParentState);
                    nextGameState.setBoard(nextBoard);
                    nextGameState.incrementSteps(); // Increment steps for the new state

                    MoveRecord3 move = new MoveRecord3(originalBlock.getId(),
                            originalBlock.getX(), originalBlock.getY(),
                            newX, newY);

                    List<MoveRecord3> newPath = new ArrayList<>(parentNode.getPath());
                    newPath.add(move);

                    successors.add(new SearchNode(nextGameState, newPath));
                }
            }
        }
        return successors;
    }
    // 以上的所有东西都与AISolver当中运用单向的BFS是一样的



    private GameState3 getWinState() {
        Board3 winBoard = new Board3(); // 创建一个标准棋盘
        Block3 caoCao = winBoard.getBlockById(GameLogic3.CAO_CAO_ID);
        Board3 goalBoard = new Board3(); // Start with initial board
        Block3 goalCaoCao = goalBoard.getBlockById(GameLogic3.CAO_CAO_ID);
        Board3 finalBoard = new Board3(); // Start with default
        finalBoard.initialize(); // Ensure it's the standard start
        GameState3 winGameState = new GameState3();
        Board3 winBoardState = winGameState.getBoard();
        Block3 caoCaoToMove = winBoardState.getBlockById(GameLogic3.CAO_CAO_ID);

        Board3 goalBoardSetup = new Board3(4, 5);
        goalBoardSetup.blocks.put(1, new Block3(1, "曹操", 2, 2, 1, 3));
        goalBoardSetup.blocks.put(2, new Block3(2, "关羽", 2, 1, 2, 2));
        goalBoardSetup.blocks.put(3, new Block3(3, "张飞", 1, 2, 3, 0));
        goalBoardSetup.blocks.put(4, new Block3(4, "赵云", 1, 2, 2, 0));
        goalBoardSetup.blocks.put(5, new Block3(5, "马超", 1, 2, 1, 0));
        goalBoardSetup.blocks.put(6, new Block3(6, "黄忠", 1, 2, 0, 0));
        goalBoardSetup.blocks.put(7, new Block3(7, "兵1", 1, 1, 0, 2));
        goalBoardSetup.blocks.put(8, new Block3(8, "兵2", 1, 1, 3, 3));
        goalBoardSetup.blocks.put(9, new Block3(9, "兵3", 1, 1, 3, 4));
        goalBoardSetup.blocks.put(10, new Block3(10, "兵4", 1, 1, 1, 2));
        goalBoardSetup.grid = new int[5][4];
        for (Block3 block : goalBoardSetup.blocks.values()) {
            for (int y = 0; y < block.getHeight(); y++) {
                for (int x = 0; x < block.getWidth(); x++) {
                    if (goalBoardSetup.isValidCoordinate(block.getX() + x, block.getY() + y)) {
                        goalBoardSetup.grid[block.getY() + y][block.getX() + x] = block.getId();
                    }
                }
            }
        }

        GameState3 goalState = new GameState3();
        goalState.setBoard(goalBoardSetup);
        goalState.setGameWon(true);
        return goalState;
    }
    // 这个方法是双向BFS所特有的，它返回了由单向BFS推出来的胜利时的GameState

    private List<MoveRecord3> reconstructPath(SearchNode meetNodeForward, SearchNode meetNodeBackward) {
        List<MoveRecord3> path = new ArrayList<>(meetNodeForward.getPath());
        List<MoveRecord3> backwardPath = meetNodeBackward.getPath();

        // 反转后向路径并添加到前向路径
        Collections.reverse(backwardPath);

        for (MoveRecord3 move : backwardPath) {
            path.add(new MoveRecord3(move.getBlockId(), move.getToX(), move.getToY(), move.getFromX(), move.getFromY()));
        }

        return path;
    }
    // 这个方法同样时双向BFS所特有的，它将正向推出来的路径和反向推出来的路径反过来以后合在了一起

    public List<MoveRecord3> solve(GameState3 initialGameState) {
        GameState3 goalState = getWinState();

        if (isWinState(initialGameState.getBoard())) {
            return Collections.emptyList();
        }
        if (goalState == null || initialGameState.getBoard().equals(goalState.getBoard())) {
            System.err.println("AI: 目标状态无法确定或与初始状态相同。");
            return Collections.emptyList();
        }


        Queue<SearchNode> forwardQueue = new LinkedList<>();
        Queue<SearchNode> backwardQueue = new LinkedList<>();
        // 这里与之前单向有所不同，建立了两个队列，一个用来储存正向的节点，另一个用来储存逆向的节点
        Map<String, SearchNode> forwardVisited = new HashMap<>();
        Map<String, SearchNode> backwardVisited = new HashMap<>();
        // 同样的，记录也有两份，而且不同的是这里用到Map替代之前的Set，
        // 其实用起来差不多，只是这样与其对应的String能联系更加紧密

        SearchNode startNode = new SearchNode(new GameState3(initialGameState), new ArrayList<>());
        SearchNode goalNode = new SearchNode(new GameState3(goalState), new ArrayList<>());

        String startKey = boardToString(startNode.getGameState().getBoard());
        String goalKey = boardToString(goalNode.getGameState().getBoard());

        forwardQueue.add(startNode);
        forwardVisited.put(startKey, startNode);
        backwardQueue.add(goalNode);
        backwardVisited.put(goalKey, goalNode);

        long statesProcessed = 0;

        while (!forwardQueue.isEmpty() && !backwardQueue.isEmpty()) {

            // 优先扩展较小的队列，能有效保证循环能够更快地进行
            if (forwardQueue.size() <= backwardQueue.size()) {
                int levelSize = forwardQueue.size();
                for (int i = 0; i < levelSize; i++) {
                    SearchNode currentNode = forwardQueue.poll();
                    statesProcessed++;

                    List<SearchNode> successors = generateSuccessorStates(currentNode);
                    for (SearchNode successor : successors) {
                        String key = boardToString(successor.getGameState().getBoard());
                        if (backwardVisited.containsKey(key)) {
                            System.out.println("AI: 找到解法! (前向遇到后向) 处理状态: " + statesProcessed);
                            return reconstructPath(successor, backwardVisited.get(key));
                        }
                        if (!forwardVisited.containsKey(key)) {
                            forwardVisited.put(key, successor);
                            forwardQueue.add(successor);
                        }
                    }
                }
            } else {
                int levelSize = backwardQueue.size();
                for (int i = 0; i < levelSize; i++) {
                    SearchNode currentNode = backwardQueue.poll();
                    statesProcessed++;

                    List<SearchNode> successors = generateSuccessorStates(currentNode);
                    for (SearchNode successor : successors) {
                        String key = boardToString(successor.getGameState().getBoard());
                        if (forwardVisited.containsKey(key)) {
                            System.out.println("AI: 找到解法! (后向遇到前向) 处理状态: " + statesProcessed);
                            return reconstructPath(forwardVisited.get(key), successor);
                        }
                        if (!backwardVisited.containsKey(key)) {
                            backwardVisited.put(key, successor);
                            backwardQueue.add(successor);
                        }
                    }
                }
            }

            if (statesProcessed > 1000000000) {
                System.out.println("AI: 已处理超过 " + statesProcessed + " 个状态，搜索中止。");
                return Collections.emptyList();
            }
        }

        System.out.println("AI: 未找到解法。处理状态: " + statesProcessed);
        return Collections.emptyList();
    }
    //solve方法的变化在于从两头开始找，且不再是以走到胜利状态为结束，而是以两个队列中出现相同的节点为结束
}
