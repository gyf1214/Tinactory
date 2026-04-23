package org.shsts.tinactory.unit.fixture;

import org.shsts.tinactory.api.logistics.ILimitedPort;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.IPortFilter;
import org.shsts.tinactory.api.logistics.PortType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public final class TestPort implements IPort<TestStack>, IPortFilter<TestStack>, ILimitedPort {
    private final PortType type;
    private final String id;
    private final String nbt;
    private final int capacity;
    private final int portLimit;
    private int storedAmount;
    private Predicate<TestStack> filter = $ -> true;

    public TestPort(String id, int capacity, int storedAmount) {
        this(id, capacity, storedAmount, Integer.MAX_VALUE);
    }

    public TestPort(String id, int capacity, int storedAmount, int portLimit) {
        this(PortType.ITEM, id, "", storedAmount, capacity, portLimit);
    }

    public TestPort(PortType type, TestStack stored, int capacity) {
        this(type, stored.id(), stored.nbt(), stored.amount(), capacity, Integer.MAX_VALUE);
    }

    public TestPort(PortType type, String id, String nbt, int storedAmount, int capacity) {
        this(type, id, nbt, storedAmount, capacity, Integer.MAX_VALUE);
    }

    private TestPort(PortType type, String id, String nbt, int storedAmount, int capacity, int portLimit) {
        this.type = type;
        this.id = id;
        this.nbt = nbt;
        this.capacity = capacity;
        this.storedAmount = storedAmount;
        this.portLimit = portLimit;
    }

    @Override
    public PortType type() {
        return type;
    }

    @Override
    public boolean acceptInput(TestStack stack) {
        return stack.amount() > 0 &&
            type == stack.type() &&
            Objects.equals(id, stack.id()) &&
            Objects.equals(nbt, stack.nbt()) &&
            storedAmount < capacity &&
            filter.test(stack);
    }

    @Override
    public TestStack insert(TestStack stack, boolean simulate) {
        if (!acceptInput(stack)) {
            return stack;
        }
        var inserted = Math.min(stack.amount(), capacity - storedAmount);
        if (!simulate) {
            storedAmount += inserted;
        }
        return TestStack.ADAPTER.withAmount(stack, stack.amount() - inserted);
    }

    @Override
    public TestStack extract(TestStack stack, boolean simulate) {
        if (type != stack.type() || !Objects.equals(id, stack.id()) ||
            !Objects.equals(nbt, stack.nbt()) || stack.amount() <= 0 || storedAmount <= 0) {
            return TestStack.ADAPTER.empty();
        }
        var moved = Math.min(stack.amount(), storedAmount);
        if (!simulate) {
            storedAmount -= moved;
        }
        return TestStack.ADAPTER.withAmount(stack, moved);
    }

    @Override
    public TestStack extract(int limit, boolean simulate) {
        if (limit <= 0 || storedAmount <= 0) {
            return TestStack.ADAPTER.empty();
        }
        var moved = Math.min(limit, storedAmount);
        if (!simulate) {
            storedAmount -= moved;
        }
        return new TestStack(type, id, nbt, moved);
    }

    @Override
    public int getStorageAmount(TestStack stack) {
        return type == stack.type() && Objects.equals(id, stack.id()) && Objects.equals(nbt, stack.nbt()) ?
            storedAmount : 0;
    }

    @Override
    public Collection<TestStack> getAllStorages() {
        if (storedAmount <= 0) {
            return List.of();
        }
        var ret = new ArrayList<TestStack>();
        ret.add(new TestStack(type, id, nbt, storedAmount));
        return ret;
    }

    @Override
    public boolean acceptOutput() {
        return storedAmount > 0;
    }

    @Override
    public int getPortLimit() {
        return portLimit;
    }

    @Override
    public void setFilters(List<? extends Predicate<TestStack>> filters) {
        filter = stack -> filters.stream().anyMatch($ -> $.test(stack));
    }

    @Override
    public void resetFilters() {
        filter = $ -> true;
    }

    public int stored() {
        return storedAmount;
    }

    public TestStack storedStack() {
        return new TestStack(type, id, nbt, storedAmount);
    }
}
