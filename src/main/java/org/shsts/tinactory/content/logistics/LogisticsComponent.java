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
                case ITEM -> this.stackSize;
                case FLUID -> this.fluidStackSize;
                default -> throw new IllegalArgumentException();
            };
        }
    }

    public final WorkerProperty workerProperty;
    private int ticks;

    public LogisticsComponent(ComponentType<LogisticsComponent> type, Network network) {
        super(type, network);
        this.workerProperty = new WorkerProperty(
                TinactoryConfig.INSTANCE.initialWorkerSize.get(),
                TinactoryConfig.INSTANCE.initialWorkerDelay.get(),
                TinactoryConfig.INSTANCE.initialWorkerStack.get(),
                TinactoryConfig.INSTANCE.initialWorkerFluidStack.get());
    }

    public void resetWorkers() {
        this.ticks = 0;
    }

    @Override
    public void onConnect() {
        this.resetWorkers();
    }

    @Override
    public void onDisconnect() {
        this.activeRequests.clear();
        this.passiveStorages.clear();
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

        var size = Math.min(this.workerProperty.getContentLimit(content), limit);
        var contentCopy = content.copyWithAmount(size);
        var remaining = this.transmitItem(from, to, contentCopy, true);
        if (!remaining.isEmpty()) {
            contentCopy.shrink(remaining.getCount());
        }
        if (contentCopy.isEmpty()) {
            return content;
        } else {
            var remaining1 = this.transmitItem(from, to, contentCopy, false);
            remaining1.grow(content.getCount() - contentCopy.getCount());
            return remaining1;
        }
    }

    private ILogisticsContentWrapper transmitItem(Request req, IPort otherPort,
                                                  ILogisticsContentWrapper item, int limit) {
        return req.dir == LogisticsDirection.PULL ?
                this.transmitItem(otherPort, req.port, item, limit) :
                this.transmitItem(req.port, otherPort, item, limit);
    }

    private boolean handleItemActiveRequest(ILogisticsTypeWrapper type, Request req) {
        var remaining = req.content;
        for (var otherReq : this.activeRequests.get(type)) {
            if (remaining.isEmpty()) {
                return true;
            }
            if (otherReq.dir == req.dir) {
                continue;
            }
            var limit = Math.min(remaining.getCount(), otherReq.content.getCount());
            var originalCount = remaining.getCount();
            remaining = this.transmitItem(req, otherReq.port, remaining, limit);
            otherReq.content.shrink(originalCount - remaining.getCount());
        }
        for (var storage : this.passiveStorages.get(req.dir.invert())) {
            if (remaining.isEmpty()) {
                return true;
            }
            if (storage.getPortType() != remaining.getPortType()) {
                continue;
            }
            remaining = this.transmitItem(req, storage, remaining, remaining.getCount());
        }
        return remaining.isEmpty();
    }

    private void onTick(Level world, Network network) {
        var delay = this.workerProperty.workerDelay;
        var workers = this.workerProperty.workerSize;
        var index = this.ticks % delay;
        var cycles = Math.min(this.ticks / delay, (workers + index) / delay);

        for (var entry : this.activeRequests.entries()) {
            if (cycles-- <= 0) {
                break;
            }
            var result = this.handleItemActiveRequest(entry.getKey(), entry.getValue());
            if (result) {
                LOGGER.debug("deal with active request {}", entry.getValue());
            }
        }
        this.activeRequests.clear();
        this.ticks++;
    }

    public void addPassiveStorage(LogisticsDirection dir, IPort port) {
        LOGGER.debug("add PassiveStorage {} {}", dir, port);
        this.passiveStorages.put(dir, port);
    }

    public void removePassiveStorage(LogisticsDirection dir, IPort port) {
        LOGGER.debug("remove PassiveStorage {} {}", dir, port);
        this.passiveStorages.remove(dir, port);
    }

    public void addActiveRequest(LogisticsDirection type, IItemCollection port, ItemStack item) {
        var item1 = item.copy();
        var req = new Request(type, port, new ItemContentWrapper(item1));
        this.activeRequests.put(new ItemTypeWrapper(item1), req);
    }

    public void addActiveRequest(LogisticsDirection type, IFluidCollection port, FluidStack fluid) {
        var fluid1 = fluid.copy();
        var req = new Request(type, port, new FluidContentWrapper(fluid1));
        this.activeRequests.put(new FluidTypeWrapper(fluid1), req);
    }

    @Override
    public void buildSchedulings(BiConsumer<Supplier<IScheduling>, Ticker> cons) {
        cons.accept(AllNetworks.LOGISTICS_SCHEDULING, this::onTick);
    }
}
