package org.shsts.tinactory.content.multiblock;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.multiblock.Multiblock;

import static org.shsts.tinactory.content.AllLayouts.DISTILLATION_TOWER;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DistillationTower extends Multiblock {
    private int height = 0;

    public DistillationTower(BlockEntity blockEntity, Builder<?> builder) {
        super(blockEntity, builder.layout(Layout.EMPTY));
    }

    public int getSlots() {
        return height - 2;
    }

    @Override
    protected void doCheckMultiblock(CheckContext ctx) {
        super.doCheckMultiblock(ctx);
        if (!ctx.isFailed()) {
            height = (int) ctx.getProperty("height");
            setLayout(DISTILLATION_TOWER.get(height - 3));
        }
    }

    @Override
    public CompoundTag serializeOnUpdate() {
        var tag = super.serializeOnUpdate();
        if (multiblockInterface != null) {
            tag.putInt("height", height);
        }
        return tag;
    }

    @Override
    protected void updateMultiblockInterface() {
        super.updateMultiblockInterface();
        if (multiblockInterface != null) {
            setLayout(DISTILLATION_TOWER.get(height - 3));
        }
    }

    @Override
    public void deserializeOnUpdate(CompoundTag tag) {
        if (tag.contains("height", Tag.TAG_INT)) {
            height = tag.getInt("height");
        }
        super.deserializeOnUpdate(tag);
    }
}
