package org.shsts.tinactory.content.logistics;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.shsts.tinactory.TinactoryConfig;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.PortDirection;
import org.shsts.tinactory.api.network.IScheduling;
import org.shsts.tinactory.content.AllNetworks;
import org.shsts.tinactory.content.machine.Machine;
import org.shsts.tinactory.core.logistics.ILogisticsContentWrapper;
import org.shsts.tinactory.core.logistics.ILogisticsTypeWrapper;
import org.shsts.tinactory.core.network.ComponentType;
import org.shsts.tinactory.core.network.Network;
import org.shsts.tinactory.core.network.NetworkComponent;
import org.shsts.tinactory.core.util.RandomList;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LogisticsComponent extends NetworkComponent {
    private static final Logger LOGGER = LogUtils.getLogger();

    private record Request(PortDirection dir, IPort port,
        ILogisticsTypeWrapper type,
        ILogisticsContentWrapper content) {}

    private final Multimap<ILogisticsTypeWrapper, Request> activeRequests = ArrayListMultimap.create();
    private final RandomList<Request> activeRequestList = new RandomList<>();
    private final Multimap<PortDirection, IPort> passivePorts = HashMultimap.create();
    private final Map<PortDirection, RandomList<IPort>> passiveList = new HashMap<>();
    private final RandomList<IPort> storages = new RandomList<>();

    private int ticks;

    public LogisticsComponent(ComponentType<LogisticsComponent> type, Network network) {
        super(type, network);
        this.passiveList.put(PortDirection.INPUT, new RandomList<>());
        this.passiveList.put(PortDirection.OUTPUT, new RandomList<>());
    }

    private int getTechLevel() {
        return network.team.getModifier("logistics_level");
    }

    private int getWorkerSize() {
        return TinactoryConfig.INSTANCE.workerSize.get().get(getTechLevel());
    }

    private int getWorkerDelay() {
        return TinactoryConfig.INSTANCE.workerDelay.get().get(getTechLevel());
    }

    private int getWorkerStack() {
        return TinactoryConfig.INSTANCE.workerStack.get().get(getTechLevel());
    }

    private int getWorkerFluidStack() {
        return TinactoryConfig.INSTANCE.workerFluidStack.get().get(getTechLevel());
    }

    private int getContentLimit(ILogisticsContentWrapper content) {
        return switch (content.getPortType()) {
            case ITEM -> getWorkerStack();
            case FLUID -> getWorkerFluidStack();
            default -> throw new IllegalArgumentException();
        };
    }

    public void resetWorkers() {
        ticks = 0;
    }

    /**
     * Return remaining items.
     */
    private ILogisticsContentWrapper transmitItem(IPort from, IPort to,
        ILogisticsContentWrapper content, boolean simulate) {
        var extracted = content.extractFrom(from, simulate);
        var notExtracted = content.getCount() - extracted.getCount();
        if (ILogisticsContentWrapper.canStack(extracted, content)) {
            var remaining = extracted.insertInto(to, simulate);
            if (!simulate && !remaining.isEmpty()) {
                LOGGER.warn("transmitContent failed from={}, to={}, content={}", from, to, content);
            }
            remaining.grow(notExtracted);
            return remaining;
        } else {
            return content;
        }
    }

    /**
     * Return remaining items.
     */
    private ILogisticsContentWrapper transmitItem(IPort from, IPort to,
        ILogisticsContentWrapper content, int limit) {

        var contentCopy = content.copyWithAmount(limit);
        var remaining = transmitItem(from, to, contentCopy, true);
        if (!remaining.isEmpty()) {
            contentCopy.shrink(remaining.getCount());
        }
        if (contentCopy.isEmpty()) {
            return content;
        } else {
            var remaining1 = transmitItem(from, to, contentCopy, false);
            var size = content.getCount() - contentCopy.getCount();
            if (remaining1.isEmpty()) {
                return content.copyWithAmount(size);
            } else {
                remaining1.grow(size);
                return remaining1;
            }
        }
    }

    private ILogisticsContentWrapper transmitItem(Request req, IPort otherPort,
        ILogisticsContentWrapper item, int limit) {
        return switch (req.dir) {
            case NONE -> throw new IllegalArgumentException();
            case INPUT -> transmitItem(otherPort, req.port, item, limit);
            case OUTPUT -> transmitItem(req.port, otherPort, item, limit);
        };
    }

    private boolean handleItemActiveRequest(Request req) {
        if (req.dir == PortDirection.NONE) {
            return false;
        }
        var remaining = req.content;
        var limit = getContentLimit(remaining);
        if (remaining.getCount() > limit) {
            remaining = remaining.copyWithAmount(limit);
        }
        if (remaining.isEmpty()) {
            return false;
        }
        for (var otherReq : activeRequests.get(req.type)) {
            if (remaining.isEmpty()) {
                return true;
            }
            if (otherReq.dir == req.dir) {
                continue;
            }
            var limit1 = Math.min(remaining.getCount(), otherReq.content.getCount());
            var originalCount = remaining.getCount();
            remaining = transmitItem(req, otherReq.port, remaining, limit1);
            otherReq.content.shrink(originalCount - remaining.getCount());
        }
        for (var port : passiveList.get(req.dir.invert())) {
            if (remaining.isEmpty()) {
                return true;
            }
            if (port.type() != remaining.getPortType()) {
                continue;
            }
            remaining = transmitItem(req, port, remaining, remaining.getCount());
        }
        for (var port : storages) {
            if (remaining.isEmpty()) {
                return true;
            }
            if (port.type() != remaining.getPortType()) {
                continue;
            }
            remaining = transmitItem(req, port, remaining, remaining.getCount());
        }
        return true;
    }

    private record PortKey(UUID machineId, int portIndex) {}

    public record PortInfo(Machine machine, int portIndex, IPort port, @Nullable BlockPos subnet) {}

    private final Map<PortKey, PortInfo> ports = new HashMap<>();
    private final Multimap<BlockPos, PortKey> subnetPorts = HashMultimap.create();
    private final Set<PortKey> globalPorts = new HashSet<>();

    public void registerPort(BlockPos subnet, Machine machine, int index, IPort port) {
        var key = new PortKey(machine.getUuid(), index);
        ports.put(key, new PortInfo(machine, index, port, subnet));
        subnetPorts.put(subnet, key);
    }

    public void registerGlobalPort(Machine machine, int index, IPort port) {
        var key = new PortKey(machine.getUuid(), index);
        ports.put(key, new PortInfo(machine, index, port, null));
        globalPorts.add(key);
    }

    public void unregisterPort(Machine machine, int index) {
        var key = new PortKey(machine.getUuid(), index);
        if (ports.containsKey(key)) {
            var info = ports.get(key);
            ports.remove(key);
            if (info.subnet != null) {
                subnetPorts.remove(info.subnet, key);
            } else {
                globalPorts.remove(key);
            }
        }
    }

    public Collection<PortInfo> getGlobalPorts() {
        return globalPorts.stream()
            .map(ports::get)
            .toList();
    }

    public Collection<PortInfo> getVisiblePorts(BlockPos subnet) {
        var ret = new ArrayList<PortInfo>();
        globalPorts.forEach(key -> ret.add(ports.get(key)));
        subnetPorts.get(subnet).forEach(key -> ret.add(ports.get(key)));
        return ret;
    }

    @Override
    public void onConnect() {
        resetWorkers();
    }

    @Override
    public void onDisconnect() {
        activeRequests.clear();
        activeRequestList.clear();
        passivePorts.clear();
        for (var list : passiveList.values()) {
            list.clear();
        }
        storages.clear();

        ports.clear();
        subnetPorts.clear();
    }

    private void onTick(Level world, Network network) {
        var delay = getWorkerDelay();
        var workers = getWorkerSize();
        var index = ticks % delay;
        var cycles = Math.min(ticks / delay, (workers + index) / delay);
        ticks++;

        for (var req : activeRequestList) {
            if (cycles <= 0) {
                break;
            }
            if (handleItemActiveRequest(req)) {
                cycles--;
            }
        }

        activeRequestList.clear();
        activeRequests.clear();
    }

    @Override
    public void buildSchedulings(BiConsumer<Supplier<IScheduling>, Ticker> cons) {
        cons.accept(AllNetworks.LOGISTICS_SCHEDULING, this::onTick);
    }
}
