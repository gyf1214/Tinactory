package org.shsts.tinactory.core.machine;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import org.shsts.tinactory.api.recipe.IProcessingIngredient;
import org.shsts.tinactory.api.recipe.IProcessingObject;
import org.shsts.tinactory.api.recipe.IProcessingResult;
import org.shsts.tinactory.core.recipe.ProcessingIngredients;
import org.shsts.tinactory.core.recipe.ProcessingResults;

import static org.shsts.tinactory.core.util.CodecHelper.encodeTag;
import static org.shsts.tinactory.core.util.CodecHelper.parseTag;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record ProcessingInfo(int port, IProcessingObject object) {
    public CompoundTag serializeNBT() {
        var ret = new CompoundTag();
        ret.putInt("port", port);
        if (object instanceof IProcessingIngredient ingredient) {
            ret.put("ingredient", encodeTag(ProcessingIngredients.CODEC, ingredient));
        } else if (object instanceof IProcessingResult result) {
            ret.put("result", encodeTag(ProcessingResults.CODEC, result));
        }
        return ret;
    }

    public static ProcessingInfo fromNBT(CompoundTag tag) {
        var port = tag.getInt("port");
        if (tag.contains("ingredient")) {
            var tag1 = tag.get("ingredient");
            assert tag1 != null;
            var ingredient = parseTag(ProcessingIngredients.CODEC, tag1);
            return new ProcessingInfo(port, ingredient);
        } else if (tag.contains("result")) {
            var tag1 = tag.get("result");
            assert tag1 != null;
            var result = parseTag(ProcessingResults.CODEC, tag1);
            return new ProcessingInfo(port, result);
        }
        throw new IllegalStateException();
    }
}
