package org.shsts.tinactory.api.tech;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface ITechnology {
    ResourceLocation getLoc();

    Collection<ITechnology> getDepends();

    long getMaxProgress();
}
