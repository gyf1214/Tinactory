package org.shsts.tinactory.integration.recipe;

import com.mojang.serialization.Codec;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.recipe.IProcessingIngredient;
import org.shsts.tinactory.api.recipe.IProcessingObject;
import org.shsts.tinactory.api.recipe.IProcessingResult;
import org.shsts.tinactory.core.logistics.IStackAdapter;
import org.shsts.tinactory.core.recipe.ProcessingInfo;
import org.shsts.tinactory.core.recipe.StackIngredient;
import org.shsts.tinactory.core.recipe.StackResult;
import org.shsts.tinactory.integration.logistics.StackHelper;

import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class ProcessingHelper {
    public static final String ITEM_INGREDIENT_CODEC_NAME = "item_ingredient";
    public static final String FLUID_INGREDIENT_CODEC_NAME = "fluid_ingredient";
    public static final String ITEM_RESULT_CODEC_NAME = "item_result";
    public static final String FLUID_RESULT_CODEC_NAME = "fluid_result";

    public static final IProcessingResult EMPTY = itemResult(0d, ItemStack.EMPTY);

    public static final Codec<IProcessingIngredient> INGREDIENT_CODEC;
    public static final Codec<IProcessingResult> RESULT_CODEC;
    public static final Codec<ProcessingInfo> INFO_CODEC;

    static {
        var ingredientCodecs = Map.of(
            ITEM_INGREDIENT_CODEC_NAME, itemIngredientCodec(),
            TagIngredient.CODEC_NAME, TagIngredient.codec(),
            FLUID_INGREDIENT_CODEC_NAME, fluidIngredientCodec());
        INGREDIENT_CODEC = Codec.STRING.dispatch(IProcessingObject::codecName, ingredientCodecs::get);

        var resultCodecs = Map.of(
            ITEM_RESULT_CODEC_NAME, itemResultCodec(),
            FLUID_RESULT_CODEC_NAME, fluidResultCodec());
        RESULT_CODEC = Codec.STRING.dispatch(IProcessingObject::codecName, resultCodecs::get);

        INFO_CODEC = ProcessingInfo.codec(INGREDIENT_CODEC, RESULT_CODEC);
    }

    private ProcessingHelper() {}

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
        return new StackIngredient<>(ITEM_INGREDIENT_CODEC_NAME, PortType.ITEM, stack, StackHelper.ITEM_ADAPTER);
    }

    public static StackIngredient<FluidStack> fluidIngredient(FluidStack stack) {
        return new StackIngredient<>(FLUID_INGREDIENT_CODEC_NAME, PortType.FLUID, stack, StackHelper.FLUID_ADAPTER);
    }

    public static StackResult<ItemStack> itemResult(double rate, ItemStack stack) {
        return new StackResult<>(ITEM_RESULT_CODEC_NAME, PortType.ITEM, rate, stack, StackHelper.ITEM_ADAPTER);
    }

    public static StackResult<ItemStack> itemResult(ItemStack stack) {
        return itemResult(1d, stack);
    }

    public static StackResult<FluidStack> fluidResult(double rate, FluidStack stack) {
        return new StackResult<>(FLUID_RESULT_CODEC_NAME, PortType.FLUID, rate, stack, StackHelper.FLUID_ADAPTER);
    }

    public static StackResult<FluidStack> fluidResult(FluidStack stack) {
        return fluidResult(1d, stack);
    }

    private static Codec<StackIngredient<ItemStack>> itemIngredientCodec() {
        return StackIngredient.codec(ITEM_INGREDIENT_CODEC_NAME, PortType.ITEM, ItemStack.CODEC,
            StackHelper.ITEM_ADAPTER);
    }

    private static Codec<StackIngredient<FluidStack>> fluidIngredientCodec() {
        return StackIngredient.codec(FLUID_INGREDIENT_CODEC_NAME, PortType.FLUID, FluidStack.CODEC,
            StackHelper.FLUID_ADAPTER);
    }

    private static Codec<StackResult<ItemStack>> itemResultCodec() {
        return StackResult.codec(ITEM_RESULT_CODEC_NAME, PortType.ITEM, ItemStack.CODEC, StackHelper.ITEM_ADAPTER);
    }

    private static Codec<StackResult<FluidStack>> fluidResultCodec() {
        return StackResult.codec(FLUID_RESULT_CODEC_NAME, PortType.FLUID, FluidStack.CODEC, StackHelper.FLUID_ADAPTER);
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
