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

import org.jbox2d.common.Mat33;
import org.jbox2d.common.Vec2;
import org.jbox2d.common.Vec3;

/**
 * 虚拟小车。
 *
 * @author davis
 */
public class CarBody {

        private final float m_lhalf;
        private final float m_whalf;
        private final Vec2 m_backCentroid = new Vec2();
        private final Vec2 m_carCentroid = new Vec2();

        private final Vec2 m_frontVelocity = new Vec2();
        private final Vec2 m_backVelocity = new Vec2();
        private final Vec2 m_backLeftVelocity = new Vec2();
        private final Vec2 m_backRightVelocity = new Vec2();

        public CarBody(float l, float w, Vec2 initialCentroid, Vec2 initialDirection) {
                m_lhalf = l / 2;
                m_whalf = w / 2;
                initialDirection.normalize();
                m_backCentroid.set(initialCentroid.x + initialDirection.x * (-l / 2), initialCentroid.y + initialDirection.y * (-l / 2));
                m_carCentroid.set(initialCentroid);
        }

        public void SetCentroidPosition(Vec2 pos) {
                Vec2 dir = GetDirection();
                m_carCentroid.set(pos);
                m_backCentroid.set(m_carCentroid.x - dir.x * m_lhalf, m_carCentroid.y - dir.y * m_lhalf);
        }

        public Vec2 GetCentroidPosition() {
                return m_carCentroid.clone();
        }

        public Vec2 GetDirection() {
                Vec2 dir = new Vec2(m_carCentroid.x - m_backCentroid.x, m_carCentroid.y - m_backCentroid.y);
                dir.normalize();
                return dir;
        }

        public Vec2 GetBackLeftPosition() {
                Vec2 dir = GetDirection();
                return new Vec2(m_backCentroid.x + dir.y * (-m_whalf), m_backCentroid.y + (-dir.x) * (-m_whalf));
        }

        public Vec2 GetBackRightPosition() {
                Vec2 dir = GetDirection();
                return new Vec2(m_backCentroid.x + dir.y * m_whalf, m_backCentroid.y + (-dir.x) * m_whalf);
        }

        public Vec2 GetFrontPosition() {
                Vec2 dir = GetDirection();
                return new Vec2(m_backCentroid.x + dir.x * m_lhalf, m_backCentroid.y + dir.y * m_lhalf);
        }

        public Vec2 GetFrontVelocity() {
                return m_frontVelocity;
        }

        public Vec2 GetBackVelocity() {
                return m_backVelocity;
        }

        public Vec2 GetBackLeftVelocity() {
                return m_backLeftVelocity;
        }

        public Vec2 GetBackRightVelocity() {
                return m_backRightVelocity;
        }

