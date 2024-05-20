package org.shsts.tinactory.core.recipe;

import com.google.common.collect.Streams;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.conditions.ICondition;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.recipe.IProcessingIngredient;
import org.shsts.tinactory.content.machine.Voltage;
import org.shsts.tinactory.core.common.SmartRecipeSerializer;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.builder.SmartRecipeBuilder;
import org.shsts.tinactory.registrate.common.RecipeTypeEntry;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ResearchRecipe extends ProcessingRecipe {
    private final ResourceLocation target;
    private final long progress;

    private static List<Input> getInputs(List<IProcessingIngredient> ingredients) {
        return ingredients.stream()
                .map(ingredient -> new Input(ingredient.type() == PortType.ITEM ? 0 : 1, ingredient))
                .toList();
    }

    private ResearchRecipe(RecipeTypeEntry<?, ?> type, ResourceLocation loc,
                           List<IProcessingIngredient> inputs,
                           ResourceLocation target, long progress,
                           long workTicks, long voltage, long power) {
        super(type, loc, getInputs(inputs), List.of(), workTicks, voltage, power);
        this.target = target;
        this.progress = progress;
    }

    @Override
    public boolean canCraftIn(IContainer container) {
        return container.getOwnerTeam()
                .map(team -> team.isTechAvailable(target) && !team.isTechFinished(target))
                .orElse(false);
    }

    public static class Builder extends SmartRecipeBuilder<ResearchRecipe, Builder> {
        private final List<Supplier<IProcessingIngredient>> inputs = new ArrayList<>();
        @Nullable
        private ResourceLocation target = null;
        private long progress = 1;
        private long workTicks = 0;
        private long voltage = 0;
        private long power = 0;
        private float amperage = 0f;

        public Builder(Registrate registrate, RecipeTypeEntry<ResearchRecipe, Builder> parent,
                       ResourceLocation loc) {
            super(registrate, parent, loc);
        }

        public Builder input(IProcessingIngredient ingredient) {
            inputs.add(() -> ingredient);
            return this;
        }

        public Builder inputItem(Supplier<? extends Item> item, int amount) {
            inputs.add(() -> new ProcessingIngredients.SimpleItemIngredient(
                    new ItemStack(item.get(), amount)));
            return this;
        }

        public Builder inputItem(Supplier<? extends Item> item) {
            return inputItem(item, 1);
        }

        public Builder target(ResourceLocation value) {
            target = value;
            return this;
        }

        public Builder progress(long value) {
            progress = value;
            return this;
        }

        public Builder workTicks(long value) {
            workTicks = value;
            return this;
        }

        public Builder voltage(Voltage value) {
            voltage = value.value;
            return this;
        }

        public Builder voltage(long value) {
            voltage = value;
            return this;
        }

        public Builder power(long value) {
            power = value;
            return this;
        }

        public Builder amperage(float value) {
            amperage = value;
            return this;
        }

        @Override
        public ResearchRecipe createObject() {
            assert target != null;
            if (power <= 0) {
                power = (long) (voltage * amperage);
            }
            var inputs = this.inputs.stream().map(Supplier::get).toList();
            return new ResearchRecipe(parent, loc, inputs, target, progress, workTicks, voltage, power);
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
                    .map(je -> ProcessingIngredients.SERIALIZER.fromJson(je.getAsJsonObject()))
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
                    .map(input -> ProcessingIngredients.SERIALIZER.toJson(input.ingredient()))
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