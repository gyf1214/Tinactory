package org.shsts.tinactory.core.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.logistics.IPortFilter;
import org.shsts.tinactory.api.logistics.IStackAdapter;
import org.shsts.tinactory.api.logistics.IStackKey;

import java.util.List;
import java.util.function.Predicate;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class DigitalStorage<T> extends MapStorage<T> implements IPortFilter<T> {
    private final IDigitalProvider provider;
    private final int bytesPerType;
    private final int bytesPerUnit;
    private Predicate<T> filter = $ -> true;

    public DigitalStorage(IDigitalProvider provider, IStackAdapter<T> adapter,
        int bytesPerType, int bytesPerUnit) {
        super(adapter);
        this.provider = provider;
        this.bytesPerType = bytesPerType;
        this.bytesPerUnit = bytesPerUnit;
    }

    @Override
    protected boolean acceptInput(IStackKey key, int existingAmount) {
        // we assume filter is amount agnostic
        return filter.test(adapter.stackOf(key)) && provider.canConsume(key,
            existingAmount > 0 ? bytesPerUnit : bytesPerUnit + bytesPerType);
    }

    @Override
    protected int insertLimit(IStackKey key, int existingAmount) {
        return existingAmount > 0 ? provider.consumeLimit(key, bytesPerUnit) :
            provider.consumeLimit(key, bytesPerType, bytesPerUnit);
    }

    @Override
    protected void postInsert(IStackKey key, int amount, int existingAmount) {
        var bytes = existingAmount > 0 ? (long) amount * bytesPerUnit :
            (long) bytesPerType + (long) amount * bytesPerUnit;
        provider.consume(key, bytes);
    }

    @Override
    protected void postExtract(IStackKey key, int amount, int existingAmount) {
        var bytes = amount >= existingAmount ? (long) bytesPerType + (long) existingAmount * bytesPerUnit :
            (long) amount * bytesPerUnit;
        provider.restore(key, bytes);
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
        super.clear();
        provider.reset();
    }
}
