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

import org.jbox2d.common.Mat33;
import org.jbox2d.common.Vec2;
import org.jbox2d.common.Vec3;

/**
 * 线性变换辅助类。
 *
 * @author davis
 */
public class LinearTransform {

        public static Mat33 TTranslation(Vec2 basis) {
                return new Mat33(new Vec3(1.0f, 0.0f, 0.0f),
                                 new Vec3(0.0f, 1.0f, 0.0f),
                                 new Vec3(basis.x, basis.y, 1.0f));
        }

        public static Mat33 TRotation(float the) {
                return new Mat33(new Vec3((float) Math.cos(the), (float) Math.sin(the), 0.0f),
                                 new Vec3((float) -Math.sin(the), (float) Math.cos(the), 0.0f),
                                 new Vec3(0.0f, 0.0f, 1.0f));
        }

        public static Mat33 TScale(float xr, float yr) {
                return new Mat33(new Vec3(xr, 0.0f, 0.0f),
                                 new Vec3(0.0f, yr, 0.0f),
                                 new Vec3(0.0f, 0.0f, 1.0f));
        }

        /*
         * 复合变换。
         */
        public static Mat33 Mul(Mat33 s, Mat33 t) {
                return new Mat33(Mat33.mul(s, t.col1),
                                 Mat33.mul(s, t.col2),
                                 Mat33.mul(s, t.col3));
        }

        /*
         * 点变换。
         */
        public static void Apply2Point(Mat33 t, Vec2 v, Vec2 out) {
                // 优化缓存
                float x0 = t.col1.x * v.x;
                float y0 = t.col1.y * v.y;
                float x1 = t.col2.x * v.x;
                float y1 = t.col2.y * v.y;
                float x2 = t.col3.x;
                float y2 = t.col3.y;
                out.x = x0 + x1 + x2;
                out.y = y0 + y1 + y2;
        }

        /*
         * 点变换。
         */
        public static Vec2 Apply2Point(Mat33 t, Vec2 v) {
                Vec2 out = new Vec2();
                Apply2Point(t, v, out);
                return out;
        }

        /*
         * 向量变换。
         */
        public static void Apply2Vector(Mat33 t, Vec2 v, Vec2 out) {
                // 优化缓存
                float x0 = t.col1.x * v.x;
                float y0 = t.col1.y * v.y;
                float x1 = t.col2.x * v.x;
                float y1 = t.col2.y * v.y;
                out.x = x0 + x1;
                out.y = y0 + y1;
        }

        /*
         * 向量变换。
         */
        public static Vec2 Apply2Vector(Mat33 t, Vec2 v) {
                Vec2 out = new Vec2();
                Apply2Point(t, v, out);
                return out;
        }
}
