package org.shsts.tinactory.unit.fixture;

import com.mojang.serialization.Lifecycle;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.shsts.tinactory.AllRegistries;
import org.shsts.tinactory.core.worldgen.ore.EllipsoidShape;
import org.shsts.tinactory.core.worldgen.ore.IOreShape;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

import static org.shsts.tinactory.core.util.LocHelper.modLoc;

public final class TestOreHelper {
    public static final MappedRegistry<Block> BLOCKS = new MappedRegistry<>(
        Registries.BLOCK, Lifecycle.stable());
    public static final Block STONE = register(Blocks.STONE);
    public static final Block IRON_ORE = register(Blocks.IRON_ORE);
    public static final Block GOLD_ORE = register(Blocks.GOLD_ORE);

    public static final MappedRegistry<IOreShape<?, ?>> SHAPES = new MappedRegistry<>(
        AllRegistries.ORE_SHAPES.key(), Lifecycle.stable());
    public static final IEntry<EllipsoidShape> ELLIPSOID = TestCodecHelper.register(SHAPES,
        modLoc("ellipsoid"), new EllipsoidShape());

    private TestOreHelper() {}

    @SuppressWarnings("deprecation")
    private static Block register(Block block) {
        var loc = block.builtInRegistryHolder().unwrapKey().orElseThrow().location();
        return Registry.register(BLOCKS, loc, block);
    }
}
