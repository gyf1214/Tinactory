package org.shsts.tinactory.content;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.shsts.tinactory.Tinactory;

public final class AllWorldGen {
    public static final ResourceKey<Level> VOID_LEVEL;

    static {
        VOID_LEVEL = ResourceKey.create(Registry.DIMENSION_REGISTRY, Tinactory.modLoc("void"));
    }

    public static void init() {}
}
