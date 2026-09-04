package org.shsts.tinactory.core.worldgen.ore;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record OreEntry(ResourceLocation block, int weight) {
    public static final Codec<OreEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ResourceLocation.CODEC.fieldOf("block").forGetter(OreEntry::block),
        Codec.INT.fieldOf("weight").forGetter(OreEntry::weight)
    ).apply(instance, OreEntry::new));

    public OreEntry {
        Objects.requireNonNull(block, "block");
        if (weight <= 0) {
            throw new IllegalArgumentException("weight must be positive");
        }
    }
}
