package org.shsts.tinactory.content.worldgen.ore;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import org.shsts.tinactory.AllWorldGens;
import org.shsts.tinactory.core.worldgen.ore.OreVeinDefinition;
import org.shsts.tinactory.core.worldgen.ore.OreVeinUtil;

import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class OreVeinStructure extends Structure {
    public static final MapCodec<OreVeinStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Structure.settingsCodec(instance),
        OreVeinDefinition.CODEC.forGetter(OreVeinStructure::definition)
    ).apply(instance, OreVeinStructure::new));

    private final OreVeinDefinition definition;

    public OreVeinStructure(StructureSettings settings, OreVeinDefinition definition) {
        super(settings);
        this.definition = definition;
    }

    public OreVeinDefinition definition() {
        return definition;
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        WorldgenRandom random = context.random();
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
