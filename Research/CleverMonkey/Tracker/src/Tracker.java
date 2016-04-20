package CleverMonkey.Tracker;

import java.awt.image.BufferedImage;
import java.awt.Point;
import java.awt.Color;
import java.awt.Graphics;

/**
 * CMTracker类用于根据图像输入给出运动控制目标信息。
 */
public class Tracker {
        // 结果类型枚举。

        public enum ResultType {
                Unk, Run, Stop
        }

        // 模式图高度。
        protected final int k_patternHeight = 160;
        // 模式图宽度。
        protected final int k_patternWidth = 90;
        // 判别颜色上限。
        protected final Color k_appointedColorMax = new Color(80, 80, 80);
        // 判别颜色下限。
        protected final Color k_appointedColorMin = new Color(0, 0, 0);
        // 路径线宽下限。
        protected final int k_trackWidthMin = 20;
        // 缺点间隙容限。
        protected final int k_gapMax = 1;
        // 停止线最小宽度。
        protected final int k_stopLineWidthMin = 80;
        // 停止指令所需的最小连续停止行数目。
        protected final int k_stopLineCounterMin = 50;
        // 运动控制目标判别基线
        protected final int k_baseLine = 40;

        // 传入的原图。
        protected BufferedImage m_originalImg;
        // Alpha模式图图像。
        protected BufferedImage m_alphaImg = new BufferedImage(k_patternWidth,
                                                               k_patternHeight, BufferedImage.TYPE_INT_RGB);
        // Beta模式图图像。
        protected BufferedImage m_betaImg = new BufferedImage(k_patternWidth,
                                                              k_patternHeight, BufferedImage.TYPE_INT_RGB);
        // Alpha模式图数组。
        protected boolean[][] m_patternAlpha = new boolean[k_patternWidth][k_patternHeight];
        // Beta模式图数组。
        protected boolean[][] m_patternBeta = new boolean[k_patternWidth][k_patternHeight];
        // 有效行标记。记录对应行是否有效。
        // 有效行定义：已识别到路径线点的行，即不需计算拟合点的行。
        protected boolean[] m_isValidLine = new boolean[k_patternHeight];
        // 停止行标记。记录对应行是否为停止标记行。
        // 停止行定义：行有效路径宽度不小于规定的停止标记宽度的行。
        protected boolean[] m_isStopLine = new boolean[k_patternHeight];
        // 模式图到原图的转换系数。
        public int m_patternToImgScale;
        // 结果量-运动控制目标点。该目标点基于自然摄像头坐标系（原点在左上角）。
        protected Point m_targetPoint = new Point(0, 0);
        // 结果量-结果类型。
        protected ResultType m_resultType = ResultType.Unk;

        // 接口。
        // 传入分析图像。
        // 图像必须是16：9的长宽比。
        public ResultType AnalyseImg(BufferedImage img) {
                m_originalImg = img;

                // 工作流程。详参设计文档。
                if (!ToAlpha()) {
                        return ResultType.Unk;
                }
                if (!ToBeta()) {
                        return ResultType.Unk;
                }
                if (!ToGamma()) {
                        return ResultType.Unk;
                }
                if (!ToOmega()) {
                        return ResultType.Unk;
                }
                // Finished.
                return m_resultType;
        }

        // 获取结果量。
        // @return 结果量-运动控制目标点。
        public Point GetResult() {
                return m_targetPoint;
        }
        
        /**
         * @return 欧氏坐标空间的目标点（传感器所在的坐标系）。
         */
        public Point ComputeTargetPoint() {
                return new Point(m_targetPoint.x - m_patternToImgScale*k_patternWidth/2, 
                                 m_patternToImgScale*k_patternHeight - m_targetPoint.y);
        }

        // 以图片对象方式输出原图。
        // @isResultDrawed 是否绘制结果点。
        // @return 原图。
        public BufferedImage GetOriginalImg(boolean isResultDrawed) {

                // 不绘制结果点则直接返回原图。
                if (isResultDrawed == false) {
                        return m_originalImg;
                }

                // 否则补充绘制结果点及基线。
                Graphics graphics = m_originalImg.getGraphics();
                graphics.setColor(Color.GRAY);
                graphics.drawLine(0, k_baseLine, m_originalImg.getWidth() - 1,
                                  k_baseLine);
                graphics.setColor(Color.ORANGE);
                graphics.fillRect(m_targetPoint.x - 15, m_targetPoint.y - 15, 30, 30);
                return m_originalImg;
        }

