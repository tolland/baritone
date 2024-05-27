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

import baritone.api.region.ITransformableRegion;
import baritone.api.utils.BetterBlockPos;
import baritone.utils.Stuff;
import baritone.utils.schematic.StaticSchematic;
import com.mojang.logging.LogUtils;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Based on EmersonDove's work
 * <a href="https://github.com/cabaletta/baritone/pull/2544">...</a>
 *
 * @author rycbar
 * @since 22.09.2022
 */
public class LitematicaSchematic extends StaticSchematic implements ITransformableRegion {
    private static final Logger LOGGER = LogUtils.getLogger();

    //final private ITransformableRegion region;

    public Map<String, LitematicaSchematicRegion> getSubregions() {
        return subregions;
    }

    // private IRegion region;
    private Map<String, LitematicaSchematicRegion> subregions = new HashMap<String,LitematicaSchematicRegion>();  ;

    private final Vec3i offsetMinCorner;
    private final CompoundTag nbt;
    /**
     * whether the schematic has been rotated by 90 or 270 degrees
     */
    private final boolean rotated;

    /**
     * @param nbtTagCompound a decompressed file stream aka nbt data.
     * @param rotated        if the schematic is rotated by 90°.
     */
    public LitematicaSchematic(CompoundTag nbtTagCompound, boolean rotated) {
        this.nbt = nbtTagCompound;
        this.rotated = rotated;

        System.out.println("LOGGER "+LOGGER.isInfoEnabled());
        System.out.println("LOGGER "+LOGGER.isDebugEnabled());
        System.out.println("LOGGER "+LOGGER.isErrorEnabled());
        System.out.println("LOGGER "+LOGGER.isWarnEnabled());
        System.out.println("LOGGER "+LOGGER.isTraceEnabled());


        parseNbt();
        this.offsetMinCorner = new Vec3i(getMinOfSchematic("x"), getMinOfSchematic("y"), getMinOfSchematic("z"));

        System.out.println("state dims: x:" + this.x + ", y:" + this.y + ", z:" + this.z);
        System.out.println("offsetMinCorner: " + Stuff._p(offsetMinCorner) + " rotated: " + rotated + " in LitematicaSchematic contructor");
        this.states = new BlockState[this.x][this.z][this.y];
        fillInSchematic();
        //region = null;
    }

    private void parseNbt() {
        // @TODO implement this in mixin style
        CompoundTag metadata = nbt.getCompound("Metadata");
        this.x = Math.abs(metadata.getCompound("EnclosingSize").getInt("x"));
        this.z = Math.abs(metadata.getCompound("EnclosingSize").getInt("z"));
        this.y = Math.abs(metadata.getCompound("EnclosingSize").getInt("y"));
        if (rotated) {
            int temp = this.x;
            this.x = this.z;
            this.z = temp;
        }
        CompoundTag regions = nbt.getCompound("Regions");
        //this.subregions = new ISchematicRegion[regions.size()];
        regions.getAllKeys().forEach(this::parseNbtRegion);

    }

    protected void parseNbtRegion(String regionName) {
        this.subregions.put(regionName, LitematicaSchematicRegion.create(regionName,
                nbt.getCompound("Regions").getCompound(regionName))
        );
    }


    /**
     * reads the file data.
     */
    protected void fillInSchematic() {

        subregions.forEach((name, region) -> {
            System.out.println("fillInSchematic(start) for subregion: " + name);

            writeSubregionIntoSchematic(nbt, name, region.getBlockList(), region.bitArray);
            System.out.println("fillInSchematic(end) for subregion: " + name);
        });

    }

    /**
     * Writes the file data in to the IBlockstate array.
     * Walks the extents of the width, height and length of the schematic and fills
     * the IBlockstate array with the block types.
     *
     * @param blockList list with the different block types used in the schematic.
     * @param bitArray  bit array that holds the placement pattern.
     */
    void writeSubregionIntoSchematic(CompoundTag nbt, String subReg, BlockState[] blockList, LitematicaBitArray bitArray) {
        Vec3i offsetwSubregion = new Vec3i(getMinOfSubregion(nbt, subReg, "x"), getMinOfSubregion(nbt, subReg, "y"), getMinOfSubregion(nbt, subReg, "z"));
        int index = 0;
        for (int y = 0; y < this.y; y++) {
            for (int z = 0; z < this.z; z++) {
                for (int x = 0; x < this.x; x++) {
                    if (inSubregion(nbt, subReg, rotated ? z : x, y, rotated ? x : z, offsetMinCorner)) {
                        this.states[x][z][y] = blockList[bitArray.getAt(index)];
                        // this.states[x - (offsetMinCorner.getX() - offsetSubregion.getX())][z - (offsetMinCorner.getZ() - offsetSubregion.getZ())][y - (offsetMinCorner.getY() - offsetSubregion.getY())] = blockList[bitArray.getAt(index)];
                        index++;
                    }
                    //  if (inSubregion(nbt, subReg, x, y, z)) {
                    //                        this.states[x - (offsetMinCorner.getX() - offsetSubregion.getX())][z - (offsetMinCorner.getZ() - offsetSubregion.getZ())][y - (offsetMinCorner.getY() - offsetSubregion.getY())] = blockList[bitArray.getAt(index)];
                    //                        index++;
                    //                    }
                }
            }
        }
    }


