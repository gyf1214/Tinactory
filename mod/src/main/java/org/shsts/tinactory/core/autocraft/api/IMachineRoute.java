package org.shsts.tinactory.core.autocraft.api;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.logistics.IIngredientKey;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IMachineRoute {
    enum Direction {
        INPUT,
        OUTPUT,
    }

    IIngredientKey key();

    Direction direction();

    long transfer(long amount, boolean simulate);
}
