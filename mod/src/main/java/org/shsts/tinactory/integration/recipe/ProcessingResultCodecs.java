package org.shsts.tinactory.integration.recipe;

import com.mojang.serialization.Codec;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.recipe.IProcessingObject;
import org.shsts.tinactory.api.recipe.IProcessingResult;
import org.shsts.tinactory.core.recipe.ProcessingResults;

import java.util.HashMap;
import java.util.Map;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class ProcessingResultCodecs {
    private static final Map<String, Codec<? extends IProcessingResult>> CODECS;

    public static final Codec<IProcessingResult> CODEC;

    static {
        CODECS = new HashMap<>();
        CODECS.put(ProcessingResults.ItemResult.CODEC_NAME, ProcessingResults.ItemResult.codec());
        CODECS.put(ProcessingResults.FluidResult.CODEC_NAME, ProcessingResults.FluidResult.codec());
        CODEC = Codec.STRING.dispatch(IProcessingObject::codecName, CODECS::get);
    }

    private ProcessingResultCodecs() {}

    public static Codec<IProcessingResult> codec() {
        return CODEC;
    }
}
