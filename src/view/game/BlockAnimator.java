package view.game;

import controller.Block;
import controller.Direction;
// Import other Block and Direction types
import controller2.Block2;
import controller2.Direction2;
import controller3.Block3;
import controller3.Direction3;
import controller4.Block4;
import controller4.Direction4;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.Timer;

/**
 * 棋子动画管理器，负责处理棋子移动的平滑动画效果
 */
public class BlockAnimator {
    // 存储每个棋子ID对应的动画状态
    private final Map<Integer, AnimationState> animationStates = new ConcurrentHashMap<>();
    
    // 新增一个集合，用于存储已完成但等待模型更新确认的动画的棋子ID
    private final Map<Integer, AnimationState> completedButPendingConfirmation = new ConcurrentHashMap<>();
    
    // 动画持续时间（毫秒）
    private static final int ANIMATION_DURATION = 200;
    
    // 动画刷新频率（毫秒）
    private static final int ANIMATION_REFRESH_RATE = 16; // 约60FPS
    
    // 动画计时器
    private Timer animationTimer;
    
    // 需要重绘界面的回调
    private Runnable repaintCallback;
    
    // 动画结束后的回调
    private Runnable animationCompleteCallback;
    
    // 是否有动画正在进行
    private boolean animating = false;
    
    /**
     * 构造函数
     * @param repaintCallback 重绘界面的回调函数
     */
    public BlockAnimator(Runnable repaintCallback) {
        this.repaintCallback = repaintCallback;
        
        // 初始化动画计时器
        animationTimer = new Timer(ANIMATION_REFRESH_RATE, e -> {
            updateAnimations();
            if (repaintCallback != null) {
                repaintCallback.run();
            }
        });
    }
    
    /**
     * 设置动画完成后的回调
     * @param callback 动画完成后的回调函数
     */
    public void setAnimationCompleteCallback(Runnable callback) {
        this.animationCompleteCallback = callback;
    }
    
    /**
     * 开始一个棋子的移动动画
     * @param block 要移动的棋子
     * @param direction 移动方向
     * @param pieceImageForAnimation 预取的图像
     */
    public void animateBlockMove(Block block, Direction direction, Image pieceImageForAnimation) {
        if (block == null || direction == null) {
            return;
        }
        
        // 计算目标位置
        int startX = block.getX();
        int startY = block.getY();
        int targetX = startX + direction.getDx();
        int targetY = startY + direction.getDy();
        
        // 创建动画状态
        AnimationState state = new AnimationState(
            block.getId(),
            startX, startY,
            targetX, targetY,
            System.currentTimeMillis(),
            pieceImageForAnimation // 存储预取的图像
        );
        
        // 存储动画状态
        animationStates.put(block.getId(), state);
        
        // 如果动画计时器未启动，则启动它
        if (!animationTimer.isRunning()) {
            animating = true;
            animationTimer.start();
        }
    }
    
    // Overload for Block2
    public void animateBlockMove(Block2 block, Direction2 direction, Image pieceImageForAnimation) {
        if (block == null || direction == null) {
            return;
        }
        int startX = block.getX();
        int startY = block.getY();
        int targetX = startX + direction.getDx();
        int targetY = startY + direction.getDy();
        AnimationState state = new AnimationState(
            block.getId(), startX, startY, targetX, targetY, System.currentTimeMillis(), pieceImageForAnimation);
        animationStates.put(block.getId(), state);
        if (!animationTimer.isRunning()) {
            animating = true;
            animationTimer.start();
        }
    }
    
    // Overload for Block3
    public void animateBlockMove(Block3 block, Direction3 direction, Image pieceImageForAnimation) {
        if (block == null || direction == null) {
            return;
        }
        int startX = block.getX();
        int startY = block.getY();
        int targetX = startX + direction.getDx();
        int targetY = startY + direction.getDy();
        AnimationState state = new AnimationState(
            block.getId(), startX, startY, targetX, targetY, System.currentTimeMillis(), pieceImageForAnimation);
        animationStates.put(block.getId(), state);
        if (!animationTimer.isRunning()) {
            animating = true;
            animationTimer.start();
        }
    }
    
