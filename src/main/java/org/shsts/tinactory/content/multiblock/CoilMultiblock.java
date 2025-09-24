package org.shsts.tinactory.content.multiblock;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.core.multiblock.Multiblock;
import org.shsts.tinactory.core.multiblock.MultiblockInterface;

import java.util.OptionalInt;

import static org.shsts.tinactory.content.AllRegistries.BLOCKS;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CoilMultiblock extends Multiblock {
    @Nullable
    private CoilBlock coilBlock = null;

    public CoilMultiblock(BlockEntity blockEntity, Builder<?> builder) {
        super(blockEntity, builder);
    }

    @Override
    protected void doCheckMultiblock(CheckContext ctx) {
        super.doCheckMultiblock(ctx);
        if (ctx.hasProperty("coil") && ctx.getProperty("coil") instanceof CoilBlock coil) {
            coilBlock = coil;
        } else {
            ctx.setFailed();
        }
    }

    public OptionalInt getTemperature() {
        return coilBlock == null ? OptionalInt.empty() : OptionalInt.of(coilBlock.temperature);
    }

    public static OptionalInt getTemperature(IMachine machine) {
        if (!(machine instanceof MultiblockInterface multiblockInterface)) {
            return OptionalInt.empty();
        }
        return multiblockInterface.getMultiblock()
            .filter($ -> $ instanceof CoilMultiblock)
            .map($ -> ((CoilMultiblock) $).getTemperature())
            .orElse(OptionalInt.empty());
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
