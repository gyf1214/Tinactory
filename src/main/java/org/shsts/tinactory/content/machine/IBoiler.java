package org.shsts.tinactory.content.machine;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.machine.IMachineProcessor;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IBoiler extends IMachineProcessor {
    /**
     * This is only for display purposes.
     */
    double maxHeat();

    double heat();

    default double heatProgress() {
        return heat() / maxHeat();
    }
}
