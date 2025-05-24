package controller;

import java.util.*;
/*
* ai方法的实现思路：
* 1.为Board和GameState都加上了拷贝构造函数
* 2.创建一个内部类SearchNode，它存储了当前的GameState和到达该GameState的步骤
* 3.接下来的关键方法是generateSuccessorStates()，它能找到从某个SearchNode往后一步的所有合法的新状态，新的SearchNode
* 4.最后是实现ai算法的最关键方法solve()，它用一个队列储存SearchNode，用一个Set储存走过的状态，
* 把没有出现过的合法新状态加到队列里面，只要队列不空就找队头去生成合法新状态，直到最终找到那个胜利状态
 */
public class AISolver {
    public AISolver() {
    }


    private static class SearchNode {
        private GameState gameState;
        // 当前游戏状态
        private List<MoveRecord> pathToState;
        // 到达此状态的移动记录列表

        public SearchNode(GameState gameState, List<MoveRecord> pathToState) {
            this.gameState = gameState;
            this.pathToState = pathToState;
        }

        public GameState getGameState() {
            return gameState;
        }

        public List<MoveRecord> getPathToState() {
            return pathToState;
        }
    }
    // 这里用到了一个内部类，它有记录了两个要点，
    // 一个是当前的游戏状态，另一个是达到当前状态的移动记录，用了一个list来储存



    /*
    * 以上代码使是第一步，完成了AISolver框架的构建，
    * 斌且准备在下面的第三步会用一个叫做solve的方法作为实现这个ai方法的主要方法
     */

    /*
    * 接下来完成了第二步，完成了BFS算法中非常关键的一步，深拷贝，
    * 给Board和GameState两个类都加上了拷贝构造函数，完成了深拷贝，
    * 保证了每一步所利用的Board，GameState都是自己独立的本地内存地址
     */


    public List<MoveRecord> solve(GameState initialGameState) {
        // 检查初始状态是否已经是胜利状态
        if (isWinState(initialGameState.getBoard())) {
            System.out.println("初始状态已是胜利状态。");
            return Collections.emptyList();
            // 不需要移动
        }

        Queue<SearchNode> queue = new LinkedList<>();
        // 补充知识：这里使用到Queue，具有先进先出的特点。
        // 且new的是LinkedList，可以看作是无界的，能放入任意多的SearchNode
        // queue常用的方法：加东西到队列：.add()/.offer()
        // 移除队头的元素：.remove()/.poll()
        // 查看队头元素：．element()/.peek()
        Set<String> visitedBoards = new HashSet<>();
        // 用于存储已经访问过的棋盘状态的字符串表示
        // 补充知识：这里使用到set，它里面的元素具有唯一性，和无序性，加入过的元素再假如便会被忽略，
        // new的是HashSet，里面的元素是没有顺序的
        // 常用方法：.add(element), .remove(element), .contains(element)


        // 创建初始搜索节点，使用 GameState 的拷贝构造函数
        SearchNode initialNode = new SearchNode(new GameState(initialGameState), new ArrayList<>());
        queue.add(initialNode);
        visitedBoards.add(boardToString(initialNode.getGameState().getBoard()));

        int statesProcessed = 0;
        // 计数器，用于调试和观察进度

        while (!queue.isEmpty()) {
            SearchNode currentNode = queue.poll();
            // 这里移除了队列里面的第一个元素，并且将其作为现在需要开始搜索的节点
            statesProcessed++;
            // 通过这个计数器，我们可以知道AI已经分析了多少个不同的棋盘状态。

            // 从当前节点生成所有可能的、合法的后继状态节点
            List<SearchNode> successorNodes = generateSuccessorStates(currentNode);

            for (SearchNode successorNode : successorNodes) {
                String boardKey = boardToString(successorNode.getGameState().getBoard());

                // 如果这个棋盘状态之前没有访问过
                if (!visitedBoards.contains(boardKey)) {
                    // 检查这个新的状态是否是胜利状态
                    if (isWinState(successorNode.getGameState().getBoard())) { //
                        System.out.println("AI: 找到解法！总共处理 " + statesProcessed + " 个状态。");
                        return successorNode.getPathToState(); // 返回到达胜利状态的路径
                    }

                    visitedBoards.add(boardKey); // 标记为已访问
                    queue.add(successorNode);    // 加入队列等待处理
                }
            }
            // 增加一个状态数量上限，防止无限运行或运行时间过长（对于复杂局面）
            if (statesProcessed > 100000000) { // 例如，1亿个状态
                System.out.println("AI: 已处理超过 " + statesProcessed + " 个状态，搜索中止以防超时。");
                return Collections.emptyList(); // 返回空表示未在限制内找到解
            }
        }

        System.out.println("AI: 未找到解法（队列已空）。总共处理 " + statesProcessed + " 个状态。");
        return Collections.emptyList(); // 队列为空，未找到解法
    }
    // 这个方法是要实现这个ai算法的最主要的方法，后面最主要需要完善的是这个方法
    // 它输入的是一个你想要开始让ai接入接替的GameState，
    // 返回的是一个一系列移动记录的List，里面记录了要实现胜利的接了下来的所有步骤，
    // 如果没有解题方法则返回null


