package org.shsts.tinactory.core.tech;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import org.shsts.tinactory.core.common.IPacket;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collection;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TechInitPacket implements IPacket {
    private static final Gson GSON = new Gson();

    private Collection<Technology> techs;

    public TechInitPacket() {}

    public TechInitPacket(Collection<Technology> techs) {
        this.techs = techs;
    }

    public Collection<Technology> getTechs() {
        return techs;
    }

    private static Technology techFromBuf(FriendlyByteBuf buf) {
        var loc = buf.readResourceLocation();
        var jo = GSON.fromJson(buf.readUtf(), JsonObject.class);
        var tech = Technology.CODEC.parse(JsonOps.INSTANCE, jo).getOrThrow(false, $ -> {});
        tech.setLoc(loc);
        return tech;
    }

    private static void techToBuf(FriendlyByteBuf buf, Technology tech) {
        buf.writeResourceLocation(tech.getLoc());
        var je = Technology.CODEC.encodeStart(JsonOps.INSTANCE, tech).getOrThrow(false, $ -> {});
        buf.writeUtf(GSON.toJson(je));
    }

    @Override
    public void serializeToBuf(FriendlyByteBuf buf) {
        buf.writeCollection(techs, TechInitPacket::techToBuf);
    }

    @Override
    public void deserializeFromBuf(FriendlyByteBuf buf) {
        techs = buf.readCollection(ArrayList::new, TechInitPacket::techFromBuf);
    }
}
