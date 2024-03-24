package org.shsts.tinactory.content.logistics;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.logging.LogUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemHandlerHelper;
import org.shsts.tinactory.TinactoryConfig;
import org.shsts.tinactory.api.logistics.IItemCollection;
import org.shsts.tinactory.api.network.IScheduling;
import org.shsts.tinactory.content.AllNetworks;
import org.shsts.tinactory.core.logistics.ItemHelper;
import org.shsts.tinactory.core.logistics.ItemTypeWrapper;
import org.shsts.tinactory.core.network.Component;
import org.shsts.tinactory.core.network.ComponentType;
import org.shsts.tinactory.core.network.CompositeNetwork;
import org.shsts.tinactory.core.network.Network;
import org.slf4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LogisticsComponent extends Component {
    private static final Logger LOGGER = LogUtils.getLogger();

    public enum Direction {
        PUSH, PULL;

        public Direction invert() {
            return this == PUSH ? PULL : PUSH;
        }
    }

    private record Request(Direction dir, IItemCollection port, ItemStack item) {}

    private final Multimap<ItemTypeWrapper, Request> activeRequests = ArrayListMultimap.create();
    private final Multimap<Direction, IItemCollection> passiveStorages = HashMultimap.create();

    public static class WorkerProperty {
        public int workerSize;
        public int workerDelay;
        public int stackSize;

        public WorkerProperty(int workerSize, int workerDelay, int stackSize) {
            this.workerSize = workerSize;
            this.workerDelay = workerDelay;
            this.stackSize = stackSize;
        }
    }

    public final WorkerProperty workerProperty;
    private int ticks;

    public LogisticsComponent(ComponentType<LogisticsComponent> type, CompositeNetwork network) {
        super(type, network);
        this.workerProperty = new WorkerProperty(
                TinactoryConfig.INSTANCE.initialWorkerSize.get(),
                TinactoryConfig.INSTANCE.initialWorkerDelay.get(),
                TinactoryConfig.INSTANCE.initialWorkerStack.get()
        );
    }

    public void resetWorkers() {
        this.ticks = 0;
    }

    @Override
    public void onConnect() {
        super.onConnect();
        this.resetWorkers();
    }

    /**
     * Return remaining items.
     */
    private ItemStack transmitItem(IItemCollection from, IItemCollection to, ItemStack item, boolean simulate) {
        var extracted = from.extractItem(item, simulate);
        var notExtracted = item.getCount() - extracted.getCount();
        if (ItemHelper.canItemsStack(extracted, item)) {
            var remaining = to.insertItem(extracted, simulate);
            assert simulate || remaining.isEmpty();
            remaining.grow(notExtracted);
            return remaining;
        } else {
            return item;
        }
    }

    /**
     * Return remaining items.
     */
    private ItemStack transmitItem(IItemCollection from, IItemCollection to, ItemStack item, int limit) {
        var size = Math.min(this.workerProperty.stackSize, limit);
        var itemCopy = ItemHandlerHelper.copyStackWithSize(item, size);
        var remaining = this.transmitItem(from, to, itemCopy, true);
        if (!remaining.isEmpty()) {
            itemCopy.shrink(remaining.getCount());
        }
        if (itemCopy.isEmpty()) {
            return item;
        } else {
            var remaining1 = this.transmitItem(from, to, itemCopy, false);
            remaining1.grow(item.getCount() - itemCopy.getCount());
            return remaining1;
        }
    }

    private ItemStack transmitItem(Request req, IItemCollection otherPort, ItemStack item, int limit) {
        return req.dir == Direction.PULL ?
                this.transmitItem(otherPort, req.port, item, limit) :
                this.transmitItem(req.port, otherPort, item, limit);
    }

    private boolean handleActiveRequest(ItemTypeWrapper itemType, Request req) {
        var remaining = req.item;
        for (var otherReq : this.activeRequests.get(itemType)) {
            if (remaining.isEmpty()) {
                return true;
            }
            if (otherReq.dir == req.dir) {
                continue;
            }
            var limit = Math.min(remaining.getCount(), otherReq.item.getCount());
            var originalCount = remaining.getCount();
            remaining = this.transmitItem(req, otherReq.port, remaining, limit);
            otherReq.item.shrink(originalCount - remaining.getCount());
        }
        for (var storage : this.passiveStorages.get(req.dir.invert())) {
            if (remaining.isEmpty()) {
                return true;
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
            var result = this.handleActiveRequest(entry.getKey(), entry.getValue());
            if (result) {
                LOGGER.debug("deal with active request {}", entry.getValue());
            }
        }
        this.activeRequests.clear();
        this.ticks++;
    }

    public void addPassiveStorage(Direction dir, IItemCollection port) {
        this.passiveStorages.put(dir, port);
    }

    public void deletePassiveStorage(Direction dir, IItemCollection port) {
        this.passiveStorages.remove(dir, port);
    }

    public void addActiveRequest(Direction type, IItemCollection port, ItemStack item) {
        var item1 = item.copy();
        var req = new Request(type, port, item1);
        this.activeRequests.put(new ItemTypeWrapper(item1), req);
    }

    public void addActiveRequest(Direction type, IItemCollection port, ItemStack item, int count) {
        var item1 = ItemHandlerHelper.copyStackWithSize(item, count);
        var req = new Request(type, port, item1);
        this.activeRequests.put(new ItemTypeWrapper(item1), req);
    }

    @Override
    public void buildSchedulings(BiConsumer<Supplier<IScheduling>, Ticker> cons) {
        cons.accept(AllNetworks.LOGISTICS_SCHEDULING, this::onTick);
    }
}
