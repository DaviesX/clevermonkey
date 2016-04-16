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

import org.jbox2d.common.Vec2;
import java.util.HashSet;
import java.util.Set;
import org.jbox2d.dynamics.World;

/**
 * 物理模拟。
 *
 * @author davis
 */
public class Simulation {

        public class Clock {

                private float m_fromTheBigBang = 0.0f;

                public float FromTheBigBang() {
                        return m_fromTheBigBang;
                }

                private void TimeEvolution(float dt) {
                        m_fromTheBigBang += dt;
                }
        }
        
        public class Universe {
                private final World m_world = new World(new Vec2(0, 0), false);
                private final Vec2 m_scale = new Vec2(1.0f, 1.0f);
                
                public void SetWorldScale(Vec2 scale) {
                        m_scale.set(scale);
                }
                
                public World GetBox2dWorld() {
                        return m_world;
                }
                
                public Vec2 GetWorldScale() {
                        return m_scale;
                }

                private void TimeEvolution(float dt) {
                        m_world.step(dt, 10, 8);
                }
        }

        // 模拟步间隔时间（秒）。
        private float m_dt = 0.1f;
        private final Clock m_clock = new Clock();
        private final Universe m_universe = new Universe();
        private final Set<IPhysEntity> m_entities = new HashSet<>();

        public Simulation() {
        }

        public Simulation(float dt) {
                m_dt = dt;
        }

        /**
         * 添加物理实体。
         *
         * @param entity
         */
        public void AddPhysEntity(IPhysEntity entity) {
                entity.OnAdd(m_universe);
                m_entities.add(entity);
        }

        /**
         * 删除物理实体。
         *
         * @param entity
         */
        public void RemovePhysEntity(IPhysEntity entity) {
                entity.OnRemove(m_universe);
                m_entities.remove(entity);
        }
        
        /**
         * 设置演变间隔。
         * 
         * @param dt 
         */
        public void SetDeltaT(float dt) {
                m_dt = dt;
        }
        
        /**
         * 设置世界大小。
         * 
         * @param scale 
         */
        public void SetWorldScale(Vec2 scale) {
                m_universe.SetWorldScale(scale);
        }
        
        /**
         * 进行演变。
         * 
         * @return 
         */
        public boolean TimeEvolution() {
                m_entities.stream().forEach((entity) -> {
                        entity.TimeEvolution(m_universe, m_dt, m_clock);
                });
                m_universe.TimeEvolution(m_dt);
                return true;
        }
}
