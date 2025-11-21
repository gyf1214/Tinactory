package org.shsts.tinactory.integration.jei;

import javax.annotation.ParametersAreNonnullByDefault;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import org.shsts.tinactory.content.AllTags;
import org.shsts.tinactory.content.gui.client.ProcessingScreen;
import org.shsts.tinactory.content.gui.client.ResearchBenchScreen;
import org.shsts.tinactory.content.gui.client.TechScreen;
import org.shsts.tinactory.content.recipe.BlastFurnaceRecipe;
import org.shsts.tinactory.content.recipe.ChemicalReactorRecipe;
import org.shsts.tinactory.content.recipe.CleanRecipe;
import org.shsts.tinactory.content.recipe.DistillationRecipe;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.recipe.AssemblyRecipe;
import org.shsts.tinactory.core.recipe.ResearchRecipe;
import org.shsts.tinactory.integration.jei.category.AssemblyCategory;
import org.shsts.tinactory.integration.jei.category.BlastFurnaceCategory;
import org.shsts.tinactory.integration.jei.category.ChemicalReactorCategory;
import org.shsts.tinactory.integration.jei.category.CleanCategory;
import org.shsts.tinactory.integration.jei.category.DistillationCategory;
import org.shsts.tinactory.integration.jei.category.ProcessingCategory;
import org.shsts.tinactory.integration.jei.category.RecipeCategory;
import org.shsts.tinactory.integration.jei.category.ResearchCategory;
import org.shsts.tinactory.integration.jei.category.ToolCategory;
import org.shsts.tinactory.integration.jei.gui.FluidScreenHandler;
import org.shsts.tinactory.integration.jei.gui.ProcessingHandler;
import org.shsts.tinactory.integration.jei.gui.ResearchHandler;
import org.shsts.tinactory.integration.jei.gui.TechMenuHandler;
import org.shsts.tinactory.integration.jei.gui.WorkbenchHandler;
import org.shsts.tinactory.integration.jei.ingredient.IngredientRenderers;
import org.shsts.tinactory.integration.jei.ingredient.RecipeMarker;
import org.shsts.tinactory.integration.jei.ingredient.TechIngredient;
import org.shsts.tinycorelib.api.recipe.IRecipeBuilderBase;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.shsts.tinactory.Tinactory.CORE;
import static org.shsts.tinactory.content.AllRecipes.PROCESSING_TYPES;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;

@JeiPlugin
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class JEI implements IModPlugin {
    private static final ResourceLocation LOC = modLoc("jei");

    private final ToolCategory toolCategory;
    private final List<RecipeCategory<?>> categories;

    public JEI() {
        this.categories = new ArrayList<>();

        this.toolCategory = new ToolCategory();
        categories.add(toolCategory);

        for (var type : PROCESSING_TYPES.values()) {
            addProcessingCategory(type.recipeType(), type.layout(), type.icon().get());
        }
    }

    @SuppressWarnings("unchecked")
    private static <A extends IRecipeBuilderBase<?>,
        B extends IRecipeBuilderBase<?>> IRecipeType<B> cast(IRecipeType<A> type) {
        return (IRecipeType<B>) type;
    }

    private ProcessingCategory<?> processingCategory(IRecipeType<?> type, Layout layout, Block icon) {
        var clazz = type.recipeClass();
        if (ResearchRecipe.class.isAssignableFrom(clazz)) {
            return new ResearchCategory(cast(type), layout, icon);
        } else if (ChemicalReactorRecipe.class.isAssignableFrom(clazz)) {
            return new ChemicalReactorCategory(cast(type), layout, icon);
        } else if (AssemblyRecipe.class.isAssignableFrom(clazz)) {
            return new AssemblyCategory<>(cast(type), layout, icon);
        } else if (CleanRecipe.class.isAssignableFrom(clazz)) {
            return new CleanCategory(cast(type), layout, icon);
        } else if (BlastFurnaceRecipe.class.isAssignableFrom(clazz)) {
            return new BlastFurnaceCategory(cast(type), layout, icon);
        } else if (DistillationRecipe.class.isAssignableFrom(clazz)) {
            return new DistillationCategory(cast(type), layout, icon);
        } else {
            return new ProcessingCategory<>(cast(type), layout, icon);
        }
    }

    private void addProcessingCategory(IRecipeType<?> type, Layout layout, Block icon) {
        categories.add(processingCategory(type, layout, icon));
    }

    @Override
    public ResourceLocation getPluginUid() {
        return LOC;
    }

    @Override
    public void registerIngredients(IModIngredientRegistration registration) {
        registration.register(TechIngredient.TYPE, Collections.emptyList(),
            TechIngredient.HELPER, IngredientRenderers.empty());
        registration.register(RecipeMarker.TYPE, Collections.emptyList(),
            RecipeMarker.HELPER, IngredientRenderers.empty());
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        for (var category : categories) {
            category.registerCategory(registration);
        }
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        var recipeManager = CORE.clientRecipeManager();
        for (var category : categories) {
            category.registerRecipes(registration, recipeManager);
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        for (var category : categories) {
            category.registerCatalysts(registration);
        }
        for (var itemStack : Ingredient.of(AllTags.ELECTRIC_FURNACE).getItems()) {
            registration.addRecipeCatalyst(VanillaTypes.ITEM_STACK, itemStack, RecipeTypes.SMELTING);
        }
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGenericGuiContainerHandler(AbstractContainerScreen.class, new FluidScreenHandler());
        registration.addGuiContainerHandler(TechScreen.class, new TechMenuHandler());
        registration.addGuiContainerHandler(ProcessingScreen.class, new ProcessingHandler());
        registration.addGuiContainerHandler(ResearchBenchScreen.class, new ResearchHandler());
        WorkbenchHandler.addWorkbenchClickArea(registration, toolCategory);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        // TODO
    }
}
