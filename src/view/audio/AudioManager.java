package view.audio;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 音频管理器类
 * 用于管理背景音乐(BGM)和音效(SFX)
 */
public class AudioManager {
    // 单例模式
    private static AudioManager instance;
    
    // 音频类型
    public enum AudioType {
        BGM,            // 背景音乐
        SFX_PIECE,      // 棋子移动音效
        SFX_UI          // UI交互音效
    }
    
    // 音频缓存
    private Map<String, Clip> audioClips = new HashMap<>();
    
    // 当前播放的背景音乐
    private Clip currentBGM;
    private String currentBGMPath;
    
    // 音量设置 (0.0 - 1.0)
    private float masterVolume = 1.0f;
    private float bgmVolume = 0.5f;
    private float sfxPieceVolume = 0.8f;
    private float sfxUIVolume = 0.8f;
    
    // 线程池，用于异步播放音频
    private ExecutorService audioExecutor = Executors.newCachedThreadPool();
    
    // 默认音频文件路径
    private String defaultBgmPath = null;
    private String defaultPieceMovePath = null;
    private String defaultButtonClickPath = null;
    private String defaultButtonHoverPath = null;
    
    // 是否启用音频系统
    private boolean audioEnabled = true;
    
    // 音频加载错误计数器
    private int errorCount = 0;
    private static final int MAX_ERROR_COUNT = 5; // 最大错误次数，超过此值将禁用音频系统
    
    /**
     * 私有构造函数，防止外部实例化
     */
    private AudioManager() {
        // 初始化音频系统
        try {
            // 尝试获取一个音频行，检查音频系统是否可用
            AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(Clip.class, format);
            
            if (!AudioSystem.isLineSupported(info)) {
                System.err.println("警告: 音频系统不支持所需的音频格式，音频功能将被禁用");
                audioEnabled = false;
            }
        } catch (Exception e) {
            System.err.println("警告: 初始化音频系统时出错，音频功能将被禁用");
            e.printStackTrace();
            audioEnabled = false;
        }
    }
    
