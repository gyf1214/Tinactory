package org.shsts.tinactory.unit.fixture;

import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.recipe.IProcessingObject;

import java.util.Objects;
import java.util.function.Predicate;

public abstract class TestProcessingObject implements IProcessingObject {
    private final String key;
    private final int amount;

    protected TestProcessingObject(String key, int amount) {
        this.key = key;
        this.amount = amount;
    }

    public String key() {
        return key;
    }

    public int amount() {
        return amount;
    }

    @Override
    public PortType type() {
        return PortType.ITEM;
    }

    @Override
    public Predicate<?> filter() {
        return (Predicate<TestStack>) stack -> key.equals(stack.id());
    }

    @Override
    public boolean equals(Object other) {
        return this == other || (other != null && getClass() == other.getClass() &&
            Objects.equals(key, ((TestProcessingObject) other).key) &&
            amount == ((TestProcessingObject) other).amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), key, amount);
    }
}
