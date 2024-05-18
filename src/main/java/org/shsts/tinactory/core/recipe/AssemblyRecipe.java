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
import org.shsts.tinactory.core.tech.TechManager;
import org.shsts.tinactory.core.tech.Technology;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.common.RecipeTypeEntry;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AssemblyRecipe extends ProcessingRecipe<AssemblyRecipe> {
    private final List<Technology> requiredTech;

    public AssemblyRecipe(RecipeTypeEntry<AssemblyRecipe, ?> type, ResourceLocation loc,
                          List<Input> inputs, List<Output> outputs, long workTicks,
                          long voltage, long power, List<Technology> requiredTech) {
        super(type, loc, inputs, outputs, workTicks, voltage, power);
        this.requiredTech = requiredTech;
    }

    @Override
    public boolean canCraftIn(IContainer container) {
        return container.getOwnerTeam()
                .map(team -> {
                    for (var tech : requiredTech) {
                        if (!team.isTechFinished(tech)) {
                            return false;
                        }
                    }
                    return true;
                }).orElse(false);
    }

    public static class Builder extends ProcessingRecipe.Builder<AssemblyRecipe, Builder> {
        private final List<ResourceLocation> requiredTech = new ArrayList<>();

        public Builder(Registrate registrate, RecipeTypeEntry<AssemblyRecipe, Builder> parent,
                       ResourceLocation loc) {
            super(registrate, parent, loc);
        }

        private List<Technology> getRequiredTech() {
            return requiredTech.stream()
                    .flatMap(loc -> TechManager.INSTANCE.techByKey(loc).stream())
                    .toList();
        }

        public Builder requireTech(ResourceLocation loc) {
            requiredTech.add(loc);
            return self();
        }

        @Override
        public AssemblyRecipe createObject() {
            return new AssemblyRecipe(parent, loc, getInputs(), getOutputs(), workTicks, voltage, power,
                    getRequiredTech());
        }
    }

    public static class Serializer extends ProcessingRecipe.Serializer<AssemblyRecipe, Builder> {
        protected Serializer(RecipeTypeEntry<AssemblyRecipe, Builder> type) {
            super(type);
        }

        @Override
        protected Builder buildFromJson(ResourceLocation loc, JsonObject jo) {
            var builder = super.buildFromJson(loc, jo);
            Streams.stream(GsonHelper.getAsJsonArray(jo, "required_tech"))
                    .map(JsonElement::getAsString)
                    .forEach(s -> builder.requireTech(new ResourceLocation(s)));
            return builder;
        }

        @Override
        public void toJson(JsonObject jo, AssemblyRecipe recipe) {
            super.toJson(jo, recipe);
            var ja = new JsonArray();
            for (var tech : recipe.requiredTech) {
                var loc = tech.getRegistryName();
                assert loc != null;
                ja.add(loc.toString());
            }
            jo.add("required_tech", ja);
        }
    }

    public static final SmartRecipeSerializer.Factory<AssemblyRecipe, Builder> SERIALIZER =
            Serializer::new;
}
