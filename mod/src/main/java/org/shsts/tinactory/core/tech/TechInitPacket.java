package org.shsts.tinactory.core.tech;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinycorelib.api.network.IPacket;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TechInitPacket implements IPacket {
    private Collection<Entry> entries;

    public TechInitPacket() {}

    public TechInitPacket(Collection<Entry> entries) {
        this.entries = entries;
    }

    public static TechInitPacket fromMap(Map<ResourceLocation, Technology> technologies) {
        return new TechInitPacket(technologies.entrySet().stream()
            .map(entry -> new Entry(entry.getKey(), entry.getValue()))
            .toList());
    }

    public Collection<Entry> entries() {
        return entries;
    }

    public Map<ResourceLocation, Technology> toMap() {
        var result = new LinkedHashMap<ResourceLocation, Technology>();
        for (var entry : entries) {
            result.put(entry.loc(), entry.technology());
        }
        return result;
    }

    @Override
    public void serializeToBuf(RegistryFriendlyByteBuf buf) {
        CodecHelper.encodeCollectionToBuf(buf, entries, (buf1, entry) -> {
            buf1.writeResourceLocation(entry.loc());
            Technology.STREAM_CODEC.encode(buf1, entry.technology());
        });
    }

    @Override
    public void deserializeFromBuf(RegistryFriendlyByteBuf buf) {
        entries = CodecHelper.parseListFromBuf(buf, buf1 -> {
            var loc = buf1.readResourceLocation();
            var tech = Technology.STREAM_CODEC.decode(buf1);
            return new Entry(loc, tech);
        });
    }

    public record Entry(ResourceLocation loc, Technology technology) {}
}
