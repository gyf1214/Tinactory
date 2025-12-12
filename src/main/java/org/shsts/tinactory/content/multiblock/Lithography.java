package org.shsts.tinactory.content.multiblock;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.shsts.tinactory.core.multiblock.Multiblock;

import java.util.Collection;
import java.util.Collections;

import static org.shsts.tinactory.AllRegistries.BLOCKS;

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
    protected void doCheckMultiblock(CheckContext ctx) {
        super.doCheckMultiblock(ctx);
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
    public CompoundTag serializeOnUpdate() {
        var tag = super.serializeOnUpdate();
        if (lensBlock != null && lensBlock.getRegistryName() != null) {
            tag.putString("lensBlock", lensBlock.getRegistryName().toString());
        }
        return tag;
    }

    @Override
    public void deserializeOnUpdate(CompoundTag tag) {
        lensBlock = null;
        if (tag.contains("lensBlock", Tag.TAG_STRING)) {
            var loc = new ResourceLocation(tag.getString("lensBlock"));
            var block = BLOCKS.getEntry(loc).get();
            if (block instanceof LensBlock lensBlock1) {
                lensBlock = lensBlock1;
            }
        }
        super.deserializeOnUpdate(tag);
    }
}
