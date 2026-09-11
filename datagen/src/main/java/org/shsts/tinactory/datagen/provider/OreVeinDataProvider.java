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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class OreVeinDataProvider implements DataProvider {
    private static final String PREFIX = "ore_vein/";

    private final String modId;
    private final IDataHandler<OreVeinDataProvider> handler;
    private final PackOutput.PathProvider structurePathProvider;
    private final PackOutput.PathProvider structureSetPathProvider;
    private final CompletableFuture<HolderLookup.Provider> lookupProvider;

    private record Vein(ResourceLocation loc, TagKey<Biome> biomeTag, OreVeinDefinition definition) {}

    private record VeinEntry(ResourceLocation loc, int weight) {}

    private final List<Vein> veins = new ArrayList<>();
    private final Map<String, List<VeinEntry>> groups = new LinkedHashMap<>();

    public OreVeinDataProvider(IDataGen dataGen,
        IDataHandler<OreVeinDataProvider> handler, GatherDataEvent event) {
        this.modId = dataGen.modid();
        this.handler = handler;
        var packOutput = event.getGenerator().getPackOutput();
        this.structurePathProvider = packOutput.createPathProvider(
            PackOutput.Target.DATA_PACK, "worldgen/structure");
        this.structureSetPathProvider = packOutput.createPathProvider(
            PackOutput.Target.DATA_PACK, "worldgen/structure_set");
        this.lookupProvider = event.getLookupProvider();
    }

    public void addGroup(String id, ResourceLocation vein, int weight) {
        groups.computeIfAbsent(id, $ -> new ArrayList<>()).add(new VeinEntry(vein, weight));
    }

    public ResourceLocation addVein(String veinId, TagKey<Biome> biomeTag, OreVeinDefinition definition) {
        var veinLoc = ResourceLocation.fromNamespaceAndPath(modId, PREFIX + veinId);
        veins.add(new Vein(veinLoc, biomeTag, definition));
        return veinLoc;
    }

    public void addVein(String groupId, int weight, String veinId,
        TagKey<Biome> biomeTag, OreVeinDefinition definition) {
        var loc = addVein(veinId, biomeTag, definition);
        addGroup(groupId, loc, weight);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        return lookupProvider.thenCompose(registries -> {
            handler.register(this);
            validateDefinitions();

            var futures = Stream.concat(
                veins.stream().map(entry -> writeStructure(output, registries, entry)),
                groups.entrySet().stream().map(entry ->
                    writeGroup(output, registries, entry.getKey(), entry.getValue()))
            ).toArray(CompletableFuture[]::new);

            return CompletableFuture.allOf(futures);
        });
    }

    @Override
    public String getName() {
        return "Ore Veins: " + modId;
    }

    private CompletableFuture<?> writeStructure(CachedOutput output, HolderLookup.Provider registries,
        Vein vein) {
        var biomeLookup = registries.lookupOrThrow(Registries.BIOME);
        var settings = new Structure.StructureSettings(
            biomeLookup.getOrThrow(vein.biomeTag),
            Map.of(),
            GenerationStep.Decoration.UNDERGROUND_ORES,
            TerrainAdjustment.NONE);
        var structure = new OreVeinStructure(settings, vein.definition);
        var json = CodecHelper.encodeJson(registries, Structure.DIRECT_CODEC, structure);
        return DataProvider.saveStable(output, json, structurePathProvider.json(vein.loc));
    }

    private CompletableFuture<?> writeGroup(CachedOutput output, HolderLookup.Provider registries,
        String id, List<VeinEntry> veins) {
        var loc = ResourceLocation.fromNamespaceAndPath(modId, PREFIX + id);

        var ops = registries.createSerializationContext(JsonOps.INSTANCE);
        var owner = ops.lookupProvider.lookup(Registries.STRUCTURE)
            .orElseThrow(() -> new IllegalStateException("Structure registry is missing from datagen lookup"))
            .owner();
        var entries = new ArrayList<StructureSet.StructureSelectionEntry>();
        for (var entry : veins) {
            var key = ResourceKey.create(Registries.STRUCTURE, entry.loc);
            var holder = Holder.Reference.createStandAlone(owner, key);
            entries.add(new StructureSet.StructureSelectionEntry(holder, entry.weight));
        }

        var placement = new MultiscaleStructurePlacement(
            Vec3i.ZERO,
            StructurePlacement.FrequencyReductionMethod.DEFAULT,
            0.75F,
            salt(id),
            Optional.empty(),
            1, 1, 4, 1, 2, 3);
        var structureSet = new StructureSet(entries, placement);

        var json = StructureSet.DIRECT_CODEC.encodeStart(ops, structureSet).getOrThrow();

        return DataProvider.saveStable(output, json, structureSetPathProvider.json(loc));
    }

    private void validateDefinitions() {
        groups.forEach((id, group) -> {
            if (group.isEmpty()) {
                throw new IllegalStateException("No ore vein definitions configured for group " + id);
            }
        });
    }

    private int salt(String id) {
        return id.hashCode() & Integer.MAX_VALUE;
    }
}
