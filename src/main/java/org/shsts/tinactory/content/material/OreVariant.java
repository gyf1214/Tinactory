package org.shsts.tinactory.content.material;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public enum OreVariant implements StringRepresentable {
    STONE(Blocks.STONE), DEEPSLATE(Blocks.DEEPSLATE);

    public final ResourceLocation baseBlock;

    OreVariant(Block baseBlock) {
        assert baseBlock.getRegistryName() != null;
        this.baseBlock = baseBlock.getRegistryName();
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase();
    }
}
