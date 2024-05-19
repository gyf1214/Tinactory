package org.shsts.tinactory.content.gui.client;

import com.mojang.blaze3d.MethodsReturnNonnullByDefault;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.gui.sync.SetMachinePacket;
import org.shsts.tinactory.content.model.ModelGen;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.gui.client.Button;
import org.shsts.tinactory.core.gui.client.MenuScreen;
import org.shsts.tinactory.core.gui.client.Panel;
import org.shsts.tinactory.core.gui.client.RenderUtil;
import org.shsts.tinactory.core.gui.client.SimpleButton;
import org.shsts.tinactory.core.gui.client.StretchImage;
import org.shsts.tinactory.core.gui.sync.MenuSyncPacket;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.core.util.ClientUtil;
import org.shsts.tinactory.core.util.MathUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.shsts.tinactory.core.gui.Menu.MARGIN_HORIZONTAL;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_TOP;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_VERTICAL;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.sync.MenuEventHandler.SET_MACHINE;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MachineRecipeBook extends Panel {
    private static final Texture RECIPE_BOOK_BUTTON = new Texture(
            ModelGen.mcLoc("gui/recipe_button"), 256, 256);
    private static final Texture RECIPE_BOOK_BG = new Texture(
            ModelGen.mcLoc("gui/recipe_book"), 256, 256);
    private static final Texture DISABLE_BUTTON = new Texture(
            ModelGen.modLoc("gui/disable_recipe"), 16, 16);
    private static final Texture RECIPE_BUTTON = new Texture(
            ModelGen.modLoc("gui/recipe_book_button"), 42, 21);

    private static final int BUTTON_PER_LINE = 4;
    private static final int BUTTON_SIZE = SLOT_SIZE + 3;
    private static final int PANEL_BORDER = 8;
    private static final int PANEL_WIDTH = BUTTON_SIZE * BUTTON_PER_LINE + PANEL_BORDER * 2;
    private static final RectD PANEL_ANCHOR = RectD.corners(0d, 0d, 0d, 1d);
    private static final Rect PANEL_OFFSET = Rect.corners(-MARGIN_HORIZONTAL - PANEL_WIDTH,
            -MARGIN_TOP, -MARGIN_HORIZONTAL, MARGIN_VERTICAL);
    private static final Rect BACKGROUND_TEX_RECT = new Rect(1, 1, 147, 166);
    private static final Rect INNER_PANEL_OFFSET = Rect.corners(PANEL_BORDER, PANEL_BORDER,
            -PANEL_BORDER, -PANEL_BORDER);
    private static final RectD PAGE_ANCHOR = new RectD(0.5d, 1d, 0d, 0d);
    private static final Rect PAGE_OFFSET = new Rect(0, -18, 12, 18);


    private class RecipeButton extends Button {
        @Nullable
        private ProcessingRecipe<?> recipe = null;

        public RecipeButton(Menu<?, ?> menu) {
            super(menu);
        }

        @Override
        public Optional<List<Component>> getTooltip() {
            if (recipe == null) {
                return Optional.of(List.of(new TranslatableComponent("tinactory.tooltip.unselectRecipe")));
            } else {
                // TODO
                return Optional.of(List.of(new TextComponent(recipe.getId().toString())));
            }
        }

        @Override
        public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
            var z = getBlitOffset();
            if (isCurrentRecipe(recipe)) {
                RenderUtil.blit(poseStack, RECIPE_BUTTON, z, rect, 21, 0);
            } else {
                RenderUtil.blit(poseStack, RECIPE_BUTTON, z, rect);
            }
            if (recipe == null) {
                RenderUtil.blit(poseStack, DISABLE_BUTTON, z, rect.offset(2, 2).enlarge(-5, -5));
            } else {
                var x = rect.x() + 2;
                var y = rect.y() + 2;
                var output = recipe.getResult();
                RenderUtil.renderIngredient(output,
                        stack -> RenderUtil.renderItem(stack, x, y),
                        stack -> RenderUtil.renderFluid(poseStack, stack, x, y, z));
            }
        }

        @Override
        public void onMouseClicked(double mouseX, double mouseY, int button) {
            super.onMouseClicked(mouseX, mouseY, button);
            if (recipe == null) {
                unselectRecipe();
            } else {
                selectRecipe(recipe);
            }
        }
    }

    private class ButtonPanel extends Panel {
        private int buttons;

        public ButtonPanel() {
            super(MachineRecipeBook.this.screen);
        }

        @Override
        protected void setRect(Rect rect) {
            var buttons = BUTTON_PER_LINE * Math.max(1, rect.height() / BUTTON_SIZE);
            var size = children.size();
            if (size <= buttons) {
                for (var i = size; i < buttons; i++) {
                    var x = (i % BUTTON_PER_LINE) * BUTTON_SIZE;
                    var y = (i / BUTTON_PER_LINE) * BUTTON_SIZE;
                    var offset = new Rect(x, y, BUTTON_SIZE, BUTTON_SIZE);
                    var button = new RecipeButton(menu);
                    button.setActive(active);
                    addWidget(offset, button);
                }
            } else {
                children.subList(buttons, children.size()).clear();
            }
            this.buttons = buttons;
            super.setRect(rect);
        }

        public void setDisableButton(int index) {
            if (children.get(index).child() instanceof RecipeButton button) {
                button.setActive(true);
                button.recipe = null;
            }
        }

        public void setRecipe(int index, @Nullable ProcessingRecipe<?> recipe) {
            if (children.get(index).child() instanceof RecipeButton button) {
                button.setActive(recipe != null);
                button.recipe = recipe;
            }
        }
    }

    private class PageButton extends SimpleButton {
        private static final int TEX_Y = 208;

        private final int pageChange;

        public PageButton(Menu<?, ?> menu, int texX, int pageChange) {
            super(menu, RECIPE_BOOK_BG, null, texX, TEX_Y, texX, TEX_Y + PAGE_OFFSET.height());
            this.pageChange = pageChange;
        }

        @Override
        protected void playDownSound() {
            ClientUtil.playSound(SoundEvents.BOOK_PAGE_TURN);
        }

        @Override
        public void onMouseClicked(double mouseX, double mouseY, int button) {
            super.onMouseClicked(mouseX, mouseY, button);
            setPage(page + pageChange);
        }
    }

    private final int syncSlot;
    private final List<ProcessingRecipe<?>> recipes = new ArrayList<>();
    private final Panel bookPanel;
    private final ButtonPanel buttonPanel;
    private final PageButton leftPageButton;
    private final PageButton rightPageButton;

    private int page = 0;

    public MachineRecipeBook(MenuScreen<? extends Menu<?, ?>> screen, int syncSlot,
                             RecipeType<? extends ProcessingRecipe<?>> recipeType,
                             int buttonX, int buttonY) {
        super(screen);
        this.syncSlot = syncSlot;

        var container = AllCapabilities.CONTAINER.get(screen.getMenu().blockEntity);
        for (var recipe : ClientUtil.getRecipeManager().getAllRecipesFor(recipeType)) {
            if (!recipe.canCraftIn(container)) {
                continue;
            }
            recipes.add(recipe);
        }

        this.bookPanel = new Panel(screen);
        bookPanel.addWidget(RectD.FULL, Rect.ZERO,
                new StretchImage(menu, RECIPE_BOOK_BG, BACKGROUND_TEX_RECT, PANEL_BORDER));

        this.buttonPanel = new ButtonPanel();
        this.leftPageButton = new PageButton(menu, 15, -1);
        this.rightPageButton = new PageButton(menu, 1, 1);

        var innerPanel = new Panel(screen);
        innerPanel.addPanel(Rect.corners(0, BUTTON_SIZE, 0, -BUTTON_SIZE), buttonPanel);
        innerPanel.addWidget(PAGE_ANCHOR, PAGE_OFFSET.offset(-BUTTON_SIZE - PAGE_OFFSET.width(), 0), leftPageButton);
        innerPanel.addWidget(PAGE_ANCHOR, PAGE_OFFSET.offset(BUTTON_SIZE, 0), rightPageButton);

        bookPanel.addPanel(INNER_PANEL_OFFSET, innerPanel);
        bookPanel.setActive(false);

        addPanel(PANEL_ANCHOR, PANEL_OFFSET, bookPanel);
        addWidget(new Rect(buttonX, buttonY, 20, 18), new SimpleButton(menu, RECIPE_BOOK_BUTTON,
                new TranslatableComponent("tinactory.tooltip.openRecipeBook"), 0, 19) {
            @Override
            public void onMouseClicked(double mouseX, double mouseY, int button) {
                super.onMouseClicked(mouseX, mouseY, button);
                bookPanel.setActive(!bookPanel.isActive());
                if (bookPanel.isActive()) {
                    setPage(page);
                }
            }
        });
    }

    @Override
    protected void setRect(Rect rect) {
        super.setRect(rect);
        if (bookPanel.isActive()) {
            setPage(page);
        }
    }

    private boolean isCurrentRecipe(@Nullable ProcessingRecipe<?> recipe) {
        var loc = menu.getSyncPacket(syncSlot, MenuSyncPacket.LocHolder.class)
                .flatMap(MenuSyncPacket.Holder::getData)
                .orElse(null);
        return recipe == null ? loc == null : recipe.getId().equals(loc);
    }

    private void unselectRecipe() {
        menu.triggerEvent(SET_MACHINE, SetMachinePacket.builder().reset("targetRecipe"));
    }

    private void selectRecipe(ProcessingRecipe<?> recipe) {
        menu.triggerEvent(SET_MACHINE, SetMachinePacket.builder().set("targetRecipe", recipe.getId()));
    }

    private void setPage(int index) {
        var buttons = buttonPanel.buttons;
        var maxPage = Math.max(1, (recipes.size() + buttons - 1) / buttons);
        var newPage = MathUtil.clamp(index, 0, maxPage - 1);
        leftPageButton.setActive(newPage != 0);
        rightPageButton.setActive(newPage != maxPage - 1);
        var offset = newPage * buttons - 1;
        for (var i = 0; i < buttons; i++) {
            var j = i + offset;
            if (j < 0) {
                buttonPanel.setDisableButton(i);
            } else if (j < recipes.size()) {
                buttonPanel.setRecipe(i, recipes.get(j));
            } else {
                buttonPanel.setRecipe(i, null);
            }
        }
        page = newPage;
    }
}
