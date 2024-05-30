package org.shsts.tinactory.content.gui.sync;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import org.shsts.tinactory.content.AllNetworks;
import org.shsts.tinactory.core.gui.sync.MenuSyncPacket;
import org.shsts.tinactory.core.network.NetworkBase;
import org.shsts.tinactory.core.network.NetworkController;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class NetworkControllerSyncPacket extends MenuSyncPacket {
    private boolean present;
    private NetworkBase.State state;
    private double workFactor;

    public NetworkControllerSyncPacket() {}

    public NetworkControllerSyncPacket(int containerId, int index, NetworkController be) {
        super(containerId, index);
        this.present = be.getNetwork().isPresent();
        if (present) {
            var network = be.getNetwork().get();
            this.state = network.getState();
            this.workFactor = network.getComponent(AllNetworks.ELECTRIC_COMPONENT).getWorkFactor();
        }
    }

    public boolean isPresent() {
        return present;
    }

    public NetworkBase.State getState() {
        assert present;
        return state;
    }

    public double getWorkFactor() {
        assert present;
        return workFactor;
    }

    @Override
    public void serializeToBuf(FriendlyByteBuf buf) {
        super.serializeToBuf(buf);
        buf.writeBoolean(present);
        if (present) {
            buf.writeEnum(state);
            buf.writeDouble(workFactor);
        }
    }

    @Override
    public void deserializeFromBuf(FriendlyByteBuf buf) {
        super.deserializeFromBuf(buf);
        present = buf.readBoolean();
        if (present) {
            state = buf.readEnum(NetworkBase.State.class);
            workFactor = buf.readDouble();
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
            return Objects.equals(state, that.state) &&
                    workFactor == that.workFactor;
        } else {
            return true;
        }
    }

    @Override
    public int hashCode() {
        if (present) {
            return Objects.hash(super.hashCode(), present);
        } else {
            return Objects.hash(super.hashCode(), present, state, workFactor);
        }
    }
}
