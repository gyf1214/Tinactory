package org.shsts.tinactory.core.worldgen.placement;

import com.mojang.datafixers.Products.P10;
import com.mojang.datafixers.Products.P5;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import org.shsts.tinactory.AllWorldGens;
import org.shsts.tinactory.core.worldgen.ore.OreVeinUtil;

import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class MultiscaleStructurePlacement extends StructurePlacement {
    private static final int LEVEL_COUNT = 3;
    private static final long FREQUENCY_SALT = 0x2F6E2B1A6C93D5A7L;
    private static final long X_CHOICE_SALT = 0x7C4A7D3E9B1F205DL;
    private static final long Z_CHOICE_SALT = 0xB8D4A6F17E3C5921L;

    public static final MapCodec<MultiscaleStructurePlacement> CODEC =
        RecordCodecBuilder.<MultiscaleStructurePlacement>mapCodec(
            instance -> codec(instance).apply(instance, MultiscaleStructurePlacement::new))
            .validate(MultiscaleStructurePlacement::validate);

    private static P10<RecordCodecBuilder.Mu<MultiscaleStructurePlacement>, Vec3i, FrequencyReductionMethod, Float,
        Integer, Optional<ExclusionZone>, Integer, Integer, Integer, Integer, Integer> codec(
        RecordCodecBuilder.Instance<MultiscaleStructurePlacement> instance) {
        P5<RecordCodecBuilder.Mu<MultiscaleStructurePlacement>, Vec3i, FrequencyReductionMethod, Float, Integer,
            Optional<ExclusionZone>> inherited = placementCodec(instance);
        P5<RecordCodecBuilder.Mu<MultiscaleStructurePlacement>, Integer, Integer, Integer, Integer, Integer> custom =
            instance.group(
                Codec.INT.fieldOf("offset_x").forGetter(MultiscaleStructurePlacement::offsetX),
                Codec.INT.fieldOf("offset_z").forGetter(MultiscaleStructurePlacement::offsetZ),
                Codec.intRange(1, Integer.MAX_VALUE).fieldOf("spacing")
                    .forGetter(MultiscaleStructurePlacement::spacing),
                Codec.intRange(0, Integer.MAX_VALUE).fieldOf("separation")
                    .forGetter(MultiscaleStructurePlacement::separation),
                Codec.intRange(1, Integer.MAX_VALUE).fieldOf("scale")
                    .forGetter(MultiscaleStructurePlacement::scale)
            );
        return new P10<>(inherited.t1(), inherited.t2(), inherited.t3(), inherited.t4(), inherited.t5(),
            custom.t1(), custom.t2(), custom.t3(), custom.t4(), custom.t5());
    }

    private final int offsetX;
    private final int offsetZ;
    private final int spacing;
    private final int separation;
    private final int scale;

    public MultiscaleStructurePlacement(Vec3i locateOffset,
        FrequencyReductionMethod frequencyReductionMethod, float frequency, int salt,
        Optional<ExclusionZone> exclusionZone, int offsetX, int offsetZ, int spacing, int separation, int scale) {
        super(locateOffset, frequencyReductionMethod, frequency, salt, exclusionZone);
        this.offsetX = offsetX;
        this.offsetZ = offsetZ;
        this.spacing = spacing;
        this.separation = separation;
        this.scale = scale;
    }

    public int offsetX() {
        return offsetX;
    }

    public int offsetZ() {
        return offsetZ;
    }

    public int spacing() {
        return spacing;
    }

    public int separation() {
        return separation;
    }

    public int scale() {
        return scale;
    }

    public boolean shouldPlace(long levelSeed, int chunkX, int chunkZ) {
        var regionScale = 1;
        for (var level = 0; level < LEVEL_COUNT; level++) {
            var effectiveSpacing = spacing * regionScale;
            var candidateRange = effectiveSpacing - separation;
            var relativeX = chunkX - offsetX * regionScale;
            var relativeZ = chunkZ - offsetZ * regionScale;
            var localX = Math.floorMod(relativeX, effectiveSpacing);
            var localZ = Math.floorMod(relativeZ, effectiveSpacing);
            var regionX = Math.floorDiv(relativeX, effectiveSpacing);
            var regionZ = Math.floorDiv(relativeZ, effectiveSpacing);
            var coordinate = new BlockPos(regionX, level, regionZ);
            if (OreVeinUtil.hashToUnit(levelSeed, coordinate, combinedSalt(FREQUENCY_SALT)) >= frequency()) {
                regionScale *= scale;
                continue;
            }
            var candidateX = (int) (OreVeinUtil.hashToUnit(levelSeed, coordinate,
                combinedSalt(X_CHOICE_SALT)) * candidateRange);
            var candidateZ = (int) (OreVeinUtil.hashToUnit(levelSeed, coordinate,
                combinedSalt(Z_CHOICE_SALT)) * candidateRange);
            if (localX == candidateX && localZ == candidateZ) {
                return true;
            }
            regionScale *= scale;
        }
        return false;
    }

    @Override
    public boolean applyAdditionalChunkRestrictions(int regionX, int regionZ, long levelSeed) {
        return true;
    }

    @Override
    protected boolean isPlacementChunk(ChunkGeneratorStructureState structureState, int x, int z) {
        return shouldPlace(structureState.getLevelSeed(), x, z);
    }

    @Override
    public StructurePlacementType<?> type() {
        return AllWorldGens.MULTISCALE_PLACEMENT_TYPE.get();
    }

    private long combinedSalt(long decisionSalt) {
        return (long) salt() ^ decisionSalt;
    }

    private static DataResult<MultiscaleStructurePlacement> validate(MultiscaleStructurePlacement placement) {
        return placement.spacing <= placement.separation ?
            DataResult.error(() -> "Spacing has to be larger than separation") :
            DataResult.success(placement);
    }
}
