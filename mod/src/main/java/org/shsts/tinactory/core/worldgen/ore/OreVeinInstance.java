package org.shsts.tinactory.core.worldgen.ore;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record OreVeinInstance(
    int algorithmVersion,
    ResourceLocation definitionId,
    long veinSeed,
    BlockPos center,
    int radiusX,
    int radiusY,
    int radiusZ,
    double density,
    ResourceLocation hostBlock,
    List<OreEntry> ores
) {
    public static final Codec<OreVeinInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.fieldOf("algorithm_version").forGetter(OreVeinInstance::algorithmVersion),
        ResourceLocation.CODEC.fieldOf("definition_id").forGetter(OreVeinInstance::definitionId),
        Codec.LONG.fieldOf("vein_seed").forGetter(OreVeinInstance::veinSeed),
        BlockPos.CODEC.fieldOf("center").forGetter(OreVeinInstance::center),
        Codec.INT.fieldOf("radius_x").forGetter(OreVeinInstance::radiusX),
        Codec.INT.fieldOf("radius_y").forGetter(OreVeinInstance::radiusY),
        Codec.INT.fieldOf("radius_z").forGetter(OreVeinInstance::radiusZ),
        Codec.DOUBLE.fieldOf("density").forGetter(OreVeinInstance::density),
        ResourceLocation.CODEC.fieldOf("host_block").forGetter(OreVeinInstance::hostBlock),
        OreEntry.CODEC.listOf().fieldOf("ores").forGetter(OreVeinInstance::ores)
    ).apply(instance, OreVeinInstance::new));

    public OreVeinInstance {
        Objects.requireNonNull(definitionId, "definitionId");
        Objects.requireNonNull(center, "center");
        Objects.requireNonNull(hostBlock, "hostBlock");
        if (algorithmVersion <= 0) {
            throw new IllegalArgumentException("algorithmVersion must be positive");
        }
        validateRadius("X", radiusX);
        validateRadius("Y", radiusY);
        validateRadius("Z", radiusZ);
        if (!Double.isFinite(density) || density <= 0d || density > 1d) {
            throw new IllegalArgumentException("density must be finite and in the range (0, 1]");
        }
        center = new BlockPos(center.getX(), center.getY(), center.getZ());
        ores = List.copyOf(ores);
        if (ores.isEmpty()) {
            throw new IllegalArgumentException("ores must not be empty");
        }
    }

    private static void validateRadius(String axis, int radius) {
        if (radius <= 0) {
            throw new IllegalArgumentException("radius " + axis + " must be positive");
        }
    }
}
