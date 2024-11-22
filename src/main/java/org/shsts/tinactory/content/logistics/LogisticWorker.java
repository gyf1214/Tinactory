package org.shsts.tinactory.content.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.shsts.tinactory.TinactoryConfig;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.AllNetworks;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.content.gui.sync.LogisticWorkerSyncPacket;
import org.shsts.tinactory.content.machine.Machine;
import org.shsts.tinactory.core.common.SmartBlockEntity;
import org.shsts.tinactory.core.network.Network;
import org.shsts.tinactory.registrate.builder.BlockEntityTypeBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.shsts.tinactory.core.gui.ProcessingMenu.portLabel;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LogisticWorker extends SmartBlockEntity {
    private final Voltage voltage;

    public LogisticWorker(BlockEntityType<LogisticWorker> type, BlockPos pos, BlockState state,
        Voltage voltage) {
        super(type, pos, state);
        this.voltage = voltage;
    }

    public static BlockEntityTypeBuilder.Factory<LogisticWorker> factory(Voltage v) {
        return (type, pos, state) -> new LogisticWorker(type, pos, state, v);
    }

    public Optional<Network> getNetwork() {
        return AllCapabilities.MACHINE.tryGet(this)
            .flatMap(Machine::getNetwork);
    }

    public List<LogisticWorkerSyncPacket.PortInfo> getVisiblePorts() {
        var ret = new ArrayList<LogisticWorkerSyncPacket.PortInfo>();
        getNetwork().ifPresent(network -> {
            var logistics = network.getComponent(AllNetworks.LOGISTIC_COMPONENT);
            var subnet = network.getSubnet(worldPosition);
            for (var info : logistics.getVisiblePorts(subnet)) {
                var machine = info.machine();
                var index = info.portIndex();
                var portName = portLabel(info.port().type(), index);

                ret.add(new LogisticWorkerSyncPacket.PortInfo(machine.getUuid(),
                    index, machine.getTitle(), machine.getIcon(), portName));
            }
        });
        return ret;
    }

    public int getConfigSlots() {
        return TinactoryConfig.INSTANCE.workerSize.get().get(voltage.rank - 1);
    }
}
