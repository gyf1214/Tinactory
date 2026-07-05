package org.shsts.tinactory.integration.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.IStackAdapter;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.recipe.IProcessingIngredient;
import org.shsts.tinactory.api.recipe.IProcessingObject;
import org.shsts.tinactory.api.recipe.IProcessingResult;
import org.shsts.tinactory.core.recipe.AssemblyRecipe;
import org.shsts.tinactory.core.recipe.DisplayInputRecipe;
import org.shsts.tinactory.core.recipe.MarkerRecipe;
import org.shsts.tinactory.core.recipe.ProcessingInfo;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.core.recipe.ResearchRecipe;
import org.shsts.tinactory.core.recipe.StackIngredient;
import org.shsts.tinactory.core.recipe.StackResult;
import org.shsts.tinactory.integration.logistics.StackHelper;

import java.util.HashMap;
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
    public static final Codec<IProcessingObject> OBJECT_CODEC;
    public static final Codec<ProcessingInfo> INFO_CODEC;
    public static final Codec<ProcessingRecipe.Input> INPUT_CODEC;
    public static final Codec<ProcessingRecipe.Output> OUTPUT_CODEC;

    public static final MapCodec<ProcessingRecipe> PROCESSING_CODEC;
    public static final MapCodec<DisplayInputRecipe> DISPLAY_INPUT_CODEC;
    public static final MapCodec<AssemblyRecipe> ASSEMBLY_CODEC;
    public static final MapCodec<MarkerRecipe> MARKER_CODEC;
    public static final MapCodec<ResearchRecipe> RESEARCH_CODEC;

    static {
        var itemIngredient = StackIngredient.codec(ITEM_INGREDIENT_CODEC_NAME, PortType.ITEM,
            ItemStack.CODEC, StackHelper.ITEM_ADAPTER);
        var fluidIngredient = StackIngredient.codec(FLUID_INGREDIENT_CODEC_NAME, PortType.FLUID,
            FluidStack.CODEC, StackHelper.FLUID_ADAPTER);
        var ingredients = Map.of(
            ITEM_INGREDIENT_CODEC_NAME, itemIngredient,
            TagIngredient.CODEC_NAME, TagIngredient.CODEC,
            FLUID_INGREDIENT_CODEC_NAME, fluidIngredient);
        INGREDIENT_CODEC = Codec.STRING.dispatch(IProcessingObject::codecName, ingredients::get);

        var itemResult = StackResult.codec(ITEM_RESULT_CODEC_NAME, PortType.ITEM,
            ItemStack.CODEC, StackHelper.ITEM_ADAPTER);
        var fluidResult = StackResult.codec(FLUID_RESULT_CODEC_NAME, PortType.FLUID,
            FluidStack.CODEC, StackHelper.FLUID_ADAPTER);
        var results = Map.of(
            ITEM_RESULT_CODEC_NAME, itemResult,
            FLUID_RESULT_CODEC_NAME, fluidResult);
        RESULT_CODEC = Codec.STRING.dispatch(IProcessingObject::codecName, results::get);

        var objects = new HashMap<String, MapCodec<? extends IProcessingObject>>();
        objects.putAll(ingredients);
        objects.putAll(results);
        OBJECT_CODEC = Codec.STRING.dispatch(IProcessingObject::codecName, objects::get);

        INFO_CODEC = ProcessingInfo.codec(OBJECT_CODEC);
        INPUT_CODEC = ProcessingRecipe.inputCodec(INGREDIENT_CODEC);
        OUTPUT_CODEC = ProcessingRecipe.outputCodec(RESULT_CODEC);

        PROCESSING_CODEC = ProcessingRecipe.codec(INPUT_CODEC, OUTPUT_CODEC, ProcessingRecipe::new);
        DISPLAY_INPUT_CODEC = ProcessingRecipe.codec(INPUT_CODEC, OUTPUT_CODEC, DisplayInputRecipe::new);
        ASSEMBLY_CODEC = AssemblyRecipe.assemblyCodec(INPUT_CODEC, OUTPUT_CODEC, AssemblyRecipe::new);
        MARKER_CODEC = MarkerRecipe.codec(INGREDIENT_CODEC, INPUT_CODEC, OUTPUT_CODEC);
        RESEARCH_CODEC = ResearchRecipe.codec(INPUT_CODEC, OUTPUT_CODEC);
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
