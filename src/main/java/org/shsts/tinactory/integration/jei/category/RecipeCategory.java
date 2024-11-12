package org.shsts.tinactory.integration.jei.category;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;
import org.shsts.tinactory.api.logistics.SlotType;
import org.shsts.tinactory.core.common.SmartRecipe;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.util.I18n;
import org.shsts.tinactory.integration.jei.ComposeDrawable;
import org.shsts.tinactory.integration.jei.DrawableHelper;
import org.shsts.tinactory.integration.jei.ingredient.FluidIngredientRenderer;
import org.shsts.tinactory.integration.jei.ingredient.RatedItemIngredientRenderer;
import org.shsts.tinactory.integration.jei.ingredient.TechIngredientRenderer;
import org.shsts.tinactory.integration.jei.ingredient.TechIngredientType;
import org.shsts.tinactory.registrate.common.RecipeTypeEntry;

import java.util.ArrayList;
import java.util.List;

import static org.shsts.tinactory.core.util.LocHelper.prepend;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class RecipeCategory<T extends SmartRecipe<?>, M extends Menu<?, M>> {
    protected static final int WIDTH = Menu.CONTENT_WIDTH;

    protected final RecipeTypeEntry<? extends T, ?> recipeType;
    public final RecipeType<T> type;
    protected final Layout layout;
    protected final Ingredient catalyst;
    protected final ItemStack iconItem;
    protected final Class<M> menuClazz;

    protected RecipeCategory(RecipeTypeEntry<? extends T, ?> recipeType, Layout layout, Ingredient catalyst,
        ItemStack iconItem, Class<M> menuClazz) {
        this.recipeType = recipeType;
        this.type = new RecipeType<>(prepend(recipeType.loc, "jei/category"), recipeType.clazz);
        this.layout = layout;
        this.catalyst = catalyst;
        this.iconItem = iconItem;
        this.menuClazz = menuClazz;
    }

    protected interface IIngredientBuilder {
        <I> void addIngredients(Layout.SlotInfo slot, RecipeIngredientRole role,
            IIngredientType<I> type, List<I> ingredients, double rate);

        default <I> void addIngredients(Layout.SlotInfo slot, RecipeIngredientRole role,
            IIngredientType<I> type, List<I> ingredients) {
            addIngredients(slot, role, type, ingredients, 1d);
        }

        default <I> void addIngredients(Layout.SlotInfo slot, IIngredientType<I> type,
            List<I> ingredients, double rate) {
            var role = switch (slot.type().direction) {
                case INPUT -> RecipeIngredientRole.INPUT;
                case OUTPUT -> RecipeIngredientRole.OUTPUT;
                case NONE -> throw new IllegalArgumentException();
            };
            addIngredients(slot, role, type, ingredients, rate);
        }

        default void item(Layout.SlotInfo slot, ItemStack stack) {
            addIngredients(slot, VanillaTypes.ITEM_STACK, List.of(stack), 1d);
        }

        default void ratedItem(Layout.SlotInfo slot, ItemStack stack, double rate) {
            addIngredients(slot, VanillaTypes.ITEM_STACK, List.of(stack), rate);
        }

        default void items(Layout.SlotInfo slot, List<ItemStack> stacks) {
            addIngredients(slot, VanillaTypes.ITEM_STACK, stacks, 1d);
        }

        default void ingredient(Layout.SlotInfo slot, Ingredient ingredient) {
            addIngredients(slot, VanillaTypes.ITEM_STACK, List.of(ingredient.getItems()), 1d);
        }

        default void fluid(Layout.SlotInfo slot, FluidStack stack) {
            addIngredients(slot, ForgeTypes.FLUID_STACK, List.of(stack), 1d);
        }

        default void ratedFluid(Layout.SlotInfo slot, FluidStack stack, double rate) {
            addIngredients(slot, ForgeTypes.FLUID_STACK, List.of(stack), rate);
        }
    }

    protected interface IDrawHelper {
        void drawProgressBar(PoseStack stack, int cycle);

        IDrawable getBackground();
    }

    protected ComposeDrawable.Builder buildBackground(ComposeDrawable.Builder builder,
        IGuiHelper helper, int xOffset) {
        builder.add(helper.createBlankDrawable(WIDTH, layout.rect.height()));
        for (var slot : layout.slots) {
            builder.add(helper.getSlotDrawable(), xOffset + slot.x(), slot.y());
        }
        for (var image : layout.images) {
            var rect = image.rect();
            var drawable = DrawableHelper.createStatic(helper, image.texture(), rect.width(), rect.height());
            builder.add(drawable, xOffset + rect.x(), rect.y());
        }
        return builder;
    }

    protected abstract void addRecipe(T recipe, IIngredientBuilder builder);

    protected void drawExtra(T recipe, IDrawHelper helper, IRecipeSlotsView recipeSlotsView,
        PoseStack stack, double mouseX, double mouseY) {}

    protected boolean canTransfer(M menu, T recipe) {
        return true;
    }

    protected List<Slot> getInventorySlots(M container) {
        var list = new ArrayList<Slot>();
        var slotSize = container.getSlotSize();
        for (var k = layout.slots.size(); k < slotSize; k++) {
            list.add(container.getSlot(k));
        }
        return list;
    }

    public static ResourceLocation categoryTitleId(ResourceLocation recipeTypeLoc) {
        return prepend(recipeTypeLoc, "jei/category");
    }

    protected Component categoryTitle() {
        return I18n.tr(categoryTitleId(recipeType.loc));
    }

    private class Category implements IRecipeCategory<T>, IDrawHelper {
        private final Component title;
        private final IDrawable icon;
        private final IDrawable background;
        private final int xOffset;

        @Nullable
        private final LoadingCache<Integer, IDrawable> cachedProgressBar;
        @Nullable
        private final Rect progressBarRect;

        private Category(IGuiHelper guiHelper) {
            this.title = categoryTitle();
            this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, iconItem);
            this.xOffset = (WIDTH - layout.rect.width()) / 2;
            this.background = createBackground(guiHelper, xOffset);

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

        private IDrawable createBackground(IGuiHelper helper, int xOffset) {
            var builder = ComposeDrawable.builder();
            return buildBackground(builder, helper, xOffset).build();
        }

        private class IngredientBuilder implements IIngredientBuilder {
            private final IRecipeLayoutBuilder builder;

            public IngredientBuilder(IRecipeLayoutBuilder builder) {
                this.builder = builder;
            }

            @Override
            public <I> void addIngredients(Layout.SlotInfo slot, RecipeIngredientRole role,
                IIngredientType<I> type, List<I> ingredients, double rate) {
                var x = slot.x() + 1 + xOffset;
                var y = slot.y() + 1;
                var slotBuilder = builder.addSlot(role, x, y).addIngredients(type, ingredients);
                if (rate >= 1d) {
                    if (type == ForgeTypes.FLUID_STACK) {
                        slotBuilder.setCustomRenderer(ForgeTypes.FLUID_STACK, FluidIngredientRenderer.INSTANCE);
                    } else if (type == TechIngredientType.INSTANCE) {
                        slotBuilder.setCustomRenderer(TechIngredientType.INSTANCE, TechIngredientRenderer.INSTANCE);
                    }
                } else {
                    if (type == VanillaTypes.ITEM_STACK) {
                        slotBuilder.setCustomRenderer(VanillaTypes.ITEM_STACK, new RatedItemIngredientRenderer(rate));
                    } else if (type == ForgeTypes.FLUID_STACK) {
                        slotBuilder.setCustomRenderer(ForgeTypes.FLUID_STACK, FluidIngredientRenderer.rated(rate));
                    }
                }
            }
        }

        @Override
        public void setRecipe(IRecipeLayoutBuilder builder, T recipe, IFocusGroup focuses) {
            var ingredientBuilder = new IngredientBuilder(builder);
            addRecipe(recipe, ingredientBuilder);
            builder.moveRecipeTransferButton(WIDTH - Menu.SLOT_SIZE, 0);
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

        @Override
        public void drawProgressBar(PoseStack stack, int cycle) {
            if (cachedProgressBar != null && progressBarRect != null) {
                var bar = cachedProgressBar.getUnchecked(cycle);
                bar.draw(stack, progressBarRect.x(), progressBarRect.y());
            }
        }

        @Override
        public void draw(T recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY) {
            drawExtra(recipe, this, recipeSlotsView, stack, mouseX, mouseY);
        }

        @Override
        public RecipeType<T> getRecipeType() {
            return type;
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
    }

    private class TransferInfo implements IRecipeTransferInfo<M, T> {
        private static final Slot EMPTY_SLOT = new SlotItemHandler(EmptyHandler.INSTANCE, 0, 0, 0);

        @Override
        public Class<M> getContainerClass() {
            return menuClazz;
        }

        @Override
        public boolean canHandle(M container, T recipe) {
            return canTransfer(container, recipe);
        }

        private class IngredientBuilder implements IIngredientBuilder {
            private final M container;
            private final List<Slot> slots = new ArrayList<>();

            public IngredientBuilder(M container) {
                this.container = container;
            }

            @Override
            public <I> void addIngredients(Layout.SlotInfo slot, RecipeIngredientRole role,
                IIngredientType<I> type, List<I> ingredients, double rate) {
                if (role != RecipeIngredientRole.INPUT) {
                    return;
                }
                if (slot.type() == SlotType.ITEM_INPUT) {
                    slots.add(container.getSlot(slot.index()));
                } else if (slot.type() == SlotType.FLUID_INPUT) {
                    slots.add(EMPTY_SLOT);
                }
            }
        }

        @Override
        public List<Slot> getRecipeSlots(M container, T recipe) {
            var builder = new IngredientBuilder(container);
            addRecipe(recipe, builder);
            return builder.slots;
        }

        @Override
        public List<Slot> getInventorySlots(M container, T recipe) {
            return RecipeCategory.this.getInventorySlots(container);
        }

        @Override
        public RecipeType<T> getRecipeType() {
            return type;
        }

        @SuppressWarnings({"removal", "unchecked"})
        @Override
        public Class<T> getRecipeClass() {
            return (Class<T>) type.getRecipeClass();
        }

        @SuppressWarnings("removal")
        @Override
        public ResourceLocation getRecipeCategoryUid() {
            return type.getUid();
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

    public void registerRecipes(IRecipeRegistration registration, RecipeManager recipeManager) {
        var list = recipeManager.getAllRecipesFor(recipeType.get()).stream()
            .map($ -> (T) $)
            .toList();
        registration.addRecipes(type, list);
    }

    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        var hasItem = layout.slots.stream().anyMatch(slot -> slot.type() == SlotType.ITEM_INPUT);
        if (hasItem) {
            registration.addRecipeTransferHandler(new TransferInfo());
        }
    }
}
