package org.shsts.tinactory.content.recipe;

import com.google.gson.JsonElement;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public interface ICombinedSerializer<T> {
    default <P> P parseJson(Decoder<P> decoder, JsonElement je) {
        return decoder.parse(JsonOps.INSTANCE, je).getOrThrow(false, $ -> {});
    }

    default <P> JsonElement encodeJson(Encoder<P> encoder, P sth) {
        return encoder.encodeStart(JsonOps.INSTANCE, sth).getOrThrow(false, $ -> {});
    }

    String getTypeName();

    void toNetwork(T sth, FriendlyByteBuf buf);

    T fromNetwork(FriendlyByteBuf buf);

    JsonElement toJson(T sth);

    T fromJson(JsonElement je);
}
