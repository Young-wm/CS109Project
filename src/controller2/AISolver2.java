package controller2;

import java.util.*;

public class AISolver2 {
    private static class SearchNode {
        GameState2 gameState;
        List<MoveRecord2> path; // 从其各自的搜索起点（真实起点或目标点）到此节点的路径

        public SearchNode(GameState2 gameState, List<MoveRecord2> path) {
            this.gameState = gameState;
            this.path = path; // 这个路径已经是完整的，从其搜索源头开始
        }

        public GameState2 getGameState() {
            return gameState;
        }

        public List<MoveRecord2> getPath() {
            return path;
        }
    }

    // 构造函数
    public AISolver2() {
    }

    private String boardToString(Board2 board) {
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

    private boolean isWinState(Board2 board) {
        Block2 caoCao = board.getBlockById(GameLogic2.CAO_CAO_ID);
        if (caoCao != null) {
            return caoCao.getX() == GameLogic2.WIN_TARGET_X && caoCao.getY() == GameLogic2.WIN_TARGET_Y;
        }
        return false;
    }

    private boolean canMoveOnBoard(Board2 board, Block2 block, int dx, int dy) {
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
        Board2 tempBoard = board; // Use the provided board

        if (dx > 0) { // 向右移动
            for (int y = 0; y < blockHeight; y++) {
                if (tempBoard.getBlockIdAt(newX + blockWidth - 1, currentBlockY + y) != Board2.EMPTY_CELL_ID) {
                    return false;
                }
            }
        } else if (dx < 0) { // 向左移动
            for (int y = 0; y < blockHeight; y++) {
                if (tempBoard.getBlockIdAt(newX, currentBlockY + y) != Board2.EMPTY_CELL_ID) {
                    return false;
                }
            }
        } else if (dy > 0) { // 向下移动
            for (int x = 0; x < blockWidth; x++) {
                if (tempBoard.getBlockIdAt(currentBlockX + x, newY + blockHeight - 1) != Board2.EMPTY_CELL_ID) {
                    return false;
                }
            }
        } else if (dy < 0) { // 向上移动
            for (int x = 0; x < blockWidth; x++) {
                if (tempBoard.getBlockIdAt(currentBlockX + x, newY) != Board2.EMPTY_CELL_ID) {
                    return false;
                }
            }
        }

        return true;
    }


    private List<SearchNode> generateSuccessorStates(SearchNode parentNode) {
        List<SearchNode> successors = new ArrayList<>();
        GameState2 currentParentState = parentNode.getGameState();
        Board2 currentBoard = currentParentState.getBoard();

        for (Block2 originalBlock : currentBoard.getBlocksCopy().values()) {
            if (originalBlock.getId() == Board2.EMPTY_CELL_ID) {
                continue;
            }

            for (Direction2 direction : Direction2.values()) {
                // Try moving 1 step
                int dx = direction.getDx();
                int dy = direction.getDy();

                if (canMoveOnBoard(currentBoard, originalBlock, dx, dy)) {
                    Board2 nextBoard = new Board2(currentBoard);
                    Block2 blockToMoveOnNextBoard = nextBoard.getBlockById(originalBlock.getId());

                    int newX = originalBlock.getX() + dx;
                    int newY = originalBlock.getY() + dy;

                    nextBoard.moveBlockOnBoard(blockToMoveOnNextBoard, newX, newY);

                    GameState2 nextGameState = new GameState2(currentParentState);
                    nextGameState.setBoard(nextBoard);
                    nextGameState.incrementSteps();

                    MoveRecord2 move = new MoveRecord2(originalBlock.getId(),
                            originalBlock.getX(), originalBlock.getY(),
                            newX, newY);

                    List<MoveRecord2> newPath = new ArrayList<>(parentNode.getPath());
                    newPath.add(move);

                    successors.add(new SearchNode(nextGameState, newPath));
                }
            }
        }
        return successors;
    }
    // 以上的所有东西都与AISolver当中运用单向的BFS是一样的



    private GameState2 getWinState() {
        Board2 winBoard = new Board2(); // 创建一个标准棋盘
        Block2 caoCao = winBoard.getBlockById(GameLogic2.CAO_CAO_ID);
        Board2 goalBoard = new Board2(); // Start with initial board
        Block2 goalCaoCao = goalBoard.getBlockById(GameLogic2.CAO_CAO_ID);
        Board2 finalBoard = new Board2(); // Start with default
        finalBoard.initialize(); // Ensure it's the standard start
        GameState2 winGameState = new GameState2();
        Board2 winBoardState = winGameState.getBoard();
        Block2 caoCaoToMove = winBoardState.getBlockById(GameLogic2.CAO_CAO_ID);

        Board2 goalBoardSetup = new Board2(4, 5);
        goalBoardSetup.blocks.put(1, new Block2(1, "曹操", 2, 2, 1, 3));
        goalBoardSetup.blocks.put(2, new Block2(2, "关羽", 2, 1, 2, 2));
        goalBoardSetup.blocks.put(3, new Block2(3, "张飞", 1, 2, 3, 0));
        goalBoardSetup.blocks.put(4, new Block2(4, "赵云", 1, 2, 2, 0));
        goalBoardSetup.blocks.put(5, new Block2(5, "马超", 1, 2, 1, 0));
        goalBoardSetup.blocks.put(6, new Block2(6, "黄忠", 1, 2, 0, 0));
        goalBoardSetup.blocks.put(7, new Block2(7, "兵1", 1, 1, 0, 2));
        goalBoardSetup.blocks.put(8, new Block2(8, "兵2", 1, 1, 3, 3));
        goalBoardSetup.blocks.put(9, new Block2(9, "兵3", 1, 1, 3, 4));
        goalBoardSetup.blocks.put(10, new Block2(10, "兵4", 1, 1, 1, 2));
        goalBoardSetup.grid = new int[5][4];
        for (Block2 block : goalBoardSetup.blocks.values()) {
            for (int y = 0; y < block.getHeight(); y++) {
                for (int x = 0; x < block.getWidth(); x++) {
                    if (goalBoardSetup.isValidCoordinate(block.getX() + x, block.getY() + y)) {
                        goalBoardSetup.grid[block.getY() + y][block.getX() + x] = block.getId();
                    }
                }
            }
        }

        GameState2 goalState = new GameState2();
        goalState.setBoard(goalBoardSetup);
        goalState.setGameWon(true);
        return goalState;
    }
    // 这个方法是双向BFS所特有的，它返回了由单向BFS推出来的胜利时的GameState

    private List<MoveRecord2> reconstructPath(SearchNode meetNodeForward, SearchNode meetNodeBackward) {
        List<MoveRecord2> path = new ArrayList<>(meetNodeForward.getPath());
        List<MoveRecord2> backwardPath = meetNodeBackward.getPath();

        // 反转后向路径并添加到前向路径
        Collections.reverse(backwardPath);

        for (MoveRecord2 move : backwardPath) {
            path.add(new MoveRecord2(move.getBlockId(), move.getToX(), move.getToY(), move.getFromX(), move.getFromY()));
        }

        return path;
    }
    // 这个方法同样时双向BFS所特有的，它将正向推出来的路径和反向推出来的路径反过来以后合在了一起

    public List<MoveRecord2> solve(GameState2 initialGameState) {
        GameState2 goalState = getWinState();

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

        SearchNode startNode = new SearchNode(new GameState2(initialGameState), new ArrayList<>());
        SearchNode goalNode = new SearchNode(new GameState2(goalState), new ArrayList<>());

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
}