    // Overload for Block4
    public void animateBlockMove(Block4 block, Direction4 direction, Image pieceImageForAnimation) {
        if (block == null || direction == null) {
            return;
        }
        int startX = block.getX();
        int startY = block.getY();
        int targetX = startX + direction.getDx();
        int targetY = startY + direction.getDy();
        AnimationState state = new AnimationState(
            block.getId(), startX, startY, targetX, targetY, System.currentTimeMillis(), pieceImageForAnimation);
        animationStates.put(block.getId(), state);
        if (!animationTimer.isRunning()) {
            animating = true;
            animationTimer.start();
        }
    }
    
    /**
     * 更新所有正在进行的动画
     */
    private void updateAnimations() {
        long currentTime = System.currentTimeMillis();
        boolean activeAnimationsRemain = false;

        // 遍历当前活跃的动画
        for (Map.Entry<Integer, AnimationState> entry : animationStates.entrySet()) {
            AnimationState state = entry.getValue();
            if (state.complete) { // 如果之前已被标记完成，则跳过，等待被移入completedButPendingConfirmation
                activeAnimationsRemain = true; // 只要还在animationStates里，就认为活跃（即使是等待确认的）
                continue;
            }

            long elapsedTime = currentTime - state.startTime;
            if (elapsedTime >= ANIMATION_DURATION) {
                state.progress = 1.0f;
                state.complete = true; // 标记为完成
                // 不立即从animationStates移除，而是等待移到completedButPendingConfirmation
            }
            else {
                state.progress = (float) elapsedTime / ANIMATION_DURATION;
            }
            activeAnimationsRemain = true;
        }

        // 将已完成的动画从 animationStates 移动到 completedButPendingConfirmation
        // 并检查是否所有动画（在animationStates里的）都已经移走或完成
        boolean allMovedToPending = true;
        if (activeAnimationsRemain) { // 仅当之前有活跃动画时才检查
            for (Map.Entry<Integer, AnimationState> entry : animationStates.entrySet()) {
                AnimationState state = entry.getValue();
                if (state.complete) {
                    // 将完成的state对象直接移动，因为它包含了targetX, targetY
                    completedButPendingConfirmation.put(entry.getKey(), state);
                }
            }
            // 从 animationStates 中移除已移至 completedButPendingConfirmation 的项
            completedButPendingConfirmation.keySet().forEach(animationStates::remove);

            // 重新检查 animationStates 是否为空，以确定是否可以停止计时器（如果外部没有正在pending的）
            if (!animationStates.isEmpty()) {
                allMovedToPending = false; // 还有未完成的动画在animationStates中
            }
        }


        // 如果 animationStates 为空 (所有动画都已完成并移至待确认区)
        // 并且 completedButPendingConfirmation 也为空 (所有已完成的动画都已被外部确认和清理)
        // 则可以安全地停止计时器。
        // animating 标志现在应该由外部的 finalizeAllAnimations 控制，或者当两个map都为空时
        if (animationStates.isEmpty() && completedButPendingConfirmation.isEmpty()) {
            if (animationTimer.isRunning()) {
                animationTimer.stop();
                animating = false; // 确实没有动画在跑了
            }
            // 此处不再自动调用 animationCompleteCallback，它应该在所有动画首次到达completed状态时被触发
        }

        // 如果有动画首次达到完成状态，并且之前没有其他正在等待确认的动画，则调用回调
        // 回调应该在所有当前批次的动画都转移到pending时触发一次
        // 或者，回调的意义变为 "有一批动画完成了视觉表现"
        // 我们修改为：当 animationStates 变为空，但 completedButPendingConfirmation 不为空时，
        // 暗示一批动画刚刚完成其视觉部分。
        if (animationStates.isEmpty() && !completedButPendingConfirmation.isEmpty() && activeAnimationsRemain) {
            // ^ activeAnimationsRemain 确保这是刚刚清空 animationStates 的结果
            if (animationCompleteCallback != null) {
                animationCompleteCallback.run(); // 通知外部，有一批动画的视觉部分已完成
            }
        }
    }
    
    /**
     * 当外部逻辑确认一个或多个棋子的模型数据已更新后，调用此方法。
     * @param blockId 要最终确定动画状态的棋子ID。
     */
    public void finalizeAnimation(int blockId) {
        completedButPendingConfirmation.remove(blockId);
        // 如果两个map都为空了，确保计时器停止
        if (animationStates.isEmpty() && completedButPendingConfirmation.isEmpty()) {
            if (animationTimer.isRunning()) {
                animationTimer.stop();
            }
            animating = false;
        }
    }

