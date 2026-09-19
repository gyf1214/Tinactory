package org.shsts.tinactory.content.logistics;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.UUIDUtil;

import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record SignalConfig(UUID machine, String key) {
    public static final Codec<SignalConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        UUIDUtil.CODEC.fieldOf("machine").forGetter(SignalConfig::machine),
        Codec.STRING.fieldOf("key").forGetter(SignalConfig::key)
    ).apply(instance, SignalConfig::new));
}
