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
package Test;

import org.jbox2d.common.Vec2;

/**
 * 跟踪策略。
 *
 * @author davis
 */
public interface ITracingStrategy {

        public float TimeEvolution(Vec2 centroid, Vec2 centroidVelocity, float dt, Simulation.Clock t);

        public float ComputeFrontWheelAngularVelocity();

        public float ComputeFrontWheelAngle();

        public float ComputeFrontWheelVelocity();
}
