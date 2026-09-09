package org.shsts.tinactory.unit.fixture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import static org.shsts.tinactory.core.util.LocHelper.modLoc;

public final class OreBlockTestHelper {
    public static final MappedRegistry<Block> BLOCKS = new MappedRegistry<>(
        ResourceKey.createRegistryKey(modLoc("ore_block_test")), Lifecycle.stable());
    public static final Block HOST = Registry.register(BLOCKS, modLoc("host"), Blocks.STONE);
    public static final Block IRON_ORE = Registry.register(BLOCKS, modLoc("ore/iron"), Blocks.IRON_ORE);
    public static final Block GOLD_ORE = Registry.register(BLOCKS, modLoc("ore/gold"), Blocks.GOLD_ORE);
    public static final Codec<Block> BLOCK_CODEC = BLOCKS.byNameCodec();

    private OreBlockTestHelper() {}
}
