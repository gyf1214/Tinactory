package org.shsts.tinactory.integration.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import org.shsts.tinactory.content.AllBlockEntities;
import org.shsts.tinactory.content.AllTags;
import org.shsts.tinactory.content.machine.Voltage;
import org.shsts.tinactory.content.model.ModelGen;
import org.shsts.tinactory.core.util.ClientUtil;
import org.shsts.tinactory.integration.jei.category.ProcessingCategory;
import org.shsts.tinactory.integration.jei.category.RecipeCategory;
import org.shsts.tinactory.integration.jei.category.ToolCategory;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@JeiPlugin
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class JEI implements IModPlugin {
    private static final ResourceLocation LOC = ModelGen.modLoc("jei");

    private final List<RecipeCategory<?, ?>> categories;

    public JEI() {
        this.categories = new ArrayList<>();

        categories.add(new ToolCategory());
        for (var set : AllBlockEntities.PROCESSING_SETS) {
            var layout = set.layoutSet.get(Voltage.MAXIMUM);
            var icon = set.block(Voltage.LV);
            var category = new ProcessingCategory(set.recipeType, layout, icon);
            categories.add(category);
        }
    }

    @Override
    public ResourceLocation getPluginUid() {
        return LOC;
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
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        for (var category : categories) {
            category.registerRecipeTransferHandlers(registration);
        }
    }
}
