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
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import org.shsts.tinactory.content.AllBlockEntities;
import org.shsts.tinactory.content.AllLayouts;
import org.shsts.tinactory.content.AllMultiBlocks;
import org.shsts.tinactory.content.AllRecipes;
import org.shsts.tinactory.content.AllTags;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.content.gui.client.NetworkControllerScreen;
import org.shsts.tinactory.content.gui.client.ProcessingScreen;
import org.shsts.tinactory.content.gui.client.ResearchBenchScreen;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.client.MenuScreen;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.core.util.ClientUtil;
import org.shsts.tinactory.integration.jei.category.ProcessingCategory;
import org.shsts.tinactory.integration.jei.category.RecipeCategory;
import org.shsts.tinactory.integration.jei.category.ToolCategory;
import org.shsts.tinactory.integration.jei.gui.MenuScreenHandler;
import org.shsts.tinactory.integration.jei.gui.NetworkControllerHandler;
import org.shsts.tinactory.integration.jei.gui.ProcessingHandler;
import org.shsts.tinactory.integration.jei.gui.ResearchBenchHandler;
import org.shsts.tinactory.integration.jei.gui.WorkbenchHandler;
import org.shsts.tinactory.integration.jei.ingredient.IngredientRenderers;
import org.shsts.tinactory.integration.jei.ingredient.TechIngredientHelper;
import org.shsts.tinactory.integration.jei.ingredient.TechIngredientType;
import org.shsts.tinactory.registrate.common.RecipeTypeEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.shsts.tinactory.core.util.LocHelper.modLoc;

@JeiPlugin
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class JEI implements IModPlugin {
    private static final ResourceLocation LOC = modLoc("jei");

    public final ToolCategory toolCategory;

    private final List<RecipeCategory<?, ?>> categories;
    private final Map<RecipeType<?>, RecipeCategory<?, ?>> processingCategories;

    public JEI() {
        this.categories = new ArrayList<>();
        this.processingCategories = new HashMap<>();

        this.toolCategory = new ToolCategory();
        categories.add(toolCategory);
        for (var set : AllBlockEntities.getProcessingSets()) {
            var layout = set.layout(Voltage.MAXIMUM);
            var icon = set.icon();
            addProcessingCategory(set.recipeType, layout, icon);
        }
        addProcessingCategory(AllRecipes.BLAST_FURNACE, AllLayouts.BLAST_FURNACE,
            AllMultiBlocks.BLAST_FURNACE.get());
        addProcessingCategory(AllRecipes.SIFTER, AllLayouts.SIFTER, AllMultiBlocks.SIFTER.get());
    }

    private void addProcessingCategory(RecipeTypeEntry<? extends ProcessingRecipe, ?> recipeType,
        Layout layout, Block icon) {
        var category = new ProcessingCategory(recipeType, layout, icon);
        categories.add(category);
        processingCategories.put(recipeType.get(), category);
    }

    public Optional<RecipeCategory<?, ?>> processingCategory(RecipeType<?> recipeType) {
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
        for (var category : categories) {
            category.registerCategory(registration);
        }
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        var recipeManager = ClientUtil.getRecipeManager();
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
        registration.addGuiContainerHandler(MenuScreen.class, MenuScreenHandler.fluid());
        registration.addGuiContainerHandler(ProcessingScreen.class, new ProcessingHandler(this));
        registration.addGuiContainerHandler(NetworkControllerScreen.class, new NetworkControllerHandler());
        registration.addGuiContainerHandler(ResearchBenchScreen.class, new ResearchBenchHandler());
        WorkbenchHandler.addWorkbenchClickArea(registration, toolCategory);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        // TODO
    }
}
