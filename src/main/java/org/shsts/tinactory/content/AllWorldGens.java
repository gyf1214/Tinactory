package org.shsts.tinactory.content;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraftforge.common.world.ForgeWorldPreset;
import net.minecraftforge.registries.ForgeRegistries;
import org.shsts.tinactory.content.worldgen.PlayerStartFeature;
import org.shsts.tinactory.content.worldgen.VoidPreset;
import org.shsts.tinactory.registrate.RegistryEntry;
import org.shsts.tinactory.registrate.handler.RegistryEntryHandler;

import javax.annotation.ParametersAreNonnullByDefault;

import static org.shsts.tinactory.Tinactory.REGISTRATE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllWorldGens {
    public static final ResourceKey<Biome> VOID_BIOME;

    private static final RegistryEntryHandler<Feature<?>> FEATURE_HANDLER;
    public static final RegistryEntry<PlayerStartFeature> PLAYER_START_FEATURE;

    private static final RegistryEntryHandler<ForgeWorldPreset> WORLD_TYPE_HANDLER;
    public static final RegistryEntry<VoidPreset> VOID_PRESET;

    static {
        // biomes
        VOID_BIOME = biome("void");

        FEATURE_HANDLER = REGISTRATE.forgeHandler(ForgeRegistries.FEATURES);
        PLAYER_START_FEATURE = REGISTRATE.registryEntry("player_start", FEATURE_HANDLER, PlayerStartFeature::new);

        // world types
        WORLD_TYPE_HANDLER = REGISTRATE.forgeHandler(
                ForgeRegistries.Keys.WORLD_TYPES, ForgeWorldPreset.class, ForgeRegistries.WORLD_TYPES);
        VOID_PRESET = REGISTRATE.registryEntry("void", WORLD_TYPE_HANDLER, VoidPreset::new);
    }

    private static ResourceKey<Biome> biome(String id) {
        REGISTRATE.biome(id);
        return ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(REGISTRATE.modid, id));
    }

    public static void init() {}
}
