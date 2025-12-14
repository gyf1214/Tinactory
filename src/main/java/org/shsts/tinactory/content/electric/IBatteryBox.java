package org.shsts.tinactory.content.electric;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.machine.IProcessor;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IBatteryBox extends IProcessor {
    long powerLevel();

    long powerCapacity();

    @Override
    default double getProgress() {
        var cap = powerCapacity();
        if (cap <= 0) {
            return 0d;
        }
        return ((double) powerLevel()) / cap;
    }
}
