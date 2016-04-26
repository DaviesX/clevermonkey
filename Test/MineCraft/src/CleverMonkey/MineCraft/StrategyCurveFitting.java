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
        
        public static Vec2 PredictTangentFromBezierPath(BezierSpline bs) {
                return bs.T(0.8f).add(new Vec2(bs.B(0.8f).x - 0.5f, 0.0f));
        }
}

/**
 * 曲线拟合策略。
 *
 * @author davis
 */
public class StrategyCurveFitting implements ITracingStrategy {

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
        public StrategyCurveFitting(boolean is2Debug, JComponent slot0, JComponent slot1, JComponent slot2, JComponent slot3) {
                m_is2Debug = is2Debug;
                m_camera = slot0;
                m_grad = slot1;
                m_path = slot2;
                m_lowPass = slot3;
                
        }

        @Override
        public void TimeEvolution(Vec2 centroid, Vec2 frontVelocity, float dt, Simulation.Clock t, 
                                    Sensor sensor, Simulation.Universe universe) {
                Vec2 dir = frontVelocity.clone();
                dir.normalize();
                
                m_pathVec.UpdateFromRasterImage(sensor.GetInternalImageRef(), k_targetRadiance);
                BezierSpline path = m_pathVec.BezierSplineFromPath();
                Vec2 vLocal = Decision.PredictTangentFromBezierPath(path);
                vLocal.normalize();
                
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
                        m_path.getGraphics().clearRect(0, 0, m_path.getWidth(), m_path.getHeight());
                        path.Draw(m_path.getGraphics(), m_path.getWidth(), m_path.getHeight());
                        m_path.getGraphics().drawString("Path: " + path.toString(), 0, 20);
                }
        }

        @Override
        public Vec2 ComputeFrontWheelVelocity() {
                return m_velo;
        }

}
