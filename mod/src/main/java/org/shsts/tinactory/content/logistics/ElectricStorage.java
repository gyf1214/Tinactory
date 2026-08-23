package org.shsts.tinactory.content.logistics;

import com.mojang.logging.LogUtils;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.IPortNotifier;
import org.shsts.tinactory.api.logistics.IStackAdapter;
import org.shsts.tinactory.api.logistics.IStackKey;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.machine.IMachineConfig;
import org.shsts.tinactory.api.network.INetwork;
import org.shsts.tinactory.core.logistics.StorageEntry;
import org.shsts.tinactory.core.machine.SimpleElectricConsumer;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinactory.core.util.MathUtil;
import org.shsts.tinactory.integration.common.CapabilityProvider;
import org.shsts.tinactory.integration.logistics.StackHelper;
import org.shsts.tinycorelib.api.blockentity.ICapabilityBuilder;
import org.shsts.tinycorelib.api.blockentity.IEventManager;
import org.shsts.tinycorelib.api.blockentity.IEventSubscriber;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.shsts.tinactory.AllCapabilities.ELECTRIC_MACHINE;
import static org.shsts.tinactory.AllCapabilities.MACHINE;
import static org.shsts.tinactory.AllEvents.CONNECT;
import static org.shsts.tinactory.AllEvents.SET_MACHINE_CONFIG;
import static org.shsts.tinactory.AllNetworks.LOGISTIC_COMPONENT;
import static org.shsts.tinactory.AllNetworks.SIGNAL_COMPONENT;
import static org.shsts.tinactory.integration.network.MachineBlock.getBlockVoltage;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ElectricStorage<T> extends CapabilityProvider implements IEventSubscriber, IPort<T>,
    IPortNotifier {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int STORAGE_VERSION = 1;
    private static final Comparator<StorageEntry> ENTRY_ORDER = Comparator.comparing(StorageEntry::key,
        StackHelper.KEY_DISPLAY_ORDER);

    public static final String UNLOCK_KEY = "unlockChest";
    public static final boolean UNLOCK_DEFAULT = true;
    public static final String PRIORITY_KEY = "priority";
    public static final int PRIORITY_DEFAULT = 2;
    public static final String VOID_KEY = "void";
    public static final boolean VOID_DEFAULT = false;
    public static final String AMOUNT_SIGNAL = "amount";

    protected final BlockEntity blockEntity;
    private final IElectricMachine electric;
    private final IStackAdapter<T> adapter;
    private final int storageSlots;
    private final int stackLimit;
    private final Map<IStackKey, StorageEntry> entries = new HashMap<>();
    private final Set<Runnable> listeners = new HashSet<>();

    protected IMachine machine;
    protected IMachineConfig machineConfig;
    private int amountSignal = 0;

    protected ElectricStorage(BlockEntity blockEntity, IStackAdapter<T> adapter, int storageSlots,
        int stackLimit, IElectricMachine electric) {
        this.blockEntity = blockEntity;
        this.adapter = adapter;
        this.storageSlots = storageSlots;
        this.stackLimit = stackLimit;
        this.electric = electric;
    }

    protected ElectricStorage(BlockEntity blockEntity, IStackAdapter<T> adapter, int storageSlots,
        int stackLimit, double power) {
        this(blockEntity, adapter, storageSlots, stackLimit,
            new SimpleElectricConsumer(getBlockVoltage(blockEntity).value, power));
    }

    protected IMachine machine() {
        if (machine == null) {
            machine = MACHINE.get(blockEntity);
        }
        return machine;
    }

    public IMachineConfig machineConfig() {
        if (machineConfig == null) {
            machineConfig = machine().config();
        }
        return machineConfig;
    }

    public int storageSlots() {
        return storageSlots;
    }

    public int stackLimit() {
        return stackLimit;
    }

    public int amountSignal() {
        return amountSignal;
    }

    public boolean isUnlocked() {
        return machineConfig().getBoolean(UNLOCK_KEY, UNLOCK_DEFAULT);
    }

    public boolean isVoid() {
        return machineConfig().getBoolean(VOID_KEY, VOID_DEFAULT);
    }

    public Collection<IStackKey> filters() {
        return entries.values().stream().filter(StorageEntry::isFilter).map(StorageEntry::key).toList();
    }

    public boolean setFilter(IStackKey key) {
        if (key.type() != type()) {
            return false;
        }
        var existing = entries.get(key);
        if (existing != null) {
            if (existing.amount() > 0 || existing.isFilter()) {
                return false;
            }
            entries.put(key, new StorageEntry(key, 0, true));
            changed();
            return true;
        }
        if (usedSlots() >= storageSlots) {
            return false;
        }
        entries.put(key, new StorageEntry(key, 0, true));
        changed();
        return true;
    }

    public boolean resetFilter(IStackKey key) {
        var existing = entries.get(key);
        if (existing == null || !existing.isFilter() || existing.amount() > 0) {
            return false;
        }
        entries.remove(key);
        changed();
        return true;
    }

    public boolean replaceFilter(IStackKey oldKey, IStackKey newKey) {
        var existing = entries.get(oldKey);
        if (existing == null || !existing.isFilter() || existing.amount() > 0 || newKey.type() != type() ||
            entries.containsKey(newKey)) {
            return false;
        }
        entries.remove(oldKey);
        entries.put(newKey, new StorageEntry(newKey, 0, true));
        changed();
        return true;
    }

    protected List<StorageEntry> sortedEntries() {
        return entries.values().stream().sorted(ENTRY_ORDER).toList();
    }

    protected List<VirtualEntry> virtualEntries() {
        var ret = new ArrayList<VirtualEntry>(storageSlots);
        for (var entry : sortedEntries()) {
            var remaining = entry.amount();
            if (remaining == 0 && entry.isFilter()) {
                ret.add(new VirtualEntry(entry.key(), 0, true));
            }
            var filter = entry.isFilter();
            while (remaining > 0) {
                var amount = (int) Math.min(remaining, stackLimit);
                ret.add(new VirtualEntry(entry.key(), amount, filter));
                remaining -= amount;
                filter = false;
            }
        }
        while (ret.size() < storageSlots) {
            ret.add(VirtualEntry.EMPTY);
        }
        return ret;
    }

    protected T stack(VirtualEntry entry) {
        return entry.key() == null ? adapter.empty() : adapter.stackOf(entry.key(), entry.amount());
    }

    protected boolean validForVirtualSlot(int slot, T stack) {
        if (slot < 0 || slot >= storageSlots || adapter.isEmpty(stack)) {
            return false;
        }
        var virtual = virtualEntries().get(slot);
        var key = adapter.keyOf(stack);
        return virtual.key() == null ? isUnlocked() || entries.containsKey(key) : virtual.key().equals(key);
    }

    protected int insertIntoVirtualSlot(int slot, T stack, boolean simulate) {
        if (!validForVirtualSlot(slot, stack)) {
            return 0;
        }
        var virtual = virtualEntries().get(slot);
        var amount = adapter.amount(stack);
        var stored = Math.min(amount, stackLimit - virtual.amount());
        if (stored > 0 && !simulate) {
            addAmount(adapter.keyOf(stack), stored);
        }
        return isVoid() ? amount : stored;
    }

    protected T extractFromVirtualSlot(int slot, int limit, boolean simulate) {
        if (slot < 0 || slot >= storageSlots || limit <= 0) {
            return adapter.empty();
        }
        var virtual = virtualEntries().get(slot);
        if (virtual.key() == null || virtual.amount() == 0) {
            return adapter.empty();
        }
        var amount = Math.min(limit, virtual.amount());
        if (!simulate) {
            removeAmount(virtual.key(), amount);
        }
        return adapter.stackOf(virtual.key(), amount);
    }

    private int usedSlots() {
        var ret = 0;
        for (var entry : entries.values()) {
            ret += slotsFor(entry.amount(), entry.isFilter());
        }
        return ret;
    }

    private int slotsFor(long amount, boolean isFilter) {
        var amountSlots = (int) ((amount + stackLimit - 1) / stackLimit);
        return isFilter ? Math.max(1, amountSlots) : amountSlots;
    }

    private long availableFor(IStackKey key) {
        var existing = entries.get(key);
        var currentSlots = existing == null ? 0 : slotsFor(existing.amount(), existing.isFilter());
        return (long) (storageSlots - usedSlots() + currentSlots) * stackLimit -
            (existing == null ? 0 : existing.amount());
    }

    private boolean eligible(IStackKey key) {
        return entries.containsKey(key) || isUnlocked();
    }

    private void addAmount(IStackKey key, long amount) {
        var existing = entries.get(key);
        var oldAmount = existing == null ? 0 : existing.amount();
        var isFilter = existing != null && existing.isFilter();
        entries.put(key, new StorageEntry(key, oldAmount + amount, isFilter));
        changed();
    }

    private void removeAmount(IStackKey key, long amount) {
        var existing = entries.get(key);
        if (existing == null) {
            return;
        }
        var remaining = Math.max(0, existing.amount() - amount);
        if (remaining == 0 && !existing.isFilter()) {
            entries.remove(key);
        } else {
            entries.put(key, new StorageEntry(key, remaining, existing.isFilter()));
        }
        changed();
    }

    private void changed() {
        blockEntity.setChanged();
        amountSignal = updateSignal();
        List.copyOf(listeners).forEach(Runnable::run);
    }

    private int updateSignal() {
        var total = entries.values().stream().mapToLong(StorageEntry::amount).sum();
        var capacity = (long) storageSlots * stackLimit;
        return capacity == 0 ? 0 : MathUtil.toSignal((double) total / capacity);
    }

    @Override
    public boolean acceptInput(T stack) {
        return !adapter.isEmpty(stack) && eligible(adapter.keyOf(stack)) &&
            (isVoid() || availableFor(adapter.keyOf(stack)) > 0);
    }

    @Override
    public T insert(T stack, boolean simulate) {
        if (adapter.isEmpty(stack)) {
            return stack;
        }
        var key = adapter.keyOf(stack);
        if (!eligible(key)) {
            return stack;
        }
        var input = adapter.amount(stack);
        var stored = (int) Math.min(input, Math.max(0, availableFor(key)));
        if (stored > 0 && !simulate) {
            addAmount(key, stored);
        }
        return isVoid() ? adapter.empty() : adapter.withAmount(stack, input - stored);
    }

    @Override
    public T extract(T stack, boolean simulate) {
        if (adapter.isEmpty(stack)) {
            return adapter.empty();
        }
        var key = adapter.keyOf(stack);
        var existing = entries.get(key);
        if (existing == null) {
            return adapter.empty();
        }
        var amount = (int) Math.min(adapter.amount(stack), existing.amount());
        if (!simulate && amount > 0) {
            removeAmount(key, amount);
        }
        return adapter.stackOf(key, amount);
    }

    @Override
    public T extract(int limit, boolean simulate) {
        if (limit <= 0) {
            return adapter.empty();
        }
        for (var entry : sortedEntries()) {
            if (entry.amount() > 0) {
                return extract(adapter.stackOf(entry.key(), Math.min(limit, entry.amount())), simulate);
            }
        }
        return adapter.empty();
    }

    @Override
    public long getStorageAmount(T stack) {
        if (adapter.isEmpty(stack)) {
            return 0;
        }
        var entry = entries.get(adapter.keyOf(stack));
        return entry == null ? 0 : entry.amount();
    }

    @Override
    public Collection<T> getAllStorages() {
        return sortedEntries().stream().filter(entry -> entry.amount() > 0)
            .map(entry -> adapter.stackOf(entry.key(), entry.amount())).toList();
    }

    @Override
    public boolean acceptOutput() {
        return true;
    }

    @Override
    public void onUpdate(Runnable listener) {
        listeners.add(listener);
    }

    @Override
    public void unregisterListener(Runnable listener) {
        listeners.remove(listener);
    }

    protected CompoundTag serializeEntries(HolderLookup.Provider provider) {
        var tag = new CompoundTag();
        tag.putInt("version", STORAGE_VERSION);
        var list = new ListTag();
        for (var entry : sortedEntries()) {
            if (entry.amount() == 0 && !entry.isFilter()) {
                continue;
            }
            var entryTag = new CompoundTag();
            entryTag.put("key", CodecHelper.encodeTag(provider, StackHelper.KEY_CODEC, entry.key()));
            entryTag.putLong("amount", entry.amount());
            entryTag.putBoolean("isFilter", entry.isFilter());
            list.add(entryTag);
        }
        tag.put("entries", list);
        return tag;
    }

    protected boolean deserializeEntries(HolderLookup.Provider provider, CompoundTag tag) {
        if (!tag.contains("version", Tag.TAG_INT)) {
            return false;
        }
        entries.clear();
        var list = tag.getList("entries", Tag.TAG_COMPOUND);
        for (var value : list) {
            var entryTag = (CompoundTag) value;
            try {
                var key = CodecHelper.parseTag(provider, StackHelper.KEY_CODEC, entryTag.get("key"));
                var entry = new StorageEntry(key, Math.max(0, entryTag.getLong("amount")),
                    entryTag.getBoolean("isFilter"));
                if (key.type() != type() || !loadEntry(entry)) {
                    LOGGER.warn("Discarding overflowing or incompatible electric storage entry {}", entry);
                }
            } catch (RuntimeException e) {
                LOGGER.warn("Discarding invalid electric storage entry {}", entryTag, e);
            }
        }
        amountSignal = updateSignal();
        return true;
    }

    protected boolean loadEntry(StorageEntry entry) {
        var existing = entries.get(entry.key());
        var merged = existing == null ? entry : new StorageEntry(entry.key(),
            existing.amount() + entry.amount(), existing.isFilter() || entry.isFilter());
        var previousSlots = existing == null ? 0 : slotsFor(existing.amount(), existing.isFilter());
        var nextSlots = slotsFor(merged.amount(), merged.isFilter());
        if (usedSlots() - previousSlots + nextSlots > storageSlots) {
            return false;
        }
        entries.put(merged.key(), merged);
        return true;
    }

    protected void registerPort(INetwork network) {
        var logistics = network.getComponent(LOGISTIC_COMPONENT.get());
        logistics.unregisterPort(machine(), 0);
        logistics.registerStoragePort(machine(), 0, this,
            machineConfig().getInt(PRIORITY_KEY, PRIORITY_DEFAULT));
    }

    private void onMachineConfig() {
        machine().network().ifPresent(this::registerPort);
    }

    private void onConnect(INetwork network) {
        onMachineConfig();
        var signal = network.getComponent(SIGNAL_COMPONENT.get());
        signal.registerRead(machine(), AMOUNT_SIGNAL, () -> amountSignal);
        amountSignal = updateSignal();
    }

    @Override
    public void subscribeEvents(IEventManager eventManager) {
        eventManager.subscribe(CONNECT.get(), this::onConnect);
        eventManager.subscribe(SET_MACHINE_CONFIG.get(), this::onMachineConfig);
    }

    @Override
    public void attachCapability(ICapabilityBuilder builder) {
        builder.attach(ELECTRIC_MACHINE, electric);
    }

    protected record VirtualEntry(IStackKey key, int amount, boolean isFilter) {
        private static final VirtualEntry EMPTY = new VirtualEntry(null, 0, false);
    }
}
