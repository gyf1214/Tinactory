package org.shsts.tinactory.core.recipe;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.MethodsReturnNonnullByDefault;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class ProcessingResults {

    public static final IProcessingResult EMPTY = new ItemResult(true, 0, ItemStack.EMPTY);

    public static abstract class RatedResult<T extends IPort> implements IProcessingResult {
        public final boolean autoVoid;
        public final double rate;
        private final PortType portType;
        private final Class<T> portClazz;
        private final String codecName;

        public RatedResult(boolean autoVoid, double rate, PortType portType, Class<T> portClazz, String codecName) {
            this.autoVoid = autoVoid;
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
                return autoVoid;
            }
            if (portClazz.isInstance(port)) {
                if (rate < 1d && (simulate || random.nextDouble() > rate)) {
                    return true;
                }
                return doInsertPort(portClazz.cast(port), random, simulate) || autoVoid;
            }
            return false;
        }
    }

    public static class ItemResult extends RatedResult<IItemCollection> {
        private static final String CODEC_NAME = "item_result";

        public final ItemStack stack;

        public ItemResult(boolean autoVoid, double rate, ItemStack stack) {
            super(autoVoid, rate, PortType.ITEM, IItemCollection.class, CODEC_NAME);
            this.stack = stack;
        }

        @Override
        protected boolean doInsertPort(IItemCollection port, Random random, boolean simulate) {
            return port.acceptInput(stack) && port.insertItem(stack, simulate).isEmpty();
        }

        private static final Codec<ItemResult> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.fieldOf("auto_void").forGetter($ -> $.autoVoid),
                Codec.DOUBLE.fieldOf("rate").forGetter($ -> $.rate),
                ItemStack.CODEC.fieldOf("item").forGetter($ -> $.stack)
        ).apply(instance, ItemResult::new));
    }

    public static class FluidResult extends RatedResult<IFluidCollection> {
        private static final String CODEC_NAME = "fluid_result";

        public final FluidStack stack;

        public FluidResult(boolean autoVoid, double rate, FluidStack stack) {
            super(autoVoid, rate, PortType.FLUID, IFluidCollection.class, CODEC_NAME);
            this.stack = stack;
        }

        @Override
        protected boolean doInsertPort(IFluidCollection port, Random random, boolean simulate) {
            return port.acceptInput(stack) && port.fill(stack, simulate) == stack.getAmount();
        }

        private static final Codec<FluidResult> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.fieldOf("auto_void").forGetter($ -> $.autoVoid),
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
}