    /**
     * 当外部逻辑确认所有当前批次的已完成动画的模型数据都已更新后，调用此方法。
     */
    public void finalizeAllPendingAnimations() {
        completedButPendingConfirmation.clear();
        if (animationTimer.isRunning()) {
             animationTimer.stop(); // 因为所有待确认的都没了，活跃的之前也已经处理完
        }
        animating = false; // 确认所有动画结束
    }
    
    /**
     * 获取指定棋子的当前绘制位置（考虑动画）
     * @param block 棋子对象
     * @param cellSize 棋盘单元格大小
     * @param offsetX 棋盘X偏移
     * @param offsetY 棋盘Y偏移
     * @return 棋子的绘制位置和大小
     */
    public Rectangle getAnimatedBlockBounds(Block block, int cellSize, int offsetX, int offsetY) {
        if (block == null) {
            return null;
        }
        
        int blockId = block.getId();
        AnimationState state = animationStates.get(blockId); // 首先检查活跃动画
        
        if (state == null) { // 如果不在活跃动画中，检查是否在"已完成但待确认"中
            state = completedButPendingConfirmation.get(blockId);
        }
        
        // 如果该棋子没有任何动画状态 (既不活跃也不在待确认列表)
        if (state == null) {
            int x = offsetX + block.getX() * cellSize;
            int y = offsetY + block.getY() * cellSize;
            int width = block.getWidth() * cellSize;
            int height = block.getHeight() * cellSize;
            return new Rectangle(x, y, width, height);
        }
        
        // 如果动画状态标记为完成 (无论是在 animationStates 还是 completedButPendingConfirmation)
        // 则绘制在动画的最终目标位置
        if (state.complete) { // 'complete' 标志在 AnimationState 对象中
            int pixelX = offsetX + Math.round(state.targetX * cellSize); 
            int pixelY = offsetY + Math.round(state.targetY * cellSize); 
            int width = block.getWidth() * cellSize;
            int height = block.getHeight() * cellSize;
            return new Rectangle(pixelX, pixelY, width, height);
        }
        
        // 动画正在进行中，计算插值位置
        float progress = state.progress;
        progress = easeInOutQuad(progress); // 应用缓动函数
        
        float currentX = state.startX + (state.targetX - state.startX) * progress;
        float currentY = state.startY + (state.targetY - state.startY) * progress;
        
        int pixelX = offsetX + Math.round(currentX * cellSize);
        int pixelY = offsetY + Math.round(currentY * cellSize);
        int width = block.getWidth() * cellSize;
        int height = block.getHeight() * cellSize;
        
        return new Rectangle(pixelX, pixelY, width, height);
    }
    
    // Overload for Block2
    public Rectangle getAnimatedBlockBounds(Block2 block, int cellSize, int offsetX, int offsetY) {
        if (block == null) return null;
        int blockId = block.getId();
        AnimationState state = animationStates.get(blockId); 
        if (state == null) state = completedButPendingConfirmation.get(blockId);
        if (state == null) {
            return new Rectangle(offsetX + block.getX() * cellSize, offsetY + block.getY() * cellSize, block.getWidth() * cellSize, block.getHeight() * cellSize);
        }
        if (state.complete) {
            return new Rectangle(offsetX + Math.round(state.targetX * cellSize), offsetY + Math.round(state.targetY * cellSize), block.getWidth() * cellSize, block.getHeight() * cellSize);
        }
        float progress = easeInOutQuad(state.progress);
        float currentX = state.startX + (state.targetX - state.startX) * progress;
        float currentY = state.startY + (state.targetY - state.startY) * progress;
        return new Rectangle(offsetX + Math.round(currentX * cellSize), offsetY + Math.round(currentY * cellSize), block.getWidth() * cellSize, block.getHeight() * cellSize);
    }