        /**
         * 演化。
         *
         * @param frontVelocity
         * @param dt
         */
        public void ClosedFormSolver(Vec2 frontVelocity, float dt) {
                float v = frontVelocity.length();
                if (v == 0) {
                        return;
                }

                Vec2 dir = GetDirection();

                // 速度在小车方向上的投影量
                float projX = dir.x * frontVelocity.x + dir.y * frontVelocity.y;
                float projY = dir.y * frontVelocity.x - dir.x * frontVelocity.y;

                if (Math.abs((projY - 0) / v) < 1e-3) {
                        // 接近直线时使用简化模型，tan(x) = sin(x) = x (x ～ 0), 避免数值问题。
                        Vec2 dsFront = new Vec2(frontVelocity.x * dt, frontVelocity.y * dt);
                        Vec2 front = new Vec2(m_carCentroid.x + dir.x * m_lhalf + dsFront.x,
                                              m_carCentroid.y + dir.y * m_lhalf + dsFront.y);
                        m_frontVelocity.set(frontVelocity);

                        Vec2 dsBack = new Vec2(dir.x * projX * dt, dir.y * projX * dt);
                        Vec2 back = new Vec2(m_backCentroid.x + dsBack.x, m_backCentroid.y + dsBack.y);
                        m_backVelocity.set(dsBack.x / dt, dsBack.y / dt);
                        Vec2 backLeft0 = GetBackLeftPosition();
                        Vec2 backRight0 = GetBackRightPosition();

                        Vec2 dirPrime = new Vec2(front.x - back.x, front.y - back.y);
                        dirPrime.normalize();
                        m_carCentroid.set(back.x + dirPrime.x * m_lhalf, back.y + dirPrime.y * m_lhalf);
                        m_backCentroid.set(back);

                        Vec2 backLeftT = GetBackLeftPosition();
                        Vec2 backRightT = GetBackRightPosition();
                        m_backLeftVelocity.set((backLeftT.x - backLeft0.x) / dt, (backLeftT.y - backLeft0.y) / dt);
                        m_backRightVelocity.set((backRightT.x - backRight0.x) / dt, (backRightT.y - backRight0.y) / dt);
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
                Vec2 dsFront = new Vec2(dirOrtho.x * ds.x + dirOrtho.y * ds.y,
                                        -dir.x * ds.x + dir.y * ds.y);
                Vec2 front = new Vec2(m_carCentroid.x + dir.x * m_lhalf + dsFront.x,
                                      m_carCentroid.y + dir.y * m_lhalf + dsFront.y);
                m_frontVelocity.set(dsFront.x / dt, dsFront.y / dt);

                // 后轮质心位置
                // s'(t) = <l*cotThe - l*cotThe*Math.cos(w*t), 
                //          l*cotThe*Math.sin(w*t)>, (t >= 0)
                // 其中 w = v/l * sin(the)
                Vec2 dsPrime = new Vec2((float) (l * cotThe - l * cotThe * Math.cos(w * dt)),
                                        (float) (l * cotThe * Math.sin(w * dt)));
                // 基底变换
                Vec2 dsBack = new Vec2(dirOrtho.x * dsPrime.x + dirOrtho.y * dsPrime.y,
                                       -dir.x * dsPrime.x + dir.y * dsPrime.y);
                Vec2 back = new Vec2(m_carCentroid.x - dir.x * m_lhalf + dsBack.x,
                                     m_carCentroid.y - dir.y * m_lhalf + dsBack.y);
                m_backVelocity.set(dsBack.x / dt, dsBack.y / dt);
                Vec2 backLeft0 = GetBackLeftPosition();
                Vec2 backRight0 = GetBackRightPosition();

                Vec2 dirPrime = new Vec2(front.x - back.x, front.y - back.y);
                dirPrime.normalize();
                m_carCentroid.set(back.x + dirPrime.x * m_lhalf, back.y + dirPrime.y * m_lhalf);
                m_backCentroid.set(back);

                Vec2 backLeftT = GetBackLeftPosition();
                Vec2 backRightT = GetBackRightPosition();
                m_backLeftVelocity.set((backLeftT.x - backLeft0.x) / dt, (backLeftT.y - backLeft0.y) / dt);
                m_backRightVelocity.set((backRightT.x - backRight0.x) / dt, (backRightT.y - backRight0.y) / dt);
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

                public void GetFourCornersCounterClockwise(Mat33 customTransform, Vec2 a, Vec2 b, Vec2 c, Vec2 d) {
                        Mat33 T = LinearTransform.Mul(customTransform, R);
                        LinearTransform.Apply2Point(T, new Vec2(-halfExtents.x, +halfExtents.y), a);
                        LinearTransform.Apply2Point(T, new Vec2(+halfExtents.x, +halfExtents.y), b);
                        LinearTransform.Apply2Point(T, new Vec2(+halfExtents.x, -halfExtents.y), c);
                        LinearTransform.Apply2Point(T, new Vec2(-halfExtents.x, -halfExtents.y), d);
                }
        }

        public OBB GetOBB() {
                return GetOBB(new Vec2(1.0f, 1.0f));
        }

        public OBB GetOBB(Vec2 scale) {
                // 基底变换 R' = {<vx, vy>, <ux, uy>} -> R = {<1, 0>, <0, 1>}
                // Vec2 u = new Vec2(m_carCentroid.x - m_backCentroid.x, m_carCentroid.y - m_backCentroid.y);
                Vec2 u = GetDirection();
                Vec2 v = new Vec2(-u.y, u.x);    // v = u x n, (n = <0, 0, 1>)
                // 用齐次坐标系区分点和向量以提供额外的自由度给平移变换
                // |x|   | 1 0 0 |^(-1) | vx*s vy*s tx*s | |x'|
                // |y| = | 0 1 0 |      | ux*s uy*s ty*s | |y'|
                // |1|   | 0 0 1 |      | 0    0    1    | |1 |
                Mat33 SxTxR = new Mat33(new Vec3(v.x * scale.x, u.x * scale.x, 0.0f),
                                        new Vec3(v.y * scale.y, u.y * scale.y, 0.0f),
                                        new Vec3(m_carCentroid.x * scale.x, m_carCentroid.y * scale.y, 1.0f));
                return new OBB(SxTxR, m_carCentroid, new Vec2(m_whalf, m_lhalf));
        }
}
