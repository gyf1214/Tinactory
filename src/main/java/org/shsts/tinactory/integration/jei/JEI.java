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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import org.shsts.tinactory.content.AllBlockEntities;
import org.shsts.tinactory.content.AllLayouts;
import org.shsts.tinactory.content.AllMultiblocks;
import org.shsts.tinactory.content.AllRecipes;
import org.shsts.tinactory.content.AllTags;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.content.gui.client.NetworkControllerScreen;
import org.shsts.tinactory.content.gui.client.ProcessingScreen;
import org.shsts.tinactory.content.gui.client.ResearchBenchScreen;
import org.shsts.tinactory.content.recipe.CleanRecipe;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.client.MenuScreen;
import org.shsts.tinactory.core.recipe.AssemblyRecipe;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.integration.jei.category.AssemblyCategory;
import org.shsts.tinactory.integration.jei.category.BlastFurnaceCategory;
import org.shsts.tinactory.integration.jei.category.ChemicalReactorCategory;
import org.shsts.tinactory.integration.jei.category.CleanCategory;
import org.shsts.tinactory.integration.jei.category.DistillationCategory;
import org.shsts.tinactory.integration.jei.category.ProcessingCategory;
import org.shsts.tinactory.integration.jei.category.RecipeCategory;
import org.shsts.tinactory.integration.jei.category.ResearchCategory;
import org.shsts.tinactory.integration.jei.category.ToolCategory;
import org.shsts.tinactory.integration.jei.gui.MenuScreenHandler;
import org.shsts.tinactory.integration.jei.gui.NetworkControllerHandler;
import org.shsts.tinactory.integration.jei.gui.ProcessingHandler;
import org.shsts.tinactory.integration.jei.gui.ResearchBenchHandler;
import org.shsts.tinactory.integration.jei.gui.WorkbenchHandler;
import org.shsts.tinactory.integration.jei.ingredient.IngredientRenderers;
import org.shsts.tinactory.integration.jei.ingredient.TechIngredientHelper;
import org.shsts.tinactory.integration.jei.ingredient.TechIngredientType;
import org.shsts.tinycorelib.api.recipe.IRecipeBuilderBase;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.shsts.tinactory.Tinactory.CORE;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;

@JeiPlugin
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class JEI implements IModPlugin {
    private static final ResourceLocation LOC = modLoc("jei");

    public final ToolCategory toolCategory;

    private final List<RecipeCategory<?>> categories;
    private final Map<IRecipeType<?>, RecipeCategory<?>> processingCategories;

    public JEI() {
        this.categories = new ArrayList<>();
        this.processingCategories = new HashMap<>();

        this.toolCategory = new ToolCategory();
        for (var set : AllBlockEntities.getProcessingSets()) {
            var type = set.recipeType;
            var icon = set.icon();
            var layout = type == AllRecipes.CHEMICAL_REACTOR ? AllLayouts.LARGE_CHEMICAL_REACTOR :
                set.layout(Voltage.MAXIMUM);

            if (type == AllRecipes.RESEARCH_BENCH) {
                addProcessingCategory(cast(type), new ResearchCategory(layout, icon));
            } else if (type == AllRecipes.CHEMICAL_REACTOR) {
                addProcessingCategory(cast(type), new ChemicalReactorCategory(cast(type), layout, icon));
            } else if (AssemblyRecipe.class.isAssignableFrom(type.recipeClass())) {
                addProcessingCategory(cast(type), new AssemblyCategory<>(cast(type), layout, icon));
            } else if (CleanRecipe.class.isAssignableFrom(type.recipeClass())) {
                addProcessingCategory(cast(type), new CleanCategory(cast(type), layout, icon));
            } else {
                addProcessingCategory(cast(type), layout, icon);
            }
        }
        addProcessingCategory(AllRecipes.BLAST_FURNACE, new BlastFurnaceCategory());
        addProcessingCategory(AllRecipes.SIFTER, AllLayouts.SIFTER, AllMultiblocks.SIFTER.get());
        addProcessingCategory(AllRecipes.VACUUM_FREEZER, AllLayouts.VACUUM_FREEZER,
            AllMultiblocks.VACUUM_FREEZER.get());
        addProcessingCategory(AllRecipes.DISTILLATION, new DistillationCategory());
        addProcessingCategory(AllRecipes.AUTOFARM, AllLayouts.AUTOFARM,
            AllMultiblocks.AUTOFARM.get());
        addProcessingCategory(AllRecipes.PYROLYSE_OVEN, AllLayouts.PYROLYSE_OVEN,
            AllMultiblocks.PYROLYSE_OVEN.get());
    }

    @SuppressWarnings("unchecked")
    private static <A extends IRecipeBuilderBase<?>,
        B extends IRecipeBuilderBase<?>> IRecipeType<B> cast(IRecipeType<A> type) {
        return (IRecipeType<B>) type;
    }

    private <R extends ProcessingRecipe, B extends IRecipeBuilderBase<R>> void addProcessingCategory(
        IRecipeType<B> recipeType, ProcessingCategory<R> category) {
        categories.add(category);
        processingCategories.put(recipeType, category);
    }

    private <R extends ProcessingRecipe, B extends IRecipeBuilderBase<R>> void addProcessingCategory(
        IRecipeType<B> recipeType, Layout layout, Block icon) {
        var category = new ProcessingCategory<>(recipeType, layout, icon);
        addProcessingCategory(recipeType, category);
    }

    public Optional<RecipeCategory<?>> processingCategory(IRecipeType<?> recipeType) {
        return Optional.ofNullable(processingCategories.get(recipeType));
    }

    @Override
    public ResourceLocation getPluginUid() {
        return LOC;
    }

    @Override
    public void registerIngredients(IModIngredientRegistration registration) {
        registration.register(TechIngredientType.INSTANCE, Collections.emptyList(),
            new TechIngredientHelper(), IngredientRenderers.empty());
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        toolCategory.registerCategory(registration);
        for (var category : categories) {
            category.registerCategory(registration);
        }
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        var recipeManager = CORE.clientRecipeManager();
        toolCategory.registerRecipes(registration, recipeManager);
        for (var category : categories) {
            category.registerRecipes(registration, recipeManager);
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        toolCategory.registerCatalysts(registration);
        for (var category : categories) {
            category.registerCatalysts(registration);
        }
        for (var itemStack : Ingredient.of(AllTags.ELECTRIC_FURNACE).getItems()) {
            registration.addRecipeCatalyst(VanillaTypes.ITEM_STACK, itemStack, RecipeTypes.SMELTING);
        }
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGenericGuiContainerHandler(MenuScreen.class, MenuScreenHandler.fluid());
        registration.addGuiContainerHandler(NetworkControllerScreen.class, new NetworkControllerHandler());
        registration.addGuiContainerHandler(ProcessingScreen.class, new ProcessingHandler(this));
        registration.addGuiContainerHandler(ResearchBenchScreen.class, new ResearchBenchHandler());
        WorkbenchHandler.addWorkbenchClickArea(registration, toolCategory);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        // TODO
    }
}
