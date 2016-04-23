/*
 * Copyright (C) 2016 davis
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package CleverMonkey.MineCraft;

import java.awt.image.BufferedImage;

/**
 * 矢量化路径。
 * @author davis
 */
public class PathVectorizer {
        
        // 源图像。@note: 保留图像内存减少设备内存的分配与释放
        private BufferedImage m_rasterImg = null;
        // 梯度映射。@note: 保留图像内存减少设备内存的分配与释放
        private BufferedImage m_gradientMap = null;
        // 低通过滤图像。@note: 保留图像内存减少设备内存的分配与释放
        private BufferedImage m_lowPass = null;
        // 高斯分布。
        private final float[] m_gaussianDist;
        // 标准差。
        private final float m_sigma = 5.0f;
        // 采样半径。@note: 性能上考虑，1个单位已经足够（Desktop上 < 2 ms)。
        private final int m_radius = 1;
        // 核心大小。
        private final int m_kernelSize = m_radius*2 + 1;
        // 高斯核心。
        private final float[][] m_G = new float [m_kernelSize][m_kernelSize];
        // Sobel核心。
        private final float[][] m_Sx = {
                {1.0f, 2.0f, 0.0f,  -2.0f, -1.0f},
                {4.0f, 8.0f, 0.0f,  -8.0f, -4.0f},
                {6.0f, 12.0f, 0.0f, -12.0f, -6.0f},
                {4.0f, 8.0f, 0.0f,  -8.0f, -4.0f},
                {1.0f, 2.0f, 0.0f,  -2.0f, -1.0f}
        };
        private final float[][] m_Sy = {
                {1.0f, 4.0f, 6.0f, 4.0f, 1.0f},
                {2.0f, 8.0f, 12.0f, 8.0f, 2.0f},
                {0.0f, 0.0f, 0.0f, 0.0f, 0.0f},
                {-2.0f, -8.0f, -12.0f, -8.0f, -2.0f},
                {-1.0f, -4.0f, -6.0f, -4.0f, -1.0f}
        };
        // 信号函数。
        private final int[][] m_Fxy = new int [m_kernelSize][m_kernelSize];
        
        // 可调整参数
        // 梯度识别度
        private final int k_gradientThreshold = 128;
        // 二进制转换识别度
        private final int k_binaryThreshold = 30;
        
        private float __GaussXY(double sigma, int x, int y) {
		return (float) Math.exp(-((x*x + y*y) / (2*sigma*sigma)));
	}
        
        private float __GaussSX(double sigma, int x) {
		return (float) Math.exp(-(x/(2*sigma*sigma)));
	}
        
        private BufferedImage __ReallocImage(BufferedImage img, int w, int h) {
                if (img == null || img.getWidth() != w || img.getHeight() != h) {
                        img = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
                }
                return img;
        }
        
        /**
         * @param rasterImg 光栅化图像。
         */
        public PathVectorizer(BufferedImage rasterImg) {
                // 计算高斯分布。
                m_gaussianDist = new float[256];
                for (int i = 0; i < 256; i ++) {
                        m_gaussianDist[i] = __GaussSX(m_sigma, i);
                }
                // 生成高斯核心。
                for (int j = 0; j < m_kernelSize; j ++) {
                        for (int i = 0; i < m_kernelSize; i ++) {
                                m_G[j][i] = __GaussXY(m_sigma, -m_radius + i, -m_radius + j);
                        }
                }
        }
        
        private void __ComputeFxy(final BufferedImage img, final int x, final int y, final int radius, final int[][] Fxy) {
                int wlimit = img.getWidth() - 1, hlimit = img.getHeight() - 1;
                for (int j = y - radius, l = 0; l <= 2*radius; j ++, l ++) {
                        for (int i = x - radius, m = 0; m <= 2*radius; i ++, m ++) {
                                int ri = Math.min(wlimit, Math.max(0, i));
                                int rj = Math.min(hlimit, Math.max(0, j));
                                int level = img.getRGB(ri, rj) & 0XFF;
                                Fxy[l][m] = level;
                        }
                }
        }
        
        private int __BinaryLevel(int level) {
                return level > k_binaryThreshold ? 0XFF : 0X0;
        }
        
