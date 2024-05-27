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

package baritone.api.schematic;

import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;

/**
 * Represents a region of a schematic.
 * <p>
 * This is a simple interface that represents a region of a schematic. It is used by the LitematicaSchematic implementation to
 * represent the region of the schematic that is being queried. But can be used by other implementations as well.
 * <p>
 * This interface is not intended to be implemented by users of Baritone, and is only used internally.
 *
 * @see ISchematic
 */
public interface ISchematicRegion {
    default String getName() {
        return "";
    }

    default String getDescription() {
        return "";
    }

    int getWidth();

    int getHeight();

    int getLength();

    default boolean isEnabled() {
        return true;
    }

    default Rotation getRotation() {
        return Rotation.NONE;
    }

    default Mirror getMirror() {
        return Mirror.NONE;
    }

}
