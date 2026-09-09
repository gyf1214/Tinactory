package org.shsts.tinactory.core.worldgen.ore;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.Block;

import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record OreEntry(Block block, int weight) {
    public static Codec<OreEntry> codec(Codec<Block> blockCodec) {
        Objects.requireNonNull(blockCodec, "blockCodec");
        return RecordCodecBuilder.create(instance -> instance.group(
            blockCodec.fieldOf("block").forGetter(OreEntry::block),
            Codec.INT.fieldOf("weight").forGetter(OreEntry::weight)
        ).apply(instance, OreEntry::new));
    }

    public OreEntry {
        Objects.requireNonNull(block, "block");
        if (weight <= 0) {
            throw new IllegalArgumentException("weight must be positive");
        }
    }
}
