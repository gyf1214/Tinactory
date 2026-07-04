package org.shsts.tinactory.content.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.shsts.tinactory.api.logistics.IFluidPort;
import org.shsts.tinactory.api.logistics.IItemPort;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.IStackAdapter;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.core.logistics.DigitalCellData;
import org.shsts.tinactory.core.logistics.IBytesProvider;
import org.shsts.tinactory.integration.logistics.StackHelper;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

import java.util.ArrayList;
import java.util.Collection;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class DigitalCellPort<T> implements IPort<T>, IBytesProvider {
    private final ItemStack stack;
    private final IEntry<DataComponentType<DigitalCellData>> componentType;
    private final IStackAdapter<T> adapter;
    private final PortType type;
    private final long bytesLimit;
    private final int bytesPerType;
    private final int bytesPerUnit;

    private DigitalCellPort(
        ItemStack stack,
        IEntry<DataComponentType<DigitalCellData>> componentType,
        IStackAdapter<T> adapter,
        PortType type,
        long bytesLimit,
        int bytesPerType,
        int bytesPerUnit) {
        this.stack = stack;
        this.componentType = componentType;
        this.adapter = adapter;
        this.type = type;
        this.bytesLimit = bytesLimit;
        this.bytesPerType = bytesPerType;
        this.bytesPerUnit = bytesPerUnit;
    }

    @Override
    public long bytesCapacity() {
        return bytesLimit;
    }

    @Override
    public long bytesUsed() {
        var data = data();
        return saturatedAdd(
            saturatedMultiply(data.keyCount(), bytesPerType),
            saturatedMultiply(data.totalAmount(), bytesPerUnit));
    }

    @Override
    public T insert(T value, boolean simulate) {
        if (adapter.isEmpty(value) || !acceptInput(value)) {
            return value;
        }
        var data = data();
        var key = adapter.keyOf(value);
        var current = data.entries().getOrDefault(key, 0L);
        var amount = adapter.amount(value);
        var limit = insertLimit(current);
        var inserted = Math.min(amount, limit);
        if (inserted <= 0L) {
            return value;
        }
        if (!simulate) {
            write(data.withEntry(key, current + inserted));
        }
        return adapter.withAmount(value, Math.toIntExact(amount - inserted));
    }

    @Override
    public T extract(T value, boolean simulate) {
        if (adapter.isEmpty(value) || !acceptOutput()) {
            return adapter.empty();
        }
        var key = adapter.keyOf(value);
        var data = data();
        var current = data.entries().getOrDefault(key, 0L);
        if (current <= 0L) {
            return adapter.empty();
        }
        var extracted = Math.min(current, adapter.amount(value));
        if (!simulate) {
            write(data.withEntry(key, current - extracted));
        }
        return adapter.withAmount(value, Math.toIntExact(extracted));
    }

    @Override
    public T extract(int limit, boolean simulate) {
        if (limit <= 0 || !acceptOutput()) {
            return adapter.empty();
        }
        var data = data();
        if (data.entries().isEmpty()) {
            return adapter.empty();
        }
        var entry = data.entries().entrySet().iterator().next();
        var extracted = Math.min(entry.getValue(), limit);
        if (!simulate) {
            write(data.withEntry(entry.getKey(), entry.getValue() - extracted));
        }
        return adapter.stackOf(entry.getKey(), extracted);
    }

    @Override
    public int getStorageAmount(T value) {
        if (!acceptOutput()) {
            return 0;
        }
        return data().entries().getOrDefault(adapter.keyOf(value), 0L).intValue();
    }

    @Override
    public Collection<T> getAllStorages() {
        var ret = new ArrayList<T>();
        if (!acceptOutput()) {
            return ret;
        }
        for (var entry : data().entries().entrySet()) {
            ret.add(adapter.stackOf(entry.getKey(), entry.getValue()));
        }
        return ret;
    }

    @Override
    public boolean acceptInput(T value) {
        if (adapter.isEmpty(value)) {
            return true;
        }
        var key = adapter.keyOf(value);
        var current = data().entries().getOrDefault(key, 0L);
        return insertLimit(current) > 0L;
    }

    @Override
    public boolean acceptOutput() {
        return true;
    }

    @Override
    public PortType type() {
        return type;
    }

    private long insertLimit(long current) {
        var remaining = bytesLimit - bytesUsed();
        if (remaining <= 0L) {
            return 0L;
        }
        if (current <= 0L) {
            remaining -= bytesPerType;
        }
        if (remaining < bytesPerUnit) {
            return 0L;
        }
        return remaining / bytesPerUnit;
    }

    private DigitalCellData data() {
        return stack.getOrDefault(componentType.get(), DigitalCellData.EMPTY);
    }

    private void write(DigitalCellData data) {
        if (data.entries().isEmpty()) {
            stack.remove(componentType.get());
        } else {
            stack.set(componentType.get(), data);
        }
    }

    private static long saturatedMultiply(long left, long right) {
        if (left == 0L || right == 0L) {
            return 0L;
        }
        if (left > Long.MAX_VALUE / right) {
            return Long.MAX_VALUE;
        }
        return left * right;
    }

    private static long saturatedAdd(long left, long right) {
        if (right > 0L && left > Long.MAX_VALUE - right) {
            return Long.MAX_VALUE;
        }
        return left + right;
    }

    public static final class Item extends DigitalCellPort<ItemStack> implements IItemPort {
        public Item(
            ItemStack stack,
            IEntry<DataComponentType<DigitalCellData>> componentType,
            long bytesLimit,
            int bytesPerType,
            int bytesPerUnit) {
            super(stack, componentType, StackHelper.ITEM_ADAPTER, PortType.ITEM, bytesLimit, bytesPerType,
                bytesPerUnit);
        }
    }

    public static final class Fluid extends DigitalCellPort<FluidStack> implements IFluidPort {
        public Fluid(
            ItemStack stack,
            IEntry<DataComponentType<DigitalCellData>> componentType,
            long bytesLimit,
            int bytesPerType,
            int bytesPerUnit) {
            super(stack, componentType, StackHelper.FLUID_ADAPTER, PortType.FLUID, bytesLimit, bytesPerType,
                bytesPerUnit);
        }
    }
}
