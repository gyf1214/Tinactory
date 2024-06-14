package org.shsts.tinactory.core.recipe;

import com.google.common.collect.Streams;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.crafting.conditions.ICondition;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.recipe.IProcessingIngredient;
import org.shsts.tinactory.api.tech.IServerTeamProfile;
import org.shsts.tinactory.api.tech.ITeamProfile;
import org.shsts.tinactory.content.AllItems;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.core.common.SmartRecipeSerializer;
import org.shsts.tinactory.registrate.common.RecipeTypeEntry;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Random;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ResearchRecipe extends ProcessingRecipe {
    private final ResourceLocation target;
    private final long progress;

    private ResearchRecipe(Builder builder) {
        super(builder);
        this.target = builder.getTarget();
        this.progress = builder.progress;
    }

    private boolean canResearch(ITeamProfile team) {
        return team.canResearch(target) && team.getTargetTech()
                .filter(tech -> tech.getLoc().equals(target))
                .isPresent();
    }

    @Override
    public boolean canCraftIn(IContainer container) {
        return container.getOwnerTeam()
                .map(this::canResearch)
                .orElse(false);
    }

    @Override
    public void insertOutputs(IContainer container, Random random) {
        container.getOwnerTeam()
                .ifPresent(team -> ((IServerTeamProfile) team).advanceTechProgress(target, progress));
    }

    public static class Builder extends ProcessingRecipe.BuilderBase<ResearchRecipe, Builder> {
        @Nullable
        private ResourceLocation target = null;
        private long progress = 1;

        public Builder(IRecipeDataConsumer consumer, RecipeTypeEntry<ResearchRecipe, Builder> parent,
                       ResourceLocation loc) {
            super(consumer, parent, loc);
        }

        public Builder input(IProcessingIngredient ingredient) {
            var port = ingredient.type() == PortType.ITEM ? 0 : 1;
            return input(port, ingredient);
        }

        public Builder inputItem(Supplier<? extends Item> item, int amount) {
            return inputItem(0, item, amount);
        }

        public Builder inputItem(Supplier<? extends Item> item) {
            return inputItem(item, 1);
        }

        public Builder inputItem(TagKey<Item> item) {
            return inputItem(0, item, 1);
        }

        public Builder defaultInput(Voltage voltage) {
            return voltage(voltage).inputItem(AllItems.researchEquipment(voltage));
        }

        public Builder target(ResourceLocation value) {
            target = value;
            return this;
        }

        public Builder progress(long value) {
            progress = value;
            return this;
        }

        private ResourceLocation getTarget() {
            assert target != null;
            return target;
        }

        @Override
        protected ResearchRecipe createObject() {
            return new ResearchRecipe(this);
        }
    }

    public static class Serializer extends SmartRecipeSerializer<ResearchRecipe, Builder> {
        protected Serializer(RecipeTypeEntry<ResearchRecipe, Builder> type) {
            super(type);
        }

        @Override
        public ResearchRecipe fromJson(ResourceLocation loc, JsonObject jo, ICondition.IContext context) {
            var builder = type.getBuilder(loc);
            Streams.stream(GsonHelper.getAsJsonArray(jo, "inputs"))
                    .map(je -> ProcessingIngredients.fromJson(je.getAsJsonObject()))
                    .forEach(builder::input);
            return builder.target(new ResourceLocation(GsonHelper.getAsString(jo, "target")))
                    .progress(GsonHelper.getAsLong(jo, "progress"))
                    .workTicks(GsonHelper.getAsLong(jo, "work_ticks"))
                    .voltage(GsonHelper.getAsLong(jo, "voltage"))
                    .power(GsonHelper.getAsLong(jo, "power"))
                    .buildObject();
        }

        @Override
        public void toJson(JsonObject jo, ResearchRecipe recipe) {
            var inputs = new JsonArray();
            recipe.inputs.stream()
                    .map(input -> ProcessingIngredients.toJson(input.ingredient()))
                    .forEach(inputs::add);
            jo.add("inputs", inputs);
            jo.addProperty("target", recipe.target.toString());
            jo.addProperty("progress", recipe.progress);
            jo.addProperty("work_ticks", recipe.workTicks);
            jo.addProperty("voltage", recipe.voltage);
            jo.addProperty("power", recipe.power);
        }
    }

    public static final SmartRecipeSerializer.Factory<ResearchRecipe, ResearchRecipe.Builder>
            SERIALIZER = ResearchRecipe.Serializer::new;
}
