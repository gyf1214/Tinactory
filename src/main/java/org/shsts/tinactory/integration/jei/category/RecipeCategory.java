package org.shsts.tinactory.integration.jei.category;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import org.shsts.tinactory.gui.layout.Layout;
import org.shsts.tinactory.integration.jei.DrawableHelper;
import org.shsts.tinactory.model.ModelGen;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class RecipeCategory<T extends Recipe<?>> implements IRecipeCategory<T> {
    protected final RecipeType<T> type;
    protected final Component title;
    protected final IDrawable background;
    protected final IDrawable icon;
    protected final Map<Integer, Layout.SlotInfo> slotsInfo = new HashMap<>();

    public RecipeCategory(RecipeType<T> type, IJeiHelpers helpers, Layout layout, ItemStack icon) {
        this.type = type;
        this.title = new TranslatableComponent(ModelGen.translate(this.type.getUid()));
        var guiHelper = helpers.getGuiHelper();
        this.background = DrawableHelper.createBackground(guiHelper, layout);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, icon);

        for (var slot : layout.slots) {
            slotsInfo.put(slot.index(), slot);
        }
    }

    @Override
    public Component getTitle() {
        return this.title;
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @SuppressWarnings("removal")
    @Override
    public ResourceLocation getUid() {
        return this.type.getUid();
    }

    @SuppressWarnings("removal")
    @Override
    public Class<? extends T> getRecipeClass() {
        return this.type.getRecipeClass();
    }

    @Override
    public RecipeType<T> getRecipeType() {
        return this.type;
    }

    protected void addIngredient(IRecipeLayoutBuilder builder, int slot,
                                 Ingredient ingredient, RecipeIngredientRole role) {
        if (this.slotsInfo.containsKey(slot)) {
            var slotInfo = this.slotsInfo.get(slot);
            builder.addSlot(role, slotInfo.x() + 1, slotInfo.y() + 1)
                    .addIngredients(ingredient);
        }
    }

    @Override
    public abstract void setRecipe(IRecipeLayoutBuilder builder, T recipe, IFocusGroup focuses);

    @FunctionalInterface
    public interface Factory<T1 extends Recipe<?>> {
        RecipeCategory<T1> create(RecipeType<T1> type, IJeiHelpers helpers);
    }
}
