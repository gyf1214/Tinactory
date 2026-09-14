package org.shsts.tinactory.core.logistics;

import com.mojang.logging.LogUtils;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.IStackAdapter;
import org.shsts.tinactory.api.logistics.IStackKey;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class MapStorage<T> extends PortNotifier implements IPort<T> {
    private static final Logger LOGGER = LogUtils.getLogger();

    protected final IStackAdapter<T> adapter;
    private final Map<IStackKey, T> contents = new HashMap<>();

    public MapStorage(IStackAdapter<T> adapter) {
        this.adapter = adapter;
    }

    protected abstract boolean acceptInput(IStackKey key, int existingAmount);

    protected abstract int insertLimit(IStackKey key, int existingAmount);

    protected abstract void doInsert(IStackKey key, int amount, int existingAmount);

    protected abstract void doExtract(IStackKey key, int amount, int existingAmount);

    private int existingAmount(IStackKey key) {
        return contents.containsKey(key) ? adapter.amount(contents.get(key)) : 0;
    }

    public boolean acceptInput(T stack) {
        if (adapter.isEmpty(stack)) {
            return true;
        }
        var key = adapter.keyOf(stack);
        return acceptInput(key, existingAmount(key));
    }

    public boolean acceptOutput() {
        return true;
    }

    public T insert(T stack, boolean simulate) {
        if (adapter.isEmpty(stack) || !acceptInput(stack)) {
            return stack;
        }
        var key = adapter.keyOf(stack);
        var existing = existingAmount(key);
        var amount = adapter.amount(stack);
        var limit = insertLimit(key, existing);
        if (limit <= 0) {
            return stack;
        }
        var inserted = Math.min(amount, limit);
        var remaining = adapter.withAmount(stack, amount - inserted);
        if (!simulate) {
            var updated = adapter.withAmount(stack, existing + inserted);
            contents.put(key, updated);
            doInsert(key, inserted, existing);
            invokeUpdate();
        }
        return remaining;
    }

    public T extract(T stack, boolean simulate) {
        if (adapter.isEmpty(stack) || !acceptOutput()) {
            return adapter.empty();
        }
        var key = adapter.keyOf(stack);
        if (!contents.containsKey(key)) {
            return adapter.empty();
        }
        var amount = adapter.amount(stack);
        var existing = contents.get(key);
        var existingAmount = adapter.amount(existing);
        if (amount >= existingAmount) {
            if (!simulate) {
                contents.remove(key);
                doExtract(key, existingAmount, existingAmount);
                invokeUpdate();
            }
            return existing;
        } else {
            if (!simulate) {
                var updated = adapter.withAmount(existing, existingAmount - amount);
                contents.put(key, updated);
                doExtract(key, amount, existingAmount);
                invokeUpdate();
            }
            return adapter.copy(stack);
        }
    }

    public T extract(int limit, boolean simulate) {
        if (limit <= 0 || !acceptOutput() || contents.isEmpty()) {
            return adapter.empty();
        }
        var entry = contents.entrySet().iterator().next();
        var key = entry.getKey();
        var existing = entry.getValue();
        var existingAmount = adapter.amount(existing);
        if (limit >= existingAmount) {
            if (!simulate) {
                contents.remove(key);
                doExtract(key, existingAmount, existingAmount);
                invokeUpdate();
            }
            return existing;
        } else {
            if (!simulate) {
                var updated = adapter.withAmount(existing, existingAmount - limit);
                contents.put(key, updated);
                doExtract(key, limit, existingAmount);
                invokeUpdate();
            }
            return adapter.withAmount(existing, limit);
        }
    }

    public long getStorageAmount(IStackKey key) {
        if (!acceptOutput()) {
            return 0;
        }
        return existingAmount(key);
    }

    public long getStorageAmount(T stack) {
        return getStorageAmount(adapter.keyOf(stack));
    }

    public Collection<T> getAllStorages() {
        return acceptOutput() ? contents.values() : Collections.emptyList();
    }

    public void clear() {
        contents.clear();
    }

    protected abstract CompoundTag serializeStack(HolderLookup.Provider provider, T stack);

    protected abstract T deserializeStack(HolderLookup.Provider provider, CompoundTag tag);

    public ListTag serializeToList(HolderLookup.Provider provider) {
        var ret = new ListTag();
        for (var stack : contents.values()) {
            ret.add(serializeStack(provider, stack));
        }
        return ret;
    }

    public void deserializeEntry(T stack) {
        var key = adapter.keyOf(stack);
        if (contents.containsKey(key)) {
            LOGGER.warn("Ignoring duplicate entry {}", key);
            return;
        }
        if (!acceptInput(key, 0)) {
            LOGGER.warn("Ignoring invalid entry {}", key);
            return;
        }
        var limit = insertLimit(key, 0);
        var amount = adapter.amount(stack);
        if (amount > limit) {
            LOGGER.warn("Entry {} overflow", key);
            contents.put(key, adapter.stackOf(key, limit));
            doInsert(key, limit, 0);
        } else {
            contents.put(key, stack);
            doInsert(key, amount, 0);
        }
    }

    public void deserializeFromList(HolderLookup.Provider provider, ListTag tag) {
        clear();
        for (var entry : tag) {
            var stack = deserializeStack(provider, (CompoundTag) entry);
            deserializeEntry(stack);
        }
    }
}
