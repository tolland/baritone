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

package baritone.process;

import baritone.Baritone;
import baritone.api.event.events.RenderEvent;
import baritone.api.event.listener.AbstractGameEventListener;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.pathing.goals.GoalComposite;
import baritone.api.process.ICollectProcess;
import baritone.api.process.PathingCommand;
import baritone.api.process.PathingCommandType;
import baritone.api.utils.BetterBlockPos;
import baritone.utils.BaritoneProcessHelper;
import baritone.utils.IRenderer;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public final class CollectProcess extends BaritoneProcessHelper implements ICollectProcess, AbstractGameEventListener {

    private boolean active;
    private List<Item> itemsToCollect;
    private int range;
    private BlockPos startPosition;
    private final List<Vec3> currentTargetItems = new ArrayList<>();

    public CollectProcess(Baritone baritone) {
        super(baritone);
        baritone.getGameEventHandler().registerEventListener(this);
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void collect(List<Item> items, int range) {
        collect(items, range, baritone.getPlayerContext().playerFeet());
    }

    @Override
    public void collect(List<Item> items, int range, BlockPos origin) {
        if (items == null || items.isEmpty()) {
            logDirect("No items specified to collect");
            return;
        }

        this.itemsToCollect = new ArrayList<>(items);
        this.startPosition = origin;
        this.range = range;
        active = true;
    }

    @Override
    public PathingCommand onTick(boolean calcFailed, boolean isSafeToCancel) {
        currentTargetItems.clear();
        if (itemsToCollect == null || itemsToCollect.isEmpty()) {
            logDirect("No items to collect");
            onLostControl();
            return new PathingCommand(null, PathingCommandType.REQUEST_PAUSE);
        }

        if (calcFailed) {
            logDirect("Collect failed - unable to path to items");
            if (Baritone.settings().notificationOnFarmFail.value) {
                logNotification("Collect failed", true);
            }
            onLostControl();
            return new PathingCommand(null, PathingCommandType.REQUEST_PAUSE);
        }

        List<Goal> goals = new ArrayList<>();

        // Find all dropped items matching our filter
        for (Entity entity : ctx.entities()) {
            if (entity instanceof ItemEntity && entity.onGround()) {
                ItemEntity itemEntity = (ItemEntity) entity;
                Item item = itemEntity.getItem().getItem();

                // Check if this item is in our collection list
                if (itemsToCollect.contains(item)) {
                    // Check if the item is within range (0 = unlimited)
                    if (range == 0 || BlockPos.containing(entity.position()).distSqr(startPosition) <= range * range) {
                        // +0.1 because of farmland's 0.9375 dummy height
                        goals.add(new GoalBlock(new BetterBlockPos(entity.position().x, entity.position().y + 0.1, entity.position().z)));
                        currentTargetItems.add(entity.position());
                    }
                }
            }
        }

        if (goals.isEmpty()) {
            logDirect("No more items to collect");
            onLostControl();
            return new PathingCommand(null, PathingCommandType.REQUEST_PAUSE);
        }

        return new PathingCommand(new GoalComposite(goals.toArray(new Goal[0])), PathingCommandType.SET_GOAL_AND_PATH);
    }

    @Override
    public void onLostControl() {
        active = false;
        itemsToCollect = null;
        currentTargetItems.clear();
    }

    @Override
    public void onRenderPass(RenderEvent event) {
        if (!isActive()) {
            return;
        }

        // 1. Draw the collection range as a bounding box
        if (startPosition != null && range > 0) {
            drawCollectionRange(event.getModelViewStack(), startPosition, range);
        }

        // 2. Draw lines to nearby items
        if (!currentTargetItems.isEmpty()) {
            drawItemLines(event.getModelViewStack());
        }
    }

    private void drawCollectionRange(PoseStack stack, BlockPos origin, int range) {
        // Create AABB for the collection range
        AABB rangeBox = new AABB(
                origin.getX() - range, origin.getY() - range, origin.getZ() - range,
                origin.getX() + range + 1, origin.getY() + range + 1, origin.getZ() + range + 1
        );

        // Start rendering with a specific color
        BufferBuilder bufferBuilder = IRenderer.startLines(Color.CYAN, 0.4f, Baritone.settings().pathRenderLineWidthPixels.value);

        // Draw the bounding box
        IRenderer.emitAABB(bufferBuilder, stack, rangeBox);

        // End rendering
        IRenderer.endLines(bufferBuilder, Baritone.settings().renderPathIgnoreDepth.value);
    }

    private void drawItemLines(PoseStack stack) {
        Vec3 playerPos = ctx.player().position();

        BufferBuilder bufferBuilder = IRenderer.startLines(Color.GREEN, 0.4f, Baritone.settings().pathRenderLineWidthPixels.value);

        for (Vec3 itemPos : currentTargetItems) {
            // Draw line from player to each item
            IRenderer.emitLine(bufferBuilder, stack, playerPos, itemPos);
        }

        IRenderer.endLines(bufferBuilder, Baritone.settings().renderPathIgnoreDepth.value);
    }

    @Override
    public String displayName0() {
        return "Collecting Items";
    }
}
