package org.shsts.tinactory.content;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.world.ForgeWorldPreset;
import net.minecraftforge.registries.ForgeRegistries;
import org.shsts.tinactory.Tinactory;
import org.shsts.tinactory.content.worldgen.VoidPreset;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.RegistryEntry;
import org.shsts.tinactory.registrate.handler.RegistryEntryHandler;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllWorldGens {
    private static final Registrate REGISTRATE = Tinactory.REGISTRATE;

    public static final ResourceKey<Biome> VOID_BIOME;

    private static final RegistryEntryHandler<ForgeWorldPreset> WORLD_TYPE_HANDLER;
    public static final RegistryEntry<VoidPreset> VOID_PRESET;

    static {
        // biomes
        VOID_BIOME = biome("void");

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
