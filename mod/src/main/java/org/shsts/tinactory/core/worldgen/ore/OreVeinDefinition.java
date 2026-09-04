package org.shsts.tinactory.core.worldgen.ore;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record OreVeinDefinition(
    ResourceLocation id,
    int selectionWeight,
    int minY,
    int maxY,
    int minRadiusX,
    int maxRadiusX,
    int minRadiusY,
    int maxRadiusY,
    int minRadiusZ,
    int maxRadiusZ,
    double density,
    ResourceLocation hostBlock,
    List<OreEntry> ores
) {
    public static final Codec<OreVeinDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ResourceLocation.CODEC.fieldOf("id").forGetter(OreVeinDefinition::id),
        Codec.INT.fieldOf("selection_weight").forGetter(OreVeinDefinition::selectionWeight),
        Codec.INT.fieldOf("min_y").forGetter(OreVeinDefinition::minY),
        Codec.INT.fieldOf("max_y").forGetter(OreVeinDefinition::maxY),
        Codec.INT.fieldOf("min_radius_x").forGetter(OreVeinDefinition::minRadiusX),
        Codec.INT.fieldOf("max_radius_x").forGetter(OreVeinDefinition::maxRadiusX),
        Codec.INT.fieldOf("min_radius_y").forGetter(OreVeinDefinition::minRadiusY),
        Codec.INT.fieldOf("max_radius_y").forGetter(OreVeinDefinition::maxRadiusY),
        Codec.INT.fieldOf("min_radius_z").forGetter(OreVeinDefinition::minRadiusZ),
        Codec.INT.fieldOf("max_radius_z").forGetter(OreVeinDefinition::maxRadiusZ),
        Codec.DOUBLE.fieldOf("density").forGetter(OreVeinDefinition::density),
        ResourceLocation.CODEC.fieldOf("host_block").forGetter(OreVeinDefinition::hostBlock),
        OreEntry.CODEC.listOf().fieldOf("ores").forGetter(OreVeinDefinition::ores)
    ).apply(instance, OreVeinDefinition::new));

    public OreVeinDefinition {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(hostBlock, "hostBlock");
        if (selectionWeight <= 0) {
            throw new IllegalArgumentException("selectionWeight must be positive");
        }
        if (minY > maxY) {
            throw new IllegalArgumentException("minY must not exceed maxY");
        }
        validateRadiusRange("X", minRadiusX, maxRadiusX);
        validateRadiusRange("Y", minRadiusY, maxRadiusY);
        validateRadiusRange("Z", minRadiusZ, maxRadiusZ);
        if (!Double.isFinite(density) || density <= 0d || density > 1d) {
            throw new IllegalArgumentException("density must be finite and in the range (0, 1]");
        }
        ores = List.copyOf(ores);
        if (ores.isEmpty()) {
            throw new IllegalArgumentException("ores must not be empty");
        }
    }

    private static void validateRadiusRange(String axis, int min, int max) {
        if (min <= 0 || min > max) {
            throw new IllegalArgumentException("radius " + axis + " range is invalid");
        }
    }
}
