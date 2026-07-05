package org.shsts.tinactory.core.autocraft.pattern;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.logistics.IStackKey;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record CraftAmount(IStackKey key, long amount) {
    public CraftAmount {
        if (amount <= 0L) {
            throw new IllegalArgumentException("amount must be positive");
        }
    }

    public static Codec<CraftAmount> codec(Codec<IStackKey> keyCodec) {
        return RecordCodecBuilder.create(instance -> instance.group(
            keyCodec.fieldOf("key").forGetter(CraftAmount::key),
            Codec.LONG.fieldOf("amount").forGetter(CraftAmount::amount)
        ).apply(instance, CraftAmount::new));
    }
}
