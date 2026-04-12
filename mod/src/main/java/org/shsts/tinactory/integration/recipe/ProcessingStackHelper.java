package org.shsts.tinactory.integration.recipe;

import com.mojang.serialization.Codec;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.recipe.IProcessingObject;
import org.shsts.tinactory.api.recipe.IProcessingResult;
import org.shsts.tinactory.core.logistics.IStackAdapter;
import org.shsts.tinactory.core.recipe.StackIngredient;
import org.shsts.tinactory.core.recipe.StackResult;
import org.shsts.tinactory.integration.logistics.FluidPortAdapter;
import org.shsts.tinactory.integration.logistics.ItemPortAdapter;

import java.util.Optional;
import java.util.function.Predicate;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class ProcessingStackHelper {
    public static final String ITEM_INGREDIENT_CODEC_NAME = "item_ingredient";
    public static final String FLUID_INGREDIENT_CODEC_NAME = "fluid_ingredient";
    public static final String ITEM_RESULT_CODEC_NAME = "item_result";
    public static final String FLUID_RESULT_CODEC_NAME = "fluid_result";

    public static final IProcessingResult EMPTY = itemResult(0d, ItemStack.EMPTY);

    private ProcessingStackHelper() {}

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

    public static StackIngredient<ItemStack> itemIngredient(ItemStack stack) {
        return new StackIngredient<>(ITEM_INGREDIENT_CODEC_NAME, PortType.ITEM, stack, ItemPortAdapter.INSTANCE);
    }

    public static StackIngredient<FluidStack> fluidIngredient(FluidStack stack) {
        return new StackIngredient<>(FLUID_INGREDIENT_CODEC_NAME, PortType.FLUID, stack, FluidPortAdapter.INSTANCE);
    }

    public static StackResult<ItemStack> itemResult(double rate, ItemStack stack) {
        return new StackResult<>(ITEM_RESULT_CODEC_NAME, PortType.ITEM, rate, stack, ItemPortAdapter.INSTANCE);
    }

    public static StackResult<ItemStack> itemResult(ItemStack stack) {
        return itemResult(1d, stack);
    }

    public static StackResult<FluidStack> fluidResult(double rate, FluidStack stack) {
        return new StackResult<>(FLUID_RESULT_CODEC_NAME, PortType.FLUID, rate, stack, FluidPortAdapter.INSTANCE);
    }

    public static StackResult<FluidStack> fluidResult(FluidStack stack) {
        return fluidResult(1d, stack);
    }

    public static Codec<StackIngredient<ItemStack>> itemIngredientCodec() {
        return StackIngredient.codec(ITEM_INGREDIENT_CODEC_NAME, PortType.ITEM, ItemStack.CODEC,
            ItemPortAdapter.INSTANCE);
    }

    public static Codec<StackIngredient<FluidStack>> fluidIngredientCodec() {
        return StackIngredient.codec(FLUID_INGREDIENT_CODEC_NAME, PortType.FLUID, FluidStack.CODEC,
            FluidPortAdapter.INSTANCE);
    }

    public static Codec<StackResult<ItemStack>> itemResultCodec() {
        return StackResult.codec(ITEM_RESULT_CODEC_NAME, PortType.ITEM, ItemStack.CODEC, ItemPortAdapter.INSTANCE);
    }

    public static Codec<StackResult<FluidStack>> fluidResultCodec() {
        return StackResult.codec(FLUID_RESULT_CODEC_NAME, PortType.FLUID, FluidStack.CODEC, FluidPortAdapter.INSTANCE);
    }

    @SuppressWarnings("unchecked")
    public static Optional<StackIngredient<ItemStack>> asItemIngredient(IProcessingObject object) {
        return object instanceof StackIngredient<?> ingredient && ingredient.type() == PortType.ITEM ?
            Optional.of((StackIngredient<ItemStack>) ingredient) : Optional.empty();
    }

    @SuppressWarnings("unchecked")
    public static Optional<StackIngredient<FluidStack>> asFluidIngredient(IProcessingObject object) {
        return object instanceof StackIngredient<?> ingredient && ingredient.type() == PortType.FLUID ?
            Optional.of((StackIngredient<FluidStack>) ingredient) : Optional.empty();
    }

    @SuppressWarnings("unchecked")
    public static Optional<StackResult<ItemStack>> asItemResult(IProcessingObject object) {
        return object instanceof StackResult<?> result && result.type() == PortType.ITEM ?
            Optional.of((StackResult<ItemStack>) result) : Optional.empty();
    }

    @SuppressWarnings("unchecked")
    public static Optional<StackResult<FluidStack>> asFluidResult(IProcessingObject object) {
        return object instanceof StackResult<?> result && result.type() == PortType.FLUID ?
            Optional.of((StackResult<FluidStack>) result) : Optional.empty();
    }

    public static Optional<ItemStack> itemStack(IProcessingObject object) {
        return asItemIngredient(object).map(StackIngredient::stack)
            .or(() -> asItemResult(object).map(StackResult::stack));
    }

    public static Optional<FluidStack> fluidStack(IProcessingObject object) {
        return asFluidIngredient(object).map(StackIngredient::stack)
            .or(() -> asFluidResult(object).map(StackResult::stack));
    }
}
