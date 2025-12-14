package org.shsts.tinactory.content.machine;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.machine.IMachineProcessor;
import org.shsts.tinactory.core.util.MathUtil;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IBoiler extends IMachineProcessor {
    /**
     * This is only for display purposes.
     */
    double minHeat();

    /**
     * This is only for display purposes.
     */
    double maxHeat();

    double heat();

    default double heatProgress() {
        return MathUtil.clamp((heat() - minHeat()) / (maxHeat() - minHeat()), 0d, 1d);
    }
}
