package org.shsts.tinactory.core.tech;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinycorelib.api.network.IPacket;

import java.util.Collection;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TechInitPacket implements IPacket {
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
        var jo = CodecHelper.jsonFromStr(buf.readUtf());
        var tech = CodecHelper.parseJson(Technology.CODEC, jo);
        tech.setLoc(loc);
        return tech;
    }

    private static void techToBuf(FriendlyByteBuf buf, Technology tech) {
        buf.writeResourceLocation(tech.loc());
        var je = CodecHelper.encodeJson(Technology.CODEC, tech);
        buf.writeUtf(CodecHelper.jsonToStr(je));
    }

    @Override
    public void serializeToBuf(RegistryFriendlyByteBuf buf) {
        CodecHelper.encodeCollectionToBuf(buf, techs, TechInitPacket::techToBuf);
    }

    @Override
    public void deserializeFromBuf(RegistryFriendlyByteBuf buf) {
        techs = CodecHelper.parseListFromBuf(buf, TechInitPacket::techFromBuf);
    }
}
