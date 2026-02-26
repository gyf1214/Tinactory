package org.shsts.tinactory.core.autocraft.integration;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public enum AutocraftSubmitErrorCode {
    CPU_NOT_VISIBLE,
    CPU_OFFLINE,
    CPU_BUSY,
    INVALID_REQUEST
}
