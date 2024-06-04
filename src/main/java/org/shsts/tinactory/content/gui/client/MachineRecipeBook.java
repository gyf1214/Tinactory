package org.shsts.tinactory.content.gui.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.gui.sync.SetMachinePacket;
import org.shsts.tinactory.content.machine.MachineConfig;
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
import org.shsts.tinactory.core.util.ClientUtil;
import org.shsts.tinactory.core.util.I18n;
import org.shsts.tinactory.core.util.MathUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.shsts.tinactory.core.gui.Menu.MARGIN_HORIZONTAL;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_TOP;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_VERTICAL;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.sync.MenuEventHandler.SET_MACHINE;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class MachineRecipeBook<T> extends Panel {
    private static final int BUTTON_SIZE = SLOT_SIZE + 3;
    private static final int BUTTON_PER_LINE = 4;
    public static final int BUTTON_TOP_MARGIN = BUTTON_SIZE;
    public static final int PANEL_BORDER = 8;
    private static final int PANEL_WIDTH = BUTTON_SIZE * BUTTON_PER_LINE + PANEL_BORDER * 2;
    public static final RectD PANEL_ANCHOR = RectD.corners(0d, 0d, 0d, 1d);
    public static final Rect PANEL_OFFSET = Rect.corners(-MARGIN_HORIZONTAL - PANEL_WIDTH,
            -MARGIN_TOP, -MARGIN_HORIZONTAL, MARGIN_VERTICAL);
    public static final Rect BACKGROUND_TEX_RECT = new Rect(1, 1, 147, 166);
    private static final Rect INNER_PANEL_OFFSET = Rect.corners(PANEL_BORDER, PANEL_BORDER,
            -PANEL_BORDER, -PANEL_BORDER);
    private static final RectD PAGE_ANCHOR = new RectD(0.5, 1d, 0d, 0d);
    private static final Rect PAGE_OFFSET = new Rect(0, -18, 12, 18);

    private static final Texture RECIPE_BOOK_BUTTON = new Texture(
            ModelGen.mcLoc("gui/recipe_button"), 256, 256);
    public static final Texture RECIPE_BOOK_BG = new Texture(
            ModelGen.mcLoc("gui/recipe_book"), 256, 256);
    private static final Texture DISABLE_BUTTON = new Texture(
            ModelGen.modLoc("gui/disable_recipe"), 16, 16);
    private static final Texture RECIPE_BUTTON = new Texture(
            ModelGen.modLoc("gui/recipe_book_button"), 42, 21);

    private class RecipeButton extends Button {
        @Nullable
        private ResourceLocation loc = null;
        @Nullable
        private T recipe = null;

        public RecipeButton(Menu<?, ?> menu) {
            super(menu);
        }

        @Override
        public Optional<List<Component>> getTooltip() {
            if (recipe == null) {
                return Optional.of(List.of(I18n.tr("tinactory.tooltip.unselectRecipe")));
            } else {
                return buttonToolTip(recipe);
            }
        }

        @Override
        public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
            var z = getBlitOffset();
            if (Objects.equals(getCurrentRecipeLoc(), loc)) {
                RenderUtil.blit(poseStack, RECIPE_BUTTON, z, rect, 21, 0);
            } else {
                RenderUtil.blit(poseStack, RECIPE_BUTTON, z, rect);
            }
            if (recipe == null) {
                RenderUtil.blit(poseStack, DISABLE_BUTTON, z, rect.offset(2, 2).enlarge(-5, -5));
            } else {
                renderButton(poseStack, mouseX, mouseY, partialTick, recipe, rect, z);
            }
        }

        @Override
        public void onMouseClicked(double mouseX, double mouseY, int button) {
            super.onMouseClicked(mouseX, mouseY, button);
            ghostRecipe.clear();
            if (recipe == null || loc == null) {
                menu.triggerEvent(SET_MACHINE, SetMachinePacket.builder().reset("targetRecipe"));
            } else {
                menu.triggerEvent(SET_MACHINE, SetMachinePacket.builder().set("targetRecipe", loc));
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

        @SuppressWarnings("unchecked")
        private RecipeButton getButton(int index) {
            return (RecipeButton) children.get(index).child();
        }

        public void setDisableButton(int index) {
            var button = getButton(index);
            button.setActive(true);
            button.recipe = null;
            button.loc = null;
        }

        public void setRecipe(int index, ResourceLocation loc, T recipe) {
            var button = getButton(index);
            button.setActive(true);
            button.recipe = recipe;
            button.loc = loc;
        }

        public void setHide(int index) {
            var button = getButton(index);
            button.setActive(false);
            button.recipe = null;
            button.loc = null;
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

    protected final MachineConfig machineConfig;
    protected final Panel bookPanel;
    protected final GhostRecipe ghostRecipe;
    private final ButtonPanel buttonPanel;
    private final PageButton leftPageButton;
    private final PageButton rightPageButton;

    protected final Map<ResourceLocation, T> recipes = new HashMap<>();
    private final List<ResourceLocation> recipeList = new ArrayList<>();
    protected int page = 0;

    public MachineRecipeBook(MenuScreen<? extends Menu<?, ?>> screen,
                             int buttonX, int buttonY, int xOffset) {
        super(screen);
        this.machineConfig = AllCapabilities.MACHINE.get(screen.getMenu().blockEntity).config;
        this.bookPanel = new Panel(screen);
        this.ghostRecipe = new GhostRecipe(screen.getMenu());
        this.buttonPanel = new ButtonPanel();
        this.leftPageButton = new PageButton(menu, 15, -1);
        this.rightPageButton = new PageButton(menu, 1, 1);

        bookPanel.addWidget(RectD.FULL, Rect.ZERO,
                new StretchImage(menu, RECIPE_BOOK_BG, BACKGROUND_TEX_RECT, PANEL_BORDER));

        var innerPanel = new Panel(screen);
        innerPanel.addPanel(Rect.corners(0, BUTTON_TOP_MARGIN, 0, -BUTTON_SIZE), buttonPanel);
        innerPanel.addWidget(PAGE_ANCHOR, PAGE_OFFSET.offset(-BUTTON_SIZE - PAGE_OFFSET.width(), 0), leftPageButton);
        innerPanel.addWidget(PAGE_ANCHOR, PAGE_OFFSET.offset(BUTTON_SIZE, 0), rightPageButton);

        bookPanel.addPanel(INNER_PANEL_OFFSET, innerPanel);
        bookPanel.setActive(false);

        addPanel(PANEL_ANCHOR, PANEL_OFFSET, bookPanel);
        addWidget(new Rect(buttonX, buttonY, 20, 18), new SimpleButton(menu, RECIPE_BOOK_BUTTON,
                I18n.tr("tinactory.tooltip.openRecipeBook"), 0, 19) {
            @Override
            public void onMouseClicked(double mouseX, double mouseY, int button) {
                super.onMouseClicked(mouseX, mouseY, button);
                bookPanel.setActive(!bookPanel.isActive());
                if (bookPanel.isActive()) {
                    setPage(page);
                }
            }
        });
        addWidget(new Rect(xOffset, 0, 0, 0), ghostRecipe);
    }

    @Override
    protected void initPanel() {
        refreshRecipes();
        ghostRecipe.clear();
        var loc = getCurrentRecipeLoc();
        if (loc != null && recipes.containsKey(loc)) {
            selectRecipe(recipes.get(loc));
        }
        super.initPanel();
    }

    public void remove() {}

    @Override
    protected void setRect(Rect rect) {
        super.setRect(rect);
        if (bookPanel.isActive()) {
            setPage(page);
        }
    }

    @Nullable
    private ResourceLocation getCurrentRecipeLoc() {
        return machineConfig.getLoc("targetRecipe").orElse(null);
    }

    protected abstract void doRefreshRecipes();

    protected abstract void selectRecipe(T recipe);

    protected abstract Optional<List<Component>> buttonToolTip(T recipe);

    protected abstract void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick,
                                         T recipe, Rect rect, int z);

    protected void refreshRecipes() {
        recipes.clear();
        doRefreshRecipes();
        recipeList.clear();
        recipeList.addAll(recipes.keySet().stream()
                .sorted(Comparator.comparing(Objects::toString))
                .toList());
    }

    protected void setPage(int index) {
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
                var loc = recipeList.get(j);
                var recipe = recipes.get(loc);
                buttonPanel.setRecipe(i, loc, recipe);
            } else {
                buttonPanel.setHide(i);
            }
        }
        page = newPage;
    }
}
