package org.shsts.tinactory.content.logistics;

import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.IStackAdapter;
import org.shsts.tinactory.api.logistics.IStackKey;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.machine.IMachineConfig;
import org.shsts.tinactory.api.network.INetwork;
import org.shsts.tinactory.core.common.SortedMultiList;
import org.shsts.tinactory.core.logistics.MapStorage;
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
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

import static org.shsts.tinactory.AllCapabilities.ELECTRIC_MACHINE;
import static org.shsts.tinactory.AllCapabilities.MACHINE;
import static org.shsts.tinactory.AllEvents.CONNECT;
import static org.shsts.tinactory.AllEvents.SERVER_LOAD;
import static org.shsts.tinactory.AllEvents.SET_MACHINE_CONFIG;
import static org.shsts.tinactory.AllNetworks.LOGISTIC_COMPONENT;
import static org.shsts.tinactory.AllNetworks.SIGNAL_COMPONENT;
import static org.shsts.tinactory.integration.network.MachineBlock.getBlockVoltage;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ElectricStorage<T> extends CapabilityProvider implements IEventSubscriber,
    INBTSerializable<CompoundTag> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int STORAGE_VERSION = 2;

    public static final String PRIORITY_KEY = "priority";
    public static final int PRIORITY_DEFAULT = 2;
    public static final String VOID_KEY = "void";
    public static final boolean VOID_DEFAULT = false;
    public static final String FILTER_KEY = "filter";
    public static final String AMOUNT_SIGNAL = "amount";

    protected final BlockEntity blockEntity;
    private final IElectricMachine electric;
    private final PortType type;
    private final IStackAdapter<T> adapter;
    private final int storageSlots;
    private final int stackLimit;
    private final int filterSlots;
    private final Storage storage;
    private final SortedMultiList<IStackKey> slotMap = new SortedMultiList<>(StackHelper.KEY_DISPLAY_ORDER);
    private final List<IStackKey> legacyFilters = new ArrayList<>();
    private final List<Predicate<T>> filters = new ArrayList<>();

    private IMachine machine;
    private IMachineConfig machineConfig;
    private int amountSignal = 0;

    private class Storage extends MapStorage<T> {
        public Storage() {
            super(ElectricStorage.this.adapter);
        }

        @Override
        public PortType type() {
            return type;
        }

        @Override
        protected boolean acceptInput(IStackKey key, int existingAmount) {
            // normally slotMap.size() <= storageSlots is always true, but we do this defensively
            var usedSlots = slotMap.size();
            return existingAmount % stackLimit == 0 ? usedSlots < storageSlots : usedSlots <= storageSlots;
        }

        @Override
        public boolean acceptInput(T stack) {
            return stackValid(stack) && super.acceptInput(stack);
        }

        @Override
        protected int insertLimit(IStackKey key, int existingAmount) {
            // empty slot amounts + existing slot remaining amount
            return Math.max(0, storageSlots - slotMap.size()) * stackLimit -
                Math.ceilMod(existingAmount, stackLimit);
        }

        private int slotsUsed(int amount) {
            return Math.ceilDiv(amount, stackLimit);
        }

        @Override
        protected void doInsert(IStackKey key, int amount, int existingAmount) {
            var newSlots = Math.max(0, slotsUsed(existingAmount + amount) - slotsUsed(existingAmount));
            slotMap.insert(key, newSlots);
        }

        @Override
        protected void doExtract(IStackKey key, int amount, int existingAmount) {
            var freeSlots = Math.max(0, slotsUsed(existingAmount) - slotsUsed(existingAmount - amount));
            slotMap.remove(key, freeSlots);
        }

        @Override
        protected CompoundTag serializeStack(HolderLookup.Provider provider, T stack) {
            return ElectricStorage.this.serializeStack(provider, stack);
        }

        @Override
        protected T deserializeStack(HolderLookup.Provider provider, CompoundTag tag) {
            return ElectricStorage.this.deserializeStack(provider, tag);
        }

        @Override
        protected void invokeUpdate() {
            blockEntity.setChanged();
            updateSignal();
            super.invokeUpdate();
        }
    }

    public record Properties(int storageSlots, int stackLimit, int filterSlots, double power) {}

    protected ElectricStorage(BlockEntity blockEntity, PortType type, IStackAdapter<T> adapter,
        Properties properties) {
        this.blockEntity = blockEntity;
        this.type = type;
        this.adapter = adapter;
        this.storageSlots = properties.storageSlots;
        this.stackLimit = properties.stackLimit;
        this.filterSlots = properties.filterSlots;
        this.storage = new Storage();

        var voltage = getBlockVoltage(blockEntity);
        this.electric = new SimpleElectricConsumer(voltage.value, properties.power);
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

    public IPort<T> port() {
        return storage;
    }

    public int storageSlots() {
        return storageSlots;
    }

    public int stackLimit() {
        return stackLimit;
    }

    public int filterSlots() {
        return filterSlots;
    }

    public int amountSignal() {
        return amountSignal;
    }

    private boolean stackValid(T stack) {
        return filters.isEmpty() || filters.stream().anyMatch($ -> $.test(stack));
    }

    protected abstract Predicate<T> asPredicate(HolderLookup.Provider provider, FilterEntry entry);

    private void updateFilters() {
        filters.clear();
        var list = machineConfig().getList(FILTER_KEY);
        var provider = machine().registryAccess();
        if (list.isPresent()) {
            for (var tag : list.get()) {
                var entry = CodecHelper.parseTag(provider, FilterEntry.CODEC, tag);
                filters.add(asPredicate(provider, entry));
            }
        }
    }

    private long getAmountInVirtualSlot(@Nullable SortedMultiList.IndexedValue<IStackKey> value) {
        if (value == null) {
            return 0;
        } else if (value.index() == value.count() - 1) {
            var key = value.value();
            return stackLimit + Math.ceilMod(storage.getStorageAmount(key), stackLimit);
        } else {
            return stackLimit;
        }
    }

    protected T getStackInVirtualSlot(int slot) {
        if (!storage.acceptOutput()) {
            return adapter.empty();
        }
        var value = slotMap.get(slot);
        if (value == null) {
            return adapter.empty();
        }
        return adapter.stackOf(value.value(), getAmountInVirtualSlot(value));
    }

    private boolean validForVirtualSlot(@Nullable SortedMultiList.IndexedValue<IStackKey> value, T stack) {
        return value == null ? stackValid(stack) : value.value().equals(adapter.keyOf(stack));
    }

    protected boolean validForVirtualSlot(int slot, T stack) {
        if (slot < 0 || slot >= storageSlots || adapter.isEmpty(stack)) {
            return false;
        }
        var value = slotMap.get(slot);
        return validForVirtualSlot(value, stack);
    }

    protected T insertIntoVirtualSlot(int slot, T stack, boolean simulate) {
        if (adapter.isEmpty(stack)) {
            return stack;
        }
        var value = slotMap.get(slot);
        if (!validForVirtualSlot(value, stack)) {
            return stack;
        }
        var existing = getAmountInVirtualSlot(value);
        var amount = adapter.amount(stack);
        var insert = (int) Math.min(amount, stackLimit - existing);
        if (insert <= 0) {
            return stack;
        }

        var remaining = storage.insert(adapter.withAmount(stack, insert), simulate);
        return adapter.withAmount(stack, amount - insert + adapter.amount(remaining));
    }

    protected T extractFromVirtualSlot(int slot, int limit, boolean simulate,
        ToIntFunction<T> maxStackSize) {
        if (slot < 0 || slot >= storageSlots || limit <= 0) {
            return adapter.empty();
        }
        var value = slotMap.get(slot);
        if (value == null) {
            return adapter.empty();
        }
        var key = value.value();
        var stackLimit = maxStackSize.applyAsInt(adapter.stackOf(key));
        var amount = Math.min(Math.min(limit, stackLimit), getAmountInVirtualSlot(value));
        var extract = adapter.stackOf(key, amount);

        return storage.extract(extract, simulate);
    }

    protected T extractFromVirtualSlot(int slot, T stack, boolean simulate,
        ToIntFunction<T> maxStackSize) {
        if (slot < 0 || slot >= storageSlots || adapter.isEmpty(stack)) {
            return adapter.empty();
        }
        var value = slotMap.get(slot);
        if (value == null) {
            return adapter.empty();
        }
        var key = value.value();
        if (!key.equals(adapter.keyOf(stack))) {
            return adapter.empty();
        }
        var stackLimit = maxStackSize.applyAsInt(adapter.stackOf(key));
        var amount = Math.min(Math.min(adapter.amount(stack), stackLimit), getAmountInVirtualSlot(value));
        var extract = adapter.stackOf(key, amount);

        return storage.extract(extract, simulate);
    }

    private void updateSignal() {
        var total = storage.getAllStorages().stream().mapToLong(adapter::amount).sum();
        var capacity = (long) storageSlots * stackLimit;
        amountSignal = capacity == 0 ? 0 : MathUtil.toSignal((double) total / capacity);
    }

    protected void registerPort(INetwork network) {
        var logistics = network.getComponent(LOGISTIC_COMPONENT.get());
        logistics.unregisterPort(machine(), 0);
        logistics.registerStoragePort(machine(), 0, storage,
            machineConfig().getInt(PRIORITY_KEY, PRIORITY_DEFAULT));
    }

    private void onMachineConfig() {
        machine().network().ifPresent(this::registerPort);
        updateFilters();
    }

    private void onConnect(INetwork network) {
        onMachineConfig();
        var signal = network.getComponent(SIGNAL_COMPONENT.get());
        signal.registerRead(machine(), AMOUNT_SIGNAL, () -> amountSignal);
        updateSignal();
    }

    private void onLoad(Level world) {
        if (!legacyFilters.isEmpty()) {
            var list = machineConfig().getCopiedList(FILTER_KEY);
            for (var key : legacyFilters) {
                if (list.size() < filterSlots) {
                    var filter = new FilterEntry(key, null);
                    list.add(CodecHelper.encodeTag(world.registryAccess(),
                        FilterEntry.CODEC, filter));
                }
                if (list.size() >= filterSlots) {
                    break;
                }
            }
            legacyFilters.clear();
        }
    }

    @Override
    public void subscribeEvents(IEventManager eventManager) {
        eventManager.subscribe(SERVER_LOAD.get(), this::onLoad);
        eventManager.subscribe(CONNECT.get(), this::onConnect);
        eventManager.subscribe(SET_MACHINE_CONFIG.get(), this::onMachineConfig);
    }

    @Override
    public void attachCapability(ICapabilityBuilder builder) {
        builder.attach(ELECTRIC_MACHINE, electric);
    }

    protected abstract CompoundTag serializeStack(HolderLookup.Provider provider, T stack);

    protected abstract T deserializeStack(HolderLookup.Provider provider, CompoundTag tag);

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        var tag = new CompoundTag();
        tag.putInt("version", STORAGE_VERSION);
        tag.put("entries", storage.serializeToList(provider));
        return tag;
    }

    private void deserializeLegacy(HolderLookup.Provider provider, ListTag tag) {
        for (var value : tag) {
            var entryTag = (CompoundTag) value;
            var key = CodecHelper.parseTag(provider, StackHelper.KEY_CODEC,
                Objects.requireNonNull(entryTag.get("key")));
            var amount = entryTag.getLong("amount");
            if (entryTag.getBoolean("isFilter")) {
                legacyFilters.add(key);
            }
            if (amount > 0) {
                storage.deserializeEntry(adapter.stackOf(key, amount));
            }
        }
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        storage.clear();
        slotMap.clear();
        legacyFilters.clear();

        var version = tag.getInt("version");
        var list = tag.getList("entries", Tag.TAG_COMPOUND);

        if (version == STORAGE_VERSION) {
            storage.deserializeFromList(provider, list);
        } else if (version == 1) {
            deserializeLegacy(provider, list);
        } else {
            LOGGER.warn("Ignoring unrecognized electric storage");
        }

        updateSignal();
    }
}