    /**
     * @return Array of subregion names.
     */
    static String[] getRegions(CompoundTag nbt) {
        return nbt.getCompound("Regions").getAllKeys().toArray(new String[0]);
    }

    /**
     * Gets both ends from a region box for a given axis and returns the lower one.
     *
     * @param s axis that should be read.
     * @return the lower coord of the requested axis.
     */
    private static int getMinOfSubregion(CompoundTag nbt, String subReg, String s) {
        int a = nbt.getCompound("Regions").getCompound(subReg).getCompound("Position").getInt(s);
        int b = nbt.getCompound("Regions").getCompound(subReg).getCompound("Size").getInt(s);
        if (b < 0) {
            b++;
        }
        return Math.min(a, a + b);

    }







    @Override
    public boolean inSchematic(int x, int y, int z, BlockState currentState) {
        //  System.out.println("called inSchematic: " + x + "," + y + "," + z + " state: " + currentState);
        for (String subReg : getRegions(nbt)) {

            Vec3i offsetSubregion = new Vec3i(getMinOfSubregion(nbt, subReg, "x"), getMinOfSubregion(nbt, subReg, "y"), getMinOfSubregion(nbt, subReg, "z"));

            if (inSubregion(nbt, subReg, rotated ? z : x, y, rotated ? x : z, offsetMinCorner)) {
                System.out.println("found " + x + "," + y + "," + z + " in subregion: " + subReg + " state: " + currentState);
                return true;
            }
        }
        return false;
    }

    /**
     * Subregion don't have to be the same size as the enclosing size of the schematic. If they are smaller we check here if the current block is part of the subregion.
     *
     * @param x coord of the block relative to the minimum corner.
     * @param y coord of the block relative to the minimum corner.
     * @param z coord of the block relative to the minimum corner.
     * @return if the current block is part of the subregion.
     */
    private static boolean inSubregion(CompoundTag nbt, String subReg, int x, int y, int z, Vec3i offsetMinCorner) {

        //         return x >= 0 && y >= 0 && z >= 0 &&
        //                x < Math.abs(nbt.getCompound("Regions").getCompound(subReg).getCompound("Size").getInt("x")) &&
        //                y < Math.abs(nbt.getCompound("Regions").getCompound(subReg).getCompound("Size").getInt("y")) &&
        //                z < Math.abs(nbt.getCompound("Regions").getCompound(subReg).getCompound("Size").getInt("z"));

        CompoundTag region = nbt.getCompound("Regions").getCompound(subReg);

        int xPos = region.getCompound("Position").getInt("x") - offsetMinCorner.getX();
        int yPos = region.getCompound("Position").getInt("y") - offsetMinCorner.getY();
        int zPos = region.getCompound("Position").getInt("z") - offsetMinCorner.getZ();
        int xSize = region.getCompound("Size").getInt("x");
        int ySize = region.getCompound("Size").getInt("y");
        int zSize = region.getCompound("Size").getInt("z");

        int xMin = (xSize >= 0) ? xPos : xPos + xSize + 1;
        int yMin = (ySize >= 0) ? yPos : yPos + ySize + 1;
        int zMin = (zSize >= 0) ? zPos : zPos + zSize + 1;

        int xMax = xMin + Math.abs(xSize);
        int yMax = yMin + Math.abs(ySize);
        int zMax = zMin + Math.abs(zSize);

        boolean withinX = xSize != 0 && x >= xMin && x < xMax;
        boolean withinY = ySize != 0 && y >= yMin && y < yMax;
        boolean withinZ = zSize != 0 && z >= zMin && z < zMax;

        boolean found = withinX && withinY && withinZ;

        LOGGER.trace("params x,y,z: (" + x + "," + y + "," + z + ") pos: (" + xPos + "," + yPos + "," + zPos + ") offset: " + Stuff._p(offsetMinCorner) + ") size: (" + xSize + "," + ySize + "," + zSize + ") min: (" + xMin + "," + yMin + "," + zMin + ") max: (" + xMax + "," + yMax + "," + zMax + ") found: " + (found ? "true " : "false") + " region: " + subReg);


        return found;
    }

