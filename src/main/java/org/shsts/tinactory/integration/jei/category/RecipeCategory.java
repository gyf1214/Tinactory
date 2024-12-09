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
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
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
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.api.logistics.SlotType;
import org.shsts.tinactory.core.common.SmartRecipe;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.util.I18n;
import org.shsts.tinactory.integration.jei.ComposeDrawable;
import org.shsts.tinactory.integration.jei.DrawableHelper;
import org.shsts.tinactory.integration.jei.ingredient.IngredientRenderers;
import org.shsts.tinactory.registrate.common.RecipeTypeEntry;

import java.util.ArrayList;
import java.util.List;

import static org.shsts.tinactory.core.util.LocHelper.prepend;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class RecipeCategory<T extends SmartRecipe<?>, M extends AbstractContainerMenu> {
    protected static final int WIDTH = Menu.CONTENT_WIDTH;

    protected final RecipeTypeEntry<? extends T, ?> recipeType;
    public final RecipeType<T> type;
    protected final Layout layout;
    protected final int xOffset;
    protected final Ingredient catalyst;
    protected final ItemStack iconItem;
    protected final Class<M> menuClazz;

    protected RecipeCategory(RecipeTypeEntry<? extends T, ?> recipeType, Layout layout, Ingredient catalyst,
        ItemStack iconItem, Class<M> menuClazz) {
        this.recipeType = recipeType;
        this.type = new RecipeType<>(prepend(recipeType.loc, "jei/category"), recipeType.clazz);
        this.layout = layout;
        this.xOffset = (WIDTH - layout.rect.width()) / 2;
        this.catalyst = catalyst;
        this.iconItem = iconItem;
        this.menuClazz = menuClazz;
    }

    protected interface IIngredientBuilder {
        void itemInput(Layout.SlotInfo slot, List<ItemStack> item);

        default void itemInput(Layout.SlotInfo slot, ItemStack item) {
            itemInput(slot, List.of(item));
        }

        default void ingredientInput(Layout.SlotInfo slot, Ingredient ingredient) {
            itemInput(slot, List.of(ingredient.getItems()));
        }

        void itemNotConsumedInput(Layout.SlotInfo slot, List<ItemStack> item);

        void fluidInput(Layout.SlotInfo slot, FluidStack fluid);

        void itemOutput(Layout.SlotInfo slot, ItemStack item);

        void ratedItemOutput(Layout.SlotInfo slot, ItemStack item, double rate);

        default void itemOutput(Layout.SlotInfo slot, ItemStack item, double rate) {
            if (rate >= 1d) {
                itemOutput(slot, item);
            } else {
                ratedItemOutput(slot, item, rate);
            }
        }

        void fluidOutput(Layout.SlotInfo slot, FluidStack fluid);

        void ratedFluidOutput(Layout.SlotInfo slot, FluidStack fluid, double rate);

        default void fluidOutput(Layout.SlotInfo slot, FluidStack fluid, double rate) {
            if (rate >= 1d) {
                fluidOutput(slot, fluid);
            } else {
                ratedFluidOutput(slot, fluid, rate);
            }
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

    protected abstract void setRecipe(T recipe, IIngredientBuilder builder);

    protected void extraLayout(T recipe, IRecipeLayoutBuilder builder) {}

    protected void drawExtra(T recipe, IDrawHelper helper, IRecipeSlotsView recipeSlotsView,
        PoseStack stack, double mouseX, double mouseY) {}

    protected boolean canTransfer(M menu, T recipe) {
        return true;
    }

    protected List<Slot> getInventorySlots(M container) {
        var list = new ArrayList<Slot>();
        var slotSize = container.slots.size();
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

        @Nullable
        private final LoadingCache<Integer, IDrawable> cachedProgressBar;
        @Nullable
        private final Rect progressBarRect;

        private Category(IGuiHelper guiHelper) {
            this.title = categoryTitle();
            this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, iconItem);
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

            private IRecipeSlotBuilder addSlot(Layout.SlotInfo slot, RecipeIngredientRole role) {
                var x = slot.x() + 1 + xOffset;
                var y = slot.y() + 1;
                return builder.addSlot(role, x, y);
            }

            @Override
            public void itemInput(Layout.SlotInfo slot, List<ItemStack> item) {
                addSlot(slot, RecipeIngredientRole.INPUT)
                    .addIngredients(VanillaTypes.ITEM_STACK, item);
            }

            @Override
            public void itemNotConsumedInput(Layout.SlotInfo slot, List<ItemStack> item) {
                addSlot(slot, RecipeIngredientRole.INPUT)
                    .addIngredients(VanillaTypes.ITEM_STACK, item)
                    .setCustomRenderer(VanillaTypes.ITEM_STACK, IngredientRenderers.ITEM_NOT_CONSUMED);
            }

            @Override
            public void fluidInput(Layout.SlotInfo slot, FluidStack fluid) {
                addSlot(slot, RecipeIngredientRole.INPUT)
                    .addIngredient(ForgeTypes.FLUID_STACK, fluid)
                    .setCustomRenderer(ForgeTypes.FLUID_STACK, IngredientRenderers.FLUID);
            }

            @Override
            public void itemOutput(Layout.SlotInfo slot, ItemStack item) {
                addSlot(slot, RecipeIngredientRole.OUTPUT)
                    .addIngredient(VanillaTypes.ITEM_STACK, item);
            }

            @Override
            public void ratedItemOutput(Layout.SlotInfo slot, ItemStack item, double rate) {
                addSlot(slot, RecipeIngredientRole.OUTPUT)
                    .addIngredient(VanillaTypes.ITEM_STACK, item)
                    .setCustomRenderer(VanillaTypes.ITEM_STACK, IngredientRenderers.ratedItem(rate));
            }

            @Override
            public void fluidOutput(Layout.SlotInfo slot, FluidStack fluid) {
                addSlot(slot, RecipeIngredientRole.OUTPUT)
                    .addIngredient(ForgeTypes.FLUID_STACK, fluid)
                    .setCustomRenderer(ForgeTypes.FLUID_STACK, IngredientRenderers.FLUID);
            }

            @Override
            public void ratedFluidOutput(Layout.SlotInfo slot, FluidStack fluid, double rate) {
                addSlot(slot, RecipeIngredientRole.OUTPUT)
                    .addIngredient(ForgeTypes.FLUID_STACK, fluid)
                    .setCustomRenderer(ForgeTypes.FLUID_STACK, IngredientRenderers.ratedFluid(rate));
            }
        }

        @Override
        public void setRecipe(IRecipeLayoutBuilder builder, T recipe, IFocusGroup focuses) {
            var ingredientBuilder = new IngredientBuilder(builder);
            RecipeCategory.this.setRecipe(recipe, ingredientBuilder);
            extraLayout(recipe, builder);
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

            private void itemInput(Layout.SlotInfo slot) {
                slots.add(container.getSlot(slot.index()));
            }

            @Override
            public void itemInput(Layout.SlotInfo slot, List<ItemStack> item) {
                itemInput(slot);
            }

            @Override
            public void itemNotConsumedInput(Layout.SlotInfo slot, List<ItemStack> item) {
                itemInput(slot);
            }

            @Override
            public void fluidInput(Layout.SlotInfo slot, FluidStack fluid) {}

            @Override
            public void itemOutput(Layout.SlotInfo slot, ItemStack item) {}

            @Override
            public void ratedItemOutput(Layout.SlotInfo slot, ItemStack item, double rate) {}

            @Override
            public void fluidOutput(Layout.SlotInfo slot, FluidStack fluid) {}

            @Override
            public void ratedFluidOutput(Layout.SlotInfo slot, FluidStack fluid, double rate) {}
        }

        @Override
        public List<Slot> getRecipeSlots(M container, T recipe) {
            var builder = new IngredientBuilder(container);
            setRecipe(recipe, builder);
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
