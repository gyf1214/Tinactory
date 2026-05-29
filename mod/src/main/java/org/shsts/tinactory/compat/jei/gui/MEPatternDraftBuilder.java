package org.shsts.tinactory.compat.jei.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.content.gui.client.MEPatternDraft;
import org.shsts.tinactory.content.gui.client.MEPatternIngredientDraft;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.core.recipe.StackIngredient;
import org.shsts.tinactory.core.recipe.StackResult;
import org.shsts.tinactory.integration.logistics.StackHelper;
import org.shsts.tinactory.integration.recipe.ItemsIngredient;
import org.shsts.tinactory.integration.util.ClientUtil;

import java.util.Optional;

import static org.shsts.tinactory.core.util.LocHelper.mcLoc;
import static org.shsts.tinactory.integration.logistics.StackHelper.FLUID_ADAPTER;
import static org.shsts.tinactory.integration.logistics.StackHelper.ITEM_ADAPTER;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class MEPatternDraftBuilder {
    private static final ResourceLocation SMELTING_RECIPE_TYPE = mcLoc("smelting");

    private MEPatternDraftBuilder() {}

    public static Optional<MEPatternDraft> fromProcessing(ProcessingRecipe recipe, ResourceLocation recipeTypeId) {
        var ret = MEPatternDraft.empty();
        ret.setRecipeTypeId(recipeTypeId);
        ret.setTargetRecipeId(recipe.loc());
        ret.setVoltageTier(Voltage.fromValue(recipe.voltage).rank);

        for (var input : recipe.inputs) {
            input(input).ifPresent(row -> {
                row.setPort(input.port());
                ret.inputRows().add(row);
            });
        }
        for (var output : recipe.outputs) {
            output(output).ifPresent(row -> {
                row.setPort(output.port());
                ret.outputRows().add(row);
            });
        }
        return ret.outputRows().isEmpty() ? Optional.empty() : Optional.of(ret);
    }

    public static Optional<MEPatternDraft> fromSmelting(SmeltingRecipe recipe) {
        if (recipe.getIngredients().isEmpty()) {
            return Optional.empty();
        }
        var input = ClientUtil.selectItemFromItems(recipe.getIngredients().get(0));
        var output = recipe.getResultItem();
        if (input.isEmpty() || output.isEmpty()) {
            return Optional.empty();
        }
        var ret = MEPatternDraft.empty();
        ret.setRecipeTypeId(SMELTING_RECIPE_TYPE);
        ret.setTargetRecipeId(recipe.getId());
        ret.setVoltageTier(Voltage.ULV.rank);
        var inputRow = MEPatternIngredientDraft.from(ITEM_ADAPTER, StackHelper.copyWithCount(input.get(), 1));
        inputRow.setPort(0);
        ret.inputRows().add(inputRow);
        var outputRow = MEPatternIngredientDraft.from(ITEM_ADAPTER, output);
        outputRow.setPort(1);
        ret.outputRows().add(outputRow);
        return Optional.of(ret);
    }

    private static Optional<MEPatternIngredientDraft> input(ProcessingRecipe.Input input) {
        var ingredient = input.ingredient();
        if (ingredient instanceof StackIngredient<?> stackIngredient && stackIngredient.type() == PortType.ITEM) {
            return Optional.of(MEPatternIngredientDraft.from(ITEM_ADAPTER, (ItemStack) stackIngredient.stack()));
        } else if (ingredient instanceof StackIngredient<?> stackIngredient &&
            stackIngredient.type() == PortType.FLUID) {
            return Optional.of(MEPatternIngredientDraft.from(FLUID_ADAPTER, (FluidStack) stackIngredient.stack()));
        } else if (ingredient instanceof ItemsIngredient item && item.amount > 0) {
            return ClientUtil.selectItemFromItems(item.ingredient)
                .map(stack -> MEPatternIngredientDraft.from(
                    ITEM_ADAPTER, StackHelper.copyWithCount(stack, item.amount)));
        }
        return Optional.empty();
    }

    private static Optional<MEPatternIngredientDraft> output(ProcessingRecipe.Output output) {
        var result = output.result();
        if (result instanceof StackResult<?> stackResult && stackResult.type() == PortType.ITEM) {
            var stack = (ItemStack) stackResult.stack();
            return stack.isEmpty() ? Optional.empty() :
                Optional.of(MEPatternIngredientDraft.from(ITEM_ADAPTER, stack));
        } else if (result instanceof StackResult<?> stackResult && stackResult.type() == PortType.FLUID) {
            var stack = (FluidStack) stackResult.stack();
            return stack.isEmpty() ? Optional.empty() :
                Optional.of(MEPatternIngredientDraft.from(FLUID_ADAPTER, stack));
        }
        return Optional.empty();
    }
}
