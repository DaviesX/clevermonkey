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

import java.awt.Graphics;
import org.jbox2d.common.Mat33;
import org.jbox2d.common.Vec2;
import org.jbox2d.common.Vec3;
import org.jbox2d.dynamics.World;

class CarBody {

        private final float m_lhalf;
        private final float m_whalf;
        protected Vec2 backCentroid = new Vec2();
        protected Vec2 carCentroid;

        public CarBody(float l, float w, Vec2 initialCentroid, Vec2 initialDirection) {
                m_lhalf = l / 2;
                m_whalf = w / 2;
                initialDirection.normalize();
                backCentroid.set(initialCentroid.x + initialDirection.x * (-l / 2), initialCentroid.y + initialDirection.y * (-l / 2));
                carCentroid = initialCentroid.clone();
        }
        
        public void SetCentroidPosition(Vec2 pos) {
                Vec2 dir = GetDirection();
                carCentroid = pos.clone();
                backCentroid.set(carCentroid.x - dir.x*m_lhalf, carCentroid.y - dir.y*m_lhalf);
        }
        
        public Vec2 GetCentroidPosition() {
                return carCentroid.clone();
        }

        public Vec2 GetDirection() {
                Vec2 dir = new Vec2(carCentroid.x - backCentroid.x, carCentroid.y - backCentroid.y);
                dir.normalize();
                return dir;
        }

        public Vec2 GetBackLeftPosition() {
                Vec2 dir = GetDirection();
                return new Vec2(backCentroid.x + dir.y * (-m_whalf), backCentroid.y + (-dir.x) * (-m_whalf));
        }

        public Vec2 GetBackRightPosition() {
                Vec2 dir = GetDirection();
                return new Vec2(backCentroid.x + dir.y * m_whalf, backCentroid.y + (-dir.x) * m_whalf);
        }

        /**
         * 演化。
         *
         * @param frontVelocity
         * @param dt
         */
        public void ClosedFormSolver(Vec2 frontVelocity, float dt) {
                Vec2 dir = GetDirection();
                float v = frontVelocity.length();

                // 速度在小车方向上的投影量
                float projX = dir.x * frontVelocity.x + dir.y * frontVelocity.y;
                float projY = dir.y * frontVelocity.x - dir.x * frontVelocity.y;

                if (Math.abs((projY - 0.0f) / v) < 1e-3f) {
                        // 接近直线时使用简化模型，避免数值问题。
                        Vec2 front = new Vec2(carCentroid.x + dir.x * m_lhalf + frontVelocity.x * dt,
                                              carCentroid.y + dir.y * m_lhalf + frontVelocity.y * dt);
                        Vec2 back = new Vec2(backCentroid.x + dir.x * projX * dt, backCentroid.y + dir.y * projX * dt);
                        Vec2 dirPrime = new Vec2(front.x - back.x, front.y - back.y);
                        dirPrime.normalize();
                        carCentroid.set(back.x + dirPrime.x * m_lhalf, back.y + dirPrime.y * m_lhalf);
                        backCentroid.set(back);
                        return;
                }
                // 前轮位置
                // s(t) = <l*cot(the) - l/sin(the)*cos(w*t + the), 
                //         -l + l/sin(the)*sin(w*t + the)>, (t >= 0)
                // 其中 w = v/l * sin(the)
                float the = (float) Math.atan2(projY, projX);
                float cosThe = projX / v;         // Comp_dir[frontVelocity] = dir.front/||front||
                float sinThe = projY / v;
                float cotThe = cosThe / sinThe;
                float l = m_lhalf * 2;
                float w = v / l * sinThe;

                float thePrime = the + w * dt;
                Vec2 ds = new Vec2((float) (l * cotThe - l / sinThe * Math.cos(thePrime)),
                                   (float) (-l + l / sinThe * Math.sin(thePrime)));
                // 基底变换
                Vec2 dirOrtho = new Vec2(dir.y, dir.x);
                Vec2 front = new Vec2(carCentroid.x + dir.x * m_lhalf + dirOrtho.x * ds.x + dirOrtho.y * ds.y,
                                      carCentroid.y + dir.y * m_lhalf - dir.x * ds.x + dir.y * ds.y);
                // System.out.println("ds: " + ds.toString());

                // 后轮质心位置
                // s'(t) = <l*cotThe - l*cotThe*Math.cos(w*t), 
                //          l*cotThe*Math.sin(w*t)>, (t >= 0)
                // 其中 w = v/l * sin(the)
                Vec2 dsPrime = new Vec2((float) (l * cotThe - l * cotThe * Math.cos(w * dt)),
                                        (float) (l * cotThe * Math.sin(w * dt)));
                // 基底变换
                Vec2 back = new Vec2(carCentroid.x - dir.x * m_lhalf + dirOrtho.x * dsPrime.x + dirOrtho.y * dsPrime.y,
                                     carCentroid.y - dir.y * m_lhalf - dir.x * dsPrime.x + dir.y * dsPrime.y);
                Vec2 dirPrime = new Vec2(front.x - back.x, front.y - back.y);
                dirPrime.normalize();
                backCentroid.set(back);
                carCentroid.set(back.x + dirPrime.x * m_lhalf, back.y + dirPrime.y * m_lhalf);
        }

