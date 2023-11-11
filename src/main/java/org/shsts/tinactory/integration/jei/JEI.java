package org.shsts.tinactory.integration.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ItemLike;
import org.shsts.tinactory.content.AllBlocks;
import org.shsts.tinactory.content.AllRecipes;
import org.shsts.tinactory.content.machine.ProcessingContainer;
import org.shsts.tinactory.content.recipe.ProcessingRecipe;
import org.shsts.tinactory.gui.layout.AllLayouts;
import org.shsts.tinactory.gui.layout.Layout;
import org.shsts.tinactory.integration.jei.category.ProcessingCategory;
import org.shsts.tinactory.integration.jei.category.RecipeCategory;
import org.shsts.tinactory.integration.jei.category.ToolCategory;
import org.shsts.tinactory.model.ModelGen;
import org.shsts.tinactory.registrate.RecipeTypeEntry;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@JeiPlugin
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class JEI implements IModPlugin {
    private static final ResourceLocation LOC = ModelGen.modLoc("jei");

    private record CategoryInfo<C extends Container, T extends Recipe<C>>(
            RecipeType<T> type, RecipeTypeEntry<T, ?> typeEntry,
            RecipeCategory.Factory<T> factory,
            Ingredient catalyst) {
        public void register(IRecipeCategoryRegistration registration) {
            registration.addRecipeCategories(this.factory.create(this.type, registration.getJeiHelpers()));
        }

        public void addRecipes(IRecipeRegistration registration, RecipeManager recipeManager) {
            registration.addRecipes(this.type, recipeManager.getAllRecipesFor(this.typeEntry.get()));
        }

        public void addCatalysts(IRecipeCatalystRegistration registration) {
            for (var itemStack : this.catalyst.getItems()) {
                registration.addRecipeCatalyst(VanillaTypes.ITEM_STACK, itemStack, this.type);
            }
        }
    }

    private static <C extends Container, T extends Recipe<C>> CategoryInfo<C, T>
    category(RecipeTypeEntry<T, ?> typeEntry, RecipeCategory.Factory<T> factory, Ingredient catalyst) {
        var type = new RecipeType<>(ModelGen.prepend(typeEntry.loc, "jei/category"), typeEntry.clazz);
        return new CategoryInfo<>(type, typeEntry, factory, catalyst);
    }

    private static <C extends Container, T extends Recipe<C>> CategoryInfo<C, T>
    category(RecipeTypeEntry<T, ?> typeEntry, RecipeCategory.Factory<T> factory, ItemLike catalyst) {
        return category(typeEntry, factory, Ingredient.of(catalyst));
    }

    private static <T extends ProcessingRecipe<T>> CategoryInfo<ProcessingContainer, T>
    processing(RecipeTypeEntry<T, ?> typeEntry, Layout layout, ItemLike icon) {
        return category(typeEntry, (type, helpers) ->
                new ProcessingCategory<>(type, helpers, layout, icon), icon);
    }

    private final List<CategoryInfo<?, ?>> categories = List.of(
            category(AllRecipes.TOOL, ToolCategory::new, AllBlocks.WORKBENCH.get()),
            processing(AllRecipes.STONE_GENERATOR, AllLayouts.STONE_GENERATOR, AllBlocks.PRIMITIVE_STONE_GENERATOR.get())
    );

    @Override
    public ResourceLocation getPluginUid() {
        return LOC;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        for (var category : this.categories) {
            category.register(registration);
        }
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        var connection = Minecraft.getInstance().getConnection();
        assert connection != null;
        var recipeManager = connection.getRecipeManager();
        for (var category : this.categories) {
            category.addRecipes(registration, recipeManager);
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        for (var category : this.categories) {
            category.addCatalysts(registration);
        }
    }
}
