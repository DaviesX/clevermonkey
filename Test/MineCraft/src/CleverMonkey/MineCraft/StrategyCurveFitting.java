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

import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;
import org.jbox2d.common.Vec2;

/**
 * 决策。
 * 
 * @author davis
 */
class Decision {
        
        public static Vec2 PredictTangentFromGradientMap(BufferedImage gradMap) {
                float factor = 1.0f/16.0f;
                int dist = (int) (factor*gradMap.getHeight());
                Vec2 st = new Vec2(), s0 = new Vec2(), ds = new Vec2(gradMap.getWidth()/2, 0.0f);
                int s = 0;
                OUTTER_ST:
                for (int j = 0; j < gradMap.getHeight(); j ++) {
                        for (int i = 0; i < gradMap.getWidth(); i ++) {
                                int v = gradMap.getRGB(i, j) & 0XFF;
                                if (v != 0X0) {
                                        int lx = i, rx = i;
                                        for (i = gradMap.getWidth() - 1; i >= 0; i --) {
                                                v = gradMap.getRGB(i, j) & 0XFF;
                                                if (v != 0X0) {
                                                        rx = i;
                                                        break;
                                                }
                                        }
                                        st.set((lx + rx)/2, j);
                                        ds.set(2.0f*factor*(st.x - ds.x), j);
                                        s = j;
                                        break OUTTER_ST;
                                } 
                        }
                }
                OUTTER_S0:
                for (int j = Math.min(s + dist, gradMap.getHeight() - 1); j < gradMap.getHeight(); j ++) {
                        for (int i = 0; i < gradMap.getWidth(); i ++) {
                                int v = gradMap.getRGB(i, j) & 0XFF;
                                if (v != 0X0) {
                                        int lx = i, rx = i;
                                        for (i = gradMap.getWidth() - 1; i >= 0; i --) {
                                                v = gradMap.getRGB(i, j) & 0XFF;
                                                if (v != 0X0) {
                                                        rx = i;
                                                        break;
                                                }
                                        }
                                        s0.set((lx + rx)/2, j);
                                        break OUTTER_S0;
                                } 
                        }
                }
                Vec2 tangent = new Vec2(st.x - s0.x, s0.y - st.y);
                tangent = tangent.add(ds);
                tangent.normalize();
                return tangent;
        }
}

/**
 * 曲线拟合策略。
 *
 * @author davis
 */
public class StrategyCurveFitting implements ITracingStrategy {

        private final Map m_map;
        // 调试工具
        private final boolean m_is2Debug;
        private final JComponent m_camera;
        private final JComponent m_grad;
        private final JComponent m_lowPass;
        private final JComponent m_path;
        // @note: 缓存PathVectorizer以减轻GC压力
        private final PathVectorizer m_pathVec = new PathVectorizer();
        // 路径的目标辐射亮度。
        private final Color k_targetRadiance = new Color(0, 0, 0);
        
        private final Vec2 m_velo = new Vec2();

        /*
         * 应该由ITracingStrategyFactory来构造这个对象。
         */
        public StrategyCurveFitting(Map map, boolean is2Debug, JComponent slot0, JComponent slot1, JComponent slot2, JComponent slot3) {
                m_map = map;
                m_is2Debug = is2Debug;
                m_camera = slot0;
                m_grad = slot1;
                m_lowPass = slot2;
                m_path = slot3;
        }

        @Override
        public float ComputeFrontWheelAngularVelocity() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void TimeEvolution(Vec2 centroid, Vec2 frontVelocity, float dt, Simulation.Clock t, 
                                    Sensor sensor, Simulation.Universe universe) {
                Vec2 dir = frontVelocity.clone();
                dir.normalize();
                
                m_pathVec.UpdateFromRasterImage(sensor.GetInternalImageRef(), k_targetRadiance);
                // BezierSpline path = m_pathVec.BezierSplineRegression();
                // BrokenLines path = m_pathVec.BorkenLinesRegression();
                BezierSpline path = m_pathVec.BezierSplineFromPath();
                Vec2 vLocal = Decision.PredictTangentFromGradientMap(m_pathVec.GetInternalGradientMap());
                Vec2 vStandard = new Vec2(vLocal.x*dir.y + vLocal.y*dir.x, -vLocal.x*dir.x + vLocal.y*dir.y);
                float speed = frontVelocity.length();
                m_velo.set(vStandard.x*speed, vStandard.y*speed);
                
                if (m_is2Debug) {
                        m_camera.getGraphics().drawImage(sensor.GetInternalImageRef(),
                                0, 0, m_camera.getWidth(), m_camera.getHeight(), null);
                        m_camera.getGraphics().drawString("Sensor", 0, 20);
                        
                        m_grad.getGraphics().drawImage(m_pathVec.GetInternalGradientMap(), 
                                                        0, 0, m_grad.getWidth(), m_grad.getHeight(), null);
                        m_grad.getGraphics().drawString("GradientMap", 0, 20);
//                        m_lowPass.getGraphics().drawImage(m_pathVec.GetInternalLowPass(), 
//                                                        0, 0, m_lowPass.getWidth(), m_lowPass.getHeight(), null);
//                        m_lowPass.getGraphics().drawString("LowPass", 0, 20);
//                        bs.Draw(m_path.getGraphics(), m_path.getWidth(), m_path.getHeight());
                        m_path.getGraphics().clearRect(0, 0, m_path.getWidth(), m_path.getHeight());
                        path.Draw(m_path.getGraphics(), m_path.getWidth(), m_path.getHeight());
                        m_path.getGraphics().drawString("PredictedPath", 0, 20);
                }
        }

        @Override
        public float ComputeFrontWheelAngle() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Vec2 ComputeFrontWheelVelocity() {
                return m_velo;
        }

}
