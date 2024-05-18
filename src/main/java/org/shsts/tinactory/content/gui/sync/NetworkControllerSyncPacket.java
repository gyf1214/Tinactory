package org.shsts.tinactory.content.gui.sync;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import org.shsts.tinactory.core.gui.sync.MenuSyncPacket;
import org.shsts.tinactory.core.network.NetworkBase;
import org.shsts.tinactory.core.network.NetworkController;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class NetworkControllerSyncPacket extends MenuSyncPacket {
    private boolean present;
    private String teamName;
    private NetworkBase.State state;

    public NetworkControllerSyncPacket() {}

    public NetworkControllerSyncPacket(int containerId, int index, NetworkController be) {
        super(containerId, index);
        this.present = be.getNetwork().isPresent();
        if (present) {
            var network = be.getNetwork().get();
            this.teamName = network.team.getName();
            this.state = network.getState();
        }
    }

    public boolean isPresent() {
        return present;
    }

    public String getTeamName() {
        return teamName;
    }

    public NetworkBase.State getState() {
        return state;
    }

    @Override
    public void serializeToBuf(FriendlyByteBuf buf) {
        super.serializeToBuf(buf);
        buf.writeBoolean(present);
        if (present) {
            buf.writeUtf(teamName);
            buf.writeEnum(state);
        }
    }

    @Override
    public void deserializeFromBuf(FriendlyByteBuf buf) {
        super.deserializeFromBuf(buf);
        present = buf.readBoolean();
        if (present) {
            teamName = buf.readUtf();
            state = buf.readEnum(NetworkBase.State.class);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NetworkControllerSyncPacket that)) return false;
        if (!super.equals(o)) return false;
        if (!Objects.equals(present, that.present)) {
            return false;
        }
        if (present) {
            return Objects.equals(teamName, that.teamName) &&
                    Objects.equals(state, that.state);
        } else {
            return true;
        }
    }

    @Override
    public int hashCode() {
        if (present) {
            return Objects.hash(super.hashCode(), present);
        } else {
            return Objects.hash(super.hashCode(), present, teamName, state);
        }
    }
}
