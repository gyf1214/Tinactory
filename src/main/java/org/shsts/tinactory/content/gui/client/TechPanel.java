package org.shsts.tinactory.content.gui.client;


import com.mojang.blaze3d.MethodsReturnNonnullByDefault;
import com.mojang.blaze3d.vertex.PoseStack;
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
import org.shsts.tinactory.core.gui.client.MenuScreen;
import org.shsts.tinactory.core.gui.client.Panel;
import org.shsts.tinactory.core.gui.client.RenderUtil;
import org.shsts.tinactory.core.gui.client.StretchImage;
import org.shsts.tinactory.core.gui.client.Widgets;
import org.shsts.tinactory.core.tech.TechManager;
import org.shsts.tinactory.core.util.I18n;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.shsts.tinactory.content.gui.client.MachineRecipeBook.BACKGROUND_TEX_RECT;
import static org.shsts.tinactory.content.gui.client.MachineRecipeBook.PANEL_BORDER;
import static org.shsts.tinactory.content.gui.client.NetworkControllerScreen.tr;
import static org.shsts.tinactory.core.gui.Menu.SPACING;
import static org.shsts.tinactory.core.gui.client.Label.LINE_HEIGHT;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TechPanel extends Panel {
    private static final int FINISHED_COLOR = 0xFFAAFFAA;
    private static final int AVAILABLE_COLOR = 0xFFFFFFAA;
    private static final int INVALID_COLOR = 0xFFFFAAAA;

    private static final int BUTTON_SIZE = 24;
    private static final int LEFT_WIDTH = PANEL_BORDER * 2 + BUTTON_SIZE * 6;

    private final Label techLabel;
    private final TechButton currentTechButton;
    private final Button startResearchButton;
    private final ITechManager techManager;
    private final List<ITechnology> availableTechs = new ArrayList<>();
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
        public Optional<List<Component>> getTooltip() {
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
            super(TechPanel.this.screen, BUTTON_SIZE, BUTTON_SIZE, 0);
        }

        @Override
        protected int getItemCount() {
            return availableTechs.size();
        }

        @Override
        protected void renderButton(PoseStack poseStack, int mouseX, int mouseY,
                                    float partialTick, Rect rect, int index) {
            renderTechButton(poseStack, getBlitOffset(), rect, availableTechs.get(index), true);
        }

        @Override
        protected void onSelect(int index) {
            TechPanel.this.onSelect(availableTechs.get(index));
        }

        @Override
        protected Optional<List<Component>> buttonTooltip(int index) {
            return techTooltip(availableTechs.get(index));
        }
    }

    public TechPanel(MenuScreen<?> screen) {
        super(screen);
        this.techManager = TechManager.client();

        var label1 = new Label(menu, Label.Alignment.BEGIN, tr("currentTechLabel"));
        label1.verticalAlign = Label.Alignment.MIDDLE;
        this.currentTechButton = new TechButton(true);
        addWidget(new Rect(LEFT_WIDTH - BUTTON_SIZE, 0, BUTTON_SIZE, BUTTON_SIZE), currentTechButton);
        addWidget(new Rect(0, 0, LEFT_WIDTH - BUTTON_SIZE, BUTTON_SIZE), label1);

        this.techLabel = new Label(menu, Label.Alignment.BEGIN, tr("techDetails"));
        this.startResearchButton = Widgets.simpleButton(menu, tr("startResearchButton"), null,
                this::startResearch);
        var offset1 = Rect.corners(0, BUTTON_SIZE + SPACING, LEFT_WIDTH, -SPACING * 2 - Widgets.BUTTON_HEIGHT);
        var offset2 = Rect.corners(0, -SPACING - Widgets.BUTTON_HEIGHT, LEFT_WIDTH, -SPACING);
        addWidget(RectD.corners(0d, 0d, 0d, 0.5), offset1, techLabel);
        addWidget(RectD.corners(0d, 0.5, 0d, 0.5), offset2, startResearchButton);

        this.availableTechPanel = new TechButtonPanel();
        var label2 = new Label(menu, Label.Alignment.BEGIN, tr("availableTechLabel"));
        var anchor1 = RectD.corners(0d, 0.5, 0d, 1d);
        var offset3 = Rect.corners(0, LINE_HEIGHT + SPACING, LEFT_WIDTH, 0);
        var offset4 = offset3.offset(PANEL_BORDER, PANEL_BORDER).enlarge(-PANEL_BORDER * 2, -PANEL_BORDER * 2);
        var bg = new StretchImage(menu, Texture.RECIPE_BOOK_BG, BACKGROUND_TEX_RECT, PANEL_BORDER);
        addWidget(RectD.corners(0d, 0.5, 0d, 0.5d), new Rect(0, 0, LEFT_WIDTH, LINE_HEIGHT), label2);
        addWidget(anchor1, offset3, bg);
        addPanel(anchor1, offset4, availableTechPanel);


    }

    private void renderTechButton(PoseStack poseStack, int z, Rect rect, ITechnology technology,
                                  boolean renderPressed) {
        if (team == null) {
            return;
        }

        int color;
        if (team.isTechFinished(technology)) {
            color = FINISHED_COLOR;
        } else if (team.isTechAvailable(technology)) {
            color = AVAILABLE_COLOR;
        } else {
            color = INVALID_COLOR;
        }

        var tex = Texture.SWITCH_BUTTON;
        var th = Texture.SWITCH_BUTTON.height() / 2;
        var texRect = new Rect(0, 0, tex.width(), th);
        if (renderPressed && technology == selectedTech) {
            texRect = texRect.offset(0, th);
        }
        StretchImage.render(poseStack, Texture.SWITCH_BUTTON, z, color, rect, texRect, 1);

        var x = rect.x() + (rect.width() - 16) / 2;
        var y = rect.y() + (rect.height() - 16) / 2;
        RenderUtil.renderItem(technology.getDisplayItem(), x, y);
    }

    private Optional<List<Component>> techTooltip(ITechnology technology) {
        return Optional.of(List.of(I18n.tr(technology.getDescriptionId())));
    }

    private void onSelect(ITechnology technology) {
        selectedTech = technology;
        refreshSelected();
    }

    private void startResearch() {
        if (menu.player instanceof LocalPlayer player && selectedTech != null) {
            var loc = selectedTech.getLoc().toString();
            var command = "/" + Tinactory.ID + " setTargetTech " + loc;
            player.chat(command);
        }
    }

    private void refreshSelected() {
        if (team == null) {
            return;
        }

        if (selectedTech == null) {
            techLabel.setLines();
        } else {
            techLabel.setLine(0, I18n.tr(selectedTech.getDescriptionId()));
            techLabel.setLine(1, I18n.tr(selectedTech.getDetailsId()));
        }

        startResearchButton.setActive(selectedTech != null && team.canResearch(selectedTech));
    }

    @Override
    protected void doRefresh() {
        if (team == null) {
            return;
        }

        var targetTech = team.getTargetTech().orElse(null);
        currentTechButton.technology = targetTech;
        currentTechButton.setActive(targetTech != null);

        refreshSelected();

        availableTechs.clear();
        availableTechs.addAll(techManager.allTechs().stream()
                .filter(team::canResearch)
                .toList());
        availableTechPanel.refresh();
    }

    public void refreshTech(ITeamProfile team) {
        this.team = team;
        refresh();
    }
}
