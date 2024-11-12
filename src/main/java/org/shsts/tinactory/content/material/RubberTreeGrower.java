package org.shsts.tinactory.content.material;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.shsts.tinactory.content.AllWorldGens;
import org.shsts.tinactory.core.util.ServerUtil;

import java.util.Random;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RubberTreeGrower extends AbstractTreeGrower {
    @Nullable
    @Override
    protected Holder<? extends ConfiguredFeature<?, ?>> getConfiguredFeature(Random random, boolean largeHive) {
        var registry = ServerUtil.getRegistry(Registry.CONFIGURED_FEATURE_REGISTRY);
        return registry.getHolder(AllWorldGens.RUBBER_TREE_GROWER).orElse(null);
    }
}
