package org.shsts.tinactory.unit.fixture;

import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.recipe.IProcessingIngredient;
import org.shsts.tinactory.api.recipe.IProcessingResult;

import java.util.Optional;
import java.util.Random;

public record TestProcessingObject(String key, int amount)
    implements IProcessingIngredient, IProcessingResult {

    @Override
    public String codecName() {
        return "test_processing_object";
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
        return testPort.consume(key, amount * parallel, simulate).map(TestPort.TestPortSnapshot::asObject);
    }

    @Override
    public Optional<IProcessingResult> insertPort(IPort<?> port, int parallel, Random random,
        boolean simulate) {
        if (!(port instanceof TestPort testPort)) {
            return Optional.empty();
        }
        return testPort.insert(key, amount * parallel, simulate).map(TestPort.TestPortSnapshot::asObject);
    }
}
