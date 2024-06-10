package org.shsts.tinactory.core.util;

import com.google.gson.JsonElement;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

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

    public static CompoundTag serializeBlockPos(BlockPos pos) {
        var tag = new CompoundTag();
        tag.putInt("x", pos.getX());
        tag.putInt("y", pos.getY());
        tag.putInt("z", pos.getZ());
        return tag;
    }

    public static BlockPos deserializeBlockPos(CompoundTag tag) {
        var x = tag.getInt("x");
        var y = tag.getInt("y");
        var z = tag.getInt("z");
        return new BlockPos(x, y, z);
    }
}
