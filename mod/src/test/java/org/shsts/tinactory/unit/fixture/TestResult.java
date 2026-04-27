package org.shsts.tinactory.unit.fixture;

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import org.shsts.tinactory.api.gui.IRenderDescriptor;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.recipe.IProcessingDisplay;
import org.shsts.tinactory.api.recipe.IProcessingResult;
import org.shsts.tinactory.core.gui.EmptyRenderDescriptor;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public final class TestResult extends TestProcessingObject implements IProcessingResult, IProcessingDisplay {
    public static final Codec<TestResult> CODEC = Codec.STRING.xmap(
        value -> {
            var parts = value.split(":");
            return new TestResult(parts[0], Integer.parseInt(parts[1]));
        },
        value -> value.key() + ":" + value.amount());

    private final IRenderDescriptor descriptor;
    private final List<Component> tooltip;

    public TestResult(String key, int amount) {
        this(key, amount, null, null);
    }

    public TestResult(String key, int amount, IRenderDescriptor descriptor, List<Component> tooltip) {
        super(key, amount);
        this.descriptor = descriptor;
        this.tooltip = tooltip;
    }

    @Override
    public String codecName() {
        return "test_result";
    }

    @Override
    public Optional<IProcessingResult> insertPort(IPort<?> port, int parallel, Random random,
        boolean simulate) {
        if (port.type() != PortType.ITEM) {
            return Optional.empty();
        }
        @SuppressWarnings("unchecked")
        var port1 = (IPort<TestStack>) port;
        var expected = TestStack.item(key(), amount() * parallel);
        if (!TestStack.ADAPTER.isEmpty(port1.insert(expected, true))) {
            return Optional.empty();
        }
        if (!simulate) {
            port1.insert(expected, false);
        }
        return Optional.of(new TestResult(key(), expected.amount()));
    }

    @Override
    public IProcessingResult scaledPreview(int parallel) {
        return new TestResult(key(), amount() * parallel);
    }

    @Override
    public IRenderDescriptor display() {
        return descriptor != null ? descriptor : EmptyRenderDescriptor.INSTANCE;
    }

    @Override
    public Optional<List<Component>> tooltip() {
        return Optional.ofNullable(tooltip);
    }
}
