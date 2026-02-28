package org.shsts.tinactory.core.logistics;

import com.mojang.logging.LogUtils;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.logistics.IPortFilter;
import org.shsts.tinactory.api.logistics.IPortNotifier;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CombinedPort<T, P extends IPortFilter<T>> implements IPortFilter<T>, IPortNotifier {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final IPortAccess<T, P> portAccess;
    private final IStackAdapter<T> stackAdapter;
    private final Set<Runnable> updateListeners = new HashSet<>();
    protected final List<P> composes = new ArrayList<>();
    protected final Runnable combinedListener = this::invokeUpdate;
    public boolean allowInput = true;
    public boolean allowOutput = true;

    public CombinedPort(IPortAccess<T, P> portAccess, IStackAdapter<T> stackAdapter, Collection<P> composes) {
        this.portAccess = portAccess;
        this.stackAdapter = stackAdapter;
        addComposes(composes);
    }

    public CombinedPort(IPortAccess<T, P> portAccess, IStackAdapter<T> stackAdapter) {
        this(portAccess, stackAdapter, List.of());
    }

    private void addComposes(Collection<P> values) {
        composes.clear();
        composes.addAll(values);
        for (var compose : composes) {
            if (compose instanceof IPortNotifier notifier) {
                notifier.onUpdate(combinedListener);
            }
        }
    }

    public void setComposes(Collection<P> values) {
        for (var compose : composes) {
            if (compose instanceof IPortNotifier notifier) {
                notifier.unregisterListener(combinedListener);
            }
        }
        addComposes(values);
        invokeUpdate();
    }

    public boolean acceptInput(T stack) {
        return allowInput && composes.stream().anyMatch($ -> portAccess.acceptInput($, stack));
    }

    public boolean acceptOutput() {
        return allowOutput && composes.stream().anyMatch(portAccess::acceptOutput);
    }

    public T insert(T stack, boolean simulate) {
        if (!allowInput) {
            return stack;
        }
        var stack1 = stackAdapter.copy(stack);
        for (var compose : composes) {
            if (stackAdapter.isEmpty(stack1)) {
                break;
            }
            stack1 = portAccess.insert(compose, stack1, simulate);
        }
        return stack1;
    }

    public T extract(T stack, boolean simulate) {
        if (!allowOutput) {
            return stackAdapter.empty();
        }
        var stack1 = stackAdapter.copy(stack);
        var ret = stackAdapter.empty();
        for (var compose : composes) {
            if (stackAdapter.isEmpty(stack1)) {
                break;
            }
            var extracted = portAccess.extract(compose, stack1, simulate);
            if (!stackAdapter.isEmpty(extracted)) {
                if (stackAdapter.isEmpty(ret)) {
                    ret = extracted;
                } else if (stackAdapter.canStack(ret, extracted)) {
                    ret = stackAdapter.withAmount(ret,
                        stackAdapter.amount(ret) + stackAdapter.amount(extracted));
                } else {
                    LOGGER.warn("{}: Extracted content {} cannot stack with required content {}",
                        this, extracted, ret);
                    continue;
                }
                stack1 = stackAdapter.withAmount(stack1,
                    stackAdapter.amount(stack1) - stackAdapter.amount(extracted));
            }
        }
        return ret;
    }

    public T extract(int limit, boolean simulate) {
        if (!allowOutput || limit <= 0 || composes.isEmpty()) {
            return stackAdapter.empty();
        }
        return portAccess.extract(composes.get(0), limit, simulate);
    }

    public int getStorageAmount(T stack) {
        return composes.stream().mapToInt($ -> portAccess.getStorageAmount($, stack)).sum();
    }

    public Collection<T> getAllStorages() {
        return composes.stream().flatMap($ -> portAccess.getAllStorages($).stream()).toList();
    }

    @Override
    public void setFilters(List<? extends Predicate<T>> filters) {
        for (var compose : composes) {
            compose.setFilters(filters);
        }
    }

    @Override
    public void resetFilters() {
        for (var compose : composes) {
            compose.resetFilters();
        }
    }

    @Override
    public void onUpdate(Runnable listener) {
        updateListeners.add(listener);
    }

    @Override
    public void unregisterListener(Runnable listener) {
        updateListeners.remove(listener);
    }

    protected void invokeUpdate() {
        for (var cb : updateListeners) {
            cb.run();
        }
    }
}
