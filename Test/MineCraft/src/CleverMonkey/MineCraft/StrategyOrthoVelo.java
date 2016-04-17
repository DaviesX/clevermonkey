/*
 * Copyright (C) 2016 Sheldon, davis
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

import CleverMonkey.Tracker.Tracker;
import java.awt.Graphics;
import javax.swing.JComponent;
import org.jbox2d.common.Mat33;
import org.jbox2d.common.Vec2;

/**
 * 约束速度令其垂直于车轴。
 *
 * @author Sheldon, davis
 */
public class StrategyOrthoVelo implements ITracingStrategy {

        private final Map m_map;
        private final boolean m_is2Debug;
        private final Graphics m_gAlpha;
        private final Graphics m_gBeta;
        private final Graphics m_gCamera;
        private final JComponent m_compAlpha;
        private final JComponent m_compBeta;
        private final JComponent m_compCamera;
        private final Tracker m_tracker = new Tracker();
        private final Sensor m_sensor = new Sensor();

        /*
         * 应该由ITracingStrategyFactory来构造这个对象。
         */
        public StrategyOrthoVelo(Map map, boolean is2Debug, JComponent alpha, JComponent beta, JComponent camera) {
                m_map = map;
                m_is2Debug = is2Debug;
                m_gAlpha = alpha != null ? alpha.getGraphics() : null;
                m_gBeta = beta != null ? beta.getGraphics() : null;
                m_gCamera = camera != null ? camera.getGraphics() : null;
                m_compAlpha = alpha;
                m_compBeta = beta;
                m_compCamera = camera;
        }

        @Override
        public float ComputeFrontWheelAngularVelocity() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void TimeEvolution(Vec2 centroid, Vec2 frontVelocity, float dt, 
                                    Simulation.Clock t, Simulation.Universe universe) {

                Screen screen = new Screen(m_map.GetInternalImageRef().getWidth(), m_map.GetInternalImageRef().getHeight());
                Mat33 screenTrans = screen.FromEuclidSpace(new Vec2(0.0f, 0.0f), universe.GetWorldScale());
                Vec2 center = LinearTransform.Apply2Point(screenTrans, centroid);
                Vec2 dir = frontVelocity.clone();
                dir.normalize();
                
                m_sensor.UpdateSensorFromSourceImage(Sensor.GetInverseTransform(center, dir), m_map.GetInternalImageRef());
                Tracker.ResultType result = m_tracker.AnalyseImg(m_sensor.GetInternalImageRef());
                if (m_is2Debug) {
                        m_gAlpha.drawImage(m_tracker.GetAlphaPatternImg(true),
                                0, 0, m_compAlpha.getWidth(), m_compAlpha.getHeight(), null);
                        m_gBeta.drawImage(m_tracker.GetBetaPatternImg(true),
                                0, 0, m_compBeta.getWidth(), m_compBeta.getHeight(), null);
                        m_gCamera.drawImage(m_sensor.GetInternalImageRef(),
                                0, 0, m_compCamera.getWidth(), m_compCamera.getHeight(), null);
                }
        }

        @Override
        public float ComputeFrontWheelAngle() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Vec2 ComputeFrontWheelVelocity() {
                return new Vec2(0.05f, 0.1f);
        }
}
