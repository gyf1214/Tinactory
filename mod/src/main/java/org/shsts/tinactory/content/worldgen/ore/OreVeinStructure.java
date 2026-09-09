package org.shsts.tinactory.content.worldgen.ore;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import org.shsts.tinactory.AllWorldGens;
import org.shsts.tinactory.core.worldgen.ore.OreVeinDefinition;
import org.shsts.tinactory.core.worldgen.ore.OreVeinUtil;

import java.util.List;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class OreVeinStructure extends Structure {
    private final List<OreVeinDefinition> definitions;

    public OreVeinStructure(StructureSettings settings, List<OreVeinDefinition> definitions) {
        super(settings);
        this.definitions = List.copyOf(definitions);
    }

    public List<OreVeinDefinition> definitions() {
        return definitions;
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        WorldgenRandom random = context.random();
        var definition = OreVeinUtil.select(definitions, random.nextLong());
        var x = context.chunkPos().getMinBlockX() + random.nextInt(16);
        var z = context.chunkPos().getMinBlockZ() + random.nextInt(16);
        var minY = Math.max(definition.minY(), context.heightAccessor().getMinBuildHeight());
        var maxY = Math.min(definition.maxY(), context.heightAccessor().getMaxBuildHeight() - 1);
        if (minY > maxY) {
            return Optional.empty();
        }
        var y = minY + random.nextInt(maxY - minY + 1);
        var center = new BlockPos(x, y, z);
        var instance = OreVeinUtil.sample(definition, random.nextLong(), center);
        return Optional.of(new GenerationStub(center, builder -> builder.addPiece(new OreVeinPiece(instance))));
    }

    @Override
    public StructureType<?> type() {
        return AllWorldGens.ORE_VEIN_STRUCTURE_TYPE.get();
    }
}
