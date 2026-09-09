package org.shsts.tinactory.unit.fixture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.shsts.tinactory.core.worldgen.ore.shape.EllipsoidShape;
import org.shsts.tinactory.core.worldgen.ore.shape.IOreShape;

import static org.shsts.tinactory.core.util.LocHelper.modLoc;

public final class OreShapeTestHelper {
    public static final MappedRegistry<IOreShape<?, ?>> SHAPES = new MappedRegistry<>(
        ResourceKey.createRegistryKey(modLoc("ore_shape_test")), Lifecycle.stable());
    public static final EllipsoidShape ELLIPSOID = Registry.register(SHAPES, modLoc("ellipsoid"),
        new EllipsoidShape());
    public static final Codec<IOreShape<?, ?>> SHAPE_CODEC = SHAPES.byNameCodec();

    private OreShapeTestHelper() {}
}
