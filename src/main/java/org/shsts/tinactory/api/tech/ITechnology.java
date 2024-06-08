package org.shsts.tinactory.api.tech;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

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

    ItemStack getDisplayItem();

    static String getDescriptionId(ResourceLocation loc) {
        return loc.getNamespace() + ".technology." + loc.getPath().replace('/', '.');
    }

    static String getDetailsId(ResourceLocation loc) {
        return getDescriptionId(loc) + ".details;";
    }

    default String getDescriptionId() {
        return getDescriptionId(getLoc());
    }

    default String getDetailsId() {
        return getDetailsId(getLoc());
    }
}