    /**
     * 获取单例实例
     * @return AudioManager实例
     */
    public static synchronized AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }
    
    /**
     * 加载音频文件
     * @param filePath 音频文件路径
     * @return 音频剪辑对象
     */
    private Clip loadAudio(String filePath) {
        // 如果音频系统被禁用，直接返回null
        if (!audioEnabled) {
            return null;
        }
        
        try {
            // 检查缓存
            if (audioClips.containsKey(filePath)) {
                Clip clip = audioClips.get(filePath);
                clip.setFramePosition(0); // 重置到开始位置
                return clip;
            }
            
            // 加载音频文件
            File audioFile = new File(filePath);
            if (!audioFile.exists()) {
                System.err.println("音频文件不存在: " + filePath);
                incrementErrorCount();
                return null;
            }
            
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            
            // 缓存音频剪辑
            audioClips.put(filePath, clip);
            
            return clip;
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("加载音频文件失败: " + e.getMessage());
            e.printStackTrace();
            incrementErrorCount();
            return null;
        }
    }
    
    /**
     * 增加错误计数，如果错误过多则禁用音频系统
     */
    private void incrementErrorCount() {
        errorCount++;
        if (errorCount >= MAX_ERROR_COUNT) {
            System.err.println("警告: 音频加载错误次数过多，音频系统将被禁用");
            audioEnabled = false;
        }
    }
    
    /**
     * 设置音量
     * @param clip 音频剪辑
     * @param volume 音量 (0.0 - 1.0)
     */
    private void setClipVolume(Clip clip, float volume) {
        if (clip == null) return;
        
        try {
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            // 将线性音量 (0.0 - 1.0) 转换为分贝 (-80.0 - 6.0)
            float dB = (float) (Math.log10(Math.max(0.0001, volume)) * 20.0);
            gainControl.setValue(Math.max(-80.0f, Math.min(6.0f, dB)));
        } catch (Exception e) {
            System.err.println("设置音量失败: " + e.getMessage());
        }
    }
    
    /**
     * 播放背景音乐
     * @param filePath 音频文件路径
     */
    public void playBGM(String filePath) {
        if (!audioEnabled) return;
        
        audioExecutor.submit(() -> {
            try {
            // 如果当前有BGM在播放，先淡出
            if (currentBGM != null && currentBGM.isRunning()) {
                fadeOutBGM();
            }
            
            // 加载新的BGM
            Clip clip = loadAudio(filePath);
            if (clip == null) return;
            
            // 设置音量
            setClipVolume(clip, masterVolume * bgmVolume);
            
            // 设置循环播放
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            
            // 开始播放
            clip.start();
            
            // 更新当前BGM
            currentBGM = clip;
            currentBGMPath = filePath;
            } catch (Exception e) {
                System.err.println("播放背景音乐失败: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    /**
     * 停止背景音乐
     */
    public void stopBGM() {
        if (!audioEnabled || currentBGM == null) return;
        
        try {
            currentBGM.stop();
            currentBGM.setFramePosition(0);
        } catch (Exception e) {
            System.err.println("停止背景音乐失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 暂停背景音乐
     */
    public void pauseBGM() {
        if (!audioEnabled || currentBGM == null || !currentBGM.isRunning()) return;
        
        try {
            currentBGM.stop();
        } catch (Exception e) {
            System.err.println("暂停背景音乐失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 恢复播放背景音乐
     */
    public void resumeBGM() {
        if (!audioEnabled || currentBGM == null || currentBGM.isRunning()) return;
        
        try {
            currentBGM.start();
        } catch (Exception e) {
            System.err.println("恢复背景音乐失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 淡出背景音乐
     */
    private void fadeOutBGM() {
        if (!audioEnabled || currentBGM == null || !currentBGM.isRunning()) return;
        
        // 创建一个新线程来处理淡出效果
        Thread fadeOutThread = new Thread(() -> {
            try {
                FloatControl gainControl = (FloatControl) currentBGM.getControl(FloatControl.Type.MASTER_GAIN);
                float currentVolume = gainControl.getValue();
                float minVolume = -80.0f; // 最小音量 (dB)
                
                // 在500毫秒内淡出
                int steps = 20;
                float deltaVolume = (currentVolume - minVolume) / steps;
                int sleepTime = 500 / steps;
                
                for (int i = 0; i < steps; i++) {
                    currentVolume -= deltaVolume;
                    gainControl.setValue(Math.max(minVolume, currentVolume));
                    Thread.sleep(sleepTime);
                }
                
                // 停止播放
                currentBGM.stop();
                currentBGM.setFramePosition(0);
                
                // 恢复音量设置
                setClipVolume(currentBGM, masterVolume * bgmVolume);
            } catch (Exception e) {
                System.err.println("淡出背景音乐失败: " + e.getMessage());
            }
        });
        
        fadeOutThread.start();
        try {
            fadeOutThread.join(600); // 等待淡出完成，最多等待600毫秒
        } catch (InterruptedException e) {
            System.err.println("等待淡出线程被中断: " + e.getMessage());
        }
    }
    
    /**
     * 设置背景音乐音量
     * @param volume 音量 (0.0 - 1.0)
     */
    public void setBGMVolume(float volume) {
        this.bgmVolume = Math.max(0.0f, Math.min(1.0f, volume));
        if (audioEnabled && currentBGM != null) {
            setClipVolume(currentBGM, masterVolume * bgmVolume);
        }
    }
    
    /**
     * 播放棋子移动音效
     * @param filePath 音频文件路径
     */
    public void playPieceMoveSound(String filePath) {
        if (!audioEnabled) return;
        
        if (filePath == null && defaultPieceMovePath != null) {
            filePath = defaultPieceMovePath;
        }
        
        if (filePath == null) {
            System.err.println("未设置棋子移动音效文件路径");
            return;
        }
        
        final String path = filePath;
        audioExecutor.submit(() -> {
            try {
                Clip clip = loadAudio(path);
            if (clip == null) return;
            
            setClipVolume(clip, masterVolume * sfxPieceVolume);
            clip.start();
            } catch (Exception e) {
                System.err.println("播放棋子移动音效失败: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    /**
     * 设置棋子移动音效音量
     * @param volume 音量 (0.0 - 1.0)
     */
    public void setPieceMoveVolume(float volume) {
        this.sfxPieceVolume = Math.max(0.0f, Math.min(1.0f, volume));
    }
    
    /**
     * 播放按钮悬停音效
     * @param filePath 音频文件路径
     */
    public void playButtonHoverSound(String filePath) {
        if (!audioEnabled) return;
        
        if (filePath == null && defaultButtonHoverPath != null) {
            filePath = defaultButtonHoverPath;
        }
        
        if (filePath == null) {
            System.err.println("未设置按钮悬停音效文件路径");
            return;
        }
        
        final String path = filePath;
        audioExecutor.submit(() -> {
            try {
                Clip clip = loadAudio(path);
            if (clip == null) return;
            
            setClipVolume(clip, masterVolume * sfxUIVolume);
            clip.start();
            } catch (Exception e) {
                System.err.println("播放按钮悬停音效失败: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    /**
     * 播放按钮点击音效
     * @param filePath 音频文件路径
     */
    public void playButtonClickSound(String filePath) {
        if (!audioEnabled) return;
        
        if (filePath == null && defaultButtonClickPath != null) {
            filePath = defaultButtonClickPath;
        }
        
        if (filePath == null) {
            System.err.println("未设置按钮点击音效文件路径");
            return;
        }
        
        final String path = filePath;
        audioExecutor.submit(() -> {
            try {
                Clip clip = loadAudio(path);
            if (clip == null) return;
            
            setClipVolume(clip, masterVolume * sfxUIVolume);
            clip.start();
            } catch (Exception e) {
                System.err.println("播放按钮点击音效失败: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    /**
     * 设置UI音效音量
     * @param volume 音量 (0.0 - 1.0)
     */
    public void setUIVolume(float volume) {
        this.sfxUIVolume = Math.max(0.0f, Math.min(1.0f, volume));
    }
    
    /**
     * 设置主音量
     * @param volume 音量 (0.0 - 1.0)
     */
    public void setMasterVolume(float volume) {
        this.masterVolume = Math.max(0.0f, Math.min(1.0f, volume));
        
        // 更新当前播放的BGM音量
        if (audioEnabled && currentBGM != null) {
            setClipVolume(currentBGM, masterVolume * bgmVolume);
        }
    }
    
    /**
     * 设置是否启用音频系统
     * @param enabled 是否启用
     */
    public void setAudioEnabled(boolean enabled) {
        // 如果从启用变为禁用，停止所有音频
        if (this.audioEnabled && !enabled) {
            stopAllAudio();
        }
        
        this.audioEnabled = enabled;
        
        // 重置错误计数
        if (enabled) {
            errorCount = 0;
        }
    }
    
    /**
     * 停止所有音频
     */
    private void stopAllAudio() {
        // 停止背景音乐
        stopBGM();
        
        // 停止所有音效
        for (Clip clip : audioClips.values()) {
            if (clip.isRunning()) {
                clip.stop();
                clip.setFramePosition(0);
            }
        }
    }
    
    /**
     * 检查音频系统是否启用
     * @return 是否启用
     */
    public boolean isAudioEnabled() {
        return audioEnabled;
    }
    
    /**
     * 获取默认背景音乐路径
     * @return 默认背景音乐路径
     */
    public String getDefaultBgmPath() {
        return defaultBgmPath;
    }
    
    /**
     * 获取默认棋子移动音效路径
     * @return 默认棋子移动音效路径
     */
    public String getDefaultPieceMovePath() {
        return defaultPieceMovePath;
    }
    
    /**
     * 获取默认按钮点击音效路径
     * @return 默认按钮点击音效路径
     */
    public String getDefaultButtonClickPath() {
        return defaultButtonClickPath;
    }
    
    /**
     * 获取默认按钮悬停音效路径
     * @return 默认按钮悬停音效路径
     */
    public String getDefaultButtonHoverPath() {
        return defaultButtonHoverPath;
    }
    
    /**
     * 设置默认背景音乐路径
     * @param filePath 音频文件路径
     */
    public void setDefaultBgmPath(String filePath) {
        this.defaultBgmPath = filePath;
    }
    
    /**
     * 设置默认棋子移动音效路径
     * @param filePath 音频文件路径
     */
    public void setDefaultPieceMovePath(String filePath) {
        this.defaultPieceMovePath = filePath;
    }
    
    /**
     * 设置默认按钮点击音效路径
     * @param filePath 音频文件路径
     */
    public void setDefaultButtonClickPath(String filePath) {
        this.defaultButtonClickPath = filePath;
    }
    
    /**
     * 设置默认按钮悬停音效路径
     * @param filePath 音频文件路径
     */
    public void setDefaultButtonHoverPath(String filePath) {
        this.defaultButtonHoverPath = filePath;
    }
    
    /**
     * 播放默认背景音乐
     */
    public void playDefaultBGM() {
        if (!audioEnabled) return;
        
        if (defaultBgmPath != null) {
            playBGM(defaultBgmPath);
        } else {
            System.err.println("未设置默认背景音乐文件路径");
        }
    }
    
    /**
     * 播放默认棋子移动音效
     */
    public void playDefaultPieceMoveSound() {
        if (!audioEnabled) return;
        
        playPieceMoveSound(defaultPieceMovePath);
    }
    
    /**
     * 播放默认按钮点击音效
     */
    public void playDefaultButtonClickSound() {
        if (!audioEnabled) return;
        
        playButtonClickSound(defaultButtonClickPath);
    }
    
    /**
     * 播放默认按钮悬停音效
     */
    public void playDefaultButtonHoverSound() {
        if (!audioEnabled) return;
        
        playButtonHoverSound(defaultButtonHoverPath);
    }
    
    /**
     * 释放资源
     */
    public void dispose() {
        // 停止所有音频
        stopAllAudio();
        
        // 关闭所有音频剪辑
        for (Clip clip : audioClips.values()) {
            clip.close();
        }
        
        // 清空缓存
        audioClips.clear();
        
        // 关闭线程池
        audioExecutor.shutdown();
    }
} 