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

package baritone.command.defaults;

import baritone.api.IBaritone;
import baritone.api.command.Command;
import baritone.api.command.argument.IArgConsumer;
import baritone.api.command.datatypes.ItemById;
import baritone.api.command.exception.CommandException;
import baritone.api.command.exception.CommandInvalidStateException;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class CollectCommand extends Command {

    public CollectCommand(IBaritone baritone) {
        super(baritone, "collect");
    }

    @Override
    public void execute(String label, IArgConsumer args) throws CommandException {
        args.requireMin(1);

        List<Item> items = new ArrayList<>();
        int range = 0;

        // Parse items until we hit an integer (range) or run out of args
        while (args.hasAny()) {
            // Check if next arg is a range (integer)
            Integer possibleRange = args.peekAsOrNull(Integer.class);
            if (possibleRange != null) {
                range = args.getAs(Integer.class);
                break;
            }

            // Otherwise, parse as item
            items.add(args.getDatatypeFor(ItemById.INSTANCE));
        }

        if (items.isEmpty()) {
            throw new CommandInvalidStateException("No items specified to collect");
        }

        baritone.getCollectProcess().collect(items, range);
        logDirect("Collecting " + items.size() + " item type(s)" + (range > 0 ? " within " + range + " blocks" : ""));
    }

    @Override
    public Stream<String> tabComplete(String label, IArgConsumer args) throws CommandException {
        return args.tabCompleteDatatype(ItemById.INSTANCE);
    }

    @Override
    public String getShortDesc() {
        return "Collect dropped items";
    }

    @Override
    public List<String> getLongDesc() {
        return Arrays.asList(
                "The collect command instructs Baritone to collect dropped items of specified types.",
                "",
                "Usage:",
                "> collect <item...> - collects all dropped items of the specified type(s).",
                "> collect <item...> <range> - collect items within range from current position.",
                "",
                "Examples:",
                "> collect minecraft:diamond - collects all dropped diamonds",
                "> collect minecraft:wheat minecraft:wheat_seeds - collects wheat and seeds",
                "> collect minecraft:iron_ingot 50 - collects iron ingots within 50 blocks",
                "",
                "Note: Range is calculated from where you are when the command starts.",
                "To collect from a specific location, navigate there first, then run collect."
        );
    }
}
