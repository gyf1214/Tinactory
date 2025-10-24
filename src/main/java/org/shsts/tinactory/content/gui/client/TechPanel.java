package org.shsts.tinactory.content.gui.client;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.Tinactory;
import org.shsts.tinactory.api.tech.ITeamProfile;
import org.shsts.tinactory.api.tech.ITechManager;
import org.shsts.tinactory.api.tech.ITechnology;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.gui.client.Button;
import org.shsts.tinactory.core.gui.client.ButtonPanel;
import org.shsts.tinactory.core.gui.client.Label;
import org.shsts.tinactory.core.gui.client.MenuWidget;
import org.shsts.tinactory.core.gui.client.Panel;
import org.shsts.tinactory.core.gui.client.RenderUtil;
import org.shsts.tinactory.core.gui.client.StretchImage;
import org.shsts.tinactory.core.gui.client.Widgets;
import org.shsts.tinactory.core.tech.TechManager;
import org.shsts.tinactory.core.util.I18n;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.shsts.tinactory.content.gui.NetworkControllerMenu.LEFT_OFFSET;
import static org.shsts.tinactory.content.gui.NetworkControllerMenu.LEFT_WIDTH;
import static org.shsts.tinactory.content.gui.client.NetworkControllerScreen.tr;
import static org.shsts.tinactory.core.gui.Menu.BUTTON_PANEL_BG;
import static org.shsts.tinactory.core.gui.Menu.FONT_HEIGHT;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_VERTICAL;
import static org.shsts.tinactory.core.gui.Menu.PANEL_BORDER;
import static org.shsts.tinactory.core.gui.Menu.SPACING;
import static org.shsts.tinactory.core.gui.Menu.TECH_SIZE;
import static org.shsts.tinactory.core.gui.Texture.RECIPE_BOOK_BG;
import static org.shsts.tinactory.core.gui.Texture.SWITCH_BUTTON;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TechPanel extends Panel {
    private static final int FINISHED_COLOR = 0xFFAAFFAA;
    private static final int AVAILABLE_COLOR = 0xFFFFFFAA;
    private static final int INVALID_COLOR = 0xFFFFAAAA;
    private static final int PROGRESS_COLOR = 0xFF00AA00;

    private static final int PROGRESS_HEIGHT = 5;

    private final ITechManager techManager;
    private final TechButton currentTechButton;
    private final List<ITechnology> availableTechs = new ArrayList<>();
    private final Panel selectedTechPanel;
    private final Label selectedTechLabel;
    private final Label selectedTechDetailsLabel;
    private final Button startResearchButton;
    @Nullable
    private ITeamProfile team = null;
    @Nullable
    private ITechnology selectedTech = null;

    private final TechButtonPanel availableTechPanel;

    private class TechButton extends Button {
        private final boolean renderPressed;
        @Nullable
        private ITechnology technology = null;

        public TechButton(boolean renderPressed) {
            super(TechPanel.this.menu);
            this.renderPressed = renderPressed;
        }

        @Override
        public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
            if (technology != null) {
                renderTechButton(poseStack, getBlitOffset(), rect, technology, renderPressed);
            }
        }

        @Override
        public Optional<List<Component>> getTooltip(double mouseX, double mouseY) {
            return technology == null ? Optional.empty() : techTooltip(technology);
        }

        @Override
        public void onMouseClicked(double mouseX, double mouseY, int button) {
            super.onMouseClicked(mouseX, mouseY, button);
            if (technology != null) {
                onSelect(technology);
            }
        }
    }

    private class TechButtonPanel extends ButtonPanel {
        public TechButtonPanel() {
            super(TechPanel.this.screen, TECH_SIZE, TECH_SIZE, 0);
        }

        private ITechnology getTech(int index) {
            return availableTechs.get(index);
        }

        @Override
        protected int getItemCount() {
            return availableTechs.size();
        }

        @Override
        protected void renderButton(PoseStack poseStack, int mouseX, int mouseY,
            float partialTick, Rect rect, int index, boolean isHovering) {
            renderTechButton(poseStack, getBlitOffset(), rect, getTech(index), true);
        }

        @Override
        protected void onSelect(int index, double mouseX, double mouseY, int button) {
            TechPanel.this.onSelect(getTech(index));
        }

        @Override
        protected Optional<List<Component>> buttonTooltip(int index, double mouseX, double mouseY) {
            return techTooltip(getTech(index));
        }
    }

    private class RequiredTechButtons extends Button {
        public RequiredTechButtons() {
            super(TechPanel.this.menu);
        }

        @Override
        public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
            var depends = selectedTech == null ? List.<ITechnology>of() : selectedTech.getDepends();
            var z = getBlitOffset();

            var i = 0;
            for (var depend : depends) {
                var x = rect.endX() - (i + 1) * TECH_SIZE;
                var y = rect.y();
                renderTechButton(poseStack, z, new Rect(x, y, TECH_SIZE, TECH_SIZE), depend, true);
                i++;
            }
        }

        private Optional<ITechnology> getSelectedTech(double mouseX) {
            if (selectedTech == null) {
                return Optional.empty();
            }
            var depends = selectedTech.getDepends();
            var index = (int) Math.floor((rect.endX() - mouseX) / TECH_SIZE);

            return index >= 0 && index < depends.size() ? Optional.of(depends.get(index)) : Optional.empty();
        }

        @Override
        public Optional<List<Component>> getTooltip(double mouseX, double mouseY) {
            return getSelectedTech(mouseX).flatMap(TechPanel.this::techTooltip);
        }

        @Override
        public void onMouseClicked(double mouseX, double mouseY, int button) {
            getSelectedTech(mouseX).ifPresent(tech -> {
                super.onMouseClicked(mouseX, mouseY, button);
                onSelect(tech);
            });
        }
    }

    private class ProgressBar extends MenuWidget {
        public ProgressBar() {
            super(TechPanel.this.menu);
        }

        @Override
        public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
            if (selectedTech == null || team == null) {
                return;
            }

            var progress = team.getTechProgress(selectedTech) * rect.width() / selectedTech.getMaxProgress();
            RenderUtil.fill(poseStack, rect.resize((int) progress, rect.height()), PROGRESS_COLOR);
        }

        @Override
        protected boolean canHover() {
            return true;
        }

        @Override
        public Optional<List<Component>> getTooltip(double mouseX, double mouseY) {
            if (selectedTech == null || team == null) {
                return Optional.empty();
            }
            return Optional.of(List.of(tr("researchProgress",
                team.getTechProgress(selectedTech), selectedTech.getMaxProgress())));
        }
    }

    public TechPanel(NetworkControllerScreen screen) {
        super(screen);
        this.techManager = TechManager.client();

        var label1 = new Label(menu, tr("currentTechLabel"));
        label1.verticalAlign = Label.Alignment.MIDDLE;
        this.currentTechButton = new TechButton(true);
        addWidget(new Rect(0, 0, LEFT_WIDTH - TECH_SIZE - 2, TECH_SIZE), label1);
        addWidget(new Rect(LEFT_WIDTH - TECH_SIZE - 2, 0, TECH_SIZE, TECH_SIZE), currentTechButton);

        this.availableTechPanel = new TechButtonPanel();
        var label2 = new Label(menu, tr("availableTechLabel"));
        var anchor1 = RectD.corners(0d, 0d, 0d, 1d);
        var top = TECH_SIZE + MARGIN_VERTICAL * 2;
        var offset1 = Rect.corners(-1, top + FONT_HEIGHT + SPACING, LEFT_WIDTH - 1, 0);
        var offset2 = offset1.offset(PANEL_BORDER, PANEL_BORDER).enlarge(-PANEL_BORDER * 2, -PANEL_BORDER * 2);
        var bg = new StretchImage(menu, RECIPE_BOOK_BG, BUTTON_PANEL_BG, PANEL_BORDER);
        addWidget(new Rect(0, top, LEFT_WIDTH, FONT_HEIGHT), label2);
        addWidget(anchor1, offset1, bg);
        addPanel(anchor1, offset2, availableTechPanel);

        this.selectedTechPanel = new Panel(screen);
        this.selectedTechLabel = new Label(menu);
        this.selectedTechDetailsLabel = new Label(menu);
        var label3 = new Label(menu, tr("techRequirementsLabel"));
        label3.verticalAlign = Label.Alignment.MIDDLE;
        this.startResearchButton = Widgets.simpleButton(menu, tr("startResearchButton"),
            null, this::startResearch);
        var y = 0;
        var offset7 = Rect.corners(0, y - Widgets.BUTTON_HEIGHT, 0, y);
        y -= Widgets.BUTTON_HEIGHT + MARGIN_VERTICAL;
        var offset6 = Rect.corners(0, y - PROGRESS_HEIGHT, 0, y);
        y -= PROGRESS_HEIGHT + MARGIN_VERTICAL;
        var offset5 = Rect.corners(0, y - TECH_SIZE, 0, y);
        y -= TECH_SIZE + MARGIN_VERTICAL;
        var offset4 = Rect.corners(0, FONT_HEIGHT + SPACING, 0, y);
        var offset3 = Rect.corners(0, 0, 0, FONT_HEIGHT);
        selectedTechPanel.addWidget(RectD.corners(0d, 0d, 1d, 0d), offset3, selectedTechLabel);
        selectedTechPanel.addWidget(RectD.FULL, offset4, selectedTechDetailsLabel);
        selectedTechPanel.addWidget(RectD.corners(0d, 1d, 0d, 1d), offset5, label3);
        selectedTechPanel.addWidget(RectD.corners(0d, 1d, 1d, 1d), offset5, new RequiredTechButtons());
        selectedTechPanel.addWidget(RectD.corners(0d, 1d, 1d, 1d), offset6, new ProgressBar());
        selectedTechPanel.addWidget(RectD.corners(0d, 1d, 1d, 1d), offset7, startResearchButton);
        addPanel(Rect.corners(LEFT_OFFSET, 0, 0, -1), selectedTechPanel);
    }

    public static void renderTechButton(PoseStack poseStack, int z, Rect rect, @Nullable ITeamProfile team,
        ITechnology technology, boolean pressed) {
        int color;
        if (team == null) {
            color = INVALID_COLOR;
        } else if (team.isTechFinished(technology)) {
            color = FINISHED_COLOR;
        } else if (team.isTechAvailable(technology)) {
            color = AVAILABLE_COLOR;
        } else {
            color = INVALID_COLOR;
        }

        var th = SWITCH_BUTTON.height() / 2;
        var texRect = new Rect(0, 0, SWITCH_BUTTON.width(), th);
        if (pressed) {
            texRect = texRect.offset(0, th);
        }
        StretchImage.render(poseStack, SWITCH_BUTTON, z, color, rect, texRect, 1);

        var x = rect.x() + (rect.width() - 16) / 2;
        var y = rect.y() + (rect.height() - 16) / 2;

        if (technology.getDisplayItem().isEmpty()) {
            var tex1 = new Texture(technology.getDisplayTexture(), 16, 16);
            var rect1 = new Rect(x, y, 16, 16);
            RenderUtil.blit(poseStack, tex1, z, rect1);
        } else {
            var poseStack1 = RenderUtil.applyToModelViewStack(poseStack);
            RenderUtil.renderItem(technology.getDisplayItem(), x, y);
            RenderUtil.popModelViewStack(poseStack1);
        }
    }

    private void renderTechButton(PoseStack poseStack, int z, Rect rect, ITechnology technology,
        boolean renderPressed) {
        if (team == null) {
            return;
        }
        renderTechButton(poseStack, z, rect, team, technology, renderPressed && technology == selectedTech);
    }

    private Optional<List<Component>> techTooltip(ITechnology technology) {
        return Optional.of(List.of(I18n.tr(technology.getDescriptionId())));
    }

    private void onSelect(ITechnology technology) {
        selectedTech = technology;
        refreshSelected();
    }

    private void startResearch() {
        if (menu.player() instanceof LocalPlayer player && selectedTech != null) {
            var loc = selectedTech.getLoc().toString();
            var command = "/" + Tinactory.ID + " setTargetTech " + loc;
            player.chat(command);
        }
    }

    private void refreshSelected() {
        if (team == null) {
            return;
        }

        selectedTechPanel.setActive(selectedTech != null);

        if (selectedTech != null) {
            selectedTechLabel.setLine(0, I18n.tr(selectedTech.getDescriptionId()));
            selectedTechDetailsLabel.setMultiline(I18n.tr(selectedTech.getDetailsId()));
            startResearchButton.setActive(team.canResearch(selectedTech));
        }
    }

    private int compareTech(ITechnology a, ITechnology b) {
        assert team != null;
        var xa = team.canResearch(a) ? 0 : (team.isTechFinished(a) ? 2 : 1);
        var xb = team.canResearch(b) ? 0 : (team.isTechFinished(b) ? 2 : 1);
        return xa == xb ? a.compareTo(b) : (xa < xb ? -1 : 1);
    }

    @Override
    protected void doRefresh() {
        if (team == null) {
            return;
        }

        var targetTech = team.getTargetTech().orElse(null);
        currentTechButton.technology = targetTech;
        currentTechButton.setActive(targetTech != null);

        if (selectedTech == null) {
            selectedTech = targetTech;
        }
        refreshSelected();

        availableTechs.clear();
        availableTechs.addAll(techManager.allTechs().stream()
            .sorted(this::compareTech)
            .toList());
        availableTechPanel.refresh();
    }

    public void refreshTech(ITeamProfile team) {
        this.team = team;
        refresh();
    }

    public static boolean isHoveringTech(Widget component) {
        return component instanceof TechButton ||
            component instanceof RequiredTechButtons ||
            (component instanceof ButtonPanel.ItemButton itemButton &&
                itemButton.getParent() instanceof TechButtonPanel);
    }

    public static Optional<ITechnology> getHoveredTech(Widget component, double mouseX) {
        if (component instanceof TechButton button) {
            return Optional.ofNullable(button.technology);
        } else if (component instanceof RequiredTechButtons buttons) {
            return buttons.getSelectedTech(mouseX);
        } else if (component instanceof ButtonPanel.ItemButton itemButton &&
            itemButton.getParent() instanceof TechButtonPanel buttonPanel) {
            return Optional.of(buttonPanel.getTech(itemButton.getIndex()));
        }
        return Optional.empty();
    }
}
