### 胜利界面背景图片说明

为了在游戏胜利界面显示自定义背景图片，请按照以下方式操作：

1. 将您的背景图片放在此目录下（src/view/frontend/resourses/）
2. 根据不同关卡需要，图片命名如下：
   - 第一关：victory_background.jpg
   - 第二关：victory_background2.jpg
   - 第三关：victory_background3.jpg
   - 第四关：victory_background4.jpg

3. 支持的图片格式：jpg, png, gif
4. 图片尺寸没有严格限制，系统会自动进行等比例缩放，但为了最佳显示效果，推荐使用：
   - 宽度至少600像素
   - 高度至少400像素
   - 建议使用接近3:2比例的图片以获得最佳效果

注意：
- 如果没有找到对应的图片，胜利界面将使用默认的蓝色背景
- 非3:2比例的图片会自动进行等比例缩放，超出部分可能会被裁剪

您也可以在代码中修改要使用的图片文件名：
- 在每个GameFrame的checkAndShowWinDialog方法中，修改VictoryFrame.showVictory的最后一个参数。 