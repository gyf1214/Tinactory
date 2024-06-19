package org.shsts.tinactory.integration.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.shsts.tinactory.content.AllBlockEntities;
import org.shsts.tinactory.content.AllTags;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.content.gui.WorkbenchMenu;
import org.shsts.tinactory.core.util.ClientUtil;
import org.shsts.tinactory.integration.jei.category.ProcessingCategory;
import org.shsts.tinactory.integration.jei.category.RecipeCategory;
import org.shsts.tinactory.integration.jei.category.ToolCategory;
import org.shsts.tinactory.integration.jei.ingredient.FluidIngredientRenderer;
import org.shsts.tinactory.integration.jei.ingredient.FluidStackHelper;
import org.shsts.tinactory.integration.jei.ingredient.FluidStackType;
import org.shsts.tinactory.integration.jei.ingredient.FluidStackWrapper;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

import static org.shsts.tinactory.core.util.LocHelper.modLoc;

@JeiPlugin
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class JEI implements IModPlugin {
    private static final ResourceLocation LOC = modLoc("jei");

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
    public void registerIngredients(IModIngredientRegistration registration) {
        var allFluids = ForgeRegistries.FLUIDS.getValues().stream()
                .filter(fluid -> fluid.isSource(fluid.defaultFluidState()))
                .map(fluid -> new FluidStackWrapper(
                        new FluidStack(fluid, FluidAttributes.BUCKET_VOLUME)))
                .toList();
        var helper = new FluidStackHelper(registration.getSubtypeManager(),
                registration.getColorHelper());
        var renderer = new FluidIngredientRenderer();
        registration.register(FluidStackType.INSTANCE, allFluids, helper, renderer);
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
        registration.addRecipeTransferHandler(WorkbenchMenu.class, RecipeTypes.CRAFTING, 9, 9, 19, 36);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jei) {
        var ingredientManager = jei.getIngredientManager();
        var allFluids = new ArrayList<>(ingredientManager.getAllIngredients(ForgeTypes.FLUID_STACK));
        ingredientManager.removeIngredientsAtRuntime(ForgeTypes.FLUID_STACK, allFluids);
    }
}
