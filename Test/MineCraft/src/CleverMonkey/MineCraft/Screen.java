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
package MineCraft.src.CleverMonkey.MineCraft;

import org.jbox2d.common.Mat33;
import org.jbox2d.common.Vec2;
import org.jbox2d.common.Vec3;

/**
 * 屏幕变换辅助类。
 *
 * @author davis
 */
public class Screen {

        private float m_w;
        private float m_h;

        public Screen(int w, int h) {
                m_w = w;
                m_h = h;
        }

        public void Resize(int w, int h) {
                m_w = w;
                m_h = h;
        }

        public float Width() {
                return m_w;
        }

        public float Height() {
                return m_h;
        }

        public Mat33 ToEuclidSpace(Vec2 t, Vec2 targetScale) {
                return new Mat33(new Vec3(targetScale.x / Width(), 0.0f, 0.0f),
                                 new Vec3(0.0f, -targetScale.y / Height(), 0.0f),
                                 new Vec3(-t.x * targetScale.x / Width(), t.y * targetScale.y / Height() + targetScale.y, 1.0f));
        }

        public Mat33 FromEuclidSpace(Vec2 t, Vec2 sourceScale) {
                return new Mat33(new Vec3(Width() / sourceScale.x, 0.0f, 0.0f),
                                 new Vec3(0.0f, -Height() / sourceScale.y, 0.0f),
                                 new Vec3(-t.x * Width() / sourceScale.x, t.y * Height() / sourceScale.y + Height(), 1.0f));
        }

        public Mat33 ToOtherScreen(Screen other) {
                return new Mat33(new Vec3(other.Width() / Width(), 0.0f, 0.0f),
                                 new Vec3(0.0f, other.Height() / Height(), 0.0f),
                                 new Vec3(0.0f, 0.0f, 1.0f));
        }

        public Mat33 FromOtherScreen(Screen other) {
                return new Mat33(new Vec3(Width() / other.Width(), 0.0f, 0.0f),
                                 new Vec3(0.0f, Height() / other.Height(), 0.0f),
                                 new Vec3(0.0f, 0.0f, 1.0f));
        }
}
