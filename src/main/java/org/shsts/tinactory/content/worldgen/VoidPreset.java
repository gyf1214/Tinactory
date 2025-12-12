package org.shsts.tinactory.content.worldgen;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraftforge.common.world.ForgeWorldPreset;
import org.shsts.tinactory.AllWorldGens;

import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class VoidPreset extends ForgeWorldPreset {
    public VoidPreset() {
        super((registryAccess, seed) -> {
            var biomes = registryAccess.registryOrThrow(Registry.BIOME_REGISTRY);
            var structureSets = registryAccess.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY);
            FlatLevelGeneratorSettings settings = new FlatLevelGeneratorSettings(Optional.empty(), biomes);
            settings.setBiome(biomes.getOrCreateHolder(AllWorldGens.VOID_BIOME));
            settings.updateLayers();
            return new FlatLevelSource(structureSets, settings);
        });
    }
}
