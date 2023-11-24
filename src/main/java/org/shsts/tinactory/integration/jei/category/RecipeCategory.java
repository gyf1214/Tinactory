package org.shsts.tinactory.integration.jei.category;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
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
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.gui.layout.Layout;
import org.shsts.tinactory.gui.layout.Rect;
import org.shsts.tinactory.integration.jei.DrawableHelper;
import org.shsts.tinactory.model.ModelGen;

import javax.annotation.Nullable;
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

    protected final @Nullable LoadingCache<Integer, IDrawable> cachedProgressBar;
    protected final @Nullable Rect progressBarRect;

    public RecipeCategory(RecipeType<T> type, IJeiHelpers helpers, Layout layout, ItemStack icon) {
        this.type = type;
        this.title = new TranslatableComponent(ModelGen.translate(this.type.getUid()));
        var guiHelper = helpers.getGuiHelper();
        this.background = DrawableHelper.createBackground(guiHelper, layout);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, icon);

        for (var slot : layout.slots) {
            slotsInfo.put(slot.index(), slot);
        }

        if (layout.progressBar != null) {
            var texture = layout.progressBar.texture();
            this.progressBarRect = layout.progressBar.rect();
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
        this.addIngredient(builder, slot, Either.left(ingredient), role);
    }

    protected void addIngredient(IRecipeLayoutBuilder builder, int slot,
                                 Either<Ingredient, FluidStack> ingredient, RecipeIngredientRole role) {
        if (this.slotsInfo.containsKey(slot)) {
            var slotInfo = this.slotsInfo.get(slot);
            var slotBuilder = builder.addSlot(role, slotInfo.x() + 1, slotInfo.y() + 1);
            ingredient.ifLeft(slotBuilder::addIngredients)
                    .ifRight(fluid -> slotBuilder.addIngredient(ForgeTypes.FLUID_STACK, fluid));
        }
    }

    @Override
    public abstract void setRecipe(IRecipeLayoutBuilder builder, T recipe, IFocusGroup focuses);

    protected void drawProgressBar(PoseStack stack, int cycle) {
        if (this.cachedProgressBar != null && this.progressBarRect != null) {
            var bar = this.cachedProgressBar.getUnchecked(cycle);
            bar.draw(stack, this.progressBarRect.x(), this.progressBarRect.y());
        }
    }

    @FunctionalInterface
    public interface Factory<T1 extends Recipe<?>> {
        RecipeCategory<T1> create(RecipeType<T1> type, IJeiHelpers helpers);
    }
}
