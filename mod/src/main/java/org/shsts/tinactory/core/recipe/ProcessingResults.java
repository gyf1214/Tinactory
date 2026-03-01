package org.shsts.tinactory.core.recipe;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.recipe.IProcessingObject;
import org.shsts.tinactory.api.recipe.IProcessingResult;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinactory.core.util.MathUtil;
import org.shsts.tinactory.integration.logistics.StackHelper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class ProcessingResults {
    public static final IProcessingResult EMPTY = new ItemResult(0d, ItemStack.EMPTY);

    public abstract static class RatedResult<T> implements IProcessingResult {
        public final double rate;
        private final PortType portType;
        private final String codecName;

        public RatedResult(double rate, PortType portType, String codecName) {
            this.rate = rate;
            this.portType = portType;
            this.codecName = codecName;
        }

        @Override
        public String codecName() {
            return codecName;
        }

        @Override
        public PortType type() {
            return portType;
        }

        protected abstract Optional<IProcessingResult> doInsertPort(IPort<T> port, int parallel,
            Random random, boolean simulate);

        @Override
        public Optional<IProcessingResult> insertPort(IPort<?> port, int parallel,
            Random random, boolean simulate) {
            if (port.type() != portType) {
                return Optional.empty();
            }
            @SuppressWarnings("unchecked")
            var port1 = (IPort<T>) port;
            if (rate < 1d && !simulate) {
                var parallel1 = MathUtil.sampleBinomial(parallel, rate, random);
                if (parallel1 <= 0) {
                    return Optional.empty();
                }
                return doInsertPort(port1, parallel1, random, false);
            } else {
                return doInsertPort(port1, parallel, random, simulate);
            }
        }
    }

    public static class ItemResult extends RatedResult<ItemStack> {
        private static final String CODEC_NAME = "item_result";

        public final ItemStack stack;

        public ItemResult(double rate, ItemStack stack) {
            super(rate, PortType.ITEM, CODEC_NAME);
            this.stack = stack;
        }

        public ItemResult(ItemStack stack) {
            this(1d, stack);
        }

        @Override
        protected Optional<IProcessingResult> doInsertPort(IPort<ItemStack> port, int parallel,
            Random random, boolean simulate) {
            var stack1 = StackHelper.copyWithCount(stack, stack.getCount() * parallel);
            return port.acceptInput(stack1) && port.insert(stack1, simulate).isEmpty() ?
                Optional.of(new ItemResult(stack1)) : Optional.empty();
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
            super(rate, PortType.FLUID, CODEC_NAME);
            this.stack = stack;
        }

        public FluidResult(FluidStack stack) {
            this(1d, stack);
        }

        @Override
        protected Optional<IProcessingResult> doInsertPort(IPort<FluidStack> port, int parallel,
            Random random, boolean simulate) {
            var stack1 = StackHelper.copyWithAmount(stack, stack.getAmount() * parallel);
            return port.acceptInput(stack1) && port.insert(stack1, simulate).isEmpty() ?
                Optional.of(new FluidResult(stack1)) : Optional.empty();
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

    public static IProcessingResult fromJson(JsonElement je) {
        return CodecHelper.parseJson(CODEC, je);
    }

    public static JsonElement toJson(IProcessingResult ingredient) {
        return CodecHelper.encodeJson(CODEC, ingredient);
    }

    public static <V> Optional<V> mapItemsOrFluid(IProcessingObject obj, Function<List<ItemStack>, V> itemsMapper,
        Function<FluidStack, V> fluidMapper) {

        if (obj instanceof ProcessingIngredients.ItemIngredient item) {
            return Optional.of(itemsMapper.apply(List.of(item.stack())));
        } else if (obj instanceof ProcessingIngredients.ItemsIngredientBase items) {
            return Optional.of(itemsMapper.apply(Arrays.asList(items.ingredient.getItems())));
        } else if (obj instanceof ProcessingIngredients.FluidIngredient fluid) {
            return Optional.of(fluidMapper.apply(fluid.fluid()));
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
            return Optional.of(fluidMapper.apply(fluid.fluid()));
        } else if (obj instanceof ProcessingResults.ItemResult item) {
            return Optional.of(itemsMapper.apply(item.stack));
        } else if (obj instanceof ProcessingResults.FluidResult fluid) {
            return Optional.of(fluidMapper.apply(fluid.stack));
        }
        return Optional.empty();
    }
}
