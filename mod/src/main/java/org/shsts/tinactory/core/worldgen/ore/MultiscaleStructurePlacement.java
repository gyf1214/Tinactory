package org.shsts.tinactory.core.worldgen.ore;

import com.mojang.datafixers.Products;
import com.mojang.datafixers.kinds.K1;
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

import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class MultiscaleStructurePlacement extends StructurePlacement {
    private static final long FREQUENCY_SALT = 0x2F6E2B1A6C93D5A7L;
    private static final long X_CHOICE_SALT = 0x7C4A7D3E9B1F205DL;
    private static final long Z_CHOICE_SALT = 0xB8D4A6F17E3C5921L;

    private static <F extends K1, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> Products.P11<F,
        T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> combine(Products.P5<F, T1, T2, T3, T4, T5> x,
        Products.P6<F, T6, T7, T8, T9, T10, T11> y) {
        return new Products.P11<>(x.t1(), x.t2(), x.t3(), x.t4(), x.t5(),
            y.t1(), y.t2(), y.t3(), y.t4(), y.t5(), y.t6());
    }

    public static final MapCodec<MultiscaleStructurePlacement> CODEC =
        RecordCodecBuilder.<MultiscaleStructurePlacement>mapCodec(
                instance -> combine(placementCodec(instance),
                    instance.group(
                        Codec.INT.fieldOf("offsetX").forGetter($ -> $.offsetX),
                        Codec.INT.fieldOf("offsetZ").forGetter($ -> $.offsetZ),
                        Codec.INT.fieldOf("spacing").forGetter($ -> $.spacing),
                        Codec.INT.fieldOf("separation").forGetter($ -> $.separation),
                        Codec.INT.fieldOf("scale").forGetter($ -> $.scale),
                        Codec.INT.fieldOf("levels").forGetter($ -> $.levels)))
                    .apply(instance, MultiscaleStructurePlacement::new))
            .validate(MultiscaleStructurePlacement::validate);

    private final int offsetX;
    private final int offsetZ;
    private final int spacing;
    private final int separation;
    private final int scale;
    private final int levels;

    @SuppressWarnings("deprecation")
    public MultiscaleStructurePlacement(Vec3i locateOffset, FrequencyReductionMethod frequencyReductionMethod,
        float frequency, int salt, Optional<ExclusionZone> exclusionZone,
        int offsetX, int offsetZ, int spacing, int separation, int scale, int levels) {
        super(locateOffset, frequencyReductionMethod, frequency, salt, exclusionZone);
        this.offsetX = offsetX;
        this.offsetZ = offsetZ;
        this.spacing = spacing;
        this.separation = separation;
        this.scale = scale;
        this.levels = levels;
    }

    public boolean shouldPlace(long levelSeed, int chunkX, int chunkZ) {
        var regionScale = 1;
        for (var level = 0; level < levels; level++) {
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
