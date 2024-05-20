package org.shsts.tinactory.core.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.api.logistics.IFluidCollection;
import org.shsts.tinactory.api.logistics.IItemCollection;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.recipe.IProcessingIngredient;
import org.shsts.tinactory.core.logistics.ItemHelper;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class ProcessingIngredients {
    public record SimpleItemIngredient(ItemStack stack) implements IProcessingIngredient {
        @Override
        public PortType type() {
            return PortType.ITEM;
        }

        @Override
        public boolean consumePort(IPort port, boolean simulate) {
            return port instanceof IItemCollection itemCollection &&
                    itemCollection.extractItem(stack, simulate).getCount() >= stack.getCount();
        }

        public static final ICombinedSerializer<SimpleItemIngredient> SERIALIZER = new ICombinedSerializer<>() {
            @Override
            public String getTypeName() {
                return "simple_item";
            }

            @Override
            public void toNetwork(SimpleItemIngredient sth, FriendlyByteBuf buf) {
                buf.writeItem(sth.stack);
            }

            @Override
            public SimpleItemIngredient fromNetwork(FriendlyByteBuf buf) {
                return new SimpleItemIngredient(buf.readItem());
            }

            @Override
            public JsonElement toJson(SimpleItemIngredient sth) {
                return encodeJson(ItemStack.CODEC, sth.stack);
            }

            @Override
            public SimpleItemIngredient fromJson(JsonElement je) {
                return new SimpleItemIngredient(parseJson(ItemStack.CODEC, je));
            }
        };
    }

    public record ItemIngredient(Ingredient ingredient, int amount) implements IProcessingIngredient {
        @Override
        public PortType type() {
            return PortType.ITEM;
        }

        @Override
        public boolean consumePort(IPort port, boolean simulate) {
            return port instanceof IItemCollection itemCollection &&
                    ItemHelper.consumeItemCollection(itemCollection, ingredient, amount, simulate);
        }

        public static final ICombinedSerializer<ItemIngredient> SERIALIZER = new ICombinedSerializer<>() {
            @Override
            public String getTypeName() {
                return "item";
            }

            @Override
            public void toNetwork(ItemIngredient sth, FriendlyByteBuf buf) {
                sth.ingredient.toNetwork(buf);
                buf.writeVarInt(sth.amount);
            }

            @Override
            public ItemIngredient fromNetwork(FriendlyByteBuf buf) {
                return new ItemIngredient(Ingredient.fromNetwork(buf), buf.readVarInt());
            }

            @Override
            public JsonElement toJson(ItemIngredient sth) {
                var jo = new JsonObject();
                jo.add("ingredient", sth.ingredient.toJson());
                jo.addProperty("amount", sth.amount);
                return jo;
            }

            @Override
            public ItemIngredient fromJson(JsonElement je) {
                var jo = je.getAsJsonObject();
                return new ItemIngredient(
                        Ingredient.fromJson(GsonHelper.getAsJsonObject(jo, "ingredient")),
                        GsonHelper.getAsInt(jo, "amount"));
            }
        };
    }

    public record FluidIngredient(FluidStack fluid) implements IProcessingIngredient {
        @Override
        public PortType type() {
            return PortType.FLUID;
        }

        @Override
        public boolean consumePort(IPort port, boolean simulate) {
            return port instanceof IFluidCollection fluidCollection &&
                    fluidCollection.drain(fluid, simulate).getAmount() >= fluid.getAmount();
        }

        public static final ICombinedSerializer<FluidIngredient> SERIALIZER = new ICombinedSerializer<>() {
            @Override
            public String getTypeName() {
                return "simple_fluid";
            }

            @Override
            public void toNetwork(FluidIngredient sth, FriendlyByteBuf buf) {
                buf.writeFluidStack(sth.fluid);
            }

            @Override
            public FluidIngredient fromNetwork(FriendlyByteBuf buf) {
                return new FluidIngredient(buf.readFluidStack());
            }

            @Override
            public JsonElement toJson(FluidIngredient sth) {
                return encodeJson(FluidStack.CODEC, sth.fluid);
            }

            @Override
            public FluidIngredient fromJson(JsonElement je) {
                return new FluidIngredient(parseJson(FluidStack.CODEC, je));
            }
        };
    }

    public static final ICombinedSerializer<IProcessingIngredient> SERIALIZER = new TypedSerializer<>() {
        @Override
        public String getTypeName() {
            return "processing_ingredient";
        }

        @Override
        protected void registerSerializers() {
            registerSerializer(SimpleItemIngredient.class, SimpleItemIngredient.SERIALIZER);
            registerSerializer(ItemIngredient.class, ItemIngredient.SERIALIZER);
            registerSerializer(FluidIngredient.class, FluidIngredient.SERIALIZER);
        }
    };
}
