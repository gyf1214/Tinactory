package org.shsts.tinactory.datagen.provider;

import com.mojang.serialization.JsonOps;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.shsts.tinactory.content.worldgen.ore.OreVeinStructure;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinactory.core.worldgen.ore.OreVeinDefinition;
import org.shsts.tinactory.core.worldgen.placement.MultiscaleStructurePlacement;
import org.shsts.tinycorelib.datagen.api.IDataGen;
import org.shsts.tinycorelib.datagen.api.IDataHandler;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class OreVeinDataProvider implements DataProvider {
    private static final Map<ResourceKey<Level>, OutputConfig> OUTPUTS = Map.of(
        Level.OVERWORLD, new OutputConfig(
            ResourceLocation.fromNamespaceAndPath("tinactory", "ore_vein/overworld"),
            TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("minecraft", "is_overworld")),
            12001),
        Level.NETHER, new OutputConfig(
            ResourceLocation.fromNamespaceAndPath("tinactory", "ore_vein/nether"),
            TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("minecraft", "is_nether")),
            12002),
        Level.END, new OutputConfig(
            ResourceLocation.fromNamespaceAndPath("tinactory", "ore_vein/end"),
            TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("minecraft", "is_end")),
            12003));

    private final String modId;
    private final IDataHandler<OreVeinDataProvider> handler;
    private final PackOutput.PathProvider structurePathProvider;
    private final PackOutput.PathProvider structureSetPathProvider;
    private final CompletableFuture<HolderLookup.Provider> lookupProvider;
    private final Map<ResourceKey<Level>, Map<ResourceLocation, OreVeinDefinition>> definitions = new LinkedHashMap<>();

    public OreVeinDataProvider(IDataGen dataGen,
        IDataHandler<OreVeinDataProvider> handler, GatherDataEvent event) {
        this.modId = dataGen.modid();
        this.handler = handler;
        var packOutput = event.getGenerator().getPackOutput();
        this.structurePathProvider = packOutput.createPathProvider(PackOutput.Target.DATA_PACK, "worldgen/structure");
        this.structureSetPathProvider = packOutput.createPathProvider(
            PackOutput.Target.DATA_PACK, "worldgen/structure_set");
        this.lookupProvider = event.getLookupProvider();
        OUTPUTS.keySet().forEach(dimension -> definitions.put(dimension, new LinkedHashMap<>()));
    }

    public void addVein(ResourceKey<Level> dimension, OreVeinDefinition definition) {
        var dimensionDefinitions = definitions.get(dimension);
        if (dimensionDefinitions == null) {
            throw new IllegalArgumentException("Ore vein dimension is not configured: " + dimension.location());
        }
        if (dimensionDefinitions.putIfAbsent(definition.id(), definition) != null) {
            throw new IllegalArgumentException("Duplicate ore vein definition " + definition.id() +
                " in dimension " + dimension.location());
        }
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        return lookupProvider.thenCompose(registries -> {
            handler.register(this);
            validateDefinitions();
            var futures = OUTPUTS.entrySet().stream()
                .map(entry -> writeDimension(output, registries, entry.getKey(), entry.getValue()))
                .toArray(CompletableFuture[]::new);
            return CompletableFuture.allOf(futures);
        });
    }

    @Override
    public String getName() {
        return "Ore Veins: " + modId;
    }

    private CompletableFuture<?> writeDimension(CachedOutput output, HolderLookup.Provider registries,
        ResourceKey<Level> dimension, OutputConfig config) {
        var dimensionDefinitions = definitions.get(dimension);
        if (dimensionDefinitions == null || dimensionDefinitions.isEmpty()) {
            throw new IllegalStateException("No ore vein definitions configured for dimension " + dimension.location());
        }
        var biomeLookup = registries.lookupOrThrow(Registries.BIOME);
        var settings = new Structure.StructureSettings(
            biomeLookup.getOrThrow(config.biomeTag()),
            Map.of(),
            GenerationStep.Decoration.UNDERGROUND_ORES,
            TerrainAdjustment.NONE);
        var structure = new OreVeinStructure(settings, List.copyOf(dimensionDefinitions.values()));
        var structureJson = CodecHelper.encodeJson(registries, Structure.DIRECT_CODEC, structure);
        var structureFuture = DataProvider.saveStable(
            output, structureJson, structurePathProvider.json(config.structureId()));

        var serializationOps = registries.createSerializationContext(JsonOps.INSTANCE);
        var structureOwner = serializationOps.lookupProvider.lookup(Registries.STRUCTURE)
            .orElseThrow(() -> new IllegalStateException("Structure registry is missing from datagen lookup"))
            .owner();
        var structureKey = ResourceKey.create(Registries.STRUCTURE, config.structureId());
        var structureHolder = Holder.Reference.createStandAlone(
            structureOwner, structureKey);
        var placement = new MultiscaleStructurePlacement(
            Vec3i.ZERO,
            StructurePlacement.FrequencyReductionMethod.DEFAULT,
            0.75F,
            config.salt(),
            Optional.empty(),
            1,
            1,
            4,
            1,
            2);
        var structureSet = new StructureSet(structureHolder, placement);
        var structureSetJson = StructureSet.DIRECT_CODEC.encodeStart(serializationOps, structureSet).getOrThrow();
        var structureSetFuture = DataProvider.saveStable(
            output, structureSetJson, structureSetPathProvider.json(config.structureId()));
        return CompletableFuture.allOf(structureFuture, structureSetFuture);
    }

    private void validateDefinitions() {
        OUTPUTS.forEach((dimension, config) -> {
            var dimensionDefinitions = definitions.get(dimension);
            if (dimensionDefinitions == null || dimensionDefinitions.isEmpty()) {
                throw new IllegalStateException("No ore vein definitions configured for dimension " +
                    dimension.location());
            }
            if (!config.structureId().getNamespace().equals(modId)) {
                throw new IllegalStateException("Ore vein output namespace does not match datagen mod ID: " +
                    config.structureId());
            }
        });
    }

    private record OutputConfig(ResourceLocation structureId, TagKey<Biome> biomeTag, int salt) {}
}
