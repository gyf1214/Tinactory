package org.shsts.tinactory.integration.recipe;

import com.mojang.serialization.Codec;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.api.recipe.IProcessingObject;
import org.shsts.tinactory.api.recipe.IProcessingResult;
import org.shsts.tinactory.core.recipe.StackResult;

import java.util.HashMap;
import java.util.Map;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class ProcessingResultCodecs {
    private static final Codec<StackResult<ItemStack>> ITEM_RESULT_CODEC = ProcessingStackHelper.itemResultCodec();
    private static final Codec<StackResult<FluidStack>> FLUID_RESULT_CODEC = ProcessingStackHelper.fluidResultCodec();
    private static final Map<String, Codec<? extends IProcessingResult>> CODECS;

    public static final Codec<IProcessingResult> CODEC;

    static {
        CODECS = new HashMap<>();
        CODECS.put(ProcessingStackHelper.ITEM_RESULT_CODEC_NAME, ITEM_RESULT_CODEC);
        CODECS.put(ProcessingStackHelper.FLUID_RESULT_CODEC_NAME, FLUID_RESULT_CODEC);
        CODEC = Codec.STRING.dispatch(IProcessingObject::codecName, CODECS::get);
    }

    private ProcessingResultCodecs() {}

    public static Codec<IProcessingResult> codec() {
        return CODEC;
    }
}
