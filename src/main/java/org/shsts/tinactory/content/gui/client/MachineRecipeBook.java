package org.shsts.tinactory.content.gui.client;

import com.mojang.blaze3d.MethodsReturnNonnullByDefault;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.content.model.ModelGen;
import org.shsts.tinactory.core.gui.ContainerMenu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.gui.client.ContainerWidget;
import org.shsts.tinactory.core.gui.client.Panel;
import org.shsts.tinactory.core.gui.client.SimpleButton;
import org.shsts.tinactory.core.gui.client.StretchImage;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.core.util.ClientUtil;
import org.shsts.tinactory.core.util.MathUtil;
import org.slf4j.Logger;

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
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Texture RECIPE_BOOK_BUTTON = new Texture(
            ModelGen.mcLoc("gui/recipe_button"), 256, 256);
    private static final Texture RECIPE_BOOK_BG = new Texture(
            ModelGen.mcLoc("gui/recipe_book"), 256, 256);

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
        @Nullable
        private ProcessingRecipe<?> recipe;

        public RecipeButton(ContainerMenu<?> menu, int index) {
            super(menu, new Rect((index % BUTTON_PER_LINE) * BUTTON_SIZE,
                    (index / BUTTON_PER_LINE) * BUTTON_SIZE,
                    BUTTON_SIZE, BUTTON_SIZE));
        }

        @Override
        protected boolean canHover() {
            return this.recipe != null;
        }

        @Override
        public Optional<List<Component>> getTooltip() {
            if (this.recipe == null) {
                return Optional.empty();
            }
            // TODO
            return Optional.of(List.of(new TextComponent(recipe.getId().toString())));
        }

        @Override
        public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
            if (this.recipe != null) {
                StretchImage.render(poseStack, RECIPE_BOOK_BG, this.zIndex, this.rect,
                        BUTTON_TEX_RECT, BUTTON_BORDER);
                var item = this.recipe.getResultItem();
                var x = this.rect.x() + 2;
                var y = this.rect.y() + 2;
                ClientUtil.getItemRenderer().renderGuiItem(item, x, y);
            }
        }

        @Override
        protected boolean canClick() {
            return this.recipe != null;
        }

        @Override
        public void onMouseClicked(double mouseX, double mouseY, int button) {
            if (this.recipe != null) {
                selectRecipe(this.recipe);
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
            var buttons = BUTTON_PER_LINE * Math.max(1, this.rect.height() / BUTTON_SIZE);
            var size = this.children.size();
            if (size <= buttons) {
                for (var i = size; i < buttons; i++) {
                    this.children.add(new RecipeButton(this.menu, i));
                }
            } else {
                this.children.subList(buttons, this.children.size()).clear();
            }
            this.buttons = buttons;
            super.initChildren();
        }

        public void setRecipe(int index, @Nullable ProcessingRecipe<?> recipe) {
            if (this.children.get(index) instanceof RecipeButton button) {
                button.recipe = recipe;
            }
        }
    }

    private final List<ProcessingRecipe<?>> recipes;
    private final Panel bookPanel;
    private final ButtonPanel buttonPanel;

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

        var innerPanel = new Panel(menu, RectD.FULL,
                Rect.corners(PANEL_BORDER, PANEL_BORDER, -PANEL_BORDER, -PANEL_BORDER));

        this.buttonPanel = new ButtonPanel(menu);
        innerPanel.addWidget(this.buttonPanel);
        innerPanel.addWidget(new SimpleButton(menu, RectD.corners(0.5, 1, 0.5, 1),
                Rect.corners(-BUTTON_SIZE - PAGE_WIDTH, -PAGE_HEIGHT, -BUTTON_SIZE, 0),
                RECIPE_BOOK_BG, null, 15, 208, 15, 208 + PAGE_HEIGHT) {
            @Override
            public void onMouseClicked(double mouseX, double mouseY, int button) {
                setPage(page + 1);
            }
        });
        innerPanel.addWidget(new SimpleButton(menu, RectD.corners(0.5, 1, 0.5, 1),
                Rect.corners(BUTTON_SIZE, -PAGE_HEIGHT, BUTTON_SIZE + PAGE_WIDTH, 0),
                RECIPE_BOOK_BG, null, 1, 208, 1, 208 + PAGE_HEIGHT) {
            @Override
            public void onMouseClicked(double mouseX, double mouseY, int button) {
                setPage(page + 1);
            }
        });

        this.bookPanel.addWidget(innerPanel);
        this.addWidget(this.bookPanel);
        this.addWidget(new SimpleButton(menu, new Rect(buttonX, buttonY, 20, 18), RECIPE_BOOK_BUTTON,
                new TranslatableComponent("tinactory.tooltip.openRecipeBook"), 0, 19) {
            @Override
            public void onMouseClicked(double mouseX, double mouseY, int button) {
                if (button == 0) {
                    bookPanel.visible = !bookPanel.visible;
                }
            }
        });
    }

    @Override
    public void init(Rect parent) {
        super.init(parent);
        this.setPage(0);
    }

    private void selectRecipe(ProcessingRecipe<?> recipe) {
        LOGGER.debug("select recipe {}", recipe);
    }

    private void setPage(int index) {
        var buttons = this.buttonPanel.buttons;
        var maxPage = Math.max(1, (this.recipes.size() + buttons - 1) / buttons);
        this.page = MathUtil.clamp(index, 0, maxPage - 1);
        var offset = this.page * buttons;
        for (var i = 0; i < buttons; i++) {
            if (i + offset < this.recipes.size()) {
                this.buttonPanel.setRecipe(i, this.recipes.get(i + offset));
            } else {
                this.buttonPanel.setRecipe(i, null);
            }
        }
    }
}
