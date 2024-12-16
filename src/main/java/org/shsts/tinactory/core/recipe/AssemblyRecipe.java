package org.shsts.tinactory.core.recipe;

import com.google.common.collect.Streams;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.shsts.tinactory.api.tech.ITeamProfile;
import org.shsts.tinycorelib.api.recipe.IRecipeSerializer;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AssemblyRecipe extends ProcessingRecipe {
    public final List<ResourceLocation> requiredTech;

    protected AssemblyRecipe(BuilderBase<?, ?> builder) {
        super(builder);
        this.requiredTech = builder.requiredTech;
    }

    @Override
    protected boolean matchTeam(Optional<ITeamProfile> team) {
        return team.map($ -> requiredTech.stream().allMatch($::isTechFinished))
            .orElse(requiredTech.isEmpty());
    }

    protected abstract static class BuilderBase<U extends AssemblyRecipe, S extends BuilderBase<U, S>> extends
        ProcessingRecipe.BuilderBase<U, S> {
        protected final List<ResourceLocation> requiredTech = new ArrayList<>();

        protected BuilderBase(IRecipeType<S> parent, ResourceLocation loc) {
            super(parent, loc);
        }

        public S requireTech(ResourceLocation... loc) {
            requiredTech.addAll(Arrays.asList(loc));
            return self();
        }
    }

    public static class Builder extends BuilderBase<AssemblyRecipe, Builder> {
        public Builder(IRecipeType<Builder> parent, ResourceLocation loc) {
            super(parent, loc);
        }

        @Override
        protected AssemblyRecipe createObject() {
            return new AssemblyRecipe(this);
        }
    }

    protected static class Serializer<T extends AssemblyRecipe, B extends BuilderBase<T, B>> extends
        ProcessingRecipe.Serializer<T, B> {
        @Override
        protected B buildFromJson(IRecipeType<B> type, ResourceLocation loc, JsonObject jo) {
            var builder = super.buildFromJson(type, loc, jo);
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

    public static final IRecipeSerializer<AssemblyRecipe, Builder> SERIALIZER = new Serializer<>();
}
