package org.shsts.tinactory.unit.fixture;

import org.shsts.tinactory.api.logistics.ILimitedPort;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.PortType;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public final class TestPort implements IPort<Object>, ILimitedPort {
    private final String key;
    private final int capacity;
    private final int portLimit;
    private int stored;

    public TestPort(String key, int capacity, int stored) {
        this(key, capacity, stored, Integer.MAX_VALUE);
    }

    public TestPort(String key, int capacity, int stored, int portLimit) {
        this.key = key;
        this.capacity = capacity;
        this.stored = stored;
        this.portLimit = portLimit;
    }

    @Override
    public PortType type() {
        return PortType.ITEM;
    }

    @Override
    public boolean acceptInput(Object stack) {
        return false;
    }

    @Override
    public Object insert(Object stack, boolean simulate) {
        return stack;
    }

    @Override
    public Object extract(Object stack, boolean simulate) {
        return stack;
    }

    @Override
    public Object extract(int limit, boolean simulate) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getStorageAmount(Object stack) {
        return stored;
    }

    @Override
    public Collection<Object> getAllStorages() {
        return List.of();
    }

    @Override
    public boolean acceptOutput() {
        return stored > 0;
    }

    @Override
    public int getPortLimit() {
        return portLimit;
    }

    public int stored() {
        return stored;
    }

    public Optional<TestPortSnapshot> consume(String expectedKey, int amount, boolean simulate) {
        if (!key.equals(expectedKey) || amount <= 0 || stored < amount) {
            return Optional.empty();
        }
        if (!simulate) {
            stored -= amount;
        }
        return Optional.of(new TestPortSnapshot(key, amount));
    }

    public Optional<TestPortSnapshot> insert(String expectedKey, int amount, boolean simulate) {
        if (!key.equals(expectedKey) || amount <= 0 || stored + amount > capacity) {
            return Optional.empty();
        }
        if (!simulate) {
            stored += amount;
        }
        return Optional.of(new TestPortSnapshot(key, amount));
    }

    public record TestPortSnapshot(String key, int amount) {
        public TestProcessingIngredient asIngredient() {
            return new TestProcessingIngredient(key, amount);
        }

        public TestProcessingResult asResult() {
            return new TestProcessingResult(key, amount);
        }
    }
}
