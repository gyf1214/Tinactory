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
import java.util.Objects;
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
        return new FluidStack(typed.fluid(), Math.toIntExact(amount), typed.components());
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
        return stack.isEmpty() ? Optional.empty() : Optional.of(ClientUtil.fluidTooltip(stack, true));
    }

    @Override
    public Optional<List<Component>> tooltip(IStackKey key) {
        return Optional.of(ClientUtil.fluidTooltip(stackOf(key), false));
    }

    private record FluidKey(Holder<Fluid> fluid, DataComponentPatch components) implements IStackKey {
        private static FluidKey of(FluidStack stack) {
            return new FluidKey(stack.getFluidHolder(), stack.getComponentsPatch());
        }

        @Override
        public PortType type() {
            return PortType.FLUID;
        }

        @Override
        public ResourceLocation loc() {
            return Objects.requireNonNull(fluid.getKey()).location();
        }

        @Override
        public IStackAdapter<?> adapter() {
            return StackHelper.FLUID_ADAPTER;
        }
    }

    public static final MapCodec<? extends IStackKey> KEY_CODEC =
        RecordCodecBuilder.<FluidKey>mapCodec(instance -> instance.group(
            BuiltInRegistries.FLUID.holderByNameCodec().fieldOf("id").forGetter(FluidKey::fluid),
            DataComponentPatch.CODEC.fieldOf("components").forGetter(FluidKey::components)
        ).apply(instance, FluidKey::new));
}
