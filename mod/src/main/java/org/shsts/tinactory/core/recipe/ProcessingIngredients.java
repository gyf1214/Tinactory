package org.shsts.tinactory.core.recipe;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.recipe.IProcessingIngredient;
import org.shsts.tinactory.api.recipe.IProcessingObject;
import org.shsts.tinactory.core.logistics.IStackAdapter;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinactory.integration.logistics.FluidPortAdapter;
import org.shsts.tinactory.integration.logistics.ItemPortAdapter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class ProcessingIngredients {
    static <T> Optional<T> findMatchingPort(IPort<T> port, Predicate<T> ingredient, IStackAdapter<T> adapter) {
        return port.getAllStorages().stream()
            .filter(ingredient)
            .findFirst()
            .map(stack -> adapter.withAmount(stack, 1));
    }

    static <T> Optional<T> consumeMatchingPort(IPort<T> port, Predicate<T> ingredient,
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

    public record ItemIngredient(ItemStack stack) implements IProcessingIngredient {
        private static final String CODEC_NAME = "item_ingredient";

        @Override
        public String codecName() {
            return CODEC_NAME;
        }

        @Override
        public PortType type() {
            return PortType.ITEM;
        }

        @Override
        public Predicate<?> filter() {
            return (Predicate<ItemStack>) stack1 -> StackHelper.canItemsStack(stack1, stack);
        }

        @Override
        public Optional<IProcessingIngredient> consumePort(IPort<?> port, int parallel, boolean simulate) {
            if (port.type() != PortType.ITEM) {
                return Optional.empty();
            }
            var item = port.asItem();
            var stack1 = ItemPortAdapter.INSTANCE.withAmount(stack, stack.getCount() * parallel);
            // it is assumed that the simulation is already done if simulate = false
            var extracted = item.extract(stack1, simulate);
            return extracted.getCount() >= stack1.getCount() ?
                Optional.of(new ItemIngredient(stack1)) : Optional.empty();
        }

        private static final Codec<ItemIngredient> CODEC =
            ItemStack.CODEC.xmap(ItemIngredient::new, ItemIngredient::stack);
    }

    public abstract static class ItemsIngredientBase implements IProcessingIngredient {
        public final Ingredient ingredient;
        public final int amount;

        protected ItemsIngredientBase(Ingredient ingredient, int amount) {
            this.amount = amount;
            this.ingredient = ingredient;
        }

        @Override
        public PortType type() {
            return PortType.ITEM;
        }

        @Override
        public Predicate<?> filter() {
            return ingredient;
        }

        @Override
        public Optional<IProcessingIngredient> consumePort(IPort<?> port, int parallel, boolean simulate) {
            if (port.type() != PortType.ITEM) {
                return Optional.empty();
            }
            var item = port.asItem();
            if (amount <= 0) {
                return findMatchingPort(item, ingredient, ItemPortAdapter.INSTANCE).map(ItemIngredient::new);
            } else {
                return consumeMatchingPort(item, ingredient, ItemPortAdapter.INSTANCE, amount * parallel, simulate)
                    .map(ItemIngredient::new);
            }
        }

        /**
         * Note that this is not serializable, only for display or JEI purpose.
         */
        public static ItemsIngredientBase of(Ingredient ingredient, int amount) {
            return new ItemsIngredientBase(ingredient, amount) {
                @Override
                public String codecName() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }

    public static class TagIngredient extends ItemsIngredientBase {
        private static final String CODEC_NAME = "tag_ingredient";

        private final TagKey<Item> tag;

        public TagIngredient(TagKey<Item> tag, int amount) {
            super(Ingredient.of(tag), amount);
            this.tag = tag;
        }

        @Override
        public String codecName() {
            return CODEC_NAME;
        }

        private static final Codec<TagIngredient> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TagKey.codec(Registry.ITEM_REGISTRY).fieldOf("tag").forGetter($ -> $.tag),
            Codec.INT.fieldOf("amount").forGetter($ -> $.amount)
        ).apply(instance, TagIngredient::new));
    }

    public record FluidIngredient(FluidStack fluid) implements IProcessingIngredient {
        private static final String CODEC_NAME = "fluid_ingredient";

        @Override
        public String codecName() {
            return CODEC_NAME;
        }

        @Override
        public PortType type() {
            return PortType.FLUID;
        }

        @Override
        public Predicate<?> filter() {
            return (Predicate<FluidStack>) fluid1 -> fluid1.isFluidEqual(fluid);
        }

        @Override
        public Optional<IProcessingIngredient> consumePort(IPort<?> port, int parallel, boolean simulate) {
            if (port.type() != PortType.FLUID) {
                return Optional.empty();
            }
            var fluidPort = port.asFluid();
            var fluid1 = FluidPortAdapter.INSTANCE.withAmount(fluid, fluid.getAmount() * parallel);
            // it is assumed that the simulation is already done if simulate = false
            var extracted = fluidPort.extract(fluid1, simulate);
            return extracted.getAmount() >= fluid1.getAmount() ?
                Optional.of(new FluidIngredient(extracted)) : Optional.empty();
        }

        private static final Codec<FluidIngredient> CODEC =
            FluidStack.CODEC.xmap(FluidIngredient::new, FluidIngredient::fluid);
    }

    private static final Map<String, Codec<? extends IProcessingIngredient>> CODECS;

    static {
        CODECS = new HashMap<>();
        CODECS.put(ItemIngredient.CODEC_NAME, ItemIngredient.CODEC);
        CODECS.put(TagIngredient.CODEC_NAME, TagIngredient.CODEC);
        CODECS.put(FluidIngredient.CODEC_NAME, FluidIngredient.CODEC);
    }

    public static final Codec<IProcessingIngredient> CODEC =
        Codec.STRING.dispatch(IProcessingObject::codecName, CODECS::get);

    public static Codec<IProcessingIngredient> codec() {
        return CODEC;
    }

    public static IProcessingIngredient fromJson(Codec<IProcessingIngredient> codec, JsonElement je) {
        return CodecHelper.parseJson(codec, je);
    }

    public static IProcessingIngredient fromJson(JsonElement je) {
        return fromJson(CODEC, je);
    }

    public static JsonElement toJson(Codec<IProcessingIngredient> codec, IProcessingIngredient ingredient) {
        return CodecHelper.encodeJson(codec, ingredient);
    }

    public static JsonElement toJson(IProcessingIngredient ingredient) {
        return toJson(CODEC, ingredient);
    }
}
