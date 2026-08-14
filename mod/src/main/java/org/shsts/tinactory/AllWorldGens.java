package org.shsts.tinactory;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.shsts.tinactory.content.worldgen.PlayerStartFeature;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

import static org.shsts.tinactory.AllRegistries.FEATURES;
import static org.shsts.tinactory.Tinactory.REGISTRATE;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllWorldGens {
    public static final ResourceKey<Biome> VOID_WITH_START_BIOME;
    public static final IEntry<PlayerStartFeature> PLAYER_START_FEATURE;
    public static final ResourceKey<ConfiguredFeature<?, ?>> RUBBER_TREE_GROWER;

    static {
        VOID_WITH_START_BIOME = ResourceKey.create(Registries.BIOME, modLoc("void_with_start"));
        PLAYER_START_FEATURE = REGISTRATE.registryEntry(FEATURES, "player_start", PlayerStartFeature::new);
        RUBBER_TREE_GROWER = ResourceKey.create(Registries.CONFIGURED_FEATURE, modLoc("rubber_tree"));
    }

    public static void init() {}
}
