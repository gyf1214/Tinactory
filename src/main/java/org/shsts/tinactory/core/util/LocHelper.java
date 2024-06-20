package org.shsts.tinactory.core.util;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.StringUtils;
import org.shsts.tinactory.Tinactory;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class LocHelper {
    public static ResourceLocation mcLoc(String id) {
        return new ResourceLocation(id);
    }

    public static ResourceLocation modLoc(String id) {
        return new ResourceLocation(Tinactory.ID, id);
    }

    public static ResourceLocation gregtech(String id) {
        return new ResourceLocation("gregtech", id);
    }

    public static ResourceLocation extend(ResourceLocation loc, String suffix) {
        if (StringUtils.isEmpty(suffix)) {
            return loc;
        }
        return new ResourceLocation(loc.getNamespace(), loc.getPath() + "/" + suffix);
    }

    public static ResourceLocation suffix(ResourceLocation loc, String suffix) {
        return new ResourceLocation(loc.getNamespace(), loc.getPath() + suffix);
    }

    public static ResourceLocation prepend(ResourceLocation loc, String prefix) {
        if (StringUtils.isEmpty(prefix)) {
            return loc;
        }
        return new ResourceLocation(loc.getNamespace(), prefix + "/" + loc.getPath());
    }
}
