package org.shsts.tinactory.api.tech;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.Map;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface ITechnology {
    ResourceLocation getLoc();

    Component getName();

    Collection<ITechnology> getDepends();

    Map<String, Integer> getModifiers();

    long getMaxProgress();
}
