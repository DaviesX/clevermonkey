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

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import org.jbox2d.common.Vec2;

/**
 * 地图。
 *
 * @author davis
 */
public class Map implements IDrawable {

        // 物理单位米(m)到物理像素的转换系数。
        private final float k_mToPixelScale = 7000.0f;
        
        private final int k_scaleWidth = 512;
        private final int k_scaleHeight = 512;

        private BufferedImage m_mem;
        private Image m_scaled;

        public Map(BufferedImage img) {
                m_mem = img;
                m_scaled = img.getScaledInstance(k_scaleWidth, k_scaleHeight, Image.SCALE_AREA_AVERAGING);
        }

        public Map(int w, int h) {
                m_mem = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                m_scaled = m_mem.getScaledInstance(k_scaleWidth, k_scaleHeight, Image.SCALE_AREA_AVERAGING);
        }

        public Map(InputStream imgFile) throws IOException {
                m_mem = ImageIO.read(imgFile);
                m_scaled = m_mem.getScaledInstance(k_scaleWidth, k_scaleHeight, Image.SCALE_AREA_AVERAGING);
        }

        public BufferedImage GetInternalImageRef() {
                return m_mem;
        }

        public float MapWidth() {
                return m_mem.getWidth() / k_mToPixelScale;
        }

        public float MapHeight() {
                return m_mem.getHeight() / k_mToPixelScale;
        }

        public Vec2 GetScale() {
                return new Vec2(MapWidth(), MapHeight());
        }

        @Override
        public void Draw(Graphics g, int width, int height) {
                g.drawImage(m_scaled, 0, 0, width, height, null);
        }
}
