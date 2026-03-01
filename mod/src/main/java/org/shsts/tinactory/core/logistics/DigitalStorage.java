package org.shsts.tinactory.core.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.IPortFilter;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class DigitalStorage<T> extends PortNotifier implements IPort<T>, IPortFilter<T> {
    private final IDigitalProvider provider;
    private final IStackAdapter<T> stackAdapter;
    private final int bytesPerType;
    private final int bytesPerUnit;
    public int maxAmount = Integer.MAX_VALUE;
    private final Map<IIngredientKey, T> contents = new HashMap<>();
    private Predicate<T> filter = $ -> true;

    public DigitalStorage(IDigitalProvider provider, IStackAdapter<T> stackAdapter,
        int bytesPerType, int bytesPerUnit) {
        this.provider = provider;
        this.stackAdapter = stackAdapter;
        this.bytesPerType = bytesPerType;
        this.bytesPerUnit = bytesPerUnit;
    }

    public boolean acceptInput(T stack) {
        if (stackAdapter.isEmpty(stack)) {
            return true;
        }
        if (!filter.test(stack)) {
            return false;
        }
        var key = stackAdapter.keyOf(stack);
        if (contents.containsKey(key)) {
            return stackAdapter.amount(contents.get(key)) < maxAmount && provider.canConsume(bytesPerUnit);
        }
        return provider.canConsume(bytesPerUnit + bytesPerType);
    }

    public boolean acceptOutput() {
        return true;
    }

    public T insert(T stack, boolean simulate) {
        if (stackAdapter.isEmpty(stack) || !acceptInput(stack)) {
            return stack;
        }
        var key = stackAdapter.keyOf(stack);
        if (!contents.containsKey(key)) {
            var limit = Math.min(provider.consumeLimit(bytesPerType, bytesPerUnit), maxAmount);
            var inserted = Math.min(stackAdapter.amount(stack), limit);
            var remaining = stackAdapter.withAmount(stack, stackAdapter.amount(stack) - inserted);
            if (!simulate) {
                var insertedStack = stackAdapter.withAmount(stack, inserted);
                contents.put(stackAdapter.keyOf(insertedStack), insertedStack);
                provider.consume(bytesPerType + inserted * bytesPerUnit);
                invokeUpdate();
            }
            return remaining;
        }
        var existing = contents.get(key);
        var limit = Math.min(provider.consumeLimit(bytesPerUnit),
            maxAmount - stackAdapter.amount(existing));
        var inserted = Math.min(stackAdapter.amount(stack), limit);
        var remaining = stackAdapter.withAmount(stack, stackAdapter.amount(stack) - inserted);
        if (!simulate) {
            var updated = stackAdapter.withAmount(existing, stackAdapter.amount(existing) + inserted);
            contents.put(key, updated);
            provider.consume(inserted * bytesPerUnit);
            invokeUpdate();
        }
        return remaining;
    }

    public T extract(T stack, boolean simulate) {
        if (stackAdapter.isEmpty(stack) || !acceptOutput()) {
            return stackAdapter.empty();
        }
        var key = stackAdapter.keyOf(stack);
        if (!contents.containsKey(key)) {
            return stackAdapter.empty();
        }
        var existing = contents.get(key);
        if (stackAdapter.amount(stack) >= stackAdapter.amount(existing)) {
            if (!simulate) {
                contents.remove(key);
                provider.restore(bytesPerType + bytesPerUnit * stackAdapter.amount(existing));
                invokeUpdate();
            }
            return stackAdapter.copy(existing);
        }
        if (!simulate) {
            var updated = stackAdapter.withAmount(existing,
                stackAdapter.amount(existing) - stackAdapter.amount(stack));
            contents.put(key, updated);
            provider.restore(bytesPerUnit * stackAdapter.amount(stack));
            invokeUpdate();
        }
        return stackAdapter.copy(stack);
    }

    public T extract(int limit, boolean simulate) {
        if (limit <= 0 || !acceptOutput() || contents.isEmpty()) {
            return stackAdapter.empty();
        }
        var entry = contents.entrySet().iterator().next();
        var existing = entry.getValue();
        if (limit >= stackAdapter.amount(existing)) {
            if (!simulate) {
                contents.remove(entry.getKey());
                provider.restore(bytesPerType + bytesPerUnit * stackAdapter.amount(existing));
                invokeUpdate();
            }
            return stackAdapter.copy(existing);
        }
        if (!simulate) {
            var updated = stackAdapter.withAmount(existing, stackAdapter.amount(existing) - limit);
            contents.put(entry.getKey(), updated);
            provider.restore(bytesPerUnit * limit);
            invokeUpdate();
        }
        return stackAdapter.withAmount(existing, limit);
    }

    public int getStorageAmount(T stack) {
        if (!acceptOutput()) {
            return 0;
        }
        var existing = contents.get(stackAdapter.keyOf(stack));
        return existing == null ? 0 : stackAdapter.amount(existing);
    }

    public Collection<T> getAllStorages() {
        return acceptOutput() ? contents.values() : Collections.emptyList();
    }

    @Override
    public void setFilters(List<? extends Predicate<T>> filters) {
        filter = stack -> filters.stream().anyMatch($ -> $.test(stack));
    }

    @Override
    public void resetFilters() {
        filter = $ -> true;
    }

    public void clear() {
        contents.clear();
        provider.reset();
    }
}
