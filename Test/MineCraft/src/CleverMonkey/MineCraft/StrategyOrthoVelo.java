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
import java.awt.Point;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.jbox2d.common.Vec2;

/**
 * 约束速度令其垂直于车轴。
 *
 * @author Sheldon, davis
 */
public class StrategyOrthoVelo implements ITracingStrategy {

        private final Map m_map;
        // 调试工具
        private final boolean m_is2Debug;
        private final Graphics m_gAlpha;
        private final Graphics m_gBeta;
        private final Graphics m_gCamera;
        private final JComponent m_compAlpha;
        private final JComponent m_compBeta;
        private final JComponent m_compCamera;
        // @note: 缓存Tracker以减轻GC压力
        private final Tracker m_tracker = new Tracker();
        // 响应敏感度 @note: [0.0 - 1.0）数值越高对曲线的响应速率越快。
        private final float k_responseFactor = 0.8f;
        
        private final Vec2 m_velo = new Vec2();

        /*
         * 应该由ITracingStrategyFactory来构造这个对象。
         */
        public StrategyOrthoVelo(Map map, boolean is2Debug, JComponent slot0, JComponent slot1, JComponent slot2, JComponent slot3) {
                m_map = map;
                m_is2Debug = is2Debug;
                m_gCamera = slot0 != null ? slot0.getGraphics() : null;
                m_gAlpha = slot1 != null ? slot1.getGraphics() : null;
                m_gBeta = slot2 != null ? slot2.getGraphics() : null;
                m_compCamera = slot0;
                m_compAlpha = slot1;
                m_compBeta = slot2;
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
                
                Tracker.ResultType result = m_tracker.AnalyseImg(sensor.GetInternalImageRef());
                
                if (result == Tracker.ResultType.Run) {
                        Point target = m_tracker.ComputeTargetPoint();
                        Vec2 vLocal = new Vec2(target.x, target.y);
                        vLocal.normalize();
                        vLocal.subLocal(new Vec2(0.0f, k_responseFactor));
                        vLocal.normalize();
                        Vec2 vStandard = new Vec2(vLocal.x*dir.y + vLocal.y*dir.x, -vLocal.x*dir.x + vLocal.y*dir.y);
                        float speed = frontVelocity.length();
                        m_velo.set(vStandard.x*speed, (vStandard.y)*speed);
                } else {
                        m_velo.setZero();
                }
                
                if (m_is2Debug) {
                        m_gAlpha.drawImage(m_tracker.GetAlphaPatternImg(true),
                                0, 0, m_compAlpha.getWidth(), m_compAlpha.getHeight(), null);
                        m_gAlpha.drawString("Alpha", 0, 20);
                        m_gBeta.drawImage(m_tracker.GetBetaPatternImg(true),
                                0, 0, m_compBeta.getWidth(), m_compBeta.getHeight(), null);
                        m_gBeta.drawString("Beta", 0, 20);
                        m_gCamera.drawImage(sensor.GetInternalImageRef(),
                                0, 0, m_compCamera.getWidth(), m_compCamera.getHeight(), null);
                        m_gCamera.drawString("Camera", 0, 20);
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
