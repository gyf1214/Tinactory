package org.shsts.tinactory.core.recipe;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.api.logistics.IFluidCollection;
import org.shsts.tinactory.api.logistics.IItemCollection;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.recipe.IProcessingObject;
import org.shsts.tinactory.api.recipe.IProcessingResult;
import org.shsts.tinactory.core.logistics.StackHelper;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinactory.core.util.MathUtil;

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

    public abstract static class RatedResult<T extends IPort> implements IProcessingResult {
        public final double rate;
        private final PortType portType;
        private final Class<T> portClazz;
        private final String codecName;

        public RatedResult(double rate, PortType portType, Class<T> portClazz, String codecName) {
            this.rate = rate;
            this.portType = portType;
            this.portClazz = portClazz;
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

        protected abstract Optional<IProcessingResult> doInsertPort(T port, int parallel,
            Random random, boolean simulate);

        @Override
        public Optional<IProcessingResult> insertPort(IPort port, int parallel,
            Random random, boolean simulate) {
            if (portClazz.isInstance(port)) {
                var port1 = portClazz.cast(port);
                if (rate < 1d) {
                    if (simulate) {
                        return Optional.of(this);
                    }
                    var parallel1 = MathUtil.sampleBinomial(parallel, rate, random);
                    if (parallel1 <= 0) {
                        return Optional.empty();
                    }
                    return doInsertPort(port1, parallel1, random, false);
                } else {
                    return doInsertPort(port1, parallel, random, simulate);
                }
            }
            return Optional.empty();
        }
    }

    public static class ItemResult extends RatedResult<IItemCollection> {
        private static final String CODEC_NAME = "item_result";

        public final ItemStack stack;

        public ItemResult(double rate, ItemStack stack) {
            super(rate, PortType.ITEM, IItemCollection.class, CODEC_NAME);
            this.stack = stack;
        }

        public ItemResult(ItemStack stack) {
            this(1d, stack);
        }

        @Override
        protected Optional<IProcessingResult> doInsertPort(IItemCollection port, int parallel,
            Random random, boolean simulate) {
            var stack1 = StackHelper.copyWithCount(stack, stack.getCount() * parallel);
            return port.insertItem(stack1, simulate).isEmpty() ?
                Optional.of(new ItemResult(1d, stack1)) : Optional.empty();
        }

        private static final Codec<ItemResult> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("rate").forGetter($ -> $.rate),
            ItemStack.CODEC.fieldOf("item").forGetter($ -> $.stack)
        ).apply(instance, ItemResult::new));
    }

    public static class FluidResult extends RatedResult<IFluidCollection> {
        private static final String CODEC_NAME = "fluid_result";

        public final FluidStack stack;

        public FluidResult(double rate, FluidStack stack) {
            super(rate, PortType.FLUID, IFluidCollection.class, CODEC_NAME);
            this.stack = stack;
        }

        public FluidResult(FluidStack stack) {
            this(1d, stack);
        }

        @Override
        protected Optional<IProcessingResult> doInsertPort(IFluidCollection port, int parallel,
            Random random, boolean simulate) {
            var stack1 = StackHelper.copyWithAmount(stack, stack.getAmount() * parallel);
            return port.acceptInput(stack1) && port.fill(stack1, simulate) == stack1.getAmount() ?
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
