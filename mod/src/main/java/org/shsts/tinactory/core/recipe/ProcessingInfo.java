package org.shsts.tinactory.core.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.recipe.IProcessingObject;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record ProcessingInfo(int port, IProcessingObject object) {
    public static Codec<ProcessingInfo> codec(Codec<IProcessingObject> objectCodec) {
        return RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("port").forGetter(ProcessingInfo::port),
            objectCodec.fieldOf("object").forGetter(ProcessingInfo::object)
        ).apply(instance, ProcessingInfo::new));
    }
}
