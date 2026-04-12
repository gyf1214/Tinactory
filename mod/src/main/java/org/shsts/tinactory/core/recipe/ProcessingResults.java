package org.shsts.tinactory.core.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.recipe.IProcessingResult;
import org.shsts.tinactory.core.logistics.IStackAdapter;
import org.shsts.tinactory.integration.logistics.FluidPortAdapter;
import org.shsts.tinactory.integration.logistics.ItemPortAdapter;

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
        public static final String CODEC_NAME = "item_result";

        public final ItemStack stack;

        public ItemResult(double rate, ItemStack stack) {
            super(rate, PortType.ITEM, CODEC_NAME, stack, ItemPortAdapter.INSTANCE);
            this.stack = stack;
        }

        public ItemResult(ItemStack stack) {
            this(1d, stack);
        }

        public static Codec<ItemResult> codec() {
            return RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.fieldOf("rate").forGetter($ -> $.rate),
                ItemStack.CODEC.fieldOf("item").forGetter($ -> $.stack)
            ).apply(instance, ItemResult::new));
        }
    }

    public static class FluidResult extends RatedResult<FluidStack> {
        public static final String CODEC_NAME = "fluid_result";

        public final FluidStack stack;

        public FluidResult(double rate, FluidStack stack) {
            super(rate, PortType.FLUID, CODEC_NAME, stack, FluidPortAdapter.INSTANCE);
            this.stack = stack;
        }

        public FluidResult(FluidStack stack) {
            this(1d, stack);
        }

        public static Codec<FluidResult> codec() {
            return RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.fieldOf("rate").forGetter($ -> $.rate),
                FluidStack.CODEC.fieldOf("fluid").forGetter($ -> $.stack)
            ).apply(instance, FluidResult::new));
        }
    }
}
