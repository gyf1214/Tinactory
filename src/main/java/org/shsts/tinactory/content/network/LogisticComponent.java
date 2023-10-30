package org.shsts.tinactory.content.network;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.mojang.logging.LogUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemHandlerHelper;
import org.shsts.tinactory.content.logistics.IItemCollection;
import org.shsts.tinactory.content.logistics.ItemHelper;
import org.shsts.tinactory.content.logistics.ItemTypeWrapper;
import org.shsts.tinactory.network.Component;
import org.shsts.tinactory.network.ComponentType;
import org.shsts.tinactory.network.CompositeNetwork;
import org.shsts.tinactory.network.Network;
import org.shsts.tinactory.network.Scheduling;
import org.slf4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LogisticComponent extends Component {
    private static final Logger LOGGER = LogUtils.getLogger();

    public enum RequestType {
        PUSH, PULL
    }

    private record Request(RequestType type, IItemCollection port, ItemStack item) {}

    private final Multimap<ItemTypeWrapper, Request> activeRequests = ArrayListMultimap.create();
    private final Set<IItemCollection> passiveStorages = new HashSet<>();

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

    public LogisticComponent(ComponentType<LogisticComponent> type, CompositeNetwork network, WorkerProperty workerProperty) {
        super(type, network);
        this.workerProperty = workerProperty;
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
        return req.type == RequestType.PULL ?
                this.transmitItem(otherPort, req.port, item, limit) :
                this.transmitItem(req.port, otherPort, item, limit);
    }

    private boolean handleActiveRequest(ItemTypeWrapper itemType, Request req) {
        var remaining = req.item;
        for (var otherReq : this.activeRequests.get(itemType)) {
            if (remaining.isEmpty()) {
                return true;
            }
            if (otherReq.type == req.type) {
                continue;
            }
            var limit = Math.min(remaining.getCount(), otherReq.item.getCount());
            var originalCount = remaining.getCount();
            remaining = this.transmitItem(req, otherReq.port, remaining, limit);
            otherReq.item.shrink(originalCount - remaining.getCount());
        }
        for (var storage : this.passiveStorages) {
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

    public void addPassiveStorage(IItemCollection port) {
        this.passiveStorages.add(port);
    }

    public void deletePassiveStorage(IItemCollection port) {
        this.passiveStorages.remove(port);
    }

    public void addActiveRequest(RequestType type, IItemCollection port, ItemStack item) {
        item = item.copy();
        var req = new Request(type, port, item);
        this.activeRequests.put(new ItemTypeWrapper(item), req);
    }

    public void addActiveRequest(RequestType type, IItemCollection port, ItemStack item, int count) {
        item = ItemHandlerHelper.copyStackWithSize(item, count);
        var req = new Request(type, port, item);
        this.activeRequests.put(new ItemTypeWrapper(item), req);
    }

    @Override
    public void buildSchedulings(BiConsumer<Supplier<Scheduling>, Ticker> cons) {
        cons.accept(AllNetworks.LOGISTICS, this::onTick);
    }
}