        // 以图片对象方式输出Alpha模式图。
        // @isResultDrawed 是否绘制结果点。
        // @return Alpha模式图。
        public BufferedImage GetAlphaPatternImg(boolean isResultDrawed) {

                // 逐一判别数组数据并映射填充图片对象中。
                for (int x = 0; x < k_patternWidth; ++x) {
                        for (int y = 0; y < k_patternHeight; ++y) {
                                // 目标点着色为黑色，其它点着色为白色。
                                if (m_patternAlpha[x][y] == true) {
                                        m_alphaImg.setRGB(x, y, Color.BLACK.getRGB());
                                } else {
                                        m_alphaImg.setRGB(x, y, Color.WHITE.getRGB());
                                }
                        }
                }
                // 不绘制结果点则直接返回。
                if (isResultDrawed == false || m_patternToImgScale == 0) {
                        return m_alphaImg;
                }

                // 否则补充绘制结果点及基线。
                Graphics graphics = m_alphaImg.getGraphics();
                graphics.setColor(Color.GRAY);
                graphics.drawLine(0, k_baseLine, k_patternWidth - 1, k_baseLine);
                graphics.setColor(Color.ORANGE);
                graphics.fillRect(m_targetPoint.x / m_patternToImgScale - 1,
                                  m_targetPoint.y / m_patternToImgScale - 1, 3, 3);
                return m_alphaImg;
        }

        // 以图片对象方式输出Beta模式图。
        // @isResultDrawed 是否绘制结果点。
        // @return Beta模式图。
        public BufferedImage GetBetaPatternImg(boolean isResultDrawed) {

                // 逐一判别数组数据并映射填充到新的图片对象中。
                for (int x = 0; x < k_patternWidth; ++x) {
                        for (int y = 0; y < k_patternHeight; ++y) {
                                // 目标点着色为黑色，停止行着色为红色，其它点着色为白色。
                                if (m_patternBeta[x][y] == true) {
                                        if (m_isStopLine[y] == true) {
                                                m_betaImg.setRGB(x, y, Color.RED.getRGB());
                                        } else {
                                                m_betaImg.setRGB(x, y, Color.BLACK.getRGB());
                                        }
                                } else {
                                        m_betaImg.setRGB(x, y, Color.WHITE.getRGB());
                                }
                        }
                }
                // 不绘制结果点则直接返回。
                if (isResultDrawed == false || m_patternToImgScale == 0) {
                        return m_betaImg;
                }

                // 否则补充绘制结果点及基线。
                Graphics graphics = m_betaImg.getGraphics();
                graphics.setColor(Color.GRAY);
                graphics.drawLine(0, k_baseLine, k_patternWidth - 1, k_baseLine);
                graphics.setColor(Color.ORANGE);
                graphics.fillRect(m_targetPoint.x / m_patternToImgScale - 1,
                                  m_targetPoint.y / m_patternToImgScale - 1, 3, 3);
                return m_betaImg;
        }

        // 工具函数。
        // 第一次模式化处理，将原始输入图像处理为黑白模式图。
        // 方法是逐一将一定大小的原图区块计算映射到模式图像素。映射时将自动判别区块颜色是否在目标颜色区间中。
        // 该流程可以过滤色块混淆及噪点干扰。
        protected boolean ToAlpha() {
                Color color;
                // 颜色分量累加器。
                int red = 0;
                int green = 0;
                int blue = 0;

                // 初始化模式图到原图的转换系数。
                m_patternToImgScale = m_originalImg.getHeight() / k_patternHeight;

                for (int x = 0; x < k_patternWidth; ++x) {
                        for (int y = 0; y < k_patternHeight; ++y) {
                                // New Method 两行取样法。
                                // 分别扫描两行的像素，取该两行的所有像素的颜色平均值作为等效区块颜色。借此消除噪点干扰。
                                // 所选择的两行分别向区块边界内偏移两行，即取边界起计的第三行。使样本在原图上的分布更平均。
                                // NOTICE 单一模式图粒子对应区块大小默认不小于3*3像素。
                                for (int i = 0; i < m_patternToImgScale; ++i) {
                                        color = new Color(m_originalImg.getRGB(x
                                                                               * m_patternToImgScale + i, y * m_patternToImgScale
                                                                                                          + 2));
                                        red += color.getRed();
                                        green += color.getGreen();
                                        blue += color.getBlue();

                                        color = new Color(m_originalImg.getRGB(x
                                                                               * m_patternToImgScale + i, (y + 1)
                                                                                                          * m_patternToImgScale - 3));
                                        red += color.getRed();
                                        green += color.getGreen();
                                        blue += color.getBlue();
                                }

                                red /= m_patternToImgScale * 2;
                                green /= m_patternToImgScale * 2;
                                blue /= m_patternToImgScale * 2;

                                if (red >= k_appointedColorMin.getRed()
                                    && red <= k_appointedColorMax.getRed()
                                    && green >= k_appointedColorMin.getGreen()
                                    && green <= k_appointedColorMax.getGreen()
                                    && blue >= k_appointedColorMin.getBlue()
                                    && blue <= k_appointedColorMax.getBlue()) {
                                        m_patternAlpha[x][y] = true;
                                } else {
                                        m_patternAlpha[x][y] = false;
                                }
                        }
                }

                return true;
        }

