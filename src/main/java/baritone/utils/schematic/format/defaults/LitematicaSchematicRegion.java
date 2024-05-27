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

package baritone.utils.schematic.format.defaults;

import baritone.api.schematic.ISchematicRegion;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.Optional;

public class LitematicaSchematicRegion implements ISchematicRegion {

    private Vec3i size;
    private Vec3i pos;
    private Rotation rotation;
    private Mirror mirror;

    public BlockState[] getBlockList() {
        return blockList;
    }

    BlockState[] blockList;
    LitematicaSchematic.LitematicaBitArray bitArray;
    ListTag usedBlockTypes;


    private LitematicaSchematicRegion(String regionName, CompoundTag region, boolean dummy) {
        usedBlockTypes = region.getList("BlockStatePalette", 10);

        // this requires bootstrapped registry stuff - @TODO can mock in some way?
        if(dummy) {
            blockList = getBlockList2(usedBlockTypes);
        }else {
            blockList = getBlockList(usedBlockTypes);
        }

        int bitsPerBlock = getBitsPerBlock(usedBlockTypes.size());
        long regionVolume = getVolume(region);
        long[] blockStateArray = getBlockStates(region);
        bitArray = new LitematicaSchematic.LitematicaBitArray(bitsPerBlock, regionVolume, blockStateArray);
    }

    public static LitematicaSchematicRegion create(String regionName, CompoundTag region) {
        return new LitematicaSchematicRegion(regionName, region, false);
    }
    public static LitematicaSchematicRegion createDummy(String regionName, CompoundTag region) {
        return new LitematicaSchematicRegion(regionName, region, true);
    }

    /**
     * @param blockStatePalette List of all different block types used in the schematic.
     * @return Array of BlockStates.
     */
    static BlockState[] getBlockList(ListTag blockStatePalette) {
        BlockState[] blockList = new BlockState[blockStatePalette.size()];

        for (int i = 0; i < blockStatePalette.size(); i++) {
            Block block = BuiltInRegistries.BLOCK.get(new ResourceLocation((((CompoundTag) blockStatePalette.get(i)).getString("Name"))));
            CompoundTag properties = ((CompoundTag) blockStatePalette.get(i)).getCompound("Properties");

            blockList[i] = getBlockState(block, properties);
        }
        return blockList;
    }

    BlockState[] getBlockList2(ListTag blockStatePalette) {


        BlockState[] blockList = new BlockState[blockStatePalette.size()];

        for (int i = 0; i < blockStatePalette.size(); i++) {
            ResourceLocation resLoc = new ResourceLocation((((CompoundTag) blockStatePalette.get(i)).getString("Name")));
        }
        return blockList;
    }

    /**
     * @param block      block.
     * @param properties List of Properties the block has.
     * @return A blockState.
     */
    private static BlockState getBlockState(Block block, CompoundTag properties) {
        BlockState blockState = block.defaultBlockState();

        for (Object key : properties.getAllKeys().toArray()) {
            Property<?> property = block.getStateDefinition().getProperty((String) key);
            String propertyValue = properties.getString((String) key);
            if (property != null) {
                blockState = setPropertyValue(blockState, property, propertyValue);
            }
        }
        return blockState;
    }

    /**
     * @author Emerson
     */
    private static <T extends Comparable<T>> BlockState setPropertyValue(BlockState state, Property<T> property, String value) {
        Optional<T> parsed = property.getValue(value);
        if (parsed.isPresent()) {
            return state.setValue(property, parsed.get());
        } else {
            throw new IllegalArgumentException("Invalid value for property " + property);
        }
    }


    /**
     * @param amountOfBlockTypes amount of block types in the schematic.
     * @return amount of bits used to encode a block.
     */
    static int getBitsPerBlock(int amountOfBlockTypes) {
        return (int) Math.max(2, Math.ceil(Math.log(amountOfBlockTypes) / Math.log(2)));
    }

    /**
     * Calculates the volume of the subregion. As size can be a negative value we take the absolute value of the
     * multiplication as the volume still holds a positive amount of blocks.
     *
     * @return the volume of the subregion.
     */
    static long getVolume(CompoundTag regNbt) {
        return Math.abs(
                regNbt.getCompound("Size").getInt("x") *
                        regNbt.getCompound("Size").getInt("y") *
                        regNbt.getCompound("Size").getInt("z"));
    }

    /**
     * @return array of Long values.
     */
    static long[] getBlockStates(CompoundTag regNbt) {
        return regNbt.getLongArray("BlockStates");
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public int getLength() {
        return 0;
    }
}
