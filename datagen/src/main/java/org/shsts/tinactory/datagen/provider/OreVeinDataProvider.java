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
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.shsts.tinactory.content.worldgen.ore.OreVeinStructure;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinactory.core.worldgen.ore.MultiscaleStructurePlacement;
import org.shsts.tinactory.core.worldgen.ore.OreVeinDefinition;
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
    private final String modId;
    private final IDataHandler<OreVeinDataProvider> handler;
    private final PackOutput.PathProvider structurePathProvider;
    private final PackOutput.PathProvider structureSetPathProvider;
    private final CompletableFuture<HolderLookup.Provider> lookupProvider;
    private final Map<String, VeinGroup> groups = new LinkedHashMap<>();

    public OreVeinDataProvider(IDataGen dataGen,
        IDataHandler<OreVeinDataProvider> handler, GatherDataEvent event) {
        this.modId = dataGen.modid();
        this.handler = handler;
        var packOutput = event.getGenerator().getPackOutput();
        this.structurePathProvider = packOutput.createPathProvider(PackOutput.Target.DATA_PACK, "worldgen/structure");
        this.structureSetPathProvider = packOutput.createPathProvider(
            PackOutput.Target.DATA_PACK, "worldgen/structure_set");
        this.lookupProvider = event.getLookupProvider();
    }

    public void addVein(String id, TagKey<Biome> biomeTag, OreVeinDefinition definition) {
        if (id.isBlank()) {
            throw new IllegalArgumentException("Ore vein group ID must not be blank");
        }
        var group = groups.computeIfAbsent(id, ignored -> new VeinGroup(biomeTag, new LinkedHashMap<>()));
        if (!group.biomeTag().equals(biomeTag)) {
            throw new IllegalArgumentException("Ore vein group " + id +
                " is registered with multiple biome tags: " + group.biomeTag() + " and " + biomeTag);
        }
        if (group.definitions().putIfAbsent(definition.id(), definition) != null) {
            throw new IllegalArgumentException("Duplicate ore vein definition " + definition.id() +
                " in group " + id);
        }
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        return lookupProvider.thenCompose(registries -> {
            handler.register(this);
            validateDefinitions();
            var futures = groups.entrySet().stream()
                .map(entry -> writeGroup(output, registries, entry.getKey(), entry.getValue()))
                .toArray(CompletableFuture[]::new);
            return CompletableFuture.allOf(futures);
        });
    }

    @Override
    public String getName() {
        return "Ore Veins: " + modId;
    }

    private CompletableFuture<?> writeGroup(CachedOutput output, HolderLookup.Provider registries,
        String id, VeinGroup group) {
        var structureId = structureId(id);
        if (group.definitions().isEmpty()) {
            throw new IllegalStateException("No ore vein definitions configured for group " + id);
        }
        var biomeLookup = registries.lookupOrThrow(Registries.BIOME);
        var settings = new Structure.StructureSettings(
            biomeLookup.getOrThrow(group.biomeTag()),
            Map.of(),
            GenerationStep.Decoration.UNDERGROUND_ORES,
            TerrainAdjustment.NONE);
        var structure = new OreVeinStructure(settings, List.copyOf(group.definitions().values()));
        var structureJson = CodecHelper.encodeJson(registries, Structure.DIRECT_CODEC, structure);
        var structureFuture = DataProvider.saveStable(
            output, structureJson, structurePathProvider.json(structureId));

        var serializationOps = registries.createSerializationContext(JsonOps.INSTANCE);
        var structureOwner = serializationOps.lookupProvider.lookup(Registries.STRUCTURE)
            .orElseThrow(() -> new IllegalStateException("Structure registry is missing from datagen lookup"))
            .owner();
        var structureKey = ResourceKey.create(Registries.STRUCTURE, structureId);
        var structureHolder = Holder.Reference.createStandAlone(
            structureOwner, structureKey);
        var placement = new MultiscaleStructurePlacement(
            Vec3i.ZERO,
            StructurePlacement.FrequencyReductionMethod.DEFAULT,
            0.75F,
            salt(id),
            Optional.empty(),
            1, 1, 4, 1, 2, 3);
        var structureSet = new StructureSet(structureHolder, placement);
        var structureSetJson = StructureSet.DIRECT_CODEC.encodeStart(serializationOps, structureSet).getOrThrow();
        var structureSetFuture = DataProvider.saveStable(
            output, structureSetJson, structureSetPathProvider.json(structureId));
        return CompletableFuture.allOf(structureFuture, structureSetFuture);
    }

    private void validateDefinitions() {
        if (groups.isEmpty()) {
            throw new IllegalStateException("No ore vein groups configured");
        }
        groups.forEach((id, group) -> {
            if (group.definitions().isEmpty()) {
                throw new IllegalStateException("No ore vein definitions configured for group " + id);
            }
            structureId(id);
        });
    }

    private ResourceLocation structureId(String id) {
        try {
            return ResourceLocation.fromNamespaceAndPath(modId, "ore_vein/" + id);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid ore vein group ID: " + id, exception);
        }
    }

    private int salt(String id) {
        return id.hashCode() & Integer.MAX_VALUE;
    }

    private record VeinGroup(TagKey<Biome> biomeTag, Map<ResourceLocation, OreVeinDefinition> definitions) {}
}
