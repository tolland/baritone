/*
 * This file is part of Baritone.
 *
 * Baritone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Baritone is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Baritone.  If not, see <https://www.gnu.org/licenses/>.
 */

package baritone.api.region;

import org.apache.commons.lang3.NotImplementedException;

/**
 * represents a region that can be transformed my mirror, rotation, etc
 */
public interface ITransformableRegion extends IRegion {

    /**
     * @return A new IRegion that is a   mirror of this region
     */
    default IRegion mirror() {
        throw new NotImplementedException();
    }
}
