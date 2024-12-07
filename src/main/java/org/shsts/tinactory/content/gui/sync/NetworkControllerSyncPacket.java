package org.shsts.tinactory.content.gui.sync;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.shsts.tinactory.content.AllNetworks;
import org.shsts.tinactory.content.electric.ElectricComponent;
import org.shsts.tinactory.core.gui.sync.MenuSyncPacket;
import org.shsts.tinactory.core.network.NetworkBase;
import org.shsts.tinactory.core.network.NetworkController;

import java.util.Objects;

import static org.shsts.tinactory.content.AllCapabilities.NETWORK_CONTROLLER;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class NetworkControllerSyncPacket extends MenuSyncPacket {
    private boolean present;
    private NetworkBase.State state;
    private ElectricComponent.Metrics electricMetrics;

    public NetworkControllerSyncPacket() {}

    public NetworkControllerSyncPacket(int containerId, int index, BlockEntity be) {
        super(containerId, index);
        var network = NETWORK_CONTROLLER.tryGet(be)
            .flatMap(NetworkController::getNetwork);
        this.present = network.isPresent();
        if (present) {
            this.state = network.get().getState();
            this.electricMetrics = network.get()
                .getComponent(AllNetworks.ELECTRIC_COMPONENT)
                .getMetrics();
        }
    }

    public boolean isPresent() {
        return present;
    }

    public NetworkBase.State getState() {
        assert present;
        return state;
    }

    public ElectricComponent.Metrics getElectricMetrics() {
        return electricMetrics;
    }

    @Override
    public void serializeToBuf(FriendlyByteBuf buf) {
        super.serializeToBuf(buf);
        buf.writeBoolean(present);
        if (present) {
            buf.writeEnum(state);
            electricMetrics.writeToBuf(buf);
        }
    }

    @Override
    public void deserializeFromBuf(FriendlyByteBuf buf) {
        super.deserializeFromBuf(buf);
        present = buf.readBoolean();
        if (present) {
            state = buf.readEnum(NetworkBase.State.class);
            electricMetrics = ElectricComponent.Metrics.readFromBuf(buf);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NetworkControllerSyncPacket that)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        if (!Objects.equals(present, that.present)) {
            return false;
        }
        if (present) {
            return Objects.equals(state, that.state) &&
                Objects.equals(electricMetrics, that.electricMetrics);
        } else {
            return true;
        }
    }

    @Override
    public int hashCode() {
        if (present) {
            return Objects.hash(super.hashCode(), present);
        } else {
            return Objects.hash(super.hashCode(), present, state, electricMetrics);
        }
    }
}
