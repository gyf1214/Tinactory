package org.shsts.tinactory.core.autocraft.api;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.model.IMachineConstraint;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IMachineConstraintType<T extends IMachineConstraint> {
    String id();

    Class<T> constraintClass();
}
