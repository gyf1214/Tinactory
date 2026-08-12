package org.shsts.tinactory.content.worldgen;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;

import static org.shsts.tinactory.core.util.LocHelper.modLoc;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PlayerStartFeature extends Feature<NoneFeatureConfiguration> {
    private static final ResourceLocation STRUCTURE = modLoc("player_start");

    public PlayerStartFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        var world = ctx.level();
        var pos = ctx.origin();
        var structure = world.getLevel().getStructureManager().getOrCreate(STRUCTURE);
        structure.placeInWorld(world, pos, pos, new StructurePlaceSettings(),
            ctx.random(), Block.UPDATE_CLIENTS);
        return true;
    }
}
