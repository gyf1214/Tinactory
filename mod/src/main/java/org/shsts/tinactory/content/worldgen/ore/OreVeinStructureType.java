package org.shsts.tinactory.content.worldgen.ore;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import org.shsts.tinactory.core.worldgen.ore.OreVeinDefinition;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class OreVeinStructureType implements StructureType<OreVeinStructure> {
    private static final MapCodec<OreVeinStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Structure.settingsCodec(instance),
        OreVeinDefinition.CODEC.codec().listOf().fieldOf("definitions").forGetter(OreVeinStructure::definitions)
    ).apply(instance, OreVeinStructure::new));

    public OreVeinStructureType() {}

    @Override
    public MapCodec<OreVeinStructure> codec() {
        return CODEC;
    }
}