        // 对应函数的简化版。以简化流程，降低识别稳定性及成功率的代价降低计算复杂度。
        protected boolean ToAlphaS() {
                Color color;
                // 颜色分量累加器。
                int red = 0;
                int green = 0;
                int blue = 0;

                // 初始化模式图到原图的转换系数。
                m_patternToImgScale = m_originalImg.getHeight() / k_patternHeight;

                for (int x = 0; x < k_patternWidth; ++x) {
                        for (int y = 0; y < k_patternHeight; ++y) {
                                // Old Method 四点取样法。
                                // 取模式图对应的原始图区块的四个角共四个点的颜色平均值作为该区块的颜色代表值。随后判断该颜色值是否为目标判别颜色并填充模式图点

                                // 左上角
                                color = new Color(m_originalImg.getRGB(x * m_patternToImgScale,
                                                                       y * m_patternToImgScale));
                                red = color.getRed();
                                green = color.getGreen();
                                blue = color.getBlue();
                                // 右上角
                                color = new Color(m_originalImg.getRGB((x + 1)
                                                                       * m_patternToImgScale - 1, y * m_patternToImgScale));
                                red += color.getRed();
                                green += color.getGreen();
                                blue += color.getBlue();
                                // 右下角
                                color = new Color(m_originalImg.getRGB((x + 1)
                                                                       * m_patternToImgScale - 1, (y + 1)
                                                                                                  * m_patternToImgScale - 1));
                                red += color.getRed();
                                green += color.getGreen();
                                blue += color.getBlue();
                                // 左下角
                                color = new Color(m_originalImg.getRGB(x * m_patternToImgScale,
                                                                       (y + 1) * m_patternToImgScale - 1));
                                red += color.getRed();
                                green += color.getGreen();
                                blue += color.getBlue();

                                red /= 4;
                                green /= 4;
                                blue /= 4;

                                if (red >= k_appointedColorMin.getRed()
                                    && red <= k_appointedColorMax.getRed()
                                    && green >= k_appointedColorMin.getGreen()
                                    && green <= k_appointedColorMax.getGreen()
                                    && blue >= k_appointedColorMin.getBlue()
                                    && blue <= k_appointedColorMax.getBlue()) {
                                        m_patternAlpha[x][y] = true;
                                } else {
                                        m_patternAlpha[x][y] = false;
                                }
                        }
                }

                return true;
        }

