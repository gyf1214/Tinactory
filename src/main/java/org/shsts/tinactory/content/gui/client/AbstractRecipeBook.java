package org.shsts.tinactory.content.gui.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.gui.sync.SetMachineConfigPacket;
import org.shsts.tinactory.content.machine.MachineConfig;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.gui.client.ButtonPanel;
import org.shsts.tinactory.core.gui.client.MenuScreen;
import org.shsts.tinactory.core.gui.client.Panel;
import org.shsts.tinactory.core.gui.client.RenderUtil;
import org.shsts.tinactory.core.gui.client.StretchImage;
import org.shsts.tinactory.core.util.I18n;

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
import static org.shsts.tinactory.core.gui.sync.MenuEventHandler.SET_MACHINE_CONFIG;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class AbstractRecipeBook<T> extends Panel {
    private static final int BUTTON_SIZE = SLOT_SIZE + 3;
    private static final int BUTTON_PER_LINE = 4;
    public static final int BUTTON_TOP_MARGIN = BUTTON_SIZE / 2;
    public static final int PANEL_BORDER = 8;
    private static final int PANEL_WIDTH = BUTTON_SIZE * BUTTON_PER_LINE + PANEL_BORDER * 2;
    public static final RectD PANEL_ANCHOR = RectD.corners(0d, 0d, 0d, 1d);
    public static final Rect PANEL_OFFSET = Rect.corners(-MARGIN_HORIZONTAL - PANEL_WIDTH,
            -MARGIN_TOP, -MARGIN_HORIZONTAL, MARGIN_VERTICAL);
    public static final Rect BACKGROUND_TEX_RECT = new Rect(1, 1, 147, 166);
    private static final Rect BUTTON_PANEL_OFFSET = Rect.corners(PANEL_BORDER, PANEL_BORDER + BUTTON_TOP_MARGIN,
            -PANEL_BORDER, -PANEL_BORDER);

    private class RecipeButtonPanel extends ButtonPanel {
        public RecipeButtonPanel() {
            super(AbstractRecipeBook.this.screen, BUTTON_SIZE, BUTTON_SIZE, 0);
        }

        @Override
        protected int getItemCount() {
            return recipeList.size() + 1;
        }

        @Nullable
        private ResourceLocation getLoc(int index) {
            return index >= 1 && index < recipeList.size() + 1 ? recipeList.get(index - 1) : null;
        }

        @Nullable
        private T getRecipe(@Nullable ResourceLocation loc) {
            return loc == null ? null : recipes.getOrDefault(loc, null);
        }

        @Nullable
        private T getRecipe(int index) {
            return getRecipe(getLoc(index));
        }

        @Override
        protected void renderButton(PoseStack poseStack, int mouseX, int mouseY,
                                    float partialTick, Rect rect, int index) {
            var loc = getLoc(index);
            var recipe = getRecipe(loc);
            var z = getBlitOffset();
            if (Objects.equals(getCurrentRecipeLoc(), loc)) {
                RenderUtil.blit(poseStack, Texture.RECIPE_BUTTON, z, rect, 21, 0);
            } else {
                RenderUtil.blit(poseStack, Texture.RECIPE_BUTTON, z, rect);
            }
            if (recipe == null) {
                RenderUtil.blit(poseStack, Texture.DISABLE_BUTTON, z, rect.offset(2, 2).enlarge(-5, -5));
            } else {
                AbstractRecipeBook.this.renderButton(poseStack, mouseX, mouseY, partialTick, recipe, rect, z);
            }
        }

        @Override
        protected void onSelect(int index) {
            var loc = getLoc(index);
            var recipe = getRecipe(loc);
            ghostRecipe.clear();
            if (recipe == null) {
                menu.triggerEvent(SET_MACHINE_CONFIG, SetMachineConfigPacket.builder().reset("targetRecipe"));
            } else {
                menu.triggerEvent(SET_MACHINE_CONFIG, SetMachineConfigPacket.builder().set("targetRecipe", loc));
                selectRecipe(recipe);
            }
        }

        @Override
        protected Optional<List<Component>> buttonTooltip(int index) {
            var recipe = getRecipe(index);
            if (recipe == null) {
                return Optional.of(List.of(I18n.tr("tinactory.tooltip.unselectRecipe")));
            } else {
                return buttonToolTip(recipe);
            }
        }
    }

    protected final BlockEntity blockEntity;
    protected final MachineConfig machineConfig;
    protected final Panel bookPanel;
    protected final ButtonPanel buttonPanel;
    protected final GhostRecipe ghostRecipe;

    protected final Map<ResourceLocation, T> recipes = new HashMap<>();
    private final List<ResourceLocation> recipeList = new ArrayList<>();

    public AbstractRecipeBook(MenuScreen<? extends Menu<?, ?>> screen, int xOffset) {
        super(screen);
        this.blockEntity = screen.getMenu().blockEntity;
        this.machineConfig = AllCapabilities.MACHINE.get(blockEntity).config;
        this.bookPanel = new Panel(screen);
        this.ghostRecipe = new GhostRecipe(screen.getMenu());

        buttonPanel = new RecipeButtonPanel();
        var panelBg = new StretchImage(menu, Texture.RECIPE_BOOK_BG, BACKGROUND_TEX_RECT, PANEL_BORDER);
        bookPanel.addWidget(RectD.FULL, Rect.ZERO, panelBg);
        bookPanel.addPanel(BUTTON_PANEL_OFFSET, buttonPanel);
        bookPanel.setActive(false);

        addPanel(PANEL_ANCHOR, PANEL_OFFSET, bookPanel);
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

    public void setBookActive(boolean value) {
        bookPanel.setActive(value);
    }

    public boolean isBookActive() {
        return bookPanel.isActive();
    }
}
