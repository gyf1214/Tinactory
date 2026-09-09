package org.shsts.tinactory.core.worldgen.ore;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.core.worldgen.ore.shape.OreShapeDefinition;

import java.util.List;
import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record OreVeinDefinition(
    ResourceLocation id,
    int selectionWeight,
    int minY,
    int maxY,
    OreShapeDefinition<?> shape,
    double density,
    ResourceLocation hostBlock,
    List<OreEntry> ores
) {
    public static MapCodec<OreVeinDefinition> codec(MapCodec<OreShapeDefinition<?>> shapeCodec) {
        Objects.requireNonNull(shapeCodec, "shapeCodec");
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(OreVeinDefinition::id),
            Codec.INT.fieldOf("selection_weight").forGetter(OreVeinDefinition::selectionWeight),
            Codec.INT.fieldOf("min_y").forGetter(OreVeinDefinition::minY),
            Codec.INT.fieldOf("max_y").forGetter(OreVeinDefinition::maxY),
            shapeCodec.fieldOf("shape").forGetter(OreVeinDefinition::shape),
            Codec.DOUBLE.fieldOf("density").forGetter(OreVeinDefinition::density),
            ResourceLocation.CODEC.fieldOf("host_block").forGetter(OreVeinDefinition::hostBlock),
            OreEntry.CODEC.listOf().fieldOf("ores").forGetter(OreVeinDefinition::ores)
        ).apply(instance, OreVeinDefinition::new));
    }

    public OreVeinDefinition {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(shape, "shape");
        Objects.requireNonNull(hostBlock, "hostBlock");
        if (selectionWeight <= 0) {
            throw new IllegalArgumentException("selectionWeight must be positive");
        }
        if (minY > maxY) {
            throw new IllegalArgumentException("minY must not exceed maxY");
        }
        if (!Double.isFinite(density) || density <= 0d || density > 1d) {
            throw new IllegalArgumentException("density must be finite and in the range (0, 1]");
        }
        ores = List.copyOf(ores);
        if (ores.isEmpty()) {
            throw new IllegalArgumentException("ores must not be empty");
        }
    }
}