        // 第二次模式化处理。将黑白模式图处理为不完全路径线图，并识别停止指令。
        // 方法是寻找每一行的路径部分中点作为该行的路径线点。但混淆和无效行会被略过。
        // 该流程可以处理无关路径混淆、散点混淆及缺点干扰。
        // NOTICE 该函数工作逻辑请参阅设计流程图。
        protected boolean ToBeta() {
                // 单联通线有效像素X坐标累加器。
                int pointX = 0;
                // 单联通线有效像素计数器。
                int pointCounter = 0;
                // 单联通线计数有效期内的每一间隙内的间隙点计数器。
                int gapCounter = 0;
                // 有效标记。当首次记录有效点后设置。
                boolean isSet = false;

                // 逐行扫描。
                for (int y = 0; y < k_patternHeight; ++y) {
                        // 重置数据。
                        pointX = 0;
                        pointCounter = 0;
                        gapCounter = 0;
                        isSet = false;
                        m_isValidLine[y] = false;
                        m_isStopLine[y] = false;

                        for (int x = 0; x < k_patternWidth; ++x) {
                                // 清理PatternBeta.
                                m_patternBeta[x][y] = false;

                                if (m_patternAlpha[x][y] == true) {
                                        gapCounter = 0;
                                        pointX += x;
                                        ++pointCounter;
                                } else if (pointCounter != 0) {
                                        if (gapCounter < k_gapMax) {
                                                ++gapCounter;
                                        } else {
                                                // 仅当连通线宽度大于预设最小宽度才进行处理，否则清理变量，进行下次判别。
                                                if (pointCounter >= k_trackWidthMin) {
                                                        // 如果已经记录过数据，则表明该行数据无效，清理该行数据，跳过剩余部分直接开始下一行扫描。
                                                        // 设计说明：因考虑到该情况的发生概率较小，所以采取优先写入，失败清理的策略。
                                                        // 如果实际测试表明该情况发生概率较大，可以改用临时变量存储数据，最后写入的策略。
                                                        if (isSet == true) {
                                                                m_isValidLine[y] = false;
                                                                for (int i = 0; i < k_patternWidth; ++i) {
                                                                        m_patternBeta[i][y] = false;
                                                                }
                                                                pointCounter = 0;
                                                                break;
                                                        }
                                                        // 验证是否为停止标记行。
                                                        if (pointCounter >= k_stopLineWidthMin) {
                                                                m_isStopLine[y] = true;
                                                        }
                                                        // 记录数据。
                                                        isSet = true;
                                                        // 取中点为路径线点。
                                                        m_patternBeta[Math.round(((float) pointX)
                                                                                 / pointCounter)][y] = true;
                                                        m_isValidLine[y] = true;
                                                }
                                                // 清理变量为下一次判别准备。
                                                pointX = 0;
                                                pointCounter = 0;
                                                gapCounter = 0;
                                        }
                                }
                        }

                        // 行内扫描完毕。处理边缘情况。
                        if (pointCounter >= k_trackWidthMin) {
                                // 如果已经记录过数据，则表明该行数据无效，清理该行数据，跳过剩余部分直接开始下一行扫描。
                                // 设计说明：因考虑到该情况的发生概率较小，所以采取优先写入，失败清理的策略。
                                // 如果实际测试表明该情况发生概率较大，可以改用临时变量存储数据，最后写入的策略。
                                if (isSet == true) {
                                        m_isValidLine[y] = false;
                                        for (int i = 0; i < k_patternWidth; ++i) {
                                                m_patternBeta[i][y] = false;
                                        }
                                        continue;
                                }
                                // 验证是否为停止标记行。
                                if (pointCounter >= k_stopLineWidthMin) {
                                        m_isStopLine[y] = true;
                                }
                                // 取中点为有效点。
                                m_patternBeta[Math.round(((float) pointX) / pointCounter)][y] = true;
                                m_isValidLine[y] = true;
                        }
                }

                // Finished.
                return true;
        }

        // 对应函数的简化版。以降低精度的代价降低计算复杂度。
        protected boolean ToBetaS() {
                // 单联通线有效像素X坐标累加器。
                int pointX = 0;
                // 单联通线有效像素计数器。
                int pointCounter = 0;
                // 单联通线计数有效期内的每一间隙内的间隙点计数器。
                int gapCounter = 0;

                // 逐行扫描。
                for (int y = 0; y < k_patternHeight; ++y) {
                        // 重置数据。
                        pointX = 0;
                        pointCounter = 0;
                        gapCounter = 0;
                        m_isValidLine[y] = false;
                        m_isStopLine[y] = false;

                        for (int x = 0; x < k_patternWidth; ++x) {
                                // 清理PatternBeta.
                                m_patternBeta[x][y] = false;

                                if (m_patternAlpha[x][y] == true) {
                                        gapCounter = 0;
                                        pointX += x;
                                        ++pointCounter;
                                } else if (pointCounter != 0) {
                                        if (gapCounter < k_gapMax) {
                                                ++gapCounter;
                                        } else // 过短的无效连通线则忽略并继续扫描。
                                        {
                                                if (pointCounter < k_trackWidthMin) {
                                                        // 清理变量为下一次判别准备。
                                                        pointX = 0;
                                                        pointCounter = 0;
                                                        gapCounter = 0;
                                                } // 否则，退出搜索后处理数据。
                                                else {
                                                        break;
                                                }
                                        }
                                }
                        }
                        // 再次判别是为了处理边缘情况。
                        if (pointCounter >= k_trackWidthMin) {
                                // 取中点为有效点。
                                m_patternBeta[Math.round(((float) pointX) / pointCounter)][y] = true;
                                m_isValidLine[y] = true;
                                // 验证是否为停止标记行。
                                if (pointCounter >= k_stopLineWidthMin) {
                                        m_isStopLine[y] = true;
                                }
                        }
                }

                // Finished.
                return true;
        }

