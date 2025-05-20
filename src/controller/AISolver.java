package controller;

import java.util.List;
//还需后续添加
public interface AISolver {
    List<MoveRecord> solve(Board board); // 返回一系列移动步骤
}
