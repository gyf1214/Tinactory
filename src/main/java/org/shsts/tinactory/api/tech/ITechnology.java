package org.shsts.tinactory.api.tech;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface ITechnology extends Comparable<ITechnology> {
    ResourceLocation getLoc();

    List<ITechnology> getDepends();

    Map<String, Integer> getModifiers();

    long getMaxProgress();

    ItemStack getDisplayItem();

    ResourceLocation getDisplayTexture();

    static String getDescriptionId(ResourceLocation loc) {
        return loc.getNamespace() + ".technology." + loc.getPath().replace('/', '.');
    }

    static String getDetailsId(ResourceLocation loc) {
        return getDescriptionId(loc) + ".details";
    }

    default String getDescriptionId() {
        return getDescriptionId(getLoc());
    }

    default String getDetailsId() {
        return getDetailsId(getLoc());
    }
}
