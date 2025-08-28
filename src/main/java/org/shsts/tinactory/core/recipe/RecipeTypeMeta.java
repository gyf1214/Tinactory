package org.shsts.tinactory.core.recipe;

import com.google.gson.JsonObject;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.shsts.tinactory.core.common.MetaConsumer;

import static org.shsts.tinactory.Tinactory.REGISTRATE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RecipeTypeMeta extends MetaConsumer {
    public RecipeTypeMeta() {
        super("RecipeType");
    }

    @Override
    protected void doAcceptMeta(ResourceLocation loc, JsonObject jo) {
        var id = loc.getPath();
        var displayInput = GsonHelper.getAsBoolean(jo, "displayInput");
        var builder = displayInput ? REGISTRATE.recipeType(id, DisplayInputRecipe::builder) :
            REGISTRATE.recipeType(id, ProcessingRecipe.Builder::new);

        builder.recipeClass(ProcessingRecipe.class)
            .serializer(ProcessingRecipe.SERIALIZER)
            .build();
    }
}
