package org.shsts.tinactory.compat.jei.category;

import javax.annotation.ParametersAreNonnullByDefault;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
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
import org.shsts.tinactory.api.multiblock.IMultiblockDisplay;
import org.shsts.tinactory.compat.jei.gui.MultiblockStructureViewer;
import org.shsts.tinactory.content.multiblock.MultiblockSet;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.util.I18n;
import org.shsts.tinactory.integration.gui.client.RenderUtil;
import org.shsts.tinactory.integration.util.ClientUtil;

import java.util.List;

import static org.shsts.tinactory.core.gui.Menu.CONTENT_WIDTH;
import static org.shsts.tinactory.core.gui.Menu.FONT_HEIGHT;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Menu.SPACING;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;
import static org.shsts.tinactory.integration.gui.client.Widgets.BUTTON_HEIGHT;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class MultiblockCategory implements IRecipeCategory<MultiblockSet> {
    public static final ResourceLocation LOC = modLoc("jei/category/multiblock_structure");
    public static final RecipeType<MultiblockSet> TYPE = new RecipeType<>(LOC, MultiblockSet.class);
    private static final int WIDTH = CONTENT_WIDTH;
    private static final int VIEW_Y = FONT_HEIGHT + SPACING;
    private static final int SLOTS_COLUMNS = 3;
    private static final int SLOTS_ROWS = 3;
    private static final int BUTTON_WIDTH = SLOTS_COLUMNS * SLOT_SIZE;
    private static final int BUTTON_X = WIDTH - BUTTON_WIDTH;
    private static final Rect BUTTON = new Rect(BUTTON_X, VIEW_Y, BUTTON_WIDTH, 2 * BUTTON_HEIGHT + SPACING);
    private static final int SLOTS_X = BUTTON_X;
    private static final int SLOTS_Y = BUTTON.endY() + SPACING;
    private static final int VIEW_WIDTH = BUTTON_X - SPACING;
    private static final int VIEW_HEIGHT = SLOTS_Y + SLOTS_ROWS * SLOT_SIZE - VIEW_Y;
    private static final Rect VIEWPORT = new Rect(0, VIEW_Y, VIEW_WIDTH, VIEW_HEIGHT);
    private static final int MAX_LABEL_LINES = 2;
    private static final int HEIGHT = VIEWPORT.endY() + SPACING + MAX_LABEL_LINES * FONT_HEIGHT;
    private final IDrawable icon;

    public MultiblockCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Items.BOOK));
    }

    private static List<ItemStack> ingredientToItems(IMultiblockDisplay.StructureIngredient structure) {
        var count = Math.toIntExact(structure.count());
        return structure.ingredient().expand(ClientUtil.registryAccess()).stream()
            .map(block -> new ItemStack(block, count))
            .toList();
    }

    @Override
    public RecipeType<MultiblockSet> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return I18n.tr("tinactory.jei.category.multiblock_structure");
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
        var controller = new ItemStack(set.controller().get());
        builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT)
            .addIngredient(VanillaTypes.ITEM_STACK, controller);
        builder.addSlot(RecipeIngredientRole.INPUT, SLOTS_X + 1, SLOTS_Y + 1)
            .addIngredient(VanillaTypes.ITEM_STACK, controller);
        var structureIngredients = set.display().getStructureIngredients();
        for (var i = 0; i < structureIngredients.size(); i++) {
            var ingredient = structureIngredients.get(i);
            var x = SLOTS_X + (i + 1) % SLOTS_COLUMNS * SLOT_SIZE + 1;
            var y = SLOTS_Y + (i + 1) / SLOTS_COLUMNS * SLOT_SIZE + 1;
            builder.addSlot(RecipeIngredientRole.INPUT, x, y)
                .addIngredients(VanillaTypes.ITEM_STACK, ingredientToItems(ingredient));
        }
    }

    @Override
    public void draw(MultiblockSet set, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics,
        double mouseX, double mouseY) {
        RenderUtil.renderText(graphics, set.controller().get().getName(), 0, 0);
        for (var i = 0; i < SLOTS_COLUMNS; i++) {
            for (var j = 0; j < SLOTS_ROWS; j++) {
                var rect = new Rect(SLOTS_X + i * SLOT_SIZE, SLOTS_Y + j * SLOT_SIZE, SLOT_SIZE, SLOT_SIZE);
                RenderUtil.blit(graphics, Texture.SLOT_BACKGROUND, rect);
            }
        }
        var detailLines = set.display().getDetailLines();
        for (var i = 0; i < detailLines.size(); i++) {
            var y = HEIGHT - (detailLines.size() - i) * FONT_HEIGHT;
            RenderUtil.renderText(graphics, detailLines.get(i), 0, y);
        }
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, MultiblockSet set, IFocusGroup focuses) {
        var viewer = new MultiblockStructureViewer(set, VIEWPORT, BUTTON);
        builder.addWidget(viewer);
        builder.addGuiEventListener(viewer);
    }
}
