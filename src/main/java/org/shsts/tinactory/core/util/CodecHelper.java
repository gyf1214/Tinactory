package org.shsts.tinactory.core.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

import java.io.Reader;
import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class CodecHelper {
    public static Gson GSON = new Gson();

    public static JsonObject jsonFromStr(String s) {
        return GSON.fromJson(s, JsonObject.class);
    }

    public static JsonObject jsonFromReader(Reader s) {
        return GSON.fromJson(s, JsonObject.class);
    }

    public static String jsonToStr(JsonElement je) {
        return GSON.toJson(je);
    }

    public static <P> P parseJson(Decoder<P> decoder, JsonElement je) {
        return decoder.parse(JsonOps.INSTANCE, je).getOrThrow(false, $ -> {});
    }

    public static <P> JsonElement encodeJson(Encoder<P> encoder, P sth) {
        return encoder.encodeStart(JsonOps.INSTANCE, sth).getOrThrow(false, $ -> {});
    }

    public static CompoundTag encodeBlockPos(BlockPos pos) {
        var tag = new CompoundTag();
        tag.putInt("x", pos.getX());
        tag.putInt("y", pos.getY());
        tag.putInt("z", pos.getZ());
        return tag;
    }

    public static BlockPos parseBlockPos(CompoundTag tag) {
        var x = tag.getInt("x");
        var y = tag.getInt("y");
        var z = tag.getInt("z");
        return new BlockPos(x, y, z);
    }

    public static String encodeComponent(Component component) {
        return Component.Serializer.toJson(component);
    }

    public static Component parseComponent(String json) {
        return Objects.requireNonNullElse(Component.Serializer.fromJsonLenient(json),
            TextComponent.EMPTY);
    }
}
