package org.shsts.tinactory.core.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.recipe.IProcessingObject;
import org.shsts.tinactory.api.recipe.IProcessingResult;
import org.shsts.tinactory.core.logistics.IStackAdapter;
import org.shsts.tinactory.integration.logistics.FluidPortAdapter;
import org.shsts.tinactory.integration.logistics.ItemPortAdapter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class ProcessingResults {
    public static final IProcessingResult EMPTY = new ItemResult(0d, ItemStack.EMPTY);

    public abstract static class RatedResult<T> extends StackResult<T> {
        public final double rate;

        public RatedResult(double rate, PortType portType, String codecName, T stack, IStackAdapter<T> adapter) {
            super(codecName, portType, rate, stack, adapter);
            this.rate = rate;
        }
    }

    public static class ItemResult extends RatedResult<ItemStack> {
        private static final String CODEC_NAME = "item_result";

        public final ItemStack stack;

        public ItemResult(double rate, ItemStack stack) {
            super(rate, PortType.ITEM, CODEC_NAME, stack, ItemPortAdapter.INSTANCE);
            this.stack = stack;
        }

        public ItemResult(ItemStack stack) {
            this(1d, stack);
        }

        private static final Codec<ItemResult> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("rate").forGetter($ -> $.rate),
            ItemStack.CODEC.fieldOf("item").forGetter($ -> $.stack)
        ).apply(instance, ItemResult::new));
    }

    public static class FluidResult extends RatedResult<FluidStack> {
        private static final String CODEC_NAME = "fluid_result";

        public final FluidStack stack;

        public FluidResult(double rate, FluidStack stack) {
            super(rate, PortType.FLUID, CODEC_NAME, stack, FluidPortAdapter.INSTANCE);
            this.stack = stack;
        }

        public FluidResult(FluidStack stack) {
            this(1d, stack);
        }

        private static final Codec<FluidResult> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("rate").forGetter($ -> $.rate),
            FluidStack.CODEC.fieldOf("fluid").forGetter($ -> $.stack)
        ).apply(instance, FluidResult::new));
    }

    private static final Map<String, Codec<? extends IProcessingResult>> CODECS;

    static {
        CODECS = new HashMap<>();
        CODECS.put(ItemResult.CODEC_NAME, ItemResult.CODEC);
        CODECS.put(FluidResult.CODEC_NAME, FluidResult.CODEC);
    }

    public static final Codec<IProcessingResult> CODEC =
        Codec.STRING.dispatch(IProcessingObject::codecName, CODECS::get);

    public static Codec<IProcessingResult> codec() {
        return CODEC;
    }

    public static <V> Optional<V> mapItemsOrFluid(IProcessingObject obj, Function<List<ItemStack>, V> itemsMapper,
        Function<FluidStack, V> fluidMapper) {

        if (obj instanceof ProcessingIngredients.ItemIngredient item) {
            return Optional.of(itemsMapper.apply(List.of(item.stack())));
        } else if (obj instanceof ProcessingIngredients.ItemsIngredientBase items) {
            return Optional.of(itemsMapper.apply(Arrays.asList(items.ingredient.getItems())));
        } else if (obj instanceof ProcessingIngredients.FluidIngredient fluid) {
            return Optional.of(fluidMapper.apply(fluid.stack()));
        } else if (obj instanceof ProcessingResults.ItemResult item) {
            return Optional.of(itemsMapper.apply(List.of(item.stack)));
        } else if (obj instanceof ProcessingResults.FluidResult fluid) {
            return Optional.of(fluidMapper.apply(fluid.stack));
        }
        return Optional.empty();
    }

    public static void consumeItemsOrFluid(IProcessingObject obj, Consumer<List<ItemStack>> itemsConsumer,
        Consumer<FluidStack> fluidConsumer) {
        mapItemsOrFluid(obj, items -> {
            itemsConsumer.accept(items);
            return Unit.INSTANCE;
        }, fluid -> {
            fluidConsumer.accept(fluid);
            return Unit.INSTANCE;
        });
    }

    public static <V> Optional<V> mapItemOrFluid(IProcessingObject obj, Function<ItemStack, V> itemsMapper,
        Function<FluidStack, V> fluidMapper) {

        if (obj instanceof ProcessingIngredients.ItemIngredient item) {
            return Optional.of(itemsMapper.apply(item.stack()));
        } else if (obj instanceof ProcessingIngredients.ItemsIngredientBase items) {
            var itemList = items.ingredient.getItems();
            if (itemList.length > 0) {
                return Optional.of(itemsMapper.apply(itemList[0]));
            }
        } else if (obj instanceof ProcessingIngredients.FluidIngredient fluid) {
            return Optional.of(fluidMapper.apply(fluid.stack()));
        } else if (obj instanceof ProcessingResults.ItemResult item) {
            return Optional.of(itemsMapper.apply(item.stack));
        } else if (obj instanceof ProcessingResults.FluidResult fluid) {
            return Optional.of(fluidMapper.apply(fluid.stack));
        }
        return Optional.empty();
    }
}
