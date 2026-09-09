package org.shsts.tinactory.content.worldgen.ore;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import org.shsts.tinactory.AllRegistries;
import org.shsts.tinactory.core.worldgen.ore.IOreShape;
import org.shsts.tinactory.core.worldgen.ore.OreVeinDefinition;
import org.shsts.tinactory.core.worldgen.ore.OreVeinUtil;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class OreVeinStructureType implements StructureType<OreVeinStructure> {
    private MapCodec<OreVeinStructure> codec;

    public OreVeinStructureType() {}

    @Override
    public MapCodec<OreVeinStructure> codec() {
        if (codec == null) {
            Codec<IOreShape<?, ?>> shapeCodec = AllRegistries.ORE_SHAPES.get().byNameCodec();
            var shapeDefinitionCodec = OreVeinUtil.definitionCodec(shapeCodec);
            var definitionCodec = OreVeinDefinition.codec(BuiltInRegistries.BLOCK.byNameCodec(), shapeDefinitionCodec);
            codec = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Structure.settingsCodec(instance),
                definitionCodec.codec().listOf().fieldOf("definitions").forGetter(OreVeinStructure::definitions)
            ).apply(instance, OreVeinStructure::new));
        }
        return codec;
    }
}
