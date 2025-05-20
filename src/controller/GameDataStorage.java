package controller;

import java.io.*;

/*
* 这个类是一个标准的工具类，提供了两个公用的静态方法，分别实现了保存数据saveGameToFile()和加载数据loadGameFromFile()两个功能
 */


public class GameDataStorage {
    public static boolean saveGameToFile(GameState gameState, File file) {
        if (gameState == null || file == null) {
            System.out.println("Error saving game: The GameState or File object is null.");
            return false;
        }

        try (FileOutputStream fileOut = new FileOutputStream(file);
             ObjectOutputStream objectOut = new ObjectOutputStream(fileOut)) {

            objectOut.writeObject(gameState);
            //这里序列化 GameState 对象
            System.out.println("Game successfully saved to: " + file.getAbsolutePath());
            return true;
        } catch (IOException e) {
            System.out.println("When the data is saving to " + file.getAbsolutePath() + ", something goes wrong: " + e.getMessage());
            e.printStackTrace();
            //这段代码是输入错误信息
            return false;
        }
    }


    public static GameState loadGameFromFile(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            String path = (file != null) ? file.getAbsolutePath() : "null";
            System.out.println("Game loading error: The file is empty, does not exist, or is not a standard file. Path: " + path);
            return null;
        }

        try (FileInputStream fileIn = new FileInputStream(file);
             ObjectInputStream objectIn = new ObjectInputStream(fileIn)) {

            GameState loadedGameState = (GameState) objectIn.readObject();
            // 反序列化对象
            System.out.println("The game successfully loaded from the following path: " + file.getAbsolutePath());
            return loadedGameState;

        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            // IOException: 文件读写错误
            // ClassNotFoundException: 如果保存的类定义与当前加载的类不兼容
            // ClassCastException: 如果文件内容不是一个 GameState 对象
            System.out.println("When we get data from " + file.getAbsolutePath() + "something goes wrong: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}