        // 双边低通滤波器。
        private BufferedImage __BilateralLowPassFilter(BufferedImage rasterImg, BufferedImage lowPass) {
                lowPass = __ReallocImage(lowPass, rasterImg.getWidth(), rasterImg.getHeight());
                // G(x, y) = 1/(2pi*sigma) * exp{-(x^2 + y^2)/(2*sigma^2)}, 其中sigma = 1
                int iw = rasterImg.getWidth(), ih = rasterImg.getHeight();
                for (int y = 0; y < ih; y ++) {
                        for (int x = 0; x < iw; x ++) {
                                __ComputeFxy(rasterImg, x, y, m_radius, m_Fxy);
                                // 卷积Gaussian核心（可分离），f * h
                                float s = 0, sw = 0;
                                int centralLumin = m_Fxy[1][1];
                                for (int j = 0; j < m_kernelSize; j ++) {
                                        for (int i = 0; i < m_kernelSize; i ++) {
                                                int level = m_Fxy[j][i];
                                                float w = m_G[j][i]*m_gaussianDist[Math.abs(level - centralLumin)];
                                                s += __BinaryLevel(level)*w;
                                                sw += w;
                                        }
                                }
                                float exp = s/sw;
                                int scale = exp > 255 ? 255 : (int) exp;
                                lowPass.setRGB(x, y, (scale << 16) | (scale << 8) | scale);
                        }
                }
                return lowPass;
        }
        
        // 计算梯度映射。
        private BufferedImage __ComputeGradients(BufferedImage rasterImg, BufferedImage grads, int threshold) {
                grads = __ReallocImage(grads, rasterImg.getWidth(), rasterImg.getHeight());
                // G[f] = (Gx i + Gy j)[f] = (d/dx i + d/dy j)[f]
                // int[][] Gx = {{-1, 0, 1}, {-2, 0, 2}, {-1, 0, 1}};
                // int[][] Gy = {{1, 2, 1}, {0, 0, 0}, {-1, -2, -1}};
                int iw = rasterImg.getWidth(), ih = rasterImg.getHeight();
                for (int y = 1; y < ih - 1; y ++) {
                        for (int x = 1; x < iw - 1; x ++) {
                                __ComputeFxy(rasterImg, x, y, 1, m_Fxy);
                                // 卷积Sobel核心（可分离）并计算梯度标量大小||G|| = sqrt(Gx.Gx + Gy.Gy) <= ||Gx|| + ||Gy||
                                int grad = Math.abs((m_Fxy[0][0] + 2*m_Fxy[0][1] + m_Fxy[0][2]) - 
                                                    (m_Fxy[2][0] + 2*m_Fxy[2][1] + m_Fxy[2][2])) + 
                                           Math.abs((m_Fxy[0][2] + 2*m_Fxy[1][2] + m_Fxy[2][2]) - 
                                                    (m_Fxy[0][0] + 2*m_Fxy[1][0] + m_Fxy[2][0]));
                                // @note: 使用2个单位的采样半径可以得到更精确的梯度，从现有的测试上看，1个单位仍然可以接受
//                                float gradX = 0.0f, gradY = 0.0f;
//                                for (int j = 0; j < m_kernelSize; j ++) {
//                                        for (int i = 0; i < m_kernelSize; i ++) {
//                                                gradX += m_Sx[j][i]*m_Fxy[j][i];
//                                        }
//                                }
//                                for (int j = 0; j < m_kernelSize; j ++) {
//                                        for (int i = 0; i < m_kernelSize; i ++) {
//                                                gradY += m_Sy[j][i]*m_Fxy[j][i];
//                                        }
//                                }
//                                int grad = (int) (Math.abs(gradX) + Math.abs(gradY));
                                grads.setRGB(x, y, grad > threshold ? 0XFFFFFFFF : 0X0);
                        }
                }
                return grads;
        }
        
        private BufferedImage __Downsampler256(BufferedImage input, BufferedImage rasterOut) {
                int downWidth = input.getWidth()/16;
                int downHeight = input.getHeight()/16;
                rasterOut = __ReallocImage(rasterOut, downWidth, downHeight);
                for (int y = 0; y < downHeight; ++y) {
                        for (int x = 0; x < downWidth; ++x) {
                                // 辐射亮度。
                                int rr = 0, rg = 0, rb = 0;
                                int vsum = 0;
                                for (int j = 0; j < 16; j ++) {
                                        for (int i = 0; i < 16; i ++) {
                                                int v = input.getRGB((x << 4) + i, (y << 4) + j);
                                                rr += v & 0X00FF0000;
                                                rg += v & 0X0000FF00;
                                                rb += v & 0X000000FF;
                                        }
                                }
                                 // 流明。
                                int lumin = (int) (0.33*(rr >>> 16)/256 + 0.34*(rg >>> 8)/256 + 0.33*rb/256);
                                rasterOut.setRGB(x, y, (lumin << 16) | (lumin << 8) | (lumin));
                        }
                }
                return rasterOut;
        }
        
        public BufferedImage GetInternalGradientMap() {
                return m_gradientMap;
        }
        
        public BufferedImage GetInternalLowPass() {
                return m_lowPass;
        }
        
        public void PreprocessRasterImage(BufferedImage input) {
                m_rasterImg = __Downsampler256(input, m_rasterImg);
                m_lowPass = __BilateralLowPassFilter(m_rasterImg, m_lowPass);
                m_gradientMap = __ComputeGradients(m_lowPass, m_gradientMap, 128);
        }
}