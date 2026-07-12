package org.shsts.tinactory.core.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.StringRepresentable;
import org.shsts.tinactory.api.logistics.PortDirection;

import java.io.Reader;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class CodecHelper {
    public static final Gson GSON = new Gson();
    public static final Codec<PortDirection> PORT_DIRECTION_CODEC =
        StringRepresentable.fromEnum(PortDirection::values);

    public static JsonObject jsonFromReader(Reader s) {
        return GSON.fromJson(s, JsonObject.class);
    }

    public static <P> P parseJson(HolderLookup.Provider provider, Decoder<P> decoder, JsonElement je) {
        var ops = provider.createSerializationContext(JsonOps.INSTANCE);
        return decoder.parse(ops, je).getOrThrow();
    }

    public static <P> JsonElement encodeJson(HolderLookup.Provider provider, Encoder<P> encoder, P sth) {
        var ops = provider.createSerializationContext(JsonOps.INSTANCE);
        return encoder.encodeStart(ops, sth).getOrThrow();
    }

    public static <P> P parseTag(HolderLookup.Provider provider, Decoder<P> decoder, Tag tag) {
        var ops = provider.createSerializationContext(NbtOps.INSTANCE);
        return decoder.parse(ops, tag).getOrThrow();
    }

    public static <P> Tag encodeTag(HolderLookup.Provider provider, Encoder<P> encoder, P sth) {
        var ops = provider.createSerializationContext(NbtOps.INSTANCE);
        return encoder.encodeStart(ops, sth).getOrThrow();
    }

    public static CompoundTag readRequiredNbt(FriendlyByteBuf buf, String name) {
        var tag = buf.readNbt();
        if (tag == null) {
            throw new IllegalArgumentException("Missing " + name + " payload");
        }
        return tag;
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

    public static void encodeComponentToBuf(RegistryFriendlyByteBuf buf, Component component) {
        ComponentSerialization.STREAM_CODEC.encode(buf, component);
    }

    public static Component parseComponentFromBuf(RegistryFriendlyByteBuf buf) {
        return ComponentSerialization.STREAM_CODEC.decode(buf);
    }

    public static <T> ListTag encodeList(List<T> list, Function<T, Tag> encoder) {
        var ret = new ListTag();
        list.forEach($ -> ret.add(encoder.apply($)));
        return ret;
    }

    public static <T> void parseList(ListTag tag, Function<Tag, T> decoder, Consumer<T> cons) {
        tag.forEach($ -> cons.accept(decoder.apply($)));
    }

    public static int[] parseIntArray(JsonArray ja) {
        var ret = new int[ja.size()];
        var i = 0;
        for (var je : ja) {
            var x = GsonHelper.convertToInt(je, "element");
            ret[i++] = x;
        }
        return ret;
    }

    public static <T> void encodeCollectionToBuf(RegistryFriendlyByteBuf buf,
        Collection<T> collection, StreamEncoder<RegistryFriendlyByteBuf, T> encoder) {
        buf.writeCollection(collection, (buf1, sth) -> encoder.encode((RegistryFriendlyByteBuf) buf1, sth));
    }

    public static <T> List<T> parseListFromBuf(RegistryFriendlyByteBuf buf,
        StreamDecoder<RegistryFriendlyByteBuf, T> decoder) {
        return buf.readList(buf1 -> decoder.decode((RegistryFriendlyByteBuf) buf1));
    }

    public static void parseWithCountFromBuf(RegistryFriendlyByteBuf buf,
        Consumer<RegistryFriendlyByteBuf> cons) {
        buf.readWithCount(buf1 -> cons.accept((RegistryFriendlyByteBuf) buf1));
    }

    public static <T> void encodeOptionalToBuf(RegistryFriendlyByteBuf buf,
        Optional<T> sth, StreamEncoder<RegistryFriendlyByteBuf, T> encoder) {
        buf.writeOptional(sth, (buf1, sth1) -> encoder.encode((RegistryFriendlyByteBuf) buf1, sth1));
    }

    public static <T> Optional<T> parseOptionalFromBuf(RegistryFriendlyByteBuf buf,
        StreamDecoder<RegistryFriendlyByteBuf, T> decoder) {
        return buf.readOptional(buf1 -> decoder.decode((RegistryFriendlyByteBuf) buf1));
    }
}
