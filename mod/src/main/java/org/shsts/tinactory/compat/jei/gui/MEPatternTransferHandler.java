package org.shsts.tinactory.compat.jei.gui;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.neoforged.neoforge.fluids.FluidStack;
import org.shsts.tinactory.AllMenus;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.content.gui.MEPatternTerminalMenu;
import org.shsts.tinactory.content.gui.client.MEPatternDraft;
import org.shsts.tinactory.content.gui.client.MEPatternIngredientDraft;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.core.recipe.StackIngredient;
import org.shsts.tinactory.core.recipe.StackResult;
import org.shsts.tinactory.integration.logistics.StackHelper;
import org.shsts.tinactory.integration.recipe.ItemsIngredient;
import org.shsts.tinactory.integration.util.ClientUtil;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

import java.util.Optional;
import java.util.function.Function;

import static org.shsts.tinactory.core.util.LocHelper.mcLoc;
import static org.shsts.tinactory.integration.logistics.StackHelper.FLUID_ADAPTER;
import static org.shsts.tinactory.integration.logistics.StackHelper.ITEM_ADAPTER;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class MEPatternTransferHandler<R> implements IRecipeTransferHandler<MEPatternTerminalMenu, R> {
    private static final ResourceLocation SMELTING_RECIPE_TYPE = mcLoc("smelting");

    private final RecipeType<R> recipeType;
    private final Function<R, Optional<MEPatternDraft>> converter;
    private final IRecipeTransferHandlerHelper helper;

    public MEPatternTransferHandler(RecipeType<R> recipeType, Function<R, Optional<MEPatternDraft>> converter,
        IRecipeTransferHandlerHelper helper) {
        this.recipeType = recipeType;
        this.converter = converter;
        this.helper = helper;
    }

    @Override
    public Class<MEPatternTerminalMenu> getContainerClass() {
        return MEPatternTerminalMenu.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<MenuType<MEPatternTerminalMenu>> getMenuType() {
        return Optional.of((MenuType<MEPatternTerminalMenu>) AllMenus.ME_PATTERN_TERMINAL.get());
    }

    @Override
    public RecipeType<R> getRecipeType() {
        return recipeType;
    }

    @Override
    @Nullable
    public IRecipeTransferError transferRecipe(MEPatternTerminalMenu container, R recipe,
        IRecipeSlotsView recipeSlotsView, Player player, boolean maxTransfer, boolean doTransfer) {
        var draft = converter.apply(recipe);
        if (draft.isEmpty()) {
            return helper.createInternalError();
        }
        if (!container.importRecipeDraft(draft.get(), doTransfer)) {
            return helper.createInternalError();
        }
        return null;
    }

    public static Optional<MEPatternDraft> fromProcessing(IEntry<? extends ProcessingRecipe> entry,
        ResourceLocation recipeTypeId) {
        var recipe = entry.get();
        var ret = MEPatternDraft.empty();
        ret.setRecipeTypeId(recipeTypeId);
        ret.setTargetRecipeId(entry.loc());
        var rank = Voltage.fromValue(recipe.voltage).rank;
        if (rank > 0) {
            ret.setVoltageTier(rank);
        }

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

    public static Optional<MEPatternDraft> fromSmelting(RecipeHolder<SmeltingRecipe> entry) {
        var recipe = entry.value();
        if (recipe.getIngredients().isEmpty()) {
            return Optional.empty();
        }
        var input = ClientUtil.selectItemFromItems(recipe.getIngredients().getFirst());
        var output = recipe.getResultItem(ClientUtil.registryAccess());
        if (input.isEmpty() || output.isEmpty()) {
            return Optional.empty();
        }
        var ret = MEPatternDraft.empty();
        ret.setRecipeTypeId(SMELTING_RECIPE_TYPE);
        ret.setTargetRecipeId(entry.id());
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
        return switch (ingredient) {
            case StackIngredient<?> stackIngredient when stackIngredient.type() == PortType.ITEM ->
                Optional.of(MEPatternIngredientDraft.from(ITEM_ADAPTER, (ItemStack) stackIngredient.stack()));
            case StackIngredient<?> stackIngredient when stackIngredient.type() == PortType.FLUID ->
                Optional.of(MEPatternIngredientDraft.from(FLUID_ADAPTER, (FluidStack) stackIngredient.stack()));
            case ItemsIngredient item when item.amount > 0 -> ClientUtil.selectItemFromItems(item.ingredient)
                .map(stack -> MEPatternIngredientDraft.from(ITEM_ADAPTER,
                    StackHelper.copyWithCount(stack, item.amount)));
            default -> Optional.empty();
        };
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
