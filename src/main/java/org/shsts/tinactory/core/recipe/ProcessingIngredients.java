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
import org.shsts.tinactory.api.logistics.IFluidCollection;
import org.shsts.tinactory.api.logistics.IItemCollection;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.recipe.IProcessingIngredient;
import org.shsts.tinactory.api.recipe.IProcessingObject;
import org.shsts.tinactory.core.logistics.StackHelper;
import org.shsts.tinactory.core.util.CodecHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class ProcessingIngredients {
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
        public Optional<IProcessingIngredient> consumePort(IPort port, int parallel, boolean simulate) {
            if (!(port instanceof IItemCollection collection)) {
                return Optional.empty();
            }
            var stack1 = StackHelper.copyWithCount(stack, stack.getCount() * parallel);
            // it is assumed that the simulation is already done if simulate = false
            var extracted = collection.extractItem(stack1, simulate);
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
        public Optional<IProcessingIngredient> consumePort(IPort port, int parallel, boolean simulate) {
            if (!(port instanceof IItemCollection item)) {
                return Optional.empty();
            }
            if (amount <= 0) {
                return StackHelper.hasItem(item, ingredient)
                    .map($ -> new ItemIngredient(StackHelper.copyWithCount($, 1)));
            } else {
                return StackHelper.consumeItemCollection(item, ingredient, amount * parallel, simulate)
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
        public Optional<IProcessingIngredient> consumePort(IPort port, int parallel, boolean simulate) {
            if (!(port instanceof IFluidCollection collection)) {
                return Optional.empty();
            }
            var fluid1 = StackHelper.copyWithAmount(fluid, fluid.getAmount() * parallel);
            // it is assumed that the simulation is already done if simulate = false
            var extracted = collection.drain(fluid1, simulate);
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

    public static IProcessingIngredient fromJson(JsonElement je) {
        return CodecHelper.parseJson(CODEC, je);
    }

    public static JsonElement toJson(IProcessingIngredient ingredient) {
        return CodecHelper.encodeJson(CODEC, ingredient);
    }
}
