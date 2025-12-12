package org.shsts.tinactory.api.tech;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.api.gui.client.IRenderable;
import org.shsts.tinycorelib.api.core.DistLazy;

import java.util.List;
import java.util.Map;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface ITechnology extends Comparable<ITechnology> {
    ResourceLocation getLoc();

    List<ITechnology> getDepends();

    Map<String, Integer> getModifiers();

    long getMaxProgress();

    DistLazy<IRenderable> getDisplay();

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
