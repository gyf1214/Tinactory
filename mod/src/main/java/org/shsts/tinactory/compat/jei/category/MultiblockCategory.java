package org.shsts.tinactory.compat.jei.category;

import javax.annotation.ParametersAreNonnullByDefault;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.shsts.tinactory.api.multiblock.IBlockIngredient;
import org.shsts.tinactory.compat.jei.gui.MultiblockStructureRenderer;
import org.shsts.tinactory.content.multiblock.MultiblockSet;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.integration.util.ClientUtil;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import static org.shsts.tinactory.core.util.LocHelper.modLoc;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class MultiblockCategory implements IRecipeCategory<MultiblockSet> {
    public static final ResourceLocation LOC = modLoc("multiblock_structure");
    public static final RecipeType<MultiblockSet> TYPE = new RecipeType<>(LOC, MultiblockSet.class);
    private static final int WIDTH = 150;
    private static final int HEIGHT = 100;
    private final IDrawable icon;
    private final MultiblockStructureRenderer renderer;

    public MultiblockCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Items.BOOK));
        renderer = new MultiblockStructureRenderer();
    }

    private static List<ItemStack> componentItems(MultiblockSet set) {
        Map<IBlockIngredient, Boolean> ingredients = new IdentityHashMap<>();
        var display = set.display();
        for (var y = 0; y < display.height(); y++) {
            for (var z = 0; z < display.depth(); z++) {
                for (var x = 0; x < display.width(); x++) {
                    display.getIngredient(x, y, z).ifPresent(ingredient -> ingredients.put(ingredient, true));
                }
            }
        }
        var items = new ArrayList<ItemStack>();
        for (var ingredient : ingredients.keySet()) {
            for (var block : ingredient.expand(ClientUtil.registryAccess())) {
                items.add(new ItemStack(block));
            }
        }
        return items;
    }

    @Override
    public RecipeType<MultiblockSet> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("tinactory.jei.category.multiblock_structure");
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, MultiblockSet set, IFocusGroup focuses) {
        builder.addInvisibleIngredients(RecipeIngredientRole.INPUT)
            .addIngredients(VanillaTypes.ITEM_STACK, componentItems(set));
        builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT)
            .addIngredient(VanillaTypes.ITEM_STACK, new ItemStack(set.controller().get()));
    }

    @Override
    public void draw(MultiblockSet set, IRecipeSlotsView slotsView,
        GuiGraphics graphics, double mouseX, double mouseY) {
        renderer.render(graphics, set, new Rect(0, 0, WIDTH, HEIGHT));
    }
}
