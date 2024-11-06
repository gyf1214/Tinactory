package org.shsts.tinactory.content.multiblock;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.Block;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Function;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CoilBlock extends Block {
    public final int temperature;

    public CoilBlock(Properties properties, int temperature) {
        super(properties);
        this.temperature = temperature;
    }

    public static Function<Properties, CoilBlock> factory(int temperature) {
        return prop -> new CoilBlock(prop, temperature);
    }
}
