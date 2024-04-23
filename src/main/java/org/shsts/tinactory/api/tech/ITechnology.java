package org.shsts.tinactory.api.tech;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface ITechnology extends IForgeRegistryEntry<ITechnology> {
    Collection<? extends ITechnology> getDepends();

    long getMaxProgress();
}
