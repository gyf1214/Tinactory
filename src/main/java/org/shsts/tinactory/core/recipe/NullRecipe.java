package org.shsts.tinactory.core.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.FalseCondition;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class NullRecipe implements FinishedRecipe {
    private final ResourceLocation loc;

    public NullRecipe(ResourceLocation loc) {
        this.loc = loc;
    }

    @Override
    public void serializeRecipeData(JsonObject jo) {
        var je = new JsonArray();
        je.add(CraftingHelper.serialize(FalseCondition.INSTANCE));
        jo.add("conditions", je);
    }

    @Override
    public ResourceLocation getId() {
        return loc;
    }

    @Override
    public RecipeSerializer<?> getType() {
        return RecipeSerializer.SHAPED_RECIPE;
    }

    @Nullable
    @Override
    public JsonObject serializeAdvancement() {
        return null;
    }

    @Nullable
    @Override
    public ResourceLocation getAdvancementId() {
        return null;
    }
}
