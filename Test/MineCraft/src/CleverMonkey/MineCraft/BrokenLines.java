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
import java.util.ArrayList;
import java.util.List;
import org.jbox2d.common.Vec2;

/**
 * 折线。
 * 
 * @author davis
 */
public final class BrokenLines implements IDrawable {
        
        private final List<Vec2> m_points;
        private final float[] m_pathLength;
        private float m_scale = 1.0f;
        private float m_length = 0.0f;
        
        public double __PathLengthSquared(Vec2 a, Vec2 b) {
                return (a.x - b.x)*(a.x - b.x) + (a.y - b.y)*(a.y - b.y);
        }
        
        public Vec2 __Interpolate(Vec2 a, Vec2 b, float t) {
                return new Vec2(a.x + (b.x - a.x)*t, a.y + (b.y - a.y)*t);
        }
        
        public BrokenLines(List<Vec2> points) {
                m_points = points;
                if (m_points.size() < 2) {
                        m_pathLength = new float[] {0.0f};
                        m_length = 0.0f;
                        return ;
                }
                m_pathLength = new float [m_points.size()];
                m_pathLength[0] = 0.0f;
                for (int i = 0; i < m_points.size() - 1; i ++) {
                        double sq = __PathLengthSquared(m_points.get(i), m_points.get(i + 1));
                        m_length += Math.sqrt(sq);
                        m_pathLength[i + 1] = m_length;
                }
        }
        
        public void SetScale(float s) {
                m_scale = s;
        }
        
        public void Clear() {
                m_points.clear();
        }
        
        private void __DrawLine(Graphics g, Vec2 s0, Vec2 st, float mx, float my) {
                g.drawLine((int) (s0.x*mx), (int) ((m_scale - s0.y)*my), 
                           (int) (st.x*mx), (int) ((m_scale - st.y)*my));
        }
        
        public Vec2 L(float t) {
                if (m_points.isEmpty()) return new Vec2();
                if (m_points.size() == 1) return m_points.get(0);
                for (int i = 0; i < m_points.size() - 1; i ++) {
                        float factor = m_pathLength[i + 1]/m_length;
                        if (factor >= t) {
                                float base = m_pathLength[i]/m_length;
                                float s = (t - base)/(factor - base);
                                return __Interpolate(m_points.get(i), m_points.get(i + 1), s);
                        }
                }
                return m_points.get(m_points.size() - 1);
        }
        
        public float D(Vec2 p) {
                throw new UnsupportedOperationException();
        }
        
        @Override
        public void Draw(Graphics g, int width, int height) {
                if (m_points.size() < 2) return ;
                float mx = width/m_scale, my = height/m_scale;
                for (int i = 0; i < m_points.size() - 1; i ++) {
                        __DrawLine(g, m_points.get(i), m_points.get(i + 1), mx, my);
                }
        }
        
}