    private String boardToString(Board board) {
        StringBuilder sb = new StringBuilder();
        int[][] grid = board.getGridCopy(); //
        for (int i = 0; i < board.getHeight(); i++) { //
            for (int j = 0; j < board.getWidth(); j++) { //
                sb.append(grid[i][j]);
                sb.append(',');
            }
            sb.append('|'); // 行分隔符
        }
        return sb.toString();
    }
    // 这方法是一个辅助方法，它用来帮助上面的solve方法的书写，它传入一个board，将其转化为String，
    // 这个String的书写是这样的，从左往右读grid的上标的序号，中间用，隔开，每行之间用|隔开

    private boolean isWinState(Board board) {
        Block caoCao = board.getBlockById(GameLogic.CAO_CAO_ID);
        if (caoCao != null) {
            // 使用 GameLogic 中定义的胜利条件
            return caoCao.getX() == GameLogic.WIN_TARGET_X && caoCao.getY() == GameLogic.WIN_TARGET_Y; //
        }
        return false;
    }
    // 这里编写了一个辅助方法来帮助solve方法的书写，传入的是一个棋盘，即需要被判断状态的棋盘，是胜利状态则返回true，否则返回false


    private List<SearchNode> generateSuccessorStates(SearchNode parentNode) {
        // 创建一个空的 ArrayList，用于存储所有找到的后继 SearchNode 节点
        // 每个后继节点代表从 parentNode 的状态再走一步能到达的新状态
        List<SearchNode> successors = new ArrayList<>();

        // 从 parentNode 中获取当前的游戏状态 (GameState)
        GameState currentParentState = parentNode.getGameState();

        // 从当前游戏状态中获取当前的棋盘布局 (Board)
        Board currentBoard = currentParentState.getBoard();

        // 获取当前棋盘上所有棋子 (Block) 的一个副本集合。
        // 调用 getBlocksCopy() 是为了防止在遍历过程中修改原始集合（尽管在这个循环中我们不修改它）
        // 并且确保我们得到的是当前棋盘上真实存在的棋子信息。
        for (Block originalBlock : currentBoard.getBlocksCopy().values()) { //

            // 检查棋子的ID是否为EMPTY_CELL_ID。如果是，则跳过这个棋子。
            // EMPTY_CELL_ID 通常代表棋盘上的空格，空格本身是不能主动移动的。
            if (originalBlock.getId() == Board.EMPTY_CELL_ID) { //
                continue; // 跳到 for 循环的下一次迭代，处理下一个棋子
            }

            // 对于每一个非空格的棋子 (originalBlock)，尝试四个可能的移动方向。
            // Direction.values() 会返回一个包含 UP, DOWN, LEFT, RIGHT 枚举常量的数组。
            for (Direction direction : Direction.values()) { //

                // 步骤 1: 检查在当前棋盘 (currentBoard) 上，当前的棋子 (originalBlock)
                // 是否可以向指定的方向 (direction) 移动一格。
                // canMoveOnBoard 是我们AI内部的一个辅助方法，用于判断移动的合法性。
                // direction.getDx() 和 direction.getDy() 分别获取该方向在x和y轴上的位移量 (通常是-1, 0, 或 1)。
                if (canMoveOnBoard(currentBoard, originalBlock, direction.getDx(), direction.getDy())) {

                    // 如果棋子可以朝这个方向移动：

                    // 步骤 2: 创建当前棋盘 (currentBoard) 的一个深拷贝 (deep copy)
                    // 这是至关重要的一步，以确保我们接下来的模拟移动不会影响到原始的 currentBoard
                    // (以及队列中其他可能引用 currentBoard 的 SearchNode)。
                    Board nextBoard = new Board(currentBoard); //

                    // 从这个新的棋盘副本 (nextBoard) 中，根据 originalBlock 的 ID 获取对应的 Block 对象。
                    // 这样做是因为 nextBoard 中的 Block 实例虽然与 originalBlock 状态相同，但它们是不同的对象（深拷贝的结果）。
                    // 我们需要在 nextBoard 上操作属于它的 Block 实例。
                    Block blockToMoveOnNextBoard = nextBoard.getBlockById(originalBlock.getId()); //

                    // 步骤 3: 在新的棋盘副本 (nextBoard) 上实际执行移动。
                    // moveBlockOnBoard 方法会更新 blockToMoveOnNextBoard 在 nextBoard 中的 x,y 坐标，
                    // 并且同时更新 nextBoard 的内部 grid 数组以反映这次移动。
                    // 移动的目标位置是 originalBlock 在原棋盘上的位置加上方向的位移。
                    nextBoard.moveBlockOnBoard(blockToMoveOnNextBoard, // 要在新棋盘上移动的棋子
                            originalBlock.getX() + direction.getDx(), // 棋子在新棋盘上的目标X坐标
                            originalBlock.getY() + direction.getDy()); // 棋子在新棋盘上的目标Y坐标

                    // 步骤 4: 创建一个新的 GameState 对象来封装这个移动后的棋盘状态 (nextBoard)。
                    // 我们使用 GameState 的拷贝构造函数，传入 currentParentState，
                    // 这样可以（如果需要的话）继承一些如步数、时间等信息（尽管对于纯粹的BFS路径搜索，这些可能不关键）。
                    // 然后，用我们新生成的 nextBoard 替换掉这个新 GameState 中的棋盘。
                    GameState nextGameState = new GameState(currentParentState); // 假设 GameState 有拷贝构造函数
                    nextGameState.setBoard(nextBoard); // 将新棋盘状态设置到新的游戏状态中

                    // 步骤 5: 创建一个 MoveRecord 对象来记录刚刚发生的这次成功的移动。
                    // originalBlock.getId(): 被移动棋子的ID。
                    // originalBlock.getX(), originalBlock.getY(): 棋子移动前的x,y坐标 (取自原始棋盘上的棋子)。
                    // blockToMoveOnNextBoard.getX(), blockToMoveOnNextBoard.getY(): 棋子移动后的x,y坐标 (取自已在nextBoard上完成移动的棋子)。
                    MoveRecord move = new MoveRecord(originalBlock.getId(),          // 棋子ID
                            originalBlock.getX(),           // 移动前X坐标
                            originalBlock.getY(),           // 移动前Y坐标
                            blockToMoveOnNextBoard.getX(),  // 移动后X坐标
                            blockToMoveOnNextBoard.getY()); // 移动后Y坐标

                    // 步骤 6: 构建到达这个新状态 (nextGameState) 的完整移动路径。
                    // 首先，创建一个父节点路径 (parentNode.getPathToState()) 的副本。
                    List<MoveRecord> newPath = new ArrayList<>(parentNode.getPathToState());
                    // 然后，将刚刚记录的这次移动 (move) 添加到新路径的末尾。
                    newPath.add(move);

                    // 步骤 7: 创建一个新的 SearchNode 对象。
                    // 这个新的 SearchNode 包含了移动后的游戏状态 (nextGameState) 和到达该状态的完整路径 (newPath)。
                    // 然后将这个新的后继节点添加到 successors 列表中。
                    successors.add(new SearchNode(nextGameState, newPath));
                }
            }
        }

        // 所有可能的后继状态都已生成完毕，返回这个包含它们的列表
        return successors;
    }
    // 这个方法是完成solve方法的一个非常关键的辅助方法，它的作用是输入一个节点，返回所有的可以出现新状态合法的节点，
    // 注意:这个方法非常关键的一个点是它会返回所有的合法节点，不管这个节点是否在之前出现过会使得ai搜索陷入循环，
    // 所以，在使用这个方法时，还要注意需要判断当前节点是否与之前的节点重复了

