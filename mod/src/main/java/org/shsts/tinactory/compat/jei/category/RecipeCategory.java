package org.shsts.tinactory.compat.jei.category;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.compat.jei.ComposeDrawable;
import org.shsts.tinactory.compat.jei.DrawableHelper;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinycorelib.api.recipe.IRecipe;
import org.shsts.tinycorelib.api.recipe.IRecipeManager;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import static org.shsts.tinactory.core.util.I18n.tr;
import static org.shsts.tinactory.core.util.LocHelper.prepend;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class RecipeCategory<R extends IRecipe<?>> {
    protected static final int WIDTH = Menu.CONTENT_WIDTH;

    private final ResourceLocation loc;
    protected final IRecipeType<R> recipeType;
    public final RecipeType<IEntry<R>> type;
    protected final Layout layout;
    protected final int xOffset;
    private final Ingredient catalyst;
    private final ItemStack iconItem;

    @SuppressWarnings("unchecked")
    private static <R> RecipeType<IEntry<R>> createJeiType(ResourceLocation loc, Class<?> clazz) {
        return new RecipeType<>(loc, (Class<? extends IEntry<R>>) clazz);
    }

    public RecipeCategory(IRecipeType<R> recipeType,
        Layout layout, Ingredient catalyst, ItemStack iconItem) {
        this.recipeType = recipeType;
        this.loc = prepend(recipeType.loc(), "jei/category");
        this.type = createJeiType(loc, IEntry.class);
        this.layout = layout;
        this.xOffset = (WIDTH - layout.rect.width()) / 2;
        this.catalyst = catalyst;
        this.iconItem = iconItem;
    }

    protected void buildBackground(ComposeDrawable.Builder builder,
        IGuiHelper helper, int xOffset) {
        builder.add(helper.createBlankDrawable(WIDTH, layout.rect.height()));
        for (var slot : layout.slots) {
            var type = slot.type().portType;
            if (type == PortType.FLUID) {
                builder.add(DrawableHelper.createStatic(helper, Texture.FLUID_SLOT_BG),
                    xOffset + slot.x(), slot.y());
            } else {
                builder.add(helper.getSlotDrawable(), xOffset + slot.x(), slot.y());
            }
        }
        for (var image : layout.images) {
            var rect = image.rect();
            var drawable = DrawableHelper.createStatic(helper, image.texture(), rect.width(), rect.height());
            builder.add(drawable, xOffset + rect.x(), rect.y());
        }
    }

    protected abstract void setRecipe(ResourceLocation loc, R recipe, IIngredientBuilder builder);

    protected void extraLayout(ResourceLocation loc, R recipe, IRecipeLayoutBuilder builder) {}

    protected void drawExtra(ResourceLocation loc, R recipe, ICategoryDrawHelper helper,
        IRecipeSlotsView slotsView, GuiGraphics graphics, double mouseX, double mouseY) {}

    public RecipeType<IEntry<R>> jeiRecipeType() {
        return type;
    }

    public ResourceLocation recipeTypeId() {
        return recipeType.loc();
    }

    private IDrawable createBackground(IGuiHelper guiHelper) {
        var builder = ComposeDrawable.builder();
        buildBackground(builder, guiHelper, xOffset);
        return builder.build();
    }

    private class Category implements IRecipeCategory<IEntry<R>>, ICategoryDrawHelper {
        private final Component title;
        private final IDrawable background;
        private final IDrawable icon;

        @Nullable
        private final LoadingCache<Integer, IDrawable> cachedProgressBar;
        @Nullable
        private final Rect progressBarRect;

        private Category(IGuiHelper guiHelper) {
            this.title = tr(loc);
            this.background = createBackground(guiHelper);
            this.icon = guiHelper.createDrawableIngredient(
                VanillaTypes.ITEM_STACK, iconItem);

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
        public void drawProgressBar(GuiGraphics graphics, int cycle) {
            if (cachedProgressBar != null && progressBarRect != null) {
                var bar = cachedProgressBar.getUnchecked(cycle);
                bar.draw(graphics, progressBarRect.x(), progressBarRect.y());
            }
        }

        @Override
        public Component getTitle() {
            return title;
        }

        @Override
        public IDrawable getIcon() {
            return icon;
        }

        @Override
        public void draw(IEntry<R> recipe, IRecipeSlotsView slotsView,
            GuiGraphics graphics, double mouseX, double mouseY) {
            background.draw(graphics);
            drawExtra(recipe.loc(), recipe.get(), this, slotsView, graphics, mouseX, mouseY);
        }

        @Override
        public int getWidth() {
            return background.getWidth();
        }

        @Override
        public int getHeight() {
            return background.getHeight();
        }

        @Override
        public void setRecipe(IRecipeLayoutBuilder builder, IEntry<R> recipe, IFocusGroup focuses) {
            var ingredientBuilder = new LayoutIngredientBuilder(builder, xOffset);
            RecipeCategory.this.setRecipe(recipe.loc(), recipe.get(), ingredientBuilder);
            extraLayout(recipe.loc(), recipe.get(), builder);
            builder.moveRecipeTransferButton(WIDTH - Menu.SLOT_SIZE, 0);
        }

        @Override
        public RecipeType<IEntry<R>> getRecipeType() {
            return type;
        }
    }

    public void registerCategory(IRecipeCategoryRegistration registration) {
        var guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new Category(guiHelper));
    }

    public void registerCatalysts(IRecipeCatalystRegistration registration) {
        var items = catalyst.getItems();
        for (var item : items) {
            registration.addRecipeCatalyst(item, type);
        }
    }

    public void registerRecipes(IRecipeRegistration registration, IRecipeManager recipeManager) {
        registration.addRecipes(type, recipeManager.getAllRecipesFor(recipeType));
    }
}
