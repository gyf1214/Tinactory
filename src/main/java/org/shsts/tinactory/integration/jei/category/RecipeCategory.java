package org.shsts.tinactory.integration.jei.category;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.blaze3d.vertex.PoseStack;
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
import org.shsts.tinactory.content.model.ModelGen;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.integration.jei.DrawableHelper;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class RecipeCategory<T extends Recipe<?>> implements IRecipeCategory<T> {
    protected static final int WIDTH = Menu.CONTENT_WIDTH;

    protected final RecipeType<T> type;
    private final Component title;
    protected final IDrawable background;
    private final IDrawable icon;
    protected final Layout layout;
    protected final int xOffset;

    @Nullable
    private final LoadingCache<Integer, IDrawable> cachedProgressBar;
    @Nullable
    private final Rect progressBarRect;

    private static IDrawable createBackground(IJeiHelpers helpers, Layout layout) {
        return DrawableHelper.createBackground(helpers.getGuiHelper(), layout, WIDTH)
                .build();
    }

    protected RecipeCategory(RecipeType<T> type, IJeiHelpers helpers, Layout layout, ItemStack icon) {
        this(type, helpers, createBackground(helpers, layout), layout, icon);
    }

    protected RecipeCategory(RecipeType<T> type, IJeiHelpers helpers, IDrawable background,
                             Layout layout, ItemStack icon) {
        this.type = type;
        this.title = new TranslatableComponent(ModelGen.translate(type.getUid()));
        var guiHelper = helpers.getGuiHelper();
        this.background = background;
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, icon);
        this.layout = layout;
        this.xOffset = (WIDTH - layout.rect.width()) / 2;

        if (layout.progressBar != null) {
            var texture = layout.progressBar.texture();
            this.progressBarRect = layout.progressBar.rect().offset(xOffset, 0);
            this.cachedProgressBar = CacheBuilder.newBuilder()
                    .maximumSize(25)
                    .build(new CacheLoader<>() {
                        @Override
                        public IDrawable load(Integer key) {
                            return DrawableHelper.createProgressBar(guiHelper, texture, key);
                        }
                    });
        } else {
            this.cachedProgressBar = null;
            this.progressBarRect = null;
        }
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @SuppressWarnings("removal")
    @Override
    public ResourceLocation getUid() {
        return type.getUid();
    }

    @SuppressWarnings("removal")
    @Override
    public Class<? extends T> getRecipeClass() {
        return type.getRecipeClass();
    }

    @Override
    public RecipeType<T> getRecipeType() {
        return type;
    }

    protected void addIngredient(IRecipeLayoutBuilder builder, Layout.SlotInfo slot,
                                 Ingredient ingredient, RecipeIngredientRole role) {
        builder.addSlot(role, slot.x() + 1 + xOffset, slot.y() + 1)
                .addIngredients(ingredient);
    }

    @Override
    public abstract void setRecipe(IRecipeLayoutBuilder builder, T recipe, IFocusGroup focuses);

    protected void drawProgressBar(PoseStack stack, int cycle) {
        if (cachedProgressBar != null && progressBarRect != null) {
            var bar = cachedProgressBar.getUnchecked(cycle);
            bar.draw(stack, progressBarRect.x(), progressBarRect.y());
        }
    }

    @FunctionalInterface
    public interface Factory<T1 extends Recipe<?>> {
        RecipeCategory<T1> create(RecipeType<T1> type, IJeiHelpers helpers);
    }
}
