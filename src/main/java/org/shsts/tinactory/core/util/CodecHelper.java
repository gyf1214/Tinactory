package org.shsts.tinactory.core.util;

import com.google.gson.JsonElement;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class CodecHelper {
    public static <P> P parseJson(Decoder<P> decoder, JsonElement je) {
        return decoder.parse(JsonOps.INSTANCE, je).getOrThrow(false, $ -> {});
    }

    public static <P> JsonElement encodeJson(Encoder<P> encoder, P sth) {
        return encoder.encodeStart(JsonOps.INSTANCE, sth).getOrThrow(false, $ -> {});
    }
}
