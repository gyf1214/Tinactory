package org.shsts.tinactory.integration.logistics;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.shsts.tinactory.api.gui.IRenderDescriptor;
import org.shsts.tinactory.api.logistics.IStackAdapter;
import org.shsts.tinactory.api.logistics.IStackKey;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.integration.gui.client.FluidRenderDescriptor;
import org.shsts.tinactory.integration.util.ClientUtil;

import java.util.List;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class FluidPortAdapter implements IStackAdapter<FluidStack> {
    @Override
    public FluidStack empty() {
        return FluidStack.EMPTY;
    }

    @Override
    public boolean isEmpty(FluidStack stack) {
        return stack.isEmpty();
    }

    @Override
    public FluidStack copy(FluidStack stack) {
        return stack.copy();
    }

    @Override
    public int amount(FluidStack stack) {
        return stack.getAmount();
    }

    @Override
    public FluidStack withAmount(FluidStack stack, int amount) {
        return StackHelper.copyWithAmount(stack, amount);
    }

    @Override
    public boolean canStack(FluidStack left, FluidStack right) {
        return FluidStack.isSameFluidSameComponents(left, right);
    }

    @Override
    public IStackKey keyOf(FluidStack stack) {
        return FluidKey.of(stack);
    }

    @Override
    public FluidStack stackOf(IStackKey key, long amount) {
        var typed = (FluidKey) key;
        return new FluidStack(Holder.direct(typed.fluid()), Math.toIntExact(amount), typed.components());
    }

    @Override
    public IRenderDescriptor display(FluidStack stack) {
        return new FluidRenderDescriptor(stack);
    }

    @Override
    public Component name(FluidStack stack) {
        return stack.getHoverName();
    }

    @Override
    public Optional<List<Component>> tooltip(FluidStack stack) {
        return stack.isEmpty() ? Optional.empty() : Optional.of(ClientUtil.fluidTooltip(stack, false));
    }

    private record FluidKey(Fluid fluid, DataComponentPatch components) implements IStackKey {
        private static FluidKey of(FluidStack stack) {
            return new FluidKey(stack.getFluid(), stack.getComponentsPatch());
        }

        private ResourceLocation id() {
            return BuiltInRegistries.FLUID.getKey(fluid);
        }

        private String componentString() {
            return components.toString();
        }

        @Override
        public PortType type() {
            return PortType.FLUID;
        }

        @Override
        public IStackAdapter<?> adapter() {
            return StackHelper.FLUID_ADAPTER;
        }

        @Override
        public int compareTo(IStackKey other) {
            if (type() != other.type()) {
                return Integer.compare(type().ordinal(), other.type().ordinal());
            }
            if (!(other instanceof FluidKey typed)) {
                throw new IllegalArgumentException("Expected fluid key for FLUID type comparison");
            }
            var byId = id().compareTo(typed.id());
            if (byId != 0) {
                return byId;
            }
            return componentString().compareTo(typed.componentString());
        }
    }

    public static final MapCodec<? extends IStackKey> KEY_CODEC =
        RecordCodecBuilder.<FluidKey>mapCodec(instance -> instance.group(
            BuiltInRegistries.FLUID.byNameCodec().fieldOf("id").forGetter(FluidKey::fluid),
            DataComponentPatch.CODEC.fieldOf("components").forGetter(FluidKey::components)
        ).apply(instance, FluidKey::new));
}
