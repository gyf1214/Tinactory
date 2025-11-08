package org.shsts.tinactory.content.gui.client;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.machine.IMachineConfig;
import org.shsts.tinactory.api.tech.ITeamProfile;
import org.shsts.tinactory.content.gui.sync.SetMachineConfigPacket;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.core.gui.client.ButtonPanel;
import org.shsts.tinactory.core.gui.client.IRecipeBookItem;
import org.shsts.tinactory.core.gui.client.IWidgetConsumer;
import org.shsts.tinactory.core.gui.client.Panel;
import org.shsts.tinactory.core.gui.client.RenderUtil;
import org.shsts.tinactory.core.gui.client.SimpleButton;
import org.shsts.tinactory.core.gui.client.StretchImage;
import org.shsts.tinactory.core.machine.MachineProcessor;
import org.shsts.tinactory.core.tech.TechManager;
import org.shsts.tinactory.core.util.I18n;
import org.shsts.tinycorelib.api.gui.MenuBase;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import static org.shsts.tinactory.content.AllCapabilities.MACHINE;
import static org.shsts.tinactory.content.AllMenus.SET_MACHINE_CONFIG;
import static org.shsts.tinactory.core.gui.Menu.BUTTON_SIZE;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_TOP;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_VERTICAL;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_X;
import static org.shsts.tinactory.core.gui.Texture.DISABLE_BUTTON;
import static org.shsts.tinactory.core.gui.Texture.RECIPE_BOOK_BG;
import static org.shsts.tinactory.core.gui.Texture.RECIPE_BOOK_BUTTON;
import static org.shsts.tinactory.core.gui.Texture.RECIPE_BUTTON;
import static org.shsts.tinactory.core.gui.client.Widgets.BUTTON_PANEL_TEX;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MachineRecipeBook extends Panel {
    private static final int BUTTON_PER_LINE = 4;
    public static final int BUTTON_TOP_MARGIN = BUTTON_SIZE / 2;
    public static final int PANEL_BORDER = 8;
    private static final int PANEL_WIDTH = BUTTON_SIZE * BUTTON_PER_LINE + PANEL_BORDER * 2;
    public static final RectD PANEL_ANCHOR = RectD.corners(0d, 0d, 0d, 1d);
    public static final Rect PANEL_OFFSET = Rect.corners(-MARGIN_X - PANEL_WIDTH,
        -MARGIN_TOP, -MARGIN_X, MARGIN_VERTICAL);
    private static final Rect BUTTON_PANEL_OFFSET = Rect.corners(PANEL_BORDER,
        PANEL_BORDER + BUTTON_TOP_MARGIN, -PANEL_BORDER, -PANEL_BORDER);

    private class RecipeButtonPanel extends ButtonPanel {
        public RecipeButtonPanel() {
            super(MachineRecipeBook.this.screen, BUTTON_SIZE, BUTTON_SIZE, 0);
        }

        @Override
        protected int getItemCount() {
            return recipes.size() + 1;
        }

        @Nullable
        public IRecipeBookItem getRecipe(int index) {
            return index >= 1 && index < recipes.size() + 1 ? recipes.get(index - 1) : null;
        }

        @Override
        protected void renderButton(PoseStack poseStack, int mouseX, int mouseY,
            float partialTick, Rect rect, int index, boolean isHovering) {
            var recipe = getRecipe(index);
            var loc = recipe == null ? null : recipe.loc();
            var z = getBlitOffset();
            if (Objects.equals(getCurrentRecipeLoc(), loc)) {
                RenderUtil.blit(poseStack, RECIPE_BUTTON, z, rect, 21, 0);
            } else {
                RenderUtil.blit(poseStack, RECIPE_BUTTON, z, rect);
            }
            if (recipe == null) {
                RenderUtil.blit(poseStack, DISABLE_BUTTON, z, rect.offset(2, 2).enlarge(-5, -5));
            } else {
                recipe.renderButton(poseStack, rect, z);
            }
        }

        @Override
        protected void onSelect(int index, double mouseX, double mouseY, int button) {
            var recipe = getRecipe(index);
            var loc = recipe == null ? null : recipe.loc();
            ghostRecipe.clear();
            if (recipe == null) {
                menu.triggerEvent(SET_MACHINE_CONFIG,
                    SetMachineConfigPacket.builder().reset("targetRecipe"));
            } else {
                menu.triggerEvent(SET_MACHINE_CONFIG,
                    SetMachineConfigPacket.builder().set("targetRecipe", loc));
                recipe.select(layout, ghostRecipe);
            }
        }

        @Override
        protected Optional<List<Component>> buttonTooltip(int index, double mouseX, double mouseY) {
            var recipe = getRecipe(index);
            if (recipe == null) {
                return Optional.of(List.of(I18n.tr("tinactory.tooltip.unselectRecipe")));
            } else {
                return recipe.buttonToolTip();
            }
        }

        private int findCurrentIndex() {
            var currentLoc = getCurrentRecipeLoc();
            if (currentLoc == null) {
                return 0;
            }
            for (var i = 0; i < recipes.size(); i++) {
                if (currentLoc.equals(recipes.get(i).loc())) {
                    return i + 1;
                }
            }
            return -1;
        }

        @Override
        protected void doRefresh() {
            var buttonCount = buttons.size();
            if (buttonCount > 0) {
                var currentIndex = findCurrentIndex();
                if (currentIndex >= 0) {
                    page = currentIndex / buttonCount;
                }
            }
            super.doRefresh();
        }
    }

    private final BlockEntity blockEntity;
    private final IMachineConfig machineConfig;
    private final Layout layout;
    private final Panel bookPanel;
    private final ButtonPanel buttonPanel;
    private final GhostRecipe ghostRecipe;
    private final List<IRecipeBookItem> recipes = new ArrayList<>();
    private final Consumer<ITeamProfile> onTechChange = $ -> onTechChange();

    public MachineRecipeBook(ProcessingScreen screen) {
        super(screen);
        this.blockEntity = menu.blockEntity();
        this.machineConfig = MACHINE.get(blockEntity).config();
        this.bookPanel = new Panel(screen);
        this.layout = screen.menu().layout();
        this.ghostRecipe = new GhostRecipe(menu);

        buttonPanel = new RecipeButtonPanel();
        var panelBg = new StretchImage(menu, RECIPE_BOOK_BG, BUTTON_PANEL_TEX, PANEL_BORDER);
        bookPanel.addWidget(RectD.FULL, Rect.ZERO, panelBg);
        bookPanel.addPanel(BUTTON_PANEL_OFFSET, buttonPanel);
        bookPanel.setActive(false);

        addPanel(PANEL_ANCHOR, PANEL_OFFSET, bookPanel);
        addWidget(new Rect(layout.getXOffset(), 0, 0, 0), ghostRecipe);
    }

    @Override
    protected void initPanel() {
        refreshRecipes();
        ghostRecipe.clear();
        var loc = getCurrentRecipeLoc();
        if (loc != null) {
            for (var recipe : recipes) {
                if (loc.equals(recipe.loc())) {
                    recipe.select(layout, ghostRecipe);
                    break;
                }
            }
        }
        super.initPanel();
    }

    @Nullable
    private ResourceLocation getCurrentRecipeLoc() {
        return machineConfig.getLoc("targetRecipe").orElse(null);
    }

    public void remove() {
        TechManager.client().removeProgressChangeListener(onTechChange);
    }

    private void refreshRecipes() {
        recipes.clear();
        var processor = MACHINE.tryGet(blockEntity).flatMap(IMachine::processor);
        if (processor.isEmpty() || !(processor.get() instanceof MachineProcessor machineProcessor)) {
            return;
        }
        var items = machineProcessor.targetRecipes().getValue();
        recipes.addAll(items);
    }

    public void setBookActive(boolean value) {
        bookPanel.setActive(value);
    }

    public boolean isBookActive() {
        return bookPanel.isActive();
    }

    @Override
    public boolean mouseIn(double mouseX, double mouseY) {
        return bookPanel.mouseIn(mouseX, mouseY);
    }

    private void onTechChange() {
        refreshRecipes();
        buttonPanel.refresh();
    }

    public static void addButton(MenuBase menu, IWidgetConsumer parent, MachineRecipeBook recipeBook,
        RectD anchor, int x, int y, Runnable extraCallback) {
        var button = new SimpleButton(menu, RECIPE_BOOK_BUTTON,
            I18n.tr("tinactory.tooltip.openRecipeBook"), 0, 19) {
            @Override
            public void onMouseClicked(double mouseX, double mouseY, int button) {
                super.onMouseClicked(mouseX, mouseY, button);
                recipeBook.setBookActive(!recipeBook.isBookActive());
                extraCallback.run();
            }
        };
        parent.addWidget(anchor, new Rect(x, y, 20, 18), button);
    }

    public static Optional<IRecipeBookItem> getHoveredRecipe(Widget widget) {
        if (!(widget instanceof ButtonPanel.ItemButton button) ||
            !(button.getParent() instanceof RecipeButtonPanel buttonPanel)) {
            return Optional.empty();
        }
        return Optional.ofNullable(buttonPanel.getRecipe(button.getIndex()));
    }
}
