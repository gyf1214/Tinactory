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
import org.shsts.tinactory.content.gui.sync.SetMachineEventPacket;
import org.shsts.tinactory.content.model.ModelGen;
import org.shsts.tinactory.core.gui.ContainerMenu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.gui.client.ContainerWidget;
import org.shsts.tinactory.core.gui.client.Panel;
import org.shsts.tinactory.core.gui.client.RenderUtil;
import org.shsts.tinactory.core.gui.client.SimpleButton;
import org.shsts.tinactory.core.gui.client.StretchImage;
import org.shsts.tinactory.core.gui.sync.ContainerEventHandler;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.core.util.ClientUtil;
import org.shsts.tinactory.core.util.MathUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.shsts.tinactory.core.gui.ContainerMenu.MARGIN_HORIZONTAL;
import static org.shsts.tinactory.core.gui.ContainerMenu.MARGIN_TOP;
import static org.shsts.tinactory.core.gui.ContainerMenu.SLOT_SIZE;

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

    private static final int BUTTON_PER_LINE = 4;
    private static final int BUTTON_SIZE = SLOT_SIZE + 3;
    private static final int BUTTON_BORDER = 3;
    private static final int PAGE_WIDTH = 12;
    private static final int PAGE_HEIGHT = 18;
    private static final int PANEL_BORDER = 8;
    private static final int PANEL_WIDTH = BUTTON_SIZE * BUTTON_PER_LINE + PANEL_BORDER * 2;
    private static final Rect BACKGROUND_TEX_RECT = new Rect(1, 1, 147, 166);
    private static final Rect BUTTON_TEX_RECT = new Rect(29, 206, 25, 25);


    private class RecipeButton extends ContainerWidget {
        private boolean isDisable = false;
        @Nullable
        private ProcessingRecipe<?> recipe = null;

        public RecipeButton(ContainerMenu<?> menu, int index) {
            super(menu, new Rect((index % BUTTON_PER_LINE) * BUTTON_SIZE,
                    (index / BUTTON_PER_LINE) * BUTTON_SIZE,
                    BUTTON_SIZE, BUTTON_SIZE));
        }

        @Override
        protected boolean canHover() {
            return isDisable || recipe != null;
        }

        @Override
        public Optional<List<Component>> getTooltip() {
            if (isDisable) {
                return Optional.of(List.of(new TranslatableComponent("tinactory.tooltip.unselectRecipe")));
            } else if (recipe == null) {
                return Optional.empty();
            }
            // TODO
            return Optional.of(List.of(new TextComponent(recipe.getId().toString())));
        }

        @Override
        public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
            if (isDisable || recipe != null) {
                StretchImage.render(poseStack, RECIPE_BOOK_BG, zIndex, rect,
                        BUTTON_TEX_RECT, BUTTON_BORDER);
            }
            if (isDisable) {
                RenderUtil.blit(poseStack, DISABLE_BUTTON, zIndex,
                        rect.offset(2, 2).enlarge(-5, -5));
            } else if (recipe != null) {
                var x = rect.x() + 2;
                var y = rect.y() + 2;
                var output = recipe.getResult();
                RenderUtil.renderIngredient(output,
                        stack -> RenderUtil.renderItem(stack, x, y),
                        stack -> RenderUtil.renderFluid(poseStack, stack,
                                new Rect(x, y, 16, 16), zIndex));
            }
        }

        @Override
        protected boolean canClick(int button) {
            return button == 0 && (isDisable || recipe != null);
        }

        @Override
        public void onMouseClicked(double mouseX, double mouseY, int button) {
            if (isDisable) {
                unselectRecipe();
            } else if (recipe != null) {
                selectRecipe(recipe);
            }
        }
    }

    private class ButtonPanel extends Panel {
        private int buttons;

        public ButtonPanel(ContainerMenu<?> menu) {
            super(menu, RectD.FULL, Rect.corners(0, BUTTON_SIZE, 0, -BUTTON_SIZE));
        }

        @Override
        protected void initChildren() {
            var buttons = BUTTON_PER_LINE * Math.max(1, rect.height() / BUTTON_SIZE);
            var size = children.size();
            if (size <= buttons) {
                for (var i = size; i < buttons; i++) {
                    children.add(new RecipeButton(menu, i));
                }
            } else {
                children.subList(buttons, children.size()).clear();
            }
            this.buttons = buttons;
            super.initChildren();
        }

        public void setDisableButton(int index) {
            if (children.get(index) instanceof RecipeButton button) {
                button.isDisable = true;
                button.recipe = null;
            }
        }

        public void setRecipe(int index, @Nullable ProcessingRecipe<?> recipe) {
            if (children.get(index) instanceof RecipeButton button) {
                button.recipe = recipe;
            }
        }
    }

    private class PageButton extends SimpleButton {
        private static final int TEX_Y = 208;

        private final int pageChange;
        private boolean visible = true;

        public PageButton(ContainerMenu<?> menu, int xOffset, int texX, int pageChange) {
            super(menu, RectD.corners(0.5, 1, 0.5, 1),
                    new Rect(xOffset, -PAGE_HEIGHT, PAGE_WIDTH, PAGE_HEIGHT),
                    RECIPE_BOOK_BG, null, texX, TEX_Y, texX, TEX_Y + PAGE_HEIGHT);
            this.pageChange = pageChange;
        }

        @Override
        protected boolean canHover() {
            return visible && super.canHover();
        }

        @Override
        protected boolean canClick(int button) {
            return visible && super.canClick(button);
        }

        @Override
        public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
            if (visible) {
                super.render(poseStack, mouseX, mouseY, partialTick);
            }
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

    private final List<ProcessingRecipe<?>> recipes;
    private final Panel bookPanel;
    private final ButtonPanel buttonPanel;
    private final PageButton leftPageButton;
    private final PageButton rightPageButton;

    private int page;

    public MachineRecipeBook(ContainerMenu<?> menu, RecipeType<? extends ProcessingRecipe<?>> recipeType,
                             int buttonX, int buttonY) {
        super(menu);
        this.recipes = new ArrayList<>(ClientUtil.getRecipeManager().getAllRecipesFor(recipeType));

        this.bookPanel = new Panel(menu, RectD.corners(0d, 0d, 0d, 1d),
                Rect.corners(-MARGIN_HORIZONTAL - PANEL_WIDTH, -MARGIN_TOP, -MARGIN_HORIZONTAL, 0));
        this.bookPanel.addWidget(new StretchImage(menu, RectD.FULL, Rect.ZERO,
                RECIPE_BOOK_BG, BACKGROUND_TEX_RECT, PANEL_BORDER));
        this.bookPanel.visible = false;

        this.buttonPanel = new ButtonPanel(menu);
        this.leftPageButton = new PageButton(menu, -BUTTON_SIZE - PAGE_WIDTH, 15, -1);
        this.rightPageButton = new PageButton(menu, BUTTON_SIZE, 1, 1);

        var innerPanel = new Panel(menu, RectD.FULL,
                Rect.corners(PANEL_BORDER, PANEL_BORDER, -PANEL_BORDER, -PANEL_BORDER));

        innerPanel.addWidget(buttonPanel);
        innerPanel.addWidget(leftPageButton);
        innerPanel.addWidget(rightPageButton);
        bookPanel.addWidget(innerPanel);
        addWidget(bookPanel);
        addWidget(new SimpleButton(menu, new Rect(buttonX, buttonY, 20, 18), RECIPE_BOOK_BUTTON,
                new TranslatableComponent("tinactory.tooltip.openRecipeBook"), 0, 19) {
            @Override
            public void onMouseClicked(double mouseX, double mouseY, int button) {
                super.onMouseClicked(mouseX, mouseY, button);
                bookPanel.visible = !bookPanel.visible;
            }
        });
    }

    @Override
    public void init(Rect parent) {
        super.init(parent);
        setPage(0);
    }

    private void unselectRecipe() {
        menu.triggerEvent(ContainerEventHandler.SET_MACHINE,
                SetMachineEventPacket.builder().resetTargetRecipe());
    }

    private void selectRecipe(ProcessingRecipe<?> recipe) {
        menu.triggerEvent(ContainerEventHandler.SET_MACHINE,
                SetMachineEventPacket.builder().targetRecipeLoc(recipe.getId()));
    }

    private void setPage(int index) {
        var buttons = buttonPanel.buttons;
        var maxPage = Math.max(1, (recipes.size() + buttons - 1) / buttons);
        var newPage = MathUtil.clamp(index, 0, maxPage - 1);
        leftPageButton.visible = newPage != 0;
        rightPageButton.visible = newPage != maxPage - 1;
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
