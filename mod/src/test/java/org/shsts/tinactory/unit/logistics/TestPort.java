package org.shsts.tinactory.unit.logistics;

import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.IPortFilter;
import org.shsts.tinactory.api.logistics.PortType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

final class TestPort implements IPort<TestStack>, IPortFilter<TestStack> {
    private final String id;
    private final int capacity;
    int stored;
    private Predicate<TestStack> filter = $ -> true;

    TestPort(String id, int capacity, int stored) {
        this.id = id;
        this.capacity = capacity;
        this.stored = stored;
    }

    @Override
    public PortType type() {
        return PortType.ITEM;
    }

    @Override
    public boolean acceptInput(TestStack stack) {
        return stack.amount() > 0 && Objects.equals(id, stack.id()) && stored < capacity && filter.test(stack);
    }

    @Override
    public boolean acceptOutput() {
        return stored > 0;
    }

    @Override
    public TestStack insert(TestStack stack, boolean simulate) {
        if (!acceptInput(stack)) {
            return stack;
        }
        var inserted = Math.min(stack.amount(), capacity - stored);
        if (!simulate) {
            stored += inserted;
        }
        return new TestStack(stack.id(), stack.amount() - inserted);
    }

    @Override
    public TestStack extract(TestStack stack, boolean simulate) {
        if (!Objects.equals(id, stack.id()) || stack.amount() <= 0 || stored <= 0) {
            return new TestStack("", 0);
        }
        var moved = Math.min(stack.amount(), stored);
        if (!simulate) {
            stored -= moved;
        }
        return new TestStack(id, moved);
    }

    @Override
    public TestStack extract(int limit, boolean simulate) {
        if (limit <= 0 || stored <= 0) {
            return new TestStack("", 0);
        }
        var moved = Math.min(limit, stored);
        if (!simulate) {
            stored -= moved;
        }
        return new TestStack(id, moved);
    }

    @Override
    public int getStorageAmount(TestStack stack) {
        return Objects.equals(id, stack.id()) ? stored : 0;
    }

    @Override
    public Collection<TestStack> getAllStorages() {
        if (stored <= 0) {
            return List.of();
        }
        var ret = new ArrayList<TestStack>();
        ret.add(new TestStack(id, stored));
        return ret;
    }

    @Override
    public void setFilters(List<? extends Predicate<TestStack>> filters) {
        filter = stack -> filters.stream().anyMatch($ -> $.test(stack));
    }

    @Override
    public void resetFilters() {
        filter = $ -> true;
    }
}
