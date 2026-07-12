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
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.shsts.tinactory.api.multiblock.IMultiblockCheckCtx;
import org.shsts.tinactory.integration.multiblock.Multiblock;

import java.util.Collection;
import java.util.Collections;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Lithography extends Multiblock {
    private final double cleannessFactor;

    @Nullable
    private LensBlock lensBlock = null;

    public Lithography(BlockEntity blockEntity, Builder<?> builder, double cleannessFactor) {
        super(blockEntity, builder);
        this.cleannessFactor = cleannessFactor;
    }

    @Override
    protected void doCheckStructure(IMultiblockCheckCtx<BlockState> ctx) {
        super.doCheckStructure(ctx);
        if (ctx.hasProperty("lens") && ctx.getProperty("lens") instanceof LensBlock block) {
            lensBlock = block;
        } else {
            ctx.setFailed();
        }
    }

    public Collection<Item> getLens() {
        return lensBlock == null ? Collections.emptyList() : lensBlock.getLens();
    }

    public double getCleannessFactor() {
        return cleannessFactor;
    }

    @Override
    public CompoundTag serializeOnUpdate(HolderLookup.Provider provider) {
        var tag = super.serializeOnUpdate(provider);
        if (lensBlock != null) {
            provider.lookup(Registries.BLOCK)
                .flatMap(registry -> registry.listElements()
                    .filter($ -> $.value() == lensBlock)
                    .map($ -> $.key().location())
                    .findFirst())
                .ifPresent(loc -> tag.putString("lensBlock", loc.toString()));
        }
        return tag;
    }

    @Override
    public void deserializeOnUpdate(HolderLookup.Provider provider, CompoundTag tag) {
        lensBlock = null;
        if (tag.contains("lensBlock", Tag.TAG_STRING)) {
            var loc = ResourceLocation.parse(tag.getString("lensBlock"));
            var key = ResourceKey.create(Registries.BLOCK, loc);
            provider.lookup(Registries.BLOCK)
                .flatMap(registry -> registry.get(key))
                .map(Holder.Reference::value)
                .filter($ -> $ instanceof LensBlock)
                .map($ -> (LensBlock) $)
                .ifPresent($ -> lensBlock = $);
        }
        super.deserializeOnUpdate(provider, tag);
    }
}