        // 第三次模式化处理。拟合不完全路径线图的合理缺失部分。
        // 方法是使用路径线缺失部分两端的有效点计算拟合方程参数，并使用该方程补全路径线图。存在不合理的缺失部分会使方法失败。
        // 该流程可以处理中断混淆。
        protected boolean ToGamma() {

                for (int i = 0; i < k_patternHeight; ++i) {
                        // 寻找断点。
                        if (m_isValidLine[i] == false) {
                                // 拟合线的两端点。
                                Point startPoint = new Point();
                                Point endPoint = new Point();

                                // 计算拟合线起点。
                                int x = 0;
                                int y = i - 1;
                                // 如果路径线从底部开始缺失，设置起点为无效值，根据终点情况处理。
                                if (y == -1) {
                                        startPoint.setLocation(-1, -1);
                                } else {
                                        for (; x < k_patternWidth && m_patternBeta[x][y] != true; ++x)
						;
                                        assert (x == k_patternWidth);
                                        startPoint.setLocation(x, y);
                                }

                                // 计算拟合线终点。
                                for (y += 2; y < k_patternHeight && m_isValidLine[y] != true; ++y)
					;
                                // 如果找不到拟合线终点。
                                if (y >= k_patternHeight) {
                                        // 以底边中点作为终点。
                                        endPoint.setLocation(k_patternWidth / 2,
                                                             k_patternHeight - 1);
                                } else {
                                        for (x = 0; x < k_patternWidth
                                                    && m_patternBeta[x][y] != true; ++x)
						;
                                        assert (x == k_patternWidth);
                                        endPoint.setLocation(x, y);
                                }

                                // 检查特殊情况。
                                if (startPoint.y == -1) {
                                        // 缺失部分穿过判别基线并抵达图像顶部，这是无法处理的情况。
                                        if (endPoint.y > k_baseLine) {
                                                m_resultType = ResultType.Unk;
                                                return false;
                                        } // 缺失部分在基线上并抵达图像顶部，则不作拟合。
                                        else {
                                                // 跳过该部分。
                                                i = endPoint.y;
                                                continue;
                                        }
                                }

                                // 线性拟合缺失部份。拟合方程为 y=ax+b。
                                // 当斜率为0时。
                                if (endPoint.x - startPoint.x == 0) {
                                        for (y = startPoint.y + 1; y < endPoint.y; ++y) {
                                                m_patternBeta[x][y] = true;
                                                m_isValidLine[y] = true;
                                        }
                                } else {
                                        double a = (double) (endPoint.y - startPoint.y)
                                                   / (endPoint.x - startPoint.x);
                                        double b = startPoint.y - a * startPoint.x;
                                        for (y = startPoint.y + 1; y < endPoint.y; ++y) {
                                                x = (int) Math.round((y - b) / a);
                                                m_patternBeta[x][y] = true;
                                                m_isValidLine[y] = true;
                                        }
                                }

                                // 跳过已拟合部分。
                                i = endPoint.y;
                        }
                }

                return true;
        }

        // 获取最终分析结果。计算目标控制点。
        // 方法是计算路径线与判别基线的交点。
        protected boolean ToOmega() {
                // 计算目标控制点x坐标。
                int x = 0;
                for (; x < k_patternWidth && m_patternBeta[x][k_baseLine] != true; ++x)
			;

                // 根据Beta模式图找出的目标点坐标须转换回原图坐标以便输出。
                m_targetPoint.setLocation(x * m_patternToImgScale, k_baseLine
                                                                   * m_patternToImgScale);

                m_resultType = ResultType.Run;

                // 验证是否为停止指令。仅当判别基线为停止行是判别。
                if (m_isStopLine[k_baseLine] == true) {
                        int start = k_baseLine - 1;
                        int end = k_baseLine + 1;
                        for (; m_isStopLine[start] != false; --start)
				;
                        for (; m_isStopLine[end] != false; ++end)
				;
                        if (end - start - 1 >= k_stopLineCounterMin) {
                                m_resultType = ResultType.Stop;
                        }
                }

                return true;
        }
}
