package org.shsts.tinactory.core.autocraft.pattern;

import com.mojang.serialization.Codec;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.shsts.tinactory.api.logistics.IStackKey;
import org.shsts.tinactory.core.util.CodecHelper;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record PatternCodec(Codec<CraftAmount> amountCodec, Codec<CraftPattern> patternCodec,
    StreamCodec<RegistryFriendlyByteBuf, CraftPattern> patternStreamCodec) {
    public Tag encodePattern(CraftPattern pattern) {
        return CodecHelper.encodeTag(patternCodec, pattern);
    }

    public void encodePatternToBuf(RegistryFriendlyByteBuf buf, CraftPattern pattern) {
        patternStreamCodec.encode(buf, pattern);
    }

    public CraftPattern decodePattern(Tag tag) {
        return CodecHelper.parseTag(patternCodec, tag);
    }

    public CraftPattern decodePatternFromBuf(RegistryFriendlyByteBuf buf) {
        return patternStreamCodec.decode(buf);
    }

    public Tag encodeAmount(CraftAmount amount) {
        return CodecHelper.encodeTag(amountCodec, amount);
    }

    public Tag encodeAmount(IStackKey key, long amount) {
        return encodeAmount(new CraftAmount(key, amount));
    }

    public CraftAmount decodeAmount(Tag tag) {
        return CodecHelper.parseTag(amountCodec, tag);
    }
}
