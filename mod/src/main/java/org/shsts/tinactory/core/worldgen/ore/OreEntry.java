package org.shsts.tinactory.core.worldgen.ore;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

import static org.shsts.tinactory.Tinactory.CORE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record OreEntry(Block block, double weight) {
    public static final Codec<Block> BLOCK_CODEC = CodecHelper.entryCodec(Registries.BLOCK)
        .xmap(IEntry::get, OreEntry::blockToEntry);

    public static final Codec<OreEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        BLOCK_CODEC.fieldOf("block").forGetter(OreEntry::block),
        Codec.DOUBLE.fieldOf("weight").forGetter(OreEntry::weight)
    ).apply(instance, OreEntry::new));

    public OreEntry {
        if (!Double.isFinite(weight) || weight <= 0d) {
            throw new IllegalArgumentException("weight must be positive");
        }
    }

    @SuppressWarnings("deprecation")
    private static IEntry<Block> blockToEntry(Block block) {
        var loc = block.builtInRegistryHolder().unwrapKey().orElseThrow().location();
        return CORE.createEntry(loc, block);
    }
}
