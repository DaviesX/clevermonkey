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

import java.util.List;
import javax.swing.JComponent;

/**
 * 绘制。
 *
 * @author davis
 */
public class Drawer {

        JComponent m_compTarget;

        public Drawer() {
        }

        public Drawer(JComponent target) {
                m_compTarget = target;
        }

        public void SetDrawingTarget(JComponent target) {
                m_compTarget = target;
        }

        public void Draw(List<IDrawable> batch) {
                batch.stream().forEach((drawable) -> {
                        if (drawable != null) {
                                drawable.Draw(m_compTarget.getGraphics(), m_compTarget.getWidth(), m_compTarget.getHeight());
                        }
                });
        }
}
