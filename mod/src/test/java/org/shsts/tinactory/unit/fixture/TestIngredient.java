package org.shsts.tinactory.unit.fixture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import org.shsts.tinactory.api.gui.IRenderDescriptor;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.recipe.IProcessingDisplay;
import org.shsts.tinactory.api.recipe.IProcessingIngredient;
import org.shsts.tinactory.core.gui.EmptyRenderDescriptor;

import java.util.List;
import java.util.Optional;

public final class TestIngredient extends TestProcessingObject implements IProcessingIngredient, IProcessingDisplay {
    public static final MapCodec<TestIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.STRING.fieldOf("key").forGetter(TestProcessingObject::key),
        Codec.INT.fieldOf("amount").forGetter(TestProcessingObject::amount)
    ).apply(instance, TestIngredient::new));

    private final IRenderDescriptor descriptor;
    private final List<Component> tooltip;

    public TestIngredient(String key, int amount) {
        this(key, amount, null, null);
    }

    public TestIngredient(String key, int amount, IRenderDescriptor descriptor, List<Component> tooltip) {
        super(key, amount);
        this.descriptor = descriptor;
        this.tooltip = tooltip;
    }

    @Override
    public String codecName() {
        return "test_ingredient";
    }

    @Override
    public Optional<IProcessingIngredient> consumePort(IPort<?> port, int parallel, boolean simulate) {
        if (port.type() != PortType.ITEM) {
            return Optional.empty();
        }
        @SuppressWarnings("unchecked")
        var port1 = (IPort<TestStack>) port;
        var expected = TestStack.item(key(), amount() * parallel);
        var extracted = port1.extract(expected, simulate);
        return extracted.amount() >= expected.amount() ?
            Optional.of(new TestIngredient(key(), extracted.amount())) : Optional.empty();
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
