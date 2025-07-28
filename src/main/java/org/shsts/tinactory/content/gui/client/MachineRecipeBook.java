package org.shsts.tinactory.content.gui.client;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.api.tech.ITeamProfile;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.client.RenderUtil;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.core.recipe.ProcessingResults;
import org.shsts.tinactory.core.tech.TechManager;
import org.shsts.tinactory.core.util.ClientUtil;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.shsts.tinactory.Tinactory.CORE;
import static org.shsts.tinactory.content.AllCapabilities.MACHINE;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MachineRecipeBook extends AbstractRecipeBook<ProcessingRecipe> {
    @Nullable
    protected final IRecipeType<?> recipeType;
    private final Consumer<ITeamProfile> onTechChange = $ -> onTechChange();
    private final Layout layout;

    private MachineRecipeBook(ProcessingScreen screen, Layout layout,
        @Nullable IRecipeType<?> recipeType) {
        super(screen, layout.getXOffset());
        this.recipeType = recipeType;
        this.layout = layout;
        TechManager.client().onProgressChange(onTechChange);
    }

    public MachineRecipeBook(ProcessingScreen screen) {
        this(screen, screen.menu().layout(),
            screen.menu().recipeType().orElse(null));
    }

    public void remove() {
        TechManager.client().removeProgressChangeListener(onTechChange);
    }

    @Override
    protected void doRefreshRecipes() {
        if (recipeType == null) {
            return;
        }
        var machine = MACHINE.get(blockEntity);
        for (var recipe : CORE.clientRecipeManager().getRawRecipesFor(recipeType)) {
            if (recipe instanceof ProcessingRecipe processingRecipe &&
                processingRecipe.canCraft(machine)) {
                recipes.put(recipe.loc(), processingRecipe);
            }
        }
    }

    @Override
    protected int compareRecipes(ProcessingRecipe r1, ProcessingRecipe r2) {
        return r1.loc().compareNamespaced(r2.loc());
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
        return recipe.getDescription().map(List::of)
            .or(() -> ProcessingResults.mapItemOrFluid(recipe.getDisplay(),
                ClientUtil::itemTooltip, fluid -> ClientUtil.fluidTooltip(fluid, false)));
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
