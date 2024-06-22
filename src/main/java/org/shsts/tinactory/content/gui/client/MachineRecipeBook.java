package org.shsts.tinactory.content.gui.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.api.tech.ITeamProfile;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.ProcessingMenu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.client.MenuScreen;
import org.shsts.tinactory.core.gui.client.RenderUtil;
import org.shsts.tinactory.core.machine.RecipeProcessor;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.core.tech.TechManager;
import org.shsts.tinactory.core.util.ClientUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MachineRecipeBook extends AbstractRecipeBook<ProcessingRecipe> {
    @Nullable
    protected final RecipeType<? extends ProcessingRecipe> recipeType;
    private final Consumer<ITeamProfile> onTechChange = $ -> onTechChange();
    @Nullable
    private final Layout layout;

    private MachineRecipeBook(MenuScreen<? extends ProcessingMenu> screen,
                              @Nullable RecipeType<? extends ProcessingRecipe> recipeType,
                              @Nullable Layout layout) {
        super(screen, layout == null ? 0 : layout.getXOffset());
        this.recipeType = recipeType;
        this.layout = layout;
        TechManager.client().onProgressChange(onTechChange);
    }

    public MachineRecipeBook(MenuScreen<? extends ProcessingMenu> screen,
                             @Nullable RecipeType<? extends ProcessingRecipe> recipeType) {
        this(screen, recipeType, screen.getMenu().layout);
    }

    public void remove() {
        TechManager.client().removeProgressChangeListener(onTechChange);
    }

    protected long getMachineVoltage() {
        return RecipeProcessor.getBlockVoltage(blockEntity).value;
    }

    @Override
    protected void doRefreshRecipes() {
        if (recipeType == null) {
            return;
        }
        var container = AllCapabilities.CONTAINER.get(blockEntity);
        var voltage = getMachineVoltage();
        for (var recipe : ClientUtil.getRecipeManager().getAllRecipesFor(recipeType)) {
            if (!recipe.canCraftIn(container) || !recipe.canCraftInVoltage(voltage)) {
                continue;
            }
            recipes.put(recipe.getId(), recipe);
        }
    }

    @Override
    protected int compareRecipes(ProcessingRecipe r1, ProcessingRecipe r2) {
        return r1.getId().compareNamespaced(r2.getId());
    }

    @Override
    protected void selectRecipe(ProcessingRecipe recipe) {
        if (layout != null) {
            layout.getProcessingInputs(recipe).forEach(ghostRecipe::addIngredient);
            layout.getProcessingOutputs(recipe).forEach(ghostRecipe::addIngredient);
        }
    }

    @Override
    protected Optional<List<Component>> buttonToolTip(ProcessingRecipe recipe) {
        // TODO
        return Optional.of(List.of(recipe.getDescription()));
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
        buttonPanel.refresh();
    }
}
