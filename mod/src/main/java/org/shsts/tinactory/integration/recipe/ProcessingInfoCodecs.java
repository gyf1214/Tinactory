package org.shsts.tinactory.integration.recipe;

import com.mojang.serialization.Codec;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.machine.ProcessingInfo;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class ProcessingInfoCodecs {
    public static final Codec<ProcessingInfo> CODEC =
        ProcessingInfo.codec(ProcessingIngredientCodecs.CODEC, ProcessingResultCodecs.CODEC);

    private ProcessingInfoCodecs() {}
}
