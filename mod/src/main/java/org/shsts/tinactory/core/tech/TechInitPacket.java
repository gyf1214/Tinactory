package org.shsts.tinactory.core.tech;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
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

    @Override
    public void serializeToBuf(RegistryFriendlyByteBuf buf) {
        CodecHelper.encodeCollectionToBuf(buf, techs, (buf1, tech) -> {
            buf1.writeResourceLocation(tech.loc());
            Technology.STREAM_CODEC.encode(buf1, tech);
        });
    }

    @Override
    public void deserializeFromBuf(RegistryFriendlyByteBuf buf) {
        techs = CodecHelper.parseListFromBuf(buf, buf1 -> {
            var loc = buf1.readResourceLocation();
            var tech = Technology.STREAM_CODEC.decode(buf1);
            tech.setLoc(loc);
            return tech;
        });
    }
}
