package org.shsts.tinactory.unit.fixture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import org.shsts.tinactory.api.gui.IRenderDescriptor;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.recipe.IProcessingDisplay;
import org.shsts.tinactory.api.recipe.IProcessingResult;
import org.shsts.tinactory.core.gui.EmptyRenderDescriptor;

import java.util.List;
import java.util.Optional;

public final class TestResult extends TestProcessingObject implements IProcessingResult, IProcessingDisplay {
    public static final MapCodec<TestResult> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.STRING.fieldOf("key").forGetter(TestProcessingObject::key),
        Codec.INT.fieldOf("amount").forGetter(TestProcessingObject::amount)
    ).apply(instance, TestResult::new));

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
    @SuppressWarnings("unchecked")
    public Optional<IProcessingResult> insertPort(IPort<?> port, int parallel, RandomSource random,
        boolean simulate) {
        if (port.type() != PortType.ITEM) {
            return Optional.empty();
        }
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
