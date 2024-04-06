package org.shsts.tinactory.content.logistics;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.logging.LogUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.TinactoryConfig;
import org.shsts.tinactory.api.logistics.IFluidCollection;
import org.shsts.tinactory.api.logistics.IItemCollection;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.network.IScheduling;
import org.shsts.tinactory.content.AllNetworks;
import org.shsts.tinactory.core.logistics.FluidContentWrapper;
import org.shsts.tinactory.core.logistics.FluidTypeWrapper;
import org.shsts.tinactory.core.logistics.ILogisticsContentWrapper;
import org.shsts.tinactory.core.logistics.ILogisticsTypeWrapper;
import org.shsts.tinactory.core.logistics.ItemContentWrapper;
import org.shsts.tinactory.core.logistics.ItemTypeWrapper;
import org.shsts.tinactory.core.network.Component;
import org.shsts.tinactory.core.network.ComponentType;
import org.shsts.tinactory.core.network.Network;
import org.slf4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LogisticsComponent extends Component {
    private static final Logger LOGGER = LogUtils.getLogger();

    private record Request(LogisticsDirection dir, IPort port, ILogisticsContentWrapper content) {}

    private final Multimap<ILogisticsTypeWrapper, Request> activeRequests = ArrayListMultimap.create();
    private final Multimap<LogisticsDirection, IPort> passiveStorages = HashMultimap.create();

    public static class WorkerProperty {
        public int workerSize;
        public int workerDelay;
        public int stackSize;
        public int fluidStackSize;

        public WorkerProperty(int workerSize, int workerDelay, int stackSize, int fluidStackSize) {
            this.workerSize = workerSize;
            this.workerDelay = workerDelay;
            this.stackSize = stackSize;
            this.fluidStackSize = fluidStackSize;
        }

        public int getContentLimit(ILogisticsContentWrapper content) {
            return switch (content.getPortType()) {
                case ITEM -> stackSize;
                case FLUID -> fluidStackSize;
                default -> throw new IllegalArgumentException();
            };
        }
    }

    public final WorkerProperty workerProperty;
    private int ticks;

    public LogisticsComponent(ComponentType<LogisticsComponent> type, Network network) {
        super(type, network);
        workerProperty = new WorkerProperty(
                TinactoryConfig.INSTANCE.initialWorkerSize.get(),
                TinactoryConfig.INSTANCE.initialWorkerDelay.get(),
                TinactoryConfig.INSTANCE.initialWorkerStack.get(),
                TinactoryConfig.INSTANCE.initialWorkerFluidStack.get());
    }

    public void resetWorkers() {
        ticks = 0;
    }

    @Override
    public void onConnect() {
        resetWorkers();
    }

    @Override
    public void onDisconnect() {
        activeRequests.clear();
        passiveStorages.clear();
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

        var size = Math.min(workerProperty.getContentLimit(content), limit);
        var contentCopy = content.copyWithAmount(size);
        var remaining = transmitItem(from, to, contentCopy, true);
        if (!remaining.isEmpty()) {
            contentCopy.shrink(remaining.getCount());
        }
        if (contentCopy.isEmpty()) {
            return content;
        } else {
            var remaining1 = transmitItem(from, to, contentCopy, false);
            remaining1.grow(content.getCount() - contentCopy.getCount());
            return remaining1;
        }
    }

    private ILogisticsContentWrapper transmitItem(Request req, IPort otherPort,
                                                  ILogisticsContentWrapper item, int limit) {
        return req.dir == LogisticsDirection.PULL ?
                transmitItem(otherPort, req.port, item, limit) :
                transmitItem(req.port, otherPort, item, limit);
    }

    private void handleItemActiveRequest(ILogisticsTypeWrapper type, Request req) {
        var remaining = req.content;
        for (var otherReq : activeRequests.get(type)) {
            if (remaining.isEmpty()) {
                return;
            }
            if (otherReq.dir == req.dir) {
                continue;
            }
            var limit = Math.min(remaining.getCount(), otherReq.content.getCount());
            var originalCount = remaining.getCount();
            remaining = transmitItem(req, otherReq.port, remaining, limit);
            otherReq.content.shrink(originalCount - remaining.getCount());
        }
        for (var storage : passiveStorages.get(req.dir.invert())) {
            if (remaining.isEmpty()) {
                return;
            }
            if (storage.getPortType() != remaining.getPortType()) {
                continue;
            }
            remaining = transmitItem(req, storage, remaining, remaining.getCount());
        }
        remaining.isEmpty();
    }

    private void onTick(Level world, Network network) {
        var delay = workerProperty.workerDelay;
        var workers = workerProperty.workerSize;
        var index = ticks % delay;
        var cycles = Math.min(ticks / delay, (workers + index) / delay);

        for (var entry : activeRequests.entries()) {
            if (cycles-- <= 0) {
                break;
            }
            handleItemActiveRequest(entry.getKey(), entry.getValue());
        }
        activeRequests.clear();
        ticks++;
    }

    public void addPassiveStorage(LogisticsDirection dir, IPort port) {
        LOGGER.debug("add PassiveStorage {} {}", dir, port);
        passiveStorages.put(dir, port);
    }

    public void removePassiveStorage(LogisticsDirection dir, IPort port) {
        LOGGER.debug("remove PassiveStorage {} {}", dir, port);
        passiveStorages.remove(dir, port);
    }

    public void addActiveRequest(LogisticsDirection type, IItemCollection port, ItemStack item) {
        var item1 = item.copy();
        var req = new Request(type, port, new ItemContentWrapper(item1));
        activeRequests.put(new ItemTypeWrapper(item1), req);
    }

    public void addActiveRequest(LogisticsDirection type, IFluidCollection port, FluidStack fluid) {
        var fluid1 = fluid.copy();
        var req = new Request(type, port, new FluidContentWrapper(fluid1));
        activeRequests.put(new FluidTypeWrapper(fluid1), req);
    }

    @Override
    public void buildSchedulings(BiConsumer<Supplier<IScheduling>, Ticker> cons) {
        cons.accept(AllNetworks.LOGISTICS_SCHEDULING, this::onTick);
    }
}
