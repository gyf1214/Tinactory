package org.shsts.tinactory;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.biome.OverworldBiomes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.registries.ForgeRegistries;
import org.shsts.tinactory.content.worldgen.PlayerStartFeature;
import org.shsts.tinactory.content.worldgen.VoidPreset;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

import static org.shsts.tinactory.AllRegistries.FEATURES;
import static org.shsts.tinactory.AllRegistries.WORLD_TYPES;
import static org.shsts.tinactory.Tinactory.REGISTRATE;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllWorldGens {
    public static final ResourceKey<Biome> VOID_BIOME;

    public static final IEntry<PlayerStartFeature> PLAYER_START_FEATURE;
    public static final ResourceKey<ConfiguredFeature<?, ?>> RUBBER_TREE_GROWER =
        ResourceKey.create(Registry.CONFIGURED_FEATURE_REGISTRY, modLoc("rubber_tree"));

    public static final IEntry<VoidPreset> VOID_PRESET;

    static {
        VOID_BIOME = REGISTRATE
            .createDynamicHandler(ForgeRegistries.BIOMES, OverworldBiomes::theVoid)
            .dynamicEntry(ForgeRegistries.BIOMES, "void");

        PLAYER_START_FEATURE = REGISTRATE.registryEntry(FEATURES, "player_start", PlayerStartFeature::new);

        BuiltinRegistries.register(BuiltinRegistries.CONFIGURED_FEATURE,
            RUBBER_TREE_GROWER, new ConfiguredFeature<>(Feature.NO_OP, NoneFeatureConfiguration.INSTANCE));

        VOID_PRESET = REGISTRATE.registryEntry(WORLD_TYPES, "void", VoidPreset::new);
    }

    public static void init() {}
}
