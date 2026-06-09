package org.shsts.tinactory.core.autocraft.plan;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record AutocraftMemoryConfig(
    long bytesPerStep,
    long bytesPerItem,
    long bytesPerItemType,
    long bytesPerFluid,
    long bytesPerFluidType) {
    public static final AutocraftMemoryConfig NONE = new AutocraftMemoryConfig(0L, 0L, 0L, 0L, 0L);

    public AutocraftMemoryConfig {
        bytesPerStep = Math.max(0L, bytesPerStep);
        bytesPerItem = Math.max(0L, bytesPerItem);
        bytesPerItemType = Math.max(0L, bytesPerItemType);
        bytesPerFluid = Math.max(0L, bytesPerFluid);
        bytesPerFluidType = Math.max(0L, bytesPerFluidType);
    }
}
