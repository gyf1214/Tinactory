package org.shsts.tinactory.core.worldgen.ore;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.Block;

import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record OreVeinDefinition(
    int minY,
    int maxY,
    OreShapeDefinition<?> shape,
    double density,
    Block hostBlock,
    List<OreEntry> ores
) {
    public static final MapCodec<OreVeinDefinition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.INT.fieldOf("min_y").forGetter(OreVeinDefinition::minY),
        Codec.INT.fieldOf("max_y").forGetter(OreVeinDefinition::maxY),
        OreVeinUtil.DEFINITION_CODEC.fieldOf("shape").forGetter(OreVeinDefinition::shape),
        Codec.DOUBLE.fieldOf("density").forGetter(OreVeinDefinition::density),
        OreEntry.BLOCK_CODEC.fieldOf("host_block").forGetter(OreVeinDefinition::hostBlock),
        OreEntry.CODEC.listOf().fieldOf("ores").forGetter(OreVeinDefinition::ores)
    ).apply(instance, OreVeinDefinition::new));

    public OreVeinDefinition {
        if (minY > maxY) {
            throw new IllegalArgumentException("minY must not exceed maxY");
        }
        if (!Double.isFinite(density) || density <= 0d || density > 1d) {
            throw new IllegalArgumentException("density must be finite and in the range (0, 1]");
        }
        if (ores.isEmpty()) {
            throw new IllegalArgumentException("ores must not be empty");
        }
    }

    public OreVeinDefinition scaleArea(double areaScale) {
        return new OreVeinDefinition(minY, maxY, shape.scaleArea(areaScale), density, hostBlock, ores);
    }
}
