package org.shsts.tinactory.content.gui.client;

import com.mojang.blaze3d.MethodsReturnNonnullByDefault;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.tech.ITeamProfile;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.gui.sync.SetMachinePacket;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.client.MenuScreen;
import org.shsts.tinactory.core.gui.client.RenderUtil;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.core.tech.TechManager;
import org.shsts.tinactory.core.util.ClientUtil;
import org.shsts.tinactory.core.util.I18n;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.shsts.tinactory.core.gui.sync.MenuEventHandler.SET_MACHINE;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ProcessingRecipeBook extends MachineRecipeBook<ProcessingRecipe> {
    private final RecipeType<? extends ProcessingRecipe> recipeType;
    private final Consumer<ITeamProfile> onTechChange = $ -> onTechChange();

    public ProcessingRecipeBook(MenuScreen<? extends Menu<?, ?>> screen,
                                RecipeType<? extends ProcessingRecipe> recipeType,
                                int buttonX, int buttonY) {
        super(screen, buttonX, buttonY);
        this.recipeType = recipeType;
        TechManager.client().onProgressChange(onTechChange);
    }

    public void remove() {
        TechManager.client().removeProgressChangeListener(onTechChange);
    }

    @Override
    protected void refreshRecipes() {
        recipes.clear();
        var be = screen.getMenu().blockEntity;
        var container = AllCapabilities.CONTAINER.get(be);
        var voltage = (long) AllCapabilities.ELECTRIC_MACHINE.tryGet(be)
                .map(IElectricMachine::getVoltage)
                .orElse(0L);
        for (var recipe : ClientUtil.getRecipeManager().getAllRecipesFor(recipeType)) {
            if (!recipe.canCraftIn(container) || !recipe.canCraftInVoltage(voltage)) {
                continue;
            }
            recipes.add(recipe);
        }
    }

    @Override
    protected boolean isCurrentRecipe(@Nullable ProcessingRecipe recipe) {
        var curRecipe = AllCapabilities.MACHINE.get(menu.blockEntity)
                .getTargetRecipe().orElse(null);
        return curRecipe == recipe;
    }

    @Override
    protected void selectRecipe(@Nullable ProcessingRecipe recipe) {
        if (recipe == null) {
            menu.triggerEvent(SET_MACHINE, SetMachinePacket.builder().reset("targetRecipe"));
        } else {
            menu.triggerEvent(SET_MACHINE, SetMachinePacket.builder().set("targetRecipe", recipe.getId()));
        }
    }

    @Override
    protected Optional<List<Component>> buttonToolTip(ProcessingRecipe recipe) {
        // TODO
        return Optional.of(List.of(I18n.raw(recipe.getId().toString())));
    }

    @Override
    protected void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick,
                                ProcessingRecipe recipe, Rect rect, int z) {
        var x = rect.x() + 2;
        var y = rect.y() + 2;
        var output = recipe.getDisplay();
        RenderUtil.renderIngredient(output,
                stack -> RenderUtil.renderItem(stack, x, y),
                stack -> RenderUtil.renderFluid(poseStack, stack, x, y, z));
    }

    private void onTechChange() {
        refreshRecipes();
        if (bookPanel.isActive()) {
            setPage(page);
        }
    }
}
