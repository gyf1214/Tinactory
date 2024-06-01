package org.shsts.tinactory.integration.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import org.shsts.tinactory.content.AllBlockEntities;
import org.shsts.tinactory.content.AllRecipes;
import org.shsts.tinactory.content.AllTags;
import org.shsts.tinactory.content.machine.ProcessingSet;
import org.shsts.tinactory.content.machine.Voltage;
import org.shsts.tinactory.content.model.ModelGen;
import org.shsts.tinactory.core.common.SmartRecipe;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.core.util.ClientUtil;
import org.shsts.tinactory.integration.jei.category.ProcessingCategory;
import org.shsts.tinactory.integration.jei.category.RecipeCategory;
import org.shsts.tinactory.integration.jei.category.ToolCategory;
import org.shsts.tinactory.registrate.common.RecipeTypeEntry;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@JeiPlugin
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class JEI implements IModPlugin {
    private static final ResourceLocation LOC = ModelGen.modLoc("jei");

    private record CategoryInfo<T extends SmartRecipe<?>>(
            RecipeType<T> type, RecipeTypeEntry<T, ?> typeEntry,
            RecipeCategory.Factory<T> factory,
            Supplier<Ingredient> catalyst) {
        public void register(IRecipeCategoryRegistration registration) {
            registration.addRecipeCategories(factory.create(type, registration.getJeiHelpers()));
        }

        public void addRecipes(IRecipeRegistration registration, RecipeManager recipeManager) {
            registration.addRecipes(type, recipeManager.getAllRecipesFor(typeEntry.get()));
        }

        public void addCatalysts(IRecipeCatalystRegistration registration) {
            for (var itemStack : catalyst.get().getItems()) {
                registration.addRecipeCatalyst(VanillaTypes.ITEM_STACK, itemStack, type);
            }
        }
    }

    private static <T extends SmartRecipe<?>> CategoryInfo<T>
    category(RecipeTypeEntry<T, ?> recipeType, RecipeCategory.Factory<T> factory, Supplier<Ingredient> catalyst) {
        var type = new RecipeType<>(ModelGen.prepend(recipeType.loc, "jei/category"), recipeType.clazz);
        return new CategoryInfo<>(type, recipeType, factory, catalyst);
    }

    private static <T extends SmartRecipe<?>> CategoryInfo<T>
    category(RecipeTypeEntry<T, ?> recipeType, RecipeCategory.Factory<T> factory, TagKey<Item> catalyst) {
        return category(recipeType, factory, () -> Ingredient.of(catalyst));
    }

    private static <T extends ProcessingRecipe> CategoryInfo<T>
    processing(ProcessingSet<T> processingSet) {
        var layout = processingSet.layoutSet.get(Voltage.MAXIMUM);
        return category(processingSet.recipeType, (type, helpers) -> new ProcessingCategory<>(type, helpers,
                        layout, processingSet.block(Voltage.LV)),
                AllTags.processingMachine(processingSet.recipeType));
    }

    private final List<CategoryInfo<?>> categories;

    public JEI() {
        this.categories = new ArrayList<>();

        categories.add(category(AllRecipes.TOOL, ToolCategory::new,
                () -> Ingredient.of(AllBlockEntities.WORKBENCH.block())));
        for (var set : AllBlockEntities.PROCESSING_SETS) {
            categories.add(processing(set));
        }
    }

    @Override
    public ResourceLocation getPluginUid() {
        return LOC;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        for (var category : categories) {
            category.register(registration);
        }
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        var recipeManager = ClientUtil.getRecipeManager();
        for (var category : categories) {
            category.addRecipes(registration, recipeManager);
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        for (var category : categories) {
            category.addCatalysts(registration);
        }
        for (var itemStack : Ingredient.of(AllTags.ELECTRIC_FURNACE).getItems()) {
            registration.addRecipeCatalyst(VanillaTypes.ITEM_STACK, itemStack, RecipeTypes.SMELTING);
        }
    }
}
