package org.shsts.tinactory.core.recipe;

import com.google.common.collect.Streams;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.core.common.SmartRecipeSerializer;
import org.shsts.tinactory.registrate.common.RecipeTypeEntry;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AssemblyRecipe extends ProcessingRecipe {
    protected final List<ResourceLocation> requiredTech;

    protected AssemblyRecipe(BuilderBase<?, ?> builder) {
        super(builder);
        this.requiredTech = builder.requiredTech;
    }

    @Override
    public boolean canCraftIn(IContainer container) {
        return container.getOwnerTeam()
                .map(team -> requiredTech.stream().allMatch(team::isTechFinished))
                .orElse(requiredTech.isEmpty());
    }

    public abstract static class BuilderBase<U extends AssemblyRecipe, S extends BuilderBase<U, S>> extends
            ProcessingRecipe.BuilderBase<U, S> {
        protected final List<ResourceLocation> requiredTech = new ArrayList<>();

        public BuilderBase(IRecipeDataConsumer consumer, RecipeTypeEntry<U, S> parent,
                           ResourceLocation loc) {
            super(consumer, parent, loc);
        }

        public S requireTech(ResourceLocation loc) {
            requiredTech.add(loc);
            return self();
        }
    }

    public static class Builder extends BuilderBase<AssemblyRecipe, Builder> {
        public Builder(IRecipeDataConsumer consumer, RecipeTypeEntry<AssemblyRecipe, Builder> parent,
                       ResourceLocation loc) {
            super(consumer, parent, loc);
        }

        @Override
        protected AssemblyRecipe createObject() {
            return new AssemblyRecipe(this);
        }
    }

    public static class Serializer<T extends AssemblyRecipe, B extends BuilderBase<T, B>> extends
            ProcessingRecipe.Serializer<T, B> {
        protected Serializer(RecipeTypeEntry<T, B> type) {
            super(type);
        }

        @Override
        protected B buildFromJson(ResourceLocation loc, JsonObject jo) {
            var builder = super.buildFromJson(loc, jo);
            Streams.stream(GsonHelper.getAsJsonArray(jo, "required_tech"))
                    .map(JsonElement::getAsString)
                    .forEach(s -> builder.requireTech(new ResourceLocation(s)));
            return builder;
        }

        @Override
        public void toJson(JsonObject jo, T recipe) {
            super.toJson(jo, recipe);
            var ja = new JsonArray();
            for (var tech : recipe.requiredTech) {
                ja.add(tech.toString());
            }
            jo.add("required_tech", ja);
        }
    }

    public static final SmartRecipeSerializer.Factory<AssemblyRecipe, Builder> SERIALIZER =
            Serializer::new;
}
