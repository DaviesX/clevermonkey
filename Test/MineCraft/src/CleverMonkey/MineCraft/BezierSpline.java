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
import java.text.DecimalFormat;
import org.jbox2d.common.Vec2;

/**
 * 贝塞尔样条曲线。@note: B(t) = (1 - t)^3*c0 + 3(1 - t)^2*t*c1 + 3(1 - t)*t^2*c2 + t^3*c3, (t∈[0,1]).
 * @author davis
 */
public class BezierSpline implements IDrawable {
        
        private final Vec2[] m_c = new Vec2[4];
        private float m_scale = 1.0f;
        
        private void __SetControlPointFromTargetPoint(Vec2 p0, Vec2 p1, Vec2 p2, Vec2 p3, Vec2[] c) {
                c[0] = p0.clone();
                c[3] = p3.clone();
                final float t1 = 1.0f/3.0f, t2 = 2.0f/3.0f;
                final float A = 3.0f*__Qudratic(1 - t1)*t1, B = 3.0f*(1 - t1)*__Qudratic(t1),
                            C = 3.0f*__Qudratic(1 - t2)*t2, D = 3.0f*(1 - t2)*__Qudratic(t2);
                final float det = A*D - C*B;
                final float iA = D/det, iB = -B/det, iC = -C/det, iD = A/det;
                final float mt1 = __Cubic(1 - t1), mt2 = __Cubic(1 - t2);
                final float nt1 = __Cubic(t1), nt2 = __Cubic(t2);
                float S1x = p1.x - mt1*p0.x - nt1*p3.x;
                float S1y = p1.y - mt1*p0.y - nt1*p3.y;
                float S2x = p2.x - mt2*p0.x - nt2*p3.x;
                float S2y = p2.y - mt2*p0.y - nt2*p3.y;
                c[1] = new Vec2(iA*S1x + iB*S2x, iA*S1y + iB*S2y);
                c[2] = new Vec2(iC*S1x + iD*S2x, iC*S1y + iD*S2y);
        }
        
        public BezierSpline(Vec2 c0, Vec2 c1, Vec2 c2, Vec2 c3, boolean isTarget) {
                if (!isTarget) {
                        m_c[0] = c0.clone();
                        m_c[1] = c1.clone();
                        m_c[2] = c2.clone();
                        m_c[3] = c3.clone();
                } else __SetControlPointFromTargetPoint(c0, c1, c2, c3, m_c);
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
        
        public double __PathLengthSquared(Vec2 a, Vec2 b) {
                return (a.x - b.x)*(a.x - b.x) + (a.y - b.y)*(a.y - b.y);
        }
        
        public void B(float t, Vec2 p) {
                float a = __Cubic(1 - t), 
                      b = 3*__Qudratic(1 - t)*t, 
                      c = 3*(1 - t)*__Qudratic(t), 
                      d = __Cubic(t);
                p.set(a*m_c[0].x + b*m_c[1].x + c*m_c[2].x + d*m_c[3].x,
                      a*m_c[0].y + b*m_c[1].y + c*m_c[2].y + d*m_c[3].y);
        }
        
        public Vec2 B(float t) {
                Vec2 p = new Vec2();
                B(t, p);
                return p;
        }
        
        public float D(Vec2 p) {
                final int k_Iterations = 5;
                float t = 0.5f;
                float tLow = 0.0f;
                float tHigh = 1.0f;
                double l = 0;
                for (int i = 0; i < k_Iterations; i ++) {
                        l = __PathLengthSquared(p, B(t));
                        double ll = __PathLengthSquared(p, B(tLow));
                        double lh = __PathLengthSquared(p, B(tHigh));
                        if (Math.abs(l - ll) < Math.abs(l - lh)) {
                                tHigh = t;
                                t = (tHigh - tLow)/2;
                        } else {
                                tLow = t;
                                t = (tHigh - tLow)/2;
                        }
                }
                return (float) Math.sqrt(l);
        }
        
        public Vec2 T(float t) {
                float a = -3*__Qudratic(1 - t), 
                      b = 3*(1 - 4*t + 3*__Qudratic(t)), 
                      c = 3*(2*t - 3*__Qudratic(t)), 
                      d = 3*__Qudratic(t);
                return new Vec2(a*m_c[0].x + b*m_c[1].x + c*m_c[2].x + d*m_c[3].x,
                         a*m_c[0].y + b*m_c[1].y + c*m_c[2].y + d*m_c[3].y);
        }
        
        private void __DrawLine(Graphics g, Vec2 s0, Vec2 st, float mx, float my) {
                g.drawLine((int) (s0.x*mx), (int) ((m_scale - s0.y)*my), 
                           (int) (st.x*mx), (int) ((m_scale - st.y)*my));
        }

        @Override
        public void Draw(Graphics g, int width, int height) {
                float mx = width/m_scale, my = height/m_scale;
                Vec2 Si = new Vec2();
                Vec2 Sj = new Vec2();
                float dt = 0.03f;
                for (float t = 0; t < 1; t += dt) {
                        B(t, Si);
                        B(t + dt, Sj);
                        __DrawLine(g, Si, Sj, mx, my);
                }
        }
        
        private final DecimalFormat m_df = new DecimalFormat("#.#");
        
        private String __FormatVec2(Vec2 vec) {
                return "(" + m_df.format(vec.x) + "," + m_df.format(vec.y) + ")";
        }
        
        @Override
        public String toString() {
                return __FormatVec2(m_c[0]) + "," + 
                       __FormatVec2(m_c[1]) + "," + 
                       __FormatVec2(m_c[2]) + "," + 
                       __FormatVec2(m_c[3]);
        }
}
