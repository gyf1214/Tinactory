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
import org.shsts.tinactory.content.AllLayouts;
import org.shsts.tinactory.content.AllTags;
import org.shsts.tinactory.content.gui.client.NetworkControllerScreen;
import org.shsts.tinactory.content.gui.client.ProcessingScreen;
import org.shsts.tinactory.content.gui.client.ResearchBenchScreen;
import org.shsts.tinactory.content.recipe.BlastFurnaceRecipe;
import org.shsts.tinactory.content.recipe.ChemicalReactorRecipe;
import org.shsts.tinactory.content.recipe.CleanRecipe;
import org.shsts.tinactory.content.recipe.DistillationRecipe;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.client.MenuScreen;
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
import static org.shsts.tinactory.content.AllBlockEntities.PROCESSING_SETS;
import static org.shsts.tinactory.content.AllMultiblocks.MULTIBLOCK_SETS;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;

@JeiPlugin
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class JEI implements IModPlugin {
    private static final ResourceLocation LOC = modLoc("jei");

    public final ToolCategory toolCategory;

    private final List<RecipeCategory<?>> categories;
    private final Map<ResourceLocation, RecipeCategory<?>> processingCategories;

    public JEI() {
        this.categories = new ArrayList<>();
        this.processingCategories = new HashMap<>();

        this.toolCategory = new ToolCategory();
        categories.add(toolCategory);

        for (var set : PROCESSING_SETS) {
            var type = set.recipeType;
            var icon = set.icon();
            var clazz = type.recipeClass();
            var layout = ChemicalReactorRecipe.class.isAssignableFrom(clazz) ?
                AllLayouts.LARGE_CHEMICAL_REACTOR : set.layout(Voltage.MAX);

            if (ResearchRecipe.class.isAssignableFrom(clazz)) {
                addProcessingCategory(type, new ResearchCategory(cast(type), layout, icon));
            } else if (ChemicalReactorRecipe.class.isAssignableFrom(clazz)) {
                addProcessingCategory(type, new ChemicalReactorCategory(cast(type), layout, icon));
            } else if (AssemblyRecipe.class.isAssignableFrom(clazz)) {
                addProcessingCategory(type, new AssemblyCategory<>(cast(type), layout, icon));
            } else if (CleanRecipe.class.isAssignableFrom(clazz)) {
                addProcessingCategory(type, new CleanCategory(cast(type), layout, icon));
            } else {
                addProcessingCategory(type, layout, icon);
            }
        }

        for (var set : MULTIBLOCK_SETS.values()) {
            var type = set.recipeType();
            var clazz = type.recipeClass();
            var layout = set.layout();
            var icon = set.block().get();
            if (processingCategories.containsKey(type.loc())) {
                continue;
            }

            if (BlastFurnaceRecipe.class.isAssignableFrom(clazz)) {
                addProcessingCategory(type, new BlastFurnaceCategory(cast(type), layout, icon));
            } else if (DistillationRecipe.class.isAssignableFrom(clazz)) {
                addProcessingCategory(type, new DistillationCategory(cast(type), icon));
            } else {
                addProcessingCategory(type, layout, icon);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <A extends IRecipeBuilderBase<?>,
        B extends IRecipeBuilderBase<?>> IRecipeType<B> cast(IRecipeType<A> type) {
        return (IRecipeType<B>) type;
    }

    private void addProcessingCategory(IRecipeType<?> recipeType, ProcessingCategory<?> category) {
        categories.add(category);
        processingCategories.put(recipeType.loc(), category);
    }

    private void addProcessingCategory(IRecipeType<?> recipeType, Layout layout, Block icon) {
        var category = new ProcessingCategory<>(cast(recipeType), layout, icon);
        addProcessingCategory(recipeType, category);
    }

    public Optional<RecipeCategory<?>> processingCategory(IRecipeType<?> recipeType) {
        return Optional.ofNullable(processingCategories.get(recipeType.loc()));
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
