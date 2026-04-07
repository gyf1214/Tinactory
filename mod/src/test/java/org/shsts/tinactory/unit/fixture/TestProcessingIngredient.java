package org.shsts.tinactory.unit.fixture;

import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.recipe.IProcessingIngredient;

import java.util.Optional;

public record TestProcessingIngredient(String key, int amount) implements IProcessingIngredient {
    @Override
    public String codecName() {
        return "test_processing_ingredient";
    }

    @Override
    public PortType type() {
        return PortType.ITEM;
    }

    @Override
    public Optional<IProcessingIngredient> consumePort(IPort<?> port, int parallel, boolean simulate) {
        if (!(port instanceof TestPort testPort)) {
            return Optional.empty();
        }
        return testPort.consume(key, amount * parallel, simulate).map(TestPort.TestPortSnapshot::asIngredient);
    }
}
