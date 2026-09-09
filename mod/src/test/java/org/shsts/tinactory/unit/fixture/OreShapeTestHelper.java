package org.shsts.tinactory.unit.fixture;

import com.mojang.serialization.Lifecycle;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.shsts.tinactory.core.worldgen.ore.EllipsoidShape;
import org.shsts.tinactory.core.worldgen.ore.IOreShape;

import static org.shsts.tinactory.core.util.LocHelper.modLoc;

public final class OreShapeTestHelper {
    public static final MappedRegistry<IOreShape<?, ?>> SHAPES = new MappedRegistry<>(
        ResourceKey.createRegistryKey(modLoc("ore_shape")), Lifecycle.stable());
    public static final EllipsoidShape ELLIPSOID = Registry.register(SHAPES, modLoc("ellipsoid"),
        new EllipsoidShape());

    private OreShapeTestHelper() {}
}
