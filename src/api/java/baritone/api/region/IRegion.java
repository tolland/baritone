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

import baritone.api.utils.BetterBlockPos;
import net.minecraft.core.Vec3i;

/**
 * A regions is an immutable object representing the current region. The region is commonly used for schematics, however it can be used for anything.
 */
public interface IRegion {

    /**
     * @return The first corner of this selection. This is meant to preserve the user's original first corner.
     */
    BetterBlockPos pos1();

    /**
     * @return The second corner of this selection. This is meant to preserve the user's original second corner.
     */
    BetterBlockPos pos2();

    /**
     * @return The {@link BetterBlockPos} with the lowest x, y, and z position in the selection.
     */
    default BetterBlockPos min() {
        // @TODO avoid creating new BetterBlockPos and stuff
        return new BetterBlockPos(Math.min(pos1().getX(), pos2().getX()), Math.min(pos1().getY(), pos2().getY()), Math.min(pos1().getZ(), pos2().getZ()));
    }

    /**
     * @return The opposite corner from the {@link #min()}.
     */
    default BetterBlockPos max() {
        // @TODO avoid creating new BetterBlockPos and stuff
        return new BetterBlockPos(Math.max(pos1().getX(), pos2().getX()), Math.max(pos1().getY(), pos2().getY()), Math.max(pos1().getZ(), pos2().getZ()));
    }

    /**
     * @return The size of this ISelection.
     */
    default Vec3i size() {
        return max().subtract(min());
    }

    /**
     * @return The width (X axis length) of this schematic
     */
    default int widthX() {
        return size().getX();
    }

    /**
     * @return The height (Y axis length) of this schematic
     */
    default int heightY() {
        return size().getY();
    }

    /**
     * @return The length (Z axis length) of this schematic
     */
    default int lengthZ() {
        return size().getZ();
    }


}
