package org.shsts.tinactory.compat.jei.category;

import javax.annotation.ParametersAreNonnullByDefault;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.shsts.tinactory.api.multiblock.IBlockIngredient;
import org.shsts.tinactory.api.multiblock.IMultiblockDisplay;
import org.shsts.tinactory.compat.jei.gui.MultiblockStructureViewer;
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
    private static final int WIDTH = 220;
    private static final int HEIGHT = 120;
    private static final Rect VIEWPORT = new Rect(0, 0, 110, 86);
    private static final Rect DETAILS = new Rect(0, 89, 110, 30);
    private static final Rect RESET = new Rect(2, 2, 48, 14);
    private static final Rect LAYER = new Rect(52, 2, 56, 14);
    private static final int REQUIREMENTS_X = 120;
    private static final int REQUIREMENTS_Y = 16;
    private static final int REQUIREMENTS_COLUMNS = 5;
    private final IDrawable icon;

    public MultiblockCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Items.BOOK));
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

    private static List<ItemStack> requiredItems(IMultiblockDisplay.RequiredIngredient required) {
        var count = Math.toIntExact(required.count());
        return required.ingredient().expand(ClientUtil.registryAccess()).stream()
            .map(block -> new ItemStack(block, count))
            .toList();
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
        var requiredIngredients = set.display().getRequiredIngredients();
        for (var i = 0; i < requiredIngredients.size(); i++) {
            var required = requiredIngredients.get(i);
            var x = REQUIREMENTS_X + (i % REQUIREMENTS_COLUMNS) * 18;
            var y = REQUIREMENTS_Y + (i / REQUIREMENTS_COLUMNS) * 18;
            builder.addSlot(RecipeIngredientRole.INPUT, x, y)
                .addIngredients(VanillaTypes.ITEM_STACK, requiredItems(required))
                .addRichTooltipCallback((view, tooltip) -> tooltip.add(Component.translatable(
                    "tinactory.jei.multiblock.requirement_count", required.count())));
        }
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, MultiblockSet set, IFocusGroup focuses) {
        var viewer = new MultiblockStructureViewer(set, VIEWPORT, DETAILS, RESET, LAYER);
        builder.addWidget(viewer);
        builder.addGuiEventListener(viewer);
    }
}
