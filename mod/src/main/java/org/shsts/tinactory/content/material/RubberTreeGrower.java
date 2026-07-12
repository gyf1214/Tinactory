package org.shsts.tinactory.content.material;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.grower.TreeGrower;
import org.shsts.tinactory.AllWorldGens;

import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class RubberTreeGrower {
    public static final TreeGrower INSTANCE = new TreeGrower("tinactory_rubber",
        Optional.empty(), Optional.of(AllWorldGens.RUBBER_TREE_GROWER), Optional.empty());

    private RubberTreeGrower() {}
}