    // Overload for Block3
    public Rectangle getAnimatedBlockBounds(Block3 block, int cellSize, int offsetX, int offsetY) {
        if (block == null) return null;
        int blockId = block.getId();
        AnimationState state = animationStates.get(blockId);
        if (state == null) state = completedButPendingConfirmation.get(blockId);
        if (state == null) {
            return new Rectangle(offsetX + block.getX() * cellSize, offsetY + block.getY() * cellSize, block.getWidth() * cellSize, block.getHeight() * cellSize);
        }
        if (state.complete) {
            return new Rectangle(offsetX + Math.round(state.targetX * cellSize), offsetY + Math.round(state.targetY * cellSize), block.getWidth() * cellSize, block.getHeight() * cellSize);
        }
        float progress = easeInOutQuad(state.progress);
        float currentX = state.startX + (state.targetX - state.startX) * progress;
        float currentY = state.startY + (state.targetY - state.startY) * progress;
        return new Rectangle(offsetX + Math.round(currentX * cellSize), offsetY + Math.round(currentY * cellSize), block.getWidth() * cellSize, block.getHeight() * cellSize);
    }

    // Overload for Block4
    public Rectangle getAnimatedBlockBounds(Block4 block, int cellSize, int offsetX, int offsetY) {
        if (block == null) return null;
        int blockId = block.getId();
        AnimationState state = animationStates.get(blockId);
        if (state == null) state = completedButPendingConfirmation.get(blockId);
        if (state == null) {
            return new Rectangle(offsetX + block.getX() * cellSize, offsetY + block.getY() * cellSize, block.getWidth() * cellSize, block.getHeight() * cellSize);
        }
        if (state.complete) {
            return new Rectangle(offsetX + Math.round(state.targetX * cellSize), offsetY + Math.round(state.targetY * cellSize), block.getWidth() * cellSize, block.getHeight() * cellSize);
        }
        float progress = easeInOutQuad(state.progress);
        float currentX = state.startX + (state.targetX - state.startX) * progress;
        float currentY = state.startY + (state.targetY - state.startY) * progress;
        return new Rectangle(offsetX + Math.round(currentX * cellSize), offsetY + Math.round(currentY * cellSize), block.getWidth() * cellSize, block.getHeight() * cellSize);
    }
    
    /**
     * 二次缓动函数，使动画更加自然
     * @param t 进度 (0.0 - 1.0)
     * @return 缓动后的进度值
     */
    private float easeInOutQuad(float t) {
        return t < 0.5f ? 2 * t * t : 1 - (float)Math.pow(-2 * t + 2, 2) / 2;
    }
    
    /**
     * 判断是否有动画正在进行
     * @return 是否有动画正在进行
     */
    public boolean isAnimating() {
        return animating;
    }
    
    /**
     * 获取指定棋子的当前动画状态对象。
     * 主要供外部（如GamePanel）查询动画相关的特定信息（如预取图像）。
     * @param blockId 棋子ID
     * @return 如果该棋子正在动画或等待确认，则返回其AnimationState；否则返回null。
     */
    public AnimationState getAnimationState(int blockId) {
        AnimationState state = animationStates.get(blockId);
        if (state == null) {
            state = completedButPendingConfirmation.get(blockId);
        }
        return state;
    }
    
    /**
     * 判断特定棋子是否当前正在进行活跃的（未完成的）动画。
     * @param blockId 棋子ID
     * @return 如果棋子在animationStates中且未完成，则返回true；否则false。
     */
    public boolean isBlockAnimating(int blockId) {
        AnimationState state = animationStates.get(blockId);
        return state != null && !state.complete;
    }
    
    /**
     * 取消所有正在进行的动画
     */
    public void cancelAnimations() {
        if (animationTimer.isRunning()) {
            animationTimer.stop();
        }
        animationStates.clear();
        completedButPendingConfirmation.clear(); // 同时清除等待确认的动画
        animating = false;
    }
    
    /**
     * 动画状态类，存储一个棋子的动画信息
     */
    public static class AnimationState {
        final int blockId;      // 棋子ID
        final float startX;     // 起始X坐标
        final float startY;     // 起始Y坐标
        final float targetX;    // 目标X坐标
        final float targetY;    // 目标Y坐标
        final long startTime;   // 动画开始时间
        float progress;         // 动画进度 (0.0 - 1.0)
        boolean complete;       // 动画是否完成
        Image pieceImageForAnimation; // 新增：用于此动画的特定图像实例
        
        AnimationState(int blockId, float startX, float startY, float targetX, float targetY, long startTime, Image pieceImage) {
            this.blockId = blockId;
            this.startX = startX;
            this.startY = startY;
            this.targetX = targetX;
            this.targetY = targetY;
            this.startTime = startTime;
            this.progress = 0.0f;
            this.complete = false;
            this.pieceImageForAnimation = pieceImage; // 初始化图像
        }
    }
} 