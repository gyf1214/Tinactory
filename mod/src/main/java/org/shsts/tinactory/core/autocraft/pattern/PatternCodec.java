package org.shsts.tinactory.core.autocraft.pattern;

import com.mojang.serialization.Codec;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.shsts.tinactory.api.logistics.IStackKey;
import org.shsts.tinactory.core.util.CodecHelper;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record PatternCodec(Codec<CraftAmount> amountCodec, Codec<CraftPattern> patternCodec,
    StreamCodec<RegistryFriendlyByteBuf, CraftPattern> patternStreamCodec) {
    public Tag encodePattern(HolderLookup.Provider provider, CraftPattern pattern) {
        return CodecHelper.encodeTag(provider, patternCodec, pattern);
    }

    public void encodePatternToBuf(RegistryFriendlyByteBuf buf, CraftPattern pattern) {
        patternStreamCodec.encode(buf, pattern);
    }

    public CraftPattern decodePattern(HolderLookup.Provider provider, Tag tag) {
        return CodecHelper.parseTag(provider, patternCodec, tag);
    }

    public CraftPattern decodePatternFromBuf(RegistryFriendlyByteBuf buf) {
        return patternStreamCodec.decode(buf);
    }

    public Tag encodeAmount(HolderLookup.Provider provider, CraftAmount amount) {
        return CodecHelper.encodeTag(provider, amountCodec, amount);
    }

    public Tag encodeAmount(HolderLookup.Provider provider, IStackKey key, long amount) {
        return encodeAmount(provider, new CraftAmount(key, amount));
    }

    public CraftAmount decodeAmount(HolderLookup.Provider provider, Tag tag) {
        return CodecHelper.parseTag(provider, amountCodec, tag);
    }
}
