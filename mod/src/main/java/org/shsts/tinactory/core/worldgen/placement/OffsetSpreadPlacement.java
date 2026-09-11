package org.shsts.tinactory.core.worldgen.placement;

import com.mojang.datafixers.Products;
import com.mojang.datafixers.kinds.K1;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import org.shsts.tinactory.AllWorldGens;

import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class OffsetSpreadPlacement extends RandomSpreadStructurePlacement {
    private static <F extends K1, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> Products.P10<F,
        T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> combine(
        Products.P8<F, T1, T2, T3, T4, T5, T6, T7, T8> x,
        Products.P2<F, T9, T10> y) {
        return new Products.P10<>(x.t1(), x.t2(), x.t3(), x.t4(), x.t5(), x.t6(), x.t7(), x.t8(), y.t1(), y.t2());
    }

    public static final MapCodec<OffsetSpreadPlacement> CODEC =
        RecordCodecBuilder.<OffsetSpreadPlacement>mapCodec(instance ->
        combine(placementCodec(instance).and(instance.group(
                Codec.intRange(0, 4096).fieldOf("spacing").forGetter(OffsetSpreadPlacement::spacing),
                Codec.intRange(0, 4096).fieldOf("separation").forGetter(OffsetSpreadPlacement::separation),
                RandomSpreadType.CODEC.optionalFieldOf("spread_type", RandomSpreadType.LINEAR)
                    .forGetter(OffsetSpreadPlacement::spreadType)
            )), instance.group(
                Codec.INT.fieldOf("offset_x").forGetter(OffsetSpreadPlacement::offsetX),
                Codec.INT.fieldOf("offset_z").forGetter(OffsetSpreadPlacement::offsetZ)
            )).apply(instance, OffsetSpreadPlacement::new))
        .validate(OffsetSpreadPlacement::validate);

    private final int offsetX;
    private final int offsetZ;

    @SuppressWarnings("deprecation")
    public OffsetSpreadPlacement(Vec3i locateOffset, FrequencyReductionMethod frequencyReductionMethod,
        float frequency, int salt, Optional<ExclusionZone> exclusionZone, int spacing, int separation,
        RandomSpreadType spreadType, int offsetX, int offsetZ) {
        super(locateOffset, frequencyReductionMethod, frequency, salt, exclusionZone, spacing, separation, spreadType);
        this.offsetX = offsetX;
        this.offsetZ = offsetZ;
    }

    public int offsetX() {
        return offsetX;
    }

    public int offsetZ() {
        return offsetZ;
    }

    @Override
    public ChunkPos getPotentialStructureChunk(long seed, int chunkX, int chunkZ) {
        var candidate = super.getPotentialStructureChunk(seed, chunkX - offsetX, chunkZ - offsetZ);
        return new ChunkPos(candidate.x + offsetX, candidate.z + offsetZ);
    }

    @Override
    public StructurePlacementType<?> type() {
        return AllWorldGens.OFFSET_SPREAD_PLACEMENT_TYPE.get();
    }

    private static DataResult<OffsetSpreadPlacement> validate(OffsetSpreadPlacement placement) {
        return placement.spacing() <= placement.separation() ?
            DataResult.error(() -> "Spacing has to be larger than separation") :
            DataResult.success(placement);
    }
}
