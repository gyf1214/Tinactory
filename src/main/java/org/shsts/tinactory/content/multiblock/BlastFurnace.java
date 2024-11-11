package org.shsts.tinactory.content.multiblock;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.core.common.SmartBlockEntity;
import org.shsts.tinactory.core.multiblock.MultiBlock;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.OptionalInt;

import static org.shsts.tinactory.Tinactory.REGISTRATE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BlastFurnace extends MultiBlock {
    @Nullable
    private CoilBlock coilBlock = null;

    public BlastFurnace(SmartBlockEntity blockEntity, Builder<?> builder) {
        super(blockEntity, builder);
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
            var block = REGISTRATE.blockHandler.getEntry(loc).get();
            if (block instanceof CoilBlock coilBlock1) {
                coilBlock = coilBlock1;
            }
        }
        super.deserializeOnUpdate(tag);
    }
}
