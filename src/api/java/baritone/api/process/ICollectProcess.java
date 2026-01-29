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

package baritone.api.process;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;

import java.util.List;

public interface ICollectProcess extends IBaritoneProcess {

    /**
     * Begin to search for dropped items to collect within specified range
     * from the current player position.
     *
     * @param items The items to collect
     * @param range The distance from player to collect from (0 = unlimited)
     */
    void collect(List<Item> items, int range);

    /**
     * Begin to search for dropped items to collect within specified range
     * from the specified position.
     *
     * @param items  The items to collect
     * @param range  The distance from player to collect from (0 = unlimited)
     * @param origin The position to collect from
     */
    void collect(List<Item> items, int range, BlockPos origin);

    /**
     * Begin to search for nearby dropped items to collect.
     *
     * @param items The items to collect
     */
    default void collect(List<Item> items) {
        collect(items, 0);
    }
}
