package org.shsts.tinactory.core.recipe;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.core.logistics.IStackAdapter;
import org.shsts.tinactory.integration.logistics.FluidPortAdapter;
import org.shsts.tinactory.integration.logistics.ItemPortAdapter;

import java.util.Optional;
import java.util.function.Predicate;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class ProcessingIngredients {
    public static <T> Optional<T> findMatchingPort(IPort<T> port, Predicate<T> ingredient, IStackAdapter<T> adapter) {
        return port.getAllStorages().stream()
            .filter(ingredient)
            .findFirst()
            .map(stack -> adapter.withAmount(stack, 1));
    }

    public static <T> Optional<T> consumeMatchingPort(IPort<T> port, Predicate<T> ingredient,
        IStackAdapter<T> adapter, int count, boolean simulate) {
        for (var stack : port.getAllStorages()) {
            if (!ingredient.test(stack) || adapter.amount(stack) < count) {
                continue;
            }
            var expected = adapter.withAmount(stack, count);
            var extracted = port.extract(expected, true);
            if (adapter.amount(extracted) < count) {
                continue;
            }
            if (simulate) {
                return Optional.of(extracted);
            }
            return Optional.of(port.extract(expected, false));
        }
        return Optional.empty();
    }

    public static final class ItemIngredient extends StackIngredient<ItemStack> {
        public static final String CODEC_NAME = "item_ingredient";

        public ItemIngredient(ItemStack stack) {
            super(CODEC_NAME, PortType.ITEM, stack, ItemPortAdapter.INSTANCE);
        }
    }

    public static final class FluidIngredient extends StackIngredient<FluidStack> {
        public static final String CODEC_NAME = "fluid_ingredient";

        public FluidIngredient(FluidStack fluid) {
            super(CODEC_NAME, PortType.FLUID, fluid, FluidPortAdapter.INSTANCE);
        }

        public FluidStack fluid() {
            return stack();
        }
    }
}