    /**
     * @param s axis.
     * @return the lowest coordinate of that axis of the schematic.
     */
    private int getMinOfSchematic(String s) {
        int n = Integer.MAX_VALUE;
        for (String subReg : getRegions(nbt)) {
            n = Math.min(n, getMinOfSubregion(nbt, subReg, s));
        }
        return n;
    }

    /**
     * @return offset from the schematic origin to the minimum Corner as a Vec3i.
     */
    public Vec3i getOffsetMinCorner() {
        return offsetMinCorner;
    }

    /**
     * @return x size of the schematic.
     */
    public int getX() {
        return this.x;
    }

    /**
     * @return y size of the schematic.
     */
    public int getY() {
        return this.y;
    }

    /**
     * @return z size of the schematic.
     */
    public int getZ() {
        return this.z;
    }

    /**
     * @param x          position relative to the minimum corner of the schematic.
     * @param y          position relative to the minimum corner of the schematic.
     * @param z          position relative to the minimum corner of the schematic.
     * @param blockState new blockstate of the block at this position.
     */
    public void setDirect(int x, int y, int z, BlockState blockState) {
        this.states[x][z][y] = blockState;
    }

    /**
     * @param rotated if the schematic is rotated by 90°.
     * @return a copy of the schematic.
     */
    public LitematicaSchematic getCopy(boolean rotated) {
        return new LitematicaSchematic(nbt, rotated);
    }

    @Override
    public BetterBlockPos pos1() {
        return null;
    }

    @Override
    public BetterBlockPos pos2() {
        return null;
    }

    /**
     * @author maruohon
     * Class from the Litematica mod by maruohon
     * Usage under LGPLv3 with the permission of the author.
     * <a href="https://github.com/maruohon/litematica">...</a>
     */
    static class LitematicaBitArray {
        /**
         * The long array that is used to store the data for this BitArray.
         */
        private final long[] longArray;
        /**
         * Number of bits a single entry takes up
         */
        private final int bitsPerEntry;
        /**
         * The maximum value for a single entry. This also works as a bitmask for a single entry.
         * For instance, if bitsPerEntry were 5, this value would be 31 (ie, {@code 0b00011111}).
         */
        private final long maxEntryValue;
        /**
         * Number of entries in this array (<b>not</b> the length of the long array that internally backs this array)
         */
        private final long arraySize;

        public LitematicaBitArray(int bitsPerEntryIn, long arraySizeIn, @Nullable long[] longArrayIn) {
            Validate.inclusiveBetween(1L, 32L, bitsPerEntryIn);
            this.arraySize = arraySizeIn;
            this.bitsPerEntry = bitsPerEntryIn;
            this.maxEntryValue = (1L << bitsPerEntryIn) - 1L;

            if (longArrayIn != null) {
                this.longArray = longArrayIn;
            } else {
                this.longArray = new long[(int) (roundUp(arraySizeIn * (long) bitsPerEntryIn, 64L) / 64L)];
            }
        }

        public static long roundUp(long number, long interval) {
            int sign = 1;
            if (interval == 0) {
                return 0;
            } else if (number == 0) {
                return interval;
            } else {
                if (number < 0) {
                    sign = -1;
                }

                long i = number % (interval * sign);
                return i == 0 ? number : number + (interval * sign) - i;
            }
        }

        public int getAt(long index) {
            Validate.inclusiveBetween(0L, this.arraySize - 1L, index);
            long startOffset = index * (long) this.bitsPerEntry;
            int startArrIndex = (int) (startOffset >> 6); // startOffset / 64
            int endArrIndex = (int) (((index + 1L) * (long) this.bitsPerEntry - 1L) >> 6);
            int startBitOffset = (int) (startOffset & 0x3F); // startOffset % 64

            if (startArrIndex == endArrIndex) {
                return (int) (this.longArray[startArrIndex] >>> startBitOffset & this.maxEntryValue);
            } else {
                int endOffset = 64 - startBitOffset;
                return (int) ((this.longArray[startArrIndex] >>> startBitOffset | this.longArray[endArrIndex] << endOffset) & this.maxEntryValue);
            }
        }

        public long size() {
            return this.arraySize;
        }
    }
}
