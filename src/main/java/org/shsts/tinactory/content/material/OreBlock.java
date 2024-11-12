package org.shsts.tinactory.content.material;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.Block;

import java.util.function.Function;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class OreBlock extends Block {
    public final OreVariant variant;

    public OreBlock(Properties properties, OreVariant variant) {
        super(properties.requiresCorrectToolForDrops());
        this.variant = variant;
    }

    public static Function<Properties, OreBlock> factory(OreVariant variant) {
        return p -> new OreBlock(p, variant);
    }
}
