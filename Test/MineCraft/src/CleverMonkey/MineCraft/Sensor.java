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
package Test;

import MineCraft.src.CleverMonkey.MineCraft.LinearTransform;
import java.awt.image.BufferedImage;
import org.jbox2d.common.Mat33;
import org.jbox2d.common.Vec2;

/**
 * 虚拟传感器。
 *
 * @author davis
 */
public class Sensor {

        // 图像传感器分辨率。
        private final int k_cameraHeight = 1280;
        // 图像传感器分辨率。
        private final int k_cameraWidth = 720;
        // 传感器图像
        private BufferedImage m_mem = new BufferedImage(k_cameraWidth, k_cameraHeight, BufferedImage.TYPE_INT_ARGB);

        /**
         * 从源图像更新传感器。
         * @param T 从传感器到源图像的反变换。
         * @param src 源图像。
         */
        public void UpdateSensorFromSourceImage(Mat33 T, BufferedImage src) {
                // 线性变换T: X -> W (X, W ≅ RxR)
                Vec2 X = new Vec2();
                Vec2 W = new Vec2();
                for (int y = 0; y < m_mem.getHeight(); ++y) {
                        for (int x = 0; x < m_mem.getWidth(); ++x) {
                                X.x = x;
                                X.y = y;
                                LinearTransform.Apply2Point(T, X, W);
                                int rgb = 0XFFFFFFFF;           // 超出源图像的默认为白色。
                                if (W.x > 0.0f && W.x < src.getWidth() &&
                                    W.y > 0.0f && W.y < src.getHeight()) {
                                        rgb = src.getRGB((int) W.x, (int) W.y);
                                }
                                m_mem.setRGB((int) X.x, (int) X.y, rgb);
                        }
                }
        }
        
        public static Mat33 GetInverseTransform(Vec2 t, float the) {
                return null;
        }
}
