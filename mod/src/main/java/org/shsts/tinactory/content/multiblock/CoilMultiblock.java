package org.shsts.tinactory.content.multiblock;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.multiblock.IMultiblockCheckCtx;
import org.shsts.tinactory.integration.multiblock.Multiblock;
import org.shsts.tinactory.integration.multiblock.MultiblockInterface;

import java.util.OptionalInt;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CoilMultiblock extends Multiblock {
    @Nullable
    private CoilBlock coilBlock = null;

    public CoilMultiblock(BlockEntity blockEntity, Builder<?> builder) {
        super(blockEntity, builder);
    }

    @Override
    protected void doCheckStructure(IMultiblockCheckCtx<BlockState> ctx) {
        super.doCheckStructure(ctx);
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
    public CompoundTag serializeOnUpdate(HolderLookup.Provider provider) {
        var tag = super.serializeOnUpdate(provider);
        if (coilBlock != null) {
            provider.lookup(Registries.BLOCK)
                .flatMap(registry -> registry.listElements()
                    .filter($ -> $.value() == coilBlock)
                    .map($ -> $.key().location())
                    .findFirst())
                .ifPresent(loc -> tag.putString("coilBlock", loc.toString()));
        }
        return tag;
    }

    @Override
    public void deserializeOnUpdate(HolderLookup.Provider provider, CompoundTag tag) {
        coilBlock = null;
        if (tag.contains("coilBlock", Tag.TAG_STRING)) {
            var loc = ResourceLocation.parse(tag.getString("coilBlock"));
            var key = ResourceKey.create(Registries.BLOCK, loc);
            provider.lookup(Registries.BLOCK)
                .flatMap(registry -> registry.get(key))
                .map(Holder.Reference::value)
                .filter($ -> $ instanceof CoilBlock)
                .map($ -> (CoilBlock) $)
                .ifPresent($ -> coilBlock = $);
        }
        super.deserializeOnUpdate(provider, tag);
    }
}
