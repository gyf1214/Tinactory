package org.shsts.tinactory.core.tech;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.core.common.IPacket;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TechUpdatePacket implements IPacket {
    private Map<ResourceLocation, Long> techs;

    public TechUpdatePacket() {}

    public TechUpdatePacket(Map<ResourceLocation, Long> techs) {
        this.techs = techs;
    }

    public Map<ResourceLocation, Long> getTechs() {
        return techs;
    }

    @Override
    public void serializeToBuf(FriendlyByteBuf buf) {
        buf.writeMap(techs, FriendlyByteBuf::writeResourceLocation, FriendlyByteBuf::writeLong);
    }

    @Override
    public void deserializeFromBuf(FriendlyByteBuf buf) {
        techs = buf.readMap(FriendlyByteBuf::readResourceLocation, FriendlyByteBuf::readLong);
    }
}
