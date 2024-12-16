package org.shsts.tinactory.content.recipe;

import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinycorelib.api.recipe.IRecipeSerializer;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MarkerRecipe extends ProcessingRecipe {
    public final RecipeType<?> baseType;

    private MarkerRecipe(BuilderBase<?, ?> builder, RecipeType<?> baseType) {
        super(builder);
        this.baseType = baseType;
    }

    @Override
    public Optional<String> getDescriptionId() {
        return Optional.of(getDescriptionId(loc));
    }

    public static class Builder extends BuilderBase<MarkerRecipe, Builder> {
        @Nullable
        private ResourceLocation baseType;

        public Builder(IRecipeType<Builder> parent, ResourceLocation loc) {
            super(parent, loc);
        }

        public Builder baseType(ResourceLocation value) {
            baseType = value;
            return this;
        }

        public Builder baseType(IRecipeType<?> value) {
            baseType = value.loc();
            return this;
        }

        public Builder inputItem(int port, TagKey<Item> tag) {
            return inputItem(port, tag, 1);
        }

        public Builder inputItem(int port, ItemLike item) {
            return inputItem(port, () -> item, 1);
        }

        public Builder inputFluid(int port, Fluid fluid) {
            return inputFluid(port, fluid, 1);
        }

        @Override
        protected void validate() {}

        @Override
        protected MarkerRecipe createObject() {
            assert baseType != null;
            var type = Registry.RECIPE_TYPE.get(baseType);
            assert type != null;
            return new MarkerRecipe(this, type);
        }
    }

    private static class Serializer extends ProcessingRecipe.Serializer<MarkerRecipe, Builder> {
        @Override
        protected Builder buildFromJson(IRecipeType<Builder> type, ResourceLocation loc, JsonObject jo) {
            return super.buildFromJson(type, loc, jo)
                .baseType(new ResourceLocation(GsonHelper.getAsString(jo, "base_type")));
        }

        @Override
        public void toJson(JsonObject jo, MarkerRecipe recipe) {
            super.toJson(jo, recipe);
            jo.addProperty("base_type", recipe.baseType.toString());
        }
    }

    public static final IRecipeSerializer<MarkerRecipe, Builder> SERIALIZER = new Serializer();
}
