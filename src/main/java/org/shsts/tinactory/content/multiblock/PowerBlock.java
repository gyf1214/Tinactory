package org.shsts.tinactory.content.multiblock;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.Block;
import org.shsts.tinactory.core.electric.Voltage;

import java.util.function.Function;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PowerBlock extends Block {
    public final long voltage;
    public final long capacity;

    public PowerBlock(Properties properties, long voltage, long capacity) {
        super(properties);
        this.voltage = voltage;
        this.capacity = capacity;
    }

    public static Function<Properties, PowerBlock> factory(Voltage v, long capacity) {
        return properties -> new PowerBlock(properties, v.value, capacity);
    }
}
