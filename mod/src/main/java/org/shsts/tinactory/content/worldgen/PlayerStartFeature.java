package org.shsts.tinactory.content.worldgen;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.shsts.tinactory.AllItems;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PlayerStartFeature extends Feature<NoneFeatureConfiguration> {
    public PlayerStartFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        var world = ctx.level();
        var pos = ctx.origin();
        for (var i = -1; i < 2; i++) {
            for (var j = -1; j < 2; j++) {
                world.setBlock(pos.offset(i, 0, j), Blocks.OBSIDIAN.defaultBlockState(), 3);
            }
        }
        var chestPos = pos.offset(0, 1, 1);
        world.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 3);
        world.setBlock(chestPos.east(), Blocks.TORCH.defaultBlockState(), 3);
        world.setBlock(chestPos.west(), Blocks.TORCH.defaultBlockState(), 3);
        world.getBlockEntity(chestPos, BlockEntityType.CHEST).ifPresent(chest -> {
            chest.setItem(0, new ItemStack(Blocks.GRASS_BLOCK, 64));
            chest.setItem(1, new ItemStack(Blocks.DIRT, 64));
            chest.setItem(2, new ItemStack(Items.OAK_SAPLING, 16));
            chest.setItem(3, new ItemStack(AllItems.RUBBER_SAPLING.get(), 16));
            chest.setItem(4, new ItemStack(Items.BONE_MEAL, 16));
            chest.setItem(5, new ItemStack(Items.WATER_BUCKET));
            chest.setItem(6, new ItemStack(Items.WATER_BUCKET));
            chest.setItem(7, new ItemStack(Items.TORCH, 64));
        });
        return true;
    }
}
