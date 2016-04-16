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

import java.awt.image.BufferedImage;
import javax.swing.JComponent;

/**
 * @author davis
 */
public class ITracingStrategyFactory {

        enum Strategy {
                OrthoVelo,
                CurveFitting
        }

        static ITracingStrategy CreateStrategy(Strategy type, Map map, boolean is2Debug, JComponent alpha, JComponent beta, JComponent gamma) {
                switch (type) {
                        case OrthoVelo:
                                return new StrategyOrthoVelo(map, is2Debug, alpha, beta, gamma);
                        case CurveFitting:
                                return new StrategyCurveFitting(map, is2Debug, alpha, beta, gamma);
                        default:
                                return null;
                }
        }
}
