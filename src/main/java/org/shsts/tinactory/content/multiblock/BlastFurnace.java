package org.shsts.tinactory.content.multiblock;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.shsts.tinactory.core.multiblock.MultiBlock;

import java.util.OptionalInt;

import static org.shsts.tinactory.content.AllLayouts.BLAST_FURNACE;
import static org.shsts.tinactory.content.AllRegistries.BLOCKS;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BlastFurnace extends MultiBlock {
    @Nullable
    private CoilBlock coilBlock = null;

    public BlastFurnace(BlockEntity blockEntity, Builder<?> builder) {
        super(blockEntity, builder.layout(BLAST_FURNACE));
    }

    @Override
    protected void doCheckMultiBlock(CheckContext ctx) {
        super.doCheckMultiBlock(ctx);
        if (ctx.hasProperty("coil") && ctx.getProperty("coil") instanceof CoilBlock coil) {
            coilBlock = coil;
        } else {
            ctx.setFailed();
        }
    }

    public OptionalInt getTemperature() {
        return coilBlock == null ? OptionalInt.empty() : OptionalInt.of(coilBlock.temperature);
    }

    @Override
    public CompoundTag serializeOnUpdate() {
        var tag = super.serializeOnUpdate();
        if (coilBlock != null && coilBlock.getRegistryName() != null) {
            tag.putString("coilBlock", coilBlock.getRegistryName().toString());
        }
        return tag;
    }

    @Override
    public void deserializeOnUpdate(CompoundTag tag) {
        coilBlock = null;
        if (tag.contains("coilBlock", Tag.TAG_STRING)) {
            var loc = new ResourceLocation(tag.getString("coilBlock"));
            var block = BLOCKS.getEntry(loc).get();
            if (block instanceof CoilBlock coilBlock1) {
                coilBlock = coilBlock1;
            }
        }
        super.deserializeOnUpdate(tag);
    }
}
