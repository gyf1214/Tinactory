package org.shsts.tinactory.core.machine;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import org.shsts.tinactory.api.recipe.IProcessingIngredient;
import org.shsts.tinactory.api.recipe.IProcessingObject;
import org.shsts.tinactory.api.recipe.IProcessingResult;

import static org.shsts.tinactory.core.util.CodecHelper.encodeTag;
import static org.shsts.tinactory.core.util.CodecHelper.parseTag;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record ProcessingInfo(int port, IProcessingObject object) {
    public static Codec<ProcessingInfo> codec(
        Codec<IProcessingIngredient> ingredientCodec, Codec<IProcessingResult> resultCodec) {
        return CompoundTag.CODEC.flatXmap(
            tag -> decode(tag, ingredientCodec, resultCodec),
            info -> encode(info, ingredientCodec, resultCodec));
    }

    public CompoundTag serializeNBT(Codec<IProcessingIngredient> ingredientCodec, Codec<IProcessingResult> resultCodec) {
        return (CompoundTag) encodeTag(codec(ingredientCodec, resultCodec), this);
    }

    public static ProcessingInfo fromNBT(CompoundTag tag,
        Codec<IProcessingIngredient> ingredientCodec, Codec<IProcessingResult> resultCodec) {
        return parseTag(codec(ingredientCodec, resultCodec), tag);
    }

    private static DataResult<ProcessingInfo> decode(CompoundTag tag,
        Codec<IProcessingIngredient> ingredientCodec, Codec<IProcessingResult> resultCodec) {
        var hasIngredient = tag.contains("ingredient");
        var hasResult = tag.contains("result");
        if (hasIngredient == hasResult) {
            return DataResult.error("ProcessingInfo requires exactly one payload");
        }
        var port = tag.getInt("port");
        if (hasIngredient) {
            var ingredientTag = tag.get("ingredient");
            assert ingredientTag != null;
            return ingredientCodec.parse(NbtOps.INSTANCE, ingredientTag)
                .map(ingredient -> new ProcessingInfo(port, ingredient));
        }
        var resultTag = tag.get("result");
        assert resultTag != null;
        return resultCodec.parse(NbtOps.INSTANCE, resultTag)
            .map(result -> new ProcessingInfo(port, result));
    }

    private static DataResult<CompoundTag> encode(ProcessingInfo info,
        Codec<IProcessingIngredient> ingredientCodec, Codec<IProcessingResult> resultCodec) {
        var ret = new CompoundTag();
        ret.putInt("port", info.port);
        if (info.object instanceof IProcessingIngredient ingredient) {
            return ingredientCodec.encodeStart(NbtOps.INSTANCE, ingredient).map(tag -> {
                ret.put("ingredient", tag);
                return ret;
            });
        }
        if (info.object instanceof IProcessingResult result) {
            return resultCodec.encodeStart(NbtOps.INSTANCE, result).map(tag -> {
                ret.put("result", tag);
                return ret;
            });
        }
        return DataResult.error("Unsupported processing object: " + info.object.getClass().getName());
    }
}
