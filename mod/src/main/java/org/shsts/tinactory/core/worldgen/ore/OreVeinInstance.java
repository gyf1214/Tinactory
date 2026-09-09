package org.shsts.tinactory.core.worldgen.ore;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record OreVeinInstance(
    int algorithmVersion,
    ResourceLocation definitionId,
    long veinSeed,
    BlockPos center,
    OreShapeInstance<?> shape,
    double density,
    Block hostBlock,
    List<OreEntry> ores
) {
    public static MapCodec<OreVeinInstance> codec(Codec<Block> blockCodec,
        MapCodec<OreShapeInstance<?>> shapeCodec) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("algorithm_version").forGetter(OreVeinInstance::algorithmVersion),
            ResourceLocation.CODEC.fieldOf("definition_id").forGetter(OreVeinInstance::definitionId),
            Codec.LONG.fieldOf("vein_seed").forGetter(OreVeinInstance::veinSeed),
            BlockPos.CODEC.fieldOf("center").forGetter(OreVeinInstance::center),
            shapeCodec.fieldOf("shape").forGetter(OreVeinInstance::shape),
            Codec.DOUBLE.fieldOf("density").forGetter(OreVeinInstance::density),
            blockCodec.fieldOf("host_block").forGetter(OreVeinInstance::hostBlock),
            OreEntry.codec(blockCodec).listOf().fieldOf("ores").forGetter(OreVeinInstance::ores)
        ).apply(instance, OreVeinInstance::new));
    }

    public OreVeinInstance {
        if (algorithmVersion != OreVeinUtil.ALGORITHM_VERSION) {
            throw new IllegalArgumentException("algorithmVersion mismatch");
        }
        if (!Double.isFinite(density) || density <= 0d || density > 1d) {
            throw new IllegalArgumentException("density must be finite and in the range (0, 1]");
        }
        if (ores.isEmpty()) {
            throw new IllegalArgumentException("ores must not be empty");
        }
    }
}
