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
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.Block;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.content.AllBlocks;
import org.shsts.tinactory.content.AllLayouts;
import org.shsts.tinactory.content.AllRecipes;
import org.shsts.tinactory.content.AllTags;
import org.shsts.tinactory.content.model.ModelGen;
import org.shsts.tinactory.core.common.SmartRecipe;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.integration.jei.category.ProcessingCategory;
import org.shsts.tinactory.integration.jei.category.RecipeCategory;
import org.shsts.tinactory.integration.jei.category.ToolCategory;
import org.shsts.tinactory.registrate.RecipeTypeEntry;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.Supplier;

@JeiPlugin
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class JEI implements IModPlugin {
    private static final ResourceLocation LOC = ModelGen.modLoc("jei");

    private record CategoryInfo<C extends Container, T extends Recipe<C>>(
            RecipeType<T> type, RecipeTypeEntry<T, ?> typeEntry,
            RecipeCategory.Factory<T> factory,
            Supplier<Ingredient> catalyst) {
        public void register(IRecipeCategoryRegistration registration) {
            registration.addRecipeCategories(this.factory.create(this.type, registration.getJeiHelpers()));
        }

        public void addRecipes(IRecipeRegistration registration, RecipeManager recipeManager) {
            registration.addRecipes(this.type, recipeManager.getAllRecipesFor(this.typeEntry.get()));
        }

        public void addCatalysts(IRecipeCatalystRegistration registration) {
            for (var itemStack : this.catalyst.get().getItems()) {
                registration.addRecipeCatalyst(VanillaTypes.ITEM_STACK, itemStack, this.type);
            }
        }
    }

    private static <C extends Container, T extends Recipe<C>> CategoryInfo<C, T>
    category(RecipeTypeEntry<T, ?> recipeType, RecipeCategory.Factory<T> factory, Supplier<Ingredient> catalyst) {
        var type = new RecipeType<>(ModelGen.prepend(recipeType.loc, "jei/category"), recipeType.clazz);
        return new CategoryInfo<>(type, recipeType, factory, catalyst);
    }

    private static <C extends Container, T extends Recipe<C>> CategoryInfo<C, T>
    category(RecipeTypeEntry<T, ?> recipeType, RecipeCategory.Factory<T> factory, TagKey<Item> catalyst) {
        return category(recipeType, factory, () -> Ingredient.of(catalyst));
    }

    private static <T extends ProcessingRecipe<T>> CategoryInfo<SmartRecipe.ContainerWrapper<IContainer>, T>
    processing(RecipeTypeEntry<T, ?> recipeType, Layout layout,
               Supplier<? extends Block> block) {
        return category(recipeType, (type, helpers) ->
                new ProcessingCategory<>(type, helpers, layout, block.get()), AllTags.processingMachine(recipeType));
    }

    private final List<CategoryInfo<?, ?>> categories = List.of(
            category(AllRecipes.TOOL, ToolCategory::new, () -> Ingredient.of(AllBlocks.WORKBENCH.get())),
            processing(AllRecipes.STONE_GENERATOR, AllLayouts.STONE_GENERATOR, AllBlocks.PRIMITIVE_STONE_GENERATOR::getBlock),
            processing(AllRecipes.ORE_ANALYZER, AllLayouts.ORE_ANALYZER, AllBlocks.PRIMITIVE_STONE_GENERATOR::getBlock)
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