        public class OBB {

                protected Mat33 R;
                protected Vec2 center;
                protected Vec2 halfExtents;

                public OBB(Mat33 R, Vec2 center, Vec2 halfExtents) {
                        this.R = R;
                        this.center = center;
                        this.halfExtents = halfExtents;
                }

                public void GetFourCornersCounterClockwise(Vec2 a, Vec2 b, Vec2 c, Vec2 d) {
                        LinearTransform.Apply2Point(R, new Vec2(-halfExtents.x, +halfExtents.y), a);
                        LinearTransform.Apply2Point(R, new Vec2(+halfExtents.x, +halfExtents.y), b);
                        LinearTransform.Apply2Point(R, new Vec2(+halfExtents.x, -halfExtents.y), c);
                        LinearTransform.Apply2Point(R, new Vec2(-halfExtents.x, -halfExtents.y), d);
                }
        }

        public OBB GetOBB() {
                return GetOBB(new Vec2(1.0f, 1.0f));
        }

        public OBB GetOBB(Vec2 scale) {
                // 基底变换 R' = {<vx, vy>, <ux, uy>} -> R = {<1, 0>, <0, 1>}
                // Vec2 u = new Vec2(carCentroid.x - backCentroid.x, carCentroid.y - backCentroid.y);
                Vec2 u = GetDirection();
                Vec2 v = new Vec2(-u.y, u.x);    // v = u x n, (n = <0, 0, 1>)
                // 用齐次坐标系区分点和向量以提供额外的自由度给平移变换
                // |x|   | 1 0 0 |^(-1) | vx*s vy*s tx*s | |x'|
                // |y| = | 0 1 0 |      | ux*s uy*s ty*s | |y'|
                // |1|   | 0 0 1 |      | 0    0    1    | |1 |
                Mat33 SXTxR = new Mat33(new Vec3(v.x * scale.x, u.x * scale.x, 0.0f),
                                        new Vec3(v.y * scale.y, u.y * scale.y, 0.0f),
                                        new Vec3(carCentroid.x * scale.x, carCentroid.y * scale.y, 1.0f));
                return new OBB(SXTxR, carCentroid, new Vec2(m_whalf, m_lhalf));
        }
}

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

        public EntityCar(Vec2 centroid, ITracingStrategy stg) {
                m_strategy = stg;
                m_carBody = new CarBody(k_carLength, k_carWidth, centroid, new Vec2(0.0f, 1.0f));
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
                m_strategy.TimeEvolution(m_carBody.GetCentroidPosition(), m_carBody.GetDirection().mul(k_speed), dt, t);
                Vec2 v = m_strategy.ComputeFrontWheelVelocity();
                m_carBody.ClosedFormSolver(v, dt);
        }

        @Override
        public void Draw(Graphics g, int width, int height) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
}
