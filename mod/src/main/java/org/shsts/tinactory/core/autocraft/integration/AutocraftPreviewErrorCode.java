package org.shsts.tinactory.core.autocraft.integration;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public enum AutocraftPreviewErrorCode {
    INVALID_REQUEST,
    CPU_NOT_VISIBLE,
    CPU_OFFLINE,
    CPU_BUSY,
    PREVIEW_FAILED
}
