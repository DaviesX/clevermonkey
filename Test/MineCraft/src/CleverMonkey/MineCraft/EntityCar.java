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
import java.awt.Graphics;
import org.jbox2d.common.Mat33;
import org.jbox2d.common.Vec2;


/**
 * 小车。
 *
 * @author davis
 */
public class EntityCar implements IPhysEntity, IDrawable {

        private ITracingStrategy m_strategy;
        private final CarBody m_carBody;
        private final float k_carLength = 0.20f;
        private final float k_carWidth = 0.14f;
        private final float k_speed = 0.1f;
        private Simulation.Universe m_universe;
        private final Sensor m_sensor = new Sensor();
        private final Map m_map;

        public EntityCar(Vec2 centroid, ITracingStrategy stg, Map map) {
                m_strategy = stg;
                m_carBody = new CarBody(k_carLength, k_carWidth, centroid, new Vec2(0.0f, 1.0f));
                m_map = map;
        }

        public void ChangeStrategy(ITracingStrategy stg) {
                m_strategy = stg;
        }

        public void SetPosition(Vec2 centroid) {
                m_carBody.SetCentroidPosition(centroid);
        }

        public Vec2 GetPosition() {
                return m_carBody.GetCentroidPosition();
        }

        @Override
        public void OnAdd(Simulation.Universe universe) {
                // 暂时不参与到box2d的世界中。
                m_universe = universe;
        }

        @Override
        public void OnRemove(Simulation.Universe universe) {
                // 暂时不参与到box2d的世界中。
                m_universe = null;
        }

        @Override
        public void TimeEvolution(Simulation.Universe universe, float dt, Simulation.Clock t) {
                if (null == m_strategy) {
                        return;
                }
                Vec2 frontVelocity = m_carBody.GetDirection().mul(k_speed);
                Vec2 centroid = m_carBody.GetCentroidPosition();
                
                Screen screen = new Screen(m_map.GetInternalImageRef().getWidth(), m_map.GetInternalImageRef().getHeight());
                Mat33 screenTrans = screen.FromEuclidSpace(new Vec2(0.0f, 0.0f), universe.GetWorldScale());
                Vec2 center = LinearTransform.Apply2Point(screenTrans, centroid);
                Vec2 dir = frontVelocity.clone();
                dir.normalize();
                
                m_sensor.UpdateSensorFromSourceImage(Sensor.GetInverseTransform(center, dir), m_map.GetInternalImageRef());
                
                m_strategy.TimeEvolution(centroid, frontVelocity,  dt, t, m_sensor, universe);
                Vec2 v = m_strategy.ComputeFrontWheelVelocity();
                
                m_carBody.ClosedFormSolver(v, dt);
        }

        private void __DrawLine(Graphics g, Vec2 a, Vec2 b) {
                g.drawLine((int) a.x, (int) a.y, (int) b.x, (int) b.y);
        }

        @Override
        public void Draw(Graphics g, int width, int height) {
                Mat33 tScreen = new Screen(width, height).FromEuclidSpace(new Vec2(0, 0), m_universe.GetWorldScale());

                CarBody.OBB obb = m_carBody.GetOBB();

                Vec2 p0 = new Vec2();
                Vec2 p1 = new Vec2();
                Vec2 p2 = new Vec2();
                Vec2 p3 = new Vec2();
                obb.GetFourCornersCounterClockwise(tScreen, p0, p1, p2, p3);

                g.setColor(Color.GREEN);
                __DrawLine(g, p0, p1);
                __DrawLine(g, p1, p2);
                __DrawLine(g, p2, p3);
                __DrawLine(g, p3, p0);
        }
}
