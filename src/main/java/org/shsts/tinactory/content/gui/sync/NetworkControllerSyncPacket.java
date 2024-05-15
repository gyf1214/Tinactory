package org.shsts.tinactory.content.gui.sync;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import org.shsts.tinactory.api.tech.ITeamProfile;
import org.shsts.tinactory.core.gui.sync.MenuSyncPacket;
import org.shsts.tinactory.core.network.NetworkController;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class NetworkControllerSyncPacket extends MenuSyncPacket {
    @Nullable
    private String teamName;

    public NetworkControllerSyncPacket() {}

    public NetworkControllerSyncPacket(int containerId, int index, NetworkController be) {
        super(containerId, index);
        this.teamName = be.getOwnerTeam().map(ITeamProfile::getName).orElse(null);
    }

    @Nullable
    public String getTeamName() {
        return teamName;
    }

    @Override
    public void serializeToBuf(FriendlyByteBuf buf) {
        super.serializeToBuf(buf);
        buf.writeOptional(Optional.ofNullable(teamName), FriendlyByteBuf::writeUtf);
    }

    @Override
    public void deserializeFromBuf(FriendlyByteBuf buf) {
        super.deserializeFromBuf(buf);
        teamName = buf.readOptional(FriendlyByteBuf::readUtf).orElse(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NetworkControllerSyncPacket that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(teamName, that.teamName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), teamName);
    }
}
