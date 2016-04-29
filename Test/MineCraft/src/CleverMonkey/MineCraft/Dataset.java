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

import java.util.ArrayList;
import org.jbox2d.common.Vec2;

/**
 * 点样本数据。
 * @author davis
 */
public class Dataset extends ArrayList<Vec2> {
        
        public void add(float singleValue) {
                super.add(new Vec2(singleValue, 0.0f));
        }
        
        public Vec2 Average() {
                Vec2 avg = new Vec2();
                super.stream().forEach(value -> {
                        avg.x += value.x;
                        avg.y += value.y;
                });
                avg.set(avg.x/super.size(), avg.y/super.size());
                return avg;
        }
        
        public Vec2 Variance() {
                Vec2 avg = Average();
                Vec2 sigma2 = new Vec2();
                super.stream().forEach(value -> {
                        sigma2.x += (value.x - avg.x)*(value.x - avg.x);
                        sigma2.y += (value.y - avg.y)*(value.x - avg.x);
                });
                avg.set(sigma2.x/super.size(), sigma2.y/super.size());
                return sigma2;
        }
        
        public Vec2 StandardDeviation() {
                Vec2 var = Variance();
                var.set((float) Math.sqrt(var.x), (float) Math.sqrt(var.y));
                return var;
        }
}
