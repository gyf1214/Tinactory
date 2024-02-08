package org.shsts.tinactory.content.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.content.logistics.IFluidCollection;
import org.shsts.tinactory.content.logistics.IItemCollection;
import org.shsts.tinactory.content.logistics.IPort;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Random;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class ProcessingResults {
    public static abstract class RatedResult<T extends IPort> implements IProcessingResult {
        public final boolean allowEmpty;
        public final double rate;
        protected final Class<T> portType;

        public RatedResult(boolean allowEmpty, double rate, Class<T> portType) {
            this.allowEmpty = allowEmpty;
            this.rate = rate;
            this.portType = portType;
        }

        protected abstract boolean doInsertPort(T port, Random random, boolean simulate);

        @Override
        public boolean insertPort(IPort port, Random random, boolean simulate) {
            if (port == IPort.EMPTY) {
                return this.allowEmpty;
            }
            if (this.portType.isInstance(port)) {
                if (this.rate < 1.0d && (simulate || random.nextDouble() > this.rate)) {
                    return true;
                }
                return this.doInsertPort(this.portType.cast(port), random, simulate);
            }
            return false;
        }

        protected void toNetwork(FriendlyByteBuf buf) {
            buf.writeBoolean(this.allowEmpty);
            buf.writeDouble(this.rate);
        }

        protected void toJson(JsonObject jo) {
            jo.addProperty("allowEmpty", this.allowEmpty);
            if (this.rate < 1.0d) {
                jo.addProperty("rate", this.rate);
            }
        }
    }

    public static class ItemResult extends RatedResult<IItemCollection> {
        public final ItemStack stack;

        public ItemResult(boolean allowEmpty, double rate, ItemStack stack) {
            super(allowEmpty, rate, IItemCollection.class);
            this.stack = stack;
        }

        @Override
        protected boolean doInsertPort(IItemCollection port, Random random, boolean simulate) {
            return port.insertItem(this.stack, simulate).isEmpty();
        }

        public static final ICombinedSerializer<ItemResult> SERIALIZER = new ICombinedSerializer<>() {
            @Override
            public String getTypeName() {
                return "item";
            }

            @Override
            public void toNetwork(ItemResult sth, FriendlyByteBuf buf) {
                sth.toNetwork(buf);
                buf.writeItem(sth.stack);
            }

            @Override
            public ItemResult fromNetwork(FriendlyByteBuf buf) {
                return new ItemResult(buf.readBoolean(), buf.readDouble(), buf.readItem());
            }

            @Override
            public JsonElement toJson(ItemResult sth) {
                var jo = new JsonObject();
                sth.toJson(jo);
                jo.add("item", this.encodeJson(ItemStack.CODEC, sth.stack));
                return jo;
            }

            @Override
            public ItemResult fromJson(JsonElement je) {
                var jo = je.getAsJsonObject();
                return new ItemResult(
                        GsonHelper.getAsBoolean(jo, "allowEmpty"),
                        GsonHelper.getAsDouble(jo, "rate", 1.0d),
                        this.parseJson(ItemStack.CODEC, GsonHelper.getAsJsonObject(jo, "item")));
            }
        };
    }

    public static class FluidResult extends RatedResult<IFluidCollection> {
        public final FluidStack stack;

        public FluidResult(boolean allowEmpty, double rate, FluidStack stack) {
            super(allowEmpty, rate, IFluidCollection.class);
            this.stack = stack;
        }

        @Override
        protected boolean doInsertPort(IFluidCollection port, Random random, boolean simulate) {
            return port.fill(this.stack, simulate) <= 0;
        }

        public static final ICombinedSerializer<FluidResult> SERIALIZER = new ICombinedSerializer<>() {
            @Override
            public String getTypeName() {
                return "fluid";
            }

            @Override
            public void toNetwork(FluidResult sth, FriendlyByteBuf buf) {
                sth.toNetwork(buf);
                buf.writeFluidStack(sth.stack);
            }

            @Override
            public FluidResult fromNetwork(FriendlyByteBuf buf) {
                return new FluidResult(buf.readBoolean(), buf.readDouble(), buf.readFluidStack());
            }

            @Override
            public JsonElement toJson(FluidResult sth) {
                var jo = new JsonObject();
                sth.toJson(jo);
                jo.add("fluid", this.encodeJson(FluidStack.CODEC, sth.stack));
                return jo;
            }

            @Override
            public FluidResult fromJson(JsonElement je) {
                var jo = je.getAsJsonObject();
                return new FluidResult(
                        GsonHelper.getAsBoolean(jo, "allowEmpty"),
                        GsonHelper.getAsDouble(jo, "rate", 1.0d),
                        this.parseJson(FluidStack.CODEC, GsonHelper.getAsJsonObject(jo, "fluid")));
            }
        };
    }


    public static final ICombinedSerializer<IProcessingResult> SERIALIZER = new TypedSerializer<>() {
        @Override
        public String getTypeName() {
            return "processing_result";
        }

        @Override
        protected void registerSerializers() {
            this.registerSerializer(ItemResult.class, ItemResult.SERIALIZER);
            this.registerSerializer(FluidResult.class, FluidResult.SERIALIZER);
        }
    };
}
