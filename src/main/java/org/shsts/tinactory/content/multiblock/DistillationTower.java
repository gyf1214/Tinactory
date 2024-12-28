package org.shsts.tinactory.content.multiblock;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.multiblock.MultiBlock;

import static org.shsts.tinactory.content.AllLayouts.DISTILLATION_TOWER;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DistillationTower extends MultiBlock {
    private int height = 0;

    public DistillationTower(BlockEntity blockEntity, Builder<?> builder) {
        super(blockEntity, builder.layout(Layout.EMPTY));
    }

    @Override
    protected void doCheckMultiBlock(CheckContext ctx) {
        super.doCheckMultiBlock(ctx);
        if (!ctx.isFailed()) {
            height = (int) ctx.getProperty("height");
            setLayout(DISTILLATION_TOWER.get(height - 3));
        }
    }

    @Override
    public CompoundTag serializeOnUpdate() {
        var tag = super.serializeOnUpdate();
        if (multiBlockInterface != null) {
            tag.putInt("height", height);
        }
        return tag;
    }

    @Override
    public void deserializeOnUpdate(CompoundTag tag) {
        super.deserializeOnUpdate(tag);
        if (tag.contains("height", Tag.TAG_INT)) {
            height = tag.getInt("height");
            setLayout(DISTILLATION_TOWER.get(height - 3));
        }
    }
}
