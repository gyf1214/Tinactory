package org.shsts.tinactory.core.recipe;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
import org.shsts.tinactory.core.util.CodecHelper;

import javax.annotation.ParametersAreNonnullByDefault;
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

    public static abstract class RatedResult<T extends IPort> implements IProcessingResult {
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

        protected abstract boolean doInsertPort(T port, Random random, boolean simulate);

        @Override
        public boolean insertPort(IPort port, Random random, boolean simulate) {
            if (port == IPort.EMPTY) {
                return true;
            }
            if (portClazz.isInstance(port)) {
                if (!simulate && random.nextDouble() > rate) {
                    return true;
                }
                return doInsertPort(portClazz.cast(port), random, simulate);
            }
            return false;
        }
    }

    public static class ItemResult extends RatedResult<IItemCollection> {
        private static final String CODEC_NAME = "item_result";

        public final ItemStack stack;

        public ItemResult(double rate, ItemStack stack) {
            super(rate, PortType.ITEM, IItemCollection.class, CODEC_NAME);
            this.stack = stack;
        }

        @Override
        protected boolean doInsertPort(IItemCollection port, Random random, boolean simulate) {
            return port.acceptInput(stack) && port.insertItem(stack, simulate).isEmpty();
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

        @Override
        protected boolean doInsertPort(IFluidCollection port, Random random, boolean simulate) {
            return port.acceptInput(stack) && port.fill(stack, simulate) == stack.getAmount();
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