    private boolean canMoveOnBoard(Board board, Block block, int dx, int dy) {
        if (block == null || (dx == 0 && dy == 0)) {
            return false;
        }

        int currentBlockX = block.getX();
        int currentBlockY = block.getY();
        int blockWidth = block.getWidth();
        int blockHeight = block.getHeight();

        // 计算棋子移动后的新区域的左上角坐标
        int newX = currentBlockX + dx;
        int newY = currentBlockY + dy;

        // 1. 检查移动是否会超出棋盘边界
        if (newX < 0 || (newX + blockWidth) > board.getWidth() ||
                newY < 0 || (newY + blockHeight) > board.getHeight()) {
            return false;
        }

        // 2. 检查目标位置是否被其他棋子占据
        // 遍历棋子将要移动到的区域所覆盖的每一个格子
        // dx, dy 代表的是单步移动的距离
        if (dx > 0) { // 向右移动
            for (int r = 0; r < blockHeight; r++) { // 检查棋子右侧的每个格子
                int checkX = currentBlockX + blockWidth; // 棋子当前最右侧格子的右边一格
                int checkY = currentBlockY + r;
                for (int step = 1; step <= dx; step++) { // 检查移动路径上的所有格子
                    int pathX = currentBlockX + blockWidth -1 + step; // 应该检查棋子前端将要占据的格子
                    if (board.getBlockIdAt(pathX, checkY) != Board.EMPTY_CELL_ID) return false;
                }
            }
        } else if (dx < 0) { // 向左移动
            for (int r = 0; r < blockHeight; r++) { // 检查棋子左侧的每个格子
                int checkX = currentBlockX -1;       // 棋子当前最左侧格子的左边一格
                int checkY = currentBlockY + r;
                for (int step = 1; step <= -dx; step++) {
                    int pathX = currentBlockX - step;
                    if (board.getBlockIdAt(pathX, checkY) != Board.EMPTY_CELL_ID) return false;
                }
            }
        } else if (dy > 0) { // 向下移动
            for (int c = 0; c < blockWidth; c++) { // 检查棋子下方的每个格子
                int checkX = currentBlockX + c;
                int checkY = currentBlockY + blockHeight; // 棋子当前最下方格子的下边一格
                for (int step = 1; step <= dy; step++) {
                    int pathY = currentBlockY + blockHeight -1 + step;
                    if (board.getBlockIdAt(checkX, pathY) != Board.EMPTY_CELL_ID) return false;
                }
            }
        } else if (dy < 0) { // 向上移动
            for (int c = 0; c < blockWidth; c++) { // 检查棋子上方的每个格子
                int checkX = currentBlockX + c;
                int checkY = currentBlockY - 1;      // 棋子当前最上方格子的上边一格
                for (int step = 1; step <= -dy; step++) {
                    int pathY = currentBlockY - step;
                    if (board.getBlockIdAt(checkX, pathY) != Board.EMPTY_CELL_ID) return false;
                }
            }
        }
        return true;
    }
    // 实现逻辑与GameLogic当中的类似，需要输入一个棋盘状态，你想要移动的块，移动的方向和距离，
    // 若能移动则返回一个true，否则则返回false
}
