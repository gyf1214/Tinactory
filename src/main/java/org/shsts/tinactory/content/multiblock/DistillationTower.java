package org.shsts.tinactory.content.multiblock;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.multiblock.Multiblock;

import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DistillationTower extends Multiblock {
    private final List<Layout> layouts;
    private int slots = 0;

    public DistillationTower(BlockEntity blockEntity, Builder<?> builder, List<Layout> layouts) {
        super(blockEntity, builder);
        this.layouts = layouts;
    }

    public int getSlots() {
        return slots;
    }

    @Override
    protected void doCheckMultiblock(CheckContext ctx) {
        super.doCheckMultiblock(ctx);
        if (!ctx.isFailed()) {
            slots = (int) ctx.getProperty("height") - 2;
            layout = layouts.get(slots - 1);
        }
    }

    @Override
    public CompoundTag serializeOnUpdate() {
        var tag = super.serializeOnUpdate();
        if (multiblockInterface != null) {
            tag.putInt("slots", slots);
        }
        return tag;
    }

    @Override
    protected void updateMultiblockInterface() {
        super.updateMultiblockInterface();
        if (multiblockInterface != null && layout != null) {
            multiblockInterface.setContainerLayout(layout);
        }
    }

    @Override
    public void deserializeOnUpdate(CompoundTag tag) {
        if (tag.contains("slots", Tag.TAG_INT)) {
            slots = tag.getInt("slots");
            layout = layouts.get(slots - 1);
        }
        super.deserializeOnUpdate(tag);
    }
}
