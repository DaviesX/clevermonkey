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
import org.jbox2d.common.Vec2;

/**
 * 贝塞尔样条曲线。@note: B(t) = (1 - t)^3*c0 + 3(1 - t)^2*t*c1 + 3(1 - t)*t^2*c2 + t^3*c3, (t∈[0,1]).
 * @author davis
 */
public class BezierSpline implements IDrawable {
        
        private final Vec2[] m_c = new Vec2[4];
        private float m_scale = 1.0f;
        
        public BezierSpline(Vec2 c0, Vec2 c1, Vec2 c2, Vec2 c3) {
                m_c[0] = c0;
                m_c[1] = c1;
                m_c[2] = c2;
                m_c[3] = c3;
        }
        
        public void SetControlPoint(int i, Vec2 c) {
                m_c[i] = c;
        }
        
        public void SetScale(float s) {
                m_scale = s;
        }
        
        private float __Cubic(float x) { 
                return x*x*x; 
        }
        
        private float __Qudratic(float x) {
                return x*x;
        }
        
        private void __B(float t, Vec2 p) {
                float a = __Cubic(1 - t), b = __Qudratic(1 - t)*t, c = (1 - t)*__Qudratic(t), d = __Cubic(t);
                p.set(a*m_c[0].x + b*m_c[1].x + c*m_c[2].x + d*m_c[3].x,
                      a*m_c[0].y + b*m_c[1].y + c*m_c[2].y + d*m_c[3].y);
        }
        
        private void __DrawLine(Graphics g, Vec2 s0, Vec2 st, float mx, float my) {
                g.drawLine((int) (s0.x*mx), (int) (s0.y*my), (int) (st.x*mx), (int) (st.y*my));
        }

        @Override
        public void Draw(Graphics g, int width, int height) {
                float mx = width/m_scale, my = height/m_scale;
                Vec2 Si = new Vec2();
                Vec2 Sj = new Vec2();
                float dt = 0.03f;
                for (float t = 0; t < 1; t += dt) {
                        __B(t, Si);
                        __B(t + dt, Sj);
                        __DrawLine(g, Si, Sj, mx, my);
                }
        }        
}
