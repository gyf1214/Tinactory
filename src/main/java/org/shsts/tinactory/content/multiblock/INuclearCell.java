package org.shsts.tinactory.content.multiblock;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface INuclearCell {
    double getFastNeutron();

    double getSlowNeutron();

    double getHeat();

    void incFastNeutron(double val);

    void incSlowNeutron(double val);

    void incHeat(double val);
}
