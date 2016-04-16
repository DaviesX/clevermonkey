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
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;

/**
 * 小车。
 *
 * @author davis
 */
public class EntityCar implements IPhysEntity, IDrawable {

        private ITracingStrategy m_strategy;
        private Vec2 m_centroid;

        public EntityCar(Vec2 position, ITracingStrategy stg) {
                m_strategy = stg;
                m_centroid = position;
        }

        public void ChangeStrategy(ITracingStrategy stg) {
                m_strategy = stg;
        }

        public void SetPosition(Vec2 centroid) {
                m_centroid = centroid;
        }

        public Vec2 GetPosition() {
                return m_centroid;
        }

        @Override
        public void OnAdd(World world) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void OnRemove(World world) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void TimeEvolution(World world, float dt, Simulation.Clock t) {
                if (null == m_strategy) {
                        return;
                }
        }

        @Override
        public void Draw(Graphics g, int width, int height) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
}
