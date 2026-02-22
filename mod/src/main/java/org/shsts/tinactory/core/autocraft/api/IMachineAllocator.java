package org.shsts.tinactory.core.autocraft.api;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.model.MachineRequirement;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IMachineAllocator {
    boolean canRun(MachineRequirement requirement);
}
