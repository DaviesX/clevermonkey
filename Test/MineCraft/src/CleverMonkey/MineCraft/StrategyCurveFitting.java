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
        private final boolean m_is2Debug;
        private final JComponent m_alpha;
        private final JComponent m_beta;
        private final JComponent m_gamma;
        private final PathVectorizer m_pathVec = new PathVectorizer(null);
        
        private final Vec2 m_velo = new Vec2();

        /*
         * 应该由ITracingStrategyFactory来构造这个对象。
         */
        public StrategyCurveFitting(Map map, boolean is2Debug, JComponent alpha, JComponent beta, JComponent gamma) {
                m_map = map;
                m_is2Debug = is2Debug;
                m_alpha = alpha;
                m_beta = beta;
                m_gamma = gamma;
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
                
                m_pathVec.PreprocessRasterImage(sensor.GetInternalImageRef());
                Vec2 vLocal = Decision.PredictTangentFromGradientMap(m_pathVec.GetInternalGradientMap());
                Vec2 vStandard = new Vec2(vLocal.x*dir.y + vLocal.y*dir.x, -vLocal.x*dir.x + vLocal.y*dir.y);
                float speed = frontVelocity.length();
                m_velo.set(vStandard.x*speed, vStandard.y*speed);
                
                if (m_is2Debug) {
                        m_gamma.getGraphics().drawImage(sensor.GetInternalImageRef(),
                                0, 0, m_gamma.getWidth(), m_gamma.getHeight(), null);
                        m_alpha.getGraphics().drawImage(m_pathVec.GetInternalGradientMap(), 
                                                        0, 0, m_alpha.getWidth(), m_alpha.getHeight(), null);
                        m_beta.getGraphics().drawImage(m_pathVec.GetInternalLowPass(), 
                                                        0, 0, m_beta.getWidth(), m_beta.getHeight(), null);
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
