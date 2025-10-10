package org.shsts.tinactory.core.recipe;

import com.google.common.collect.Streams;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.conditions.ICondition;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.recipe.IProcessingIngredient;
import org.shsts.tinactory.api.tech.IServerTeamProfile;
import org.shsts.tinactory.api.tech.ITeamProfile;
import org.shsts.tinactory.api.tech.ITechManager;
import org.shsts.tinactory.core.tech.TechManager;
import org.shsts.tinycorelib.api.recipe.IRecipeSerializer;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.util.Optional;
import java.util.Random;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ResearchRecipe extends ProcessingRecipe {
    public final ResourceLocation target;
    public final long progress;

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
    protected boolean matchTeam(Optional<ITeamProfile> team) {
        return team.filter(this::canResearch).isPresent();
    }

    private boolean matchOutputs(ITechManager techManager, Optional<ITeamProfile> team, int parallel) {
        if (parallel <= 1) {
            return true;
        }
        return team.flatMap(team1 -> techManager
                .techByKey(target)
                .map(tech -> team1.getTechProgress(tech) + progress * parallel <= tech.getMaxProgress()))
            .orElse(false);
    }

    @Override
    public boolean matches(IMachine machine, Level world, int parallel) {
        var container = machine.container();
        var manager = TechManager.get(world);
        return canCraft(machine) && matchOutputs(manager, machine.owner(), parallel) && container
            .filter($ -> matchInputs($, parallel))
            .isPresent();
    }

    @Override
    public void insertOutputs(IMachine machine, int parallel, Random random) {
        machine.owner()
            .ifPresent(team -> ((IServerTeamProfile) team).advanceTechProgress(target, progress * parallel));
    }

    public static class Builder extends ProcessingRecipe.BuilderBase<ResearchRecipe, Builder> {
        @Nullable
        private ResourceLocation target = null;
        private long progress = 1;

        public Builder(IRecipeType<Builder> parent, ResourceLocation loc) {
            super(parent, loc);
        }

        public Builder input(IProcessingIngredient ingredient) {
            var port = ingredient.type() == PortType.ITEM ? 0 : 1;
            return input(port, ingredient);
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
        protected void validate() {
            assert power > 0 : loc;
            assert workTicks > 0 : loc;
        }

        @Override
        protected ResearchRecipe createObject() {
            return new ResearchRecipe(this);
        }
    }

    private static class Serializer implements IRecipeSerializer<ResearchRecipe, Builder> {
        @Override
        public ResearchRecipe fromJson(IRecipeType<Builder> type, ResourceLocation loc, JsonObject jo,
            ICondition.IContext context) {
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

    public static final IRecipeSerializer<ResearchRecipe, Builder> SERIALIZER = new Serializer();
}
