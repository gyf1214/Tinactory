package org.shsts.tinactory.content.gui.client;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import org.shsts.tinactory.api.logistics.IStackKey;
import org.shsts.tinactory.content.gui.sync.AutocraftPreviewSyncPacket;
import org.shsts.tinactory.core.autocraft.plan.PlanSummary;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.integration.gui.client.ButtonPanel;
import org.shsts.tinactory.integration.gui.client.Panel;
import org.shsts.tinactory.integration.gui.client.RenderUtil;
import org.shsts.tinactory.integration.gui.client.VanillaButton;
import org.shsts.tinactory.integration.util.ClientUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.shsts.tinactory.content.gui.client.AutocraftTerminalScreen.BUTTON_WIDTH;
import static org.shsts.tinactory.content.gui.client.AutocraftTerminalScreen.tr;
import static org.shsts.tinactory.core.gui.Menu.FONT_HEIGHT;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Menu.SPACING;
import static org.shsts.tinactory.integration.gui.client.Widgets.BUTTON_HEIGHT;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AutocraftPreviewPanel extends Panel {
    private static final int SUMMARY_COLUMNS = 2;
    private static final int SUMMARY_HEIGHT = SLOT_SIZE + SPACING * 2;
    private static final int SUMMARY_WIDTH = (SLOT_SIZE * 9 - 1) / SUMMARY_COLUMNS;
    private static final float LABEL_SCALE = 0.5f;
    private static final int LABEL_WIDTH = SUMMARY_WIDTH - SLOT_SIZE - SPACING * 3;
    private static final int LABEL_HEIGHT = (int) (FONT_HEIGHT * LABEL_SCALE);
    private static final int LABEL_SPACING = (SUMMARY_HEIGHT - LABEL_HEIGHT * 2) / 3;
    private static final int BG_COLOR = 0xFFBBBBBB;
    private static final int BG_COLOR_ERROR = 0xFFDD9898;

    private final VanillaButton executeButton;
    private final List<Map.Entry<IStackKey, PlanSummary.Entry>> summary = new ArrayList<>();

    private class SummaryPanel extends ButtonPanel {
        public SummaryPanel() {
            super(AutocraftPreviewPanel.this.screen, SUMMARY_WIDTH, SUMMARY_HEIGHT, 0);
        }

        @Override
        protected int getItemCount() {
            return summary.size();
        }

        private boolean isHoveringDisplay(double mouseX, double mouseY) {
            mouseX -= LABEL_WIDTH + SPACING * 2;
            mouseY -= SPACING;
            return mouseX >= 0 && mouseX < SLOT_SIZE && mouseY >= 0 && mouseY < SLOT_SIZE;
        }

        @Override
        protected void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick,
            Rect rect, int index, boolean isHovering) {
            var entry = summary.get(index);
            var display = entry.getKey().display();
            var amounts = entry.getValue();

            var color = amounts.consumedFromInventory() > amounts.existingAmount() ?
                BG_COLOR_ERROR : BG_COLOR;
            RenderUtil.fill(poseStack, rect, color);

            var rect1 = rect.offset(LABEL_WIDTH + SPACING * 2 + 1, SPACING + 1)
                .resize(SLOT_SIZE - 2, SLOT_SIZE - 2);
            RenderUtil.renderDescriptor(poseStack, display, rect1, getBlitOffset());

            var textX = rect.x() + LABEL_SPACING;
            var textY1 = rect.y() + LABEL_SPACING;
            var textY2 = rect.y() + SUMMARY_HEIGHT - LABEL_SPACING - LABEL_HEIGHT;

            var line1 = entry.getValue().craftedAmount() > 0 ?
                tr("summary.crafted", ClientUtil.getNumberString(amounts.craftedAmount())) :
                tr("summary.existing", ClientUtil.getNumberString(amounts.existingAmount()));
            var line2 = tr("summary.consumed", ClientUtil.getNumberString(amounts.consumedFromInventory()));

            RenderUtil.renderText(poseStack, line1, textX, textY1, LABEL_SCALE);
            RenderUtil.renderText(poseStack, line2, textX, textY2, LABEL_SCALE);
        }

        @Override
        protected void onSelect(int index, double mouseX, double mouseY, int button) {}

        @Override
        protected Optional<List<Component>> buttonTooltip(int index, double mouseX, double mouseY) {
            if (isHoveringDisplay(mouseX, mouseY)) {
                return summary.get(index).getKey().tooltip();
            } else {
                return Optional.empty();
            }
        }
    }

    public AutocraftPreviewPanel(AutocraftTerminalScreen screen) {
        super(screen);
        this.executeButton = new VanillaButton(menu, tr("execute"), null, screen::executePreview);
        executeButton.disabled = true;
        var cancelButton = new VanillaButton(menu, tr("cancel"), null, () -> {
            executeButton.disabled = true;
            summary.clear();
            screen.cancelPreview();
        });

        addGroup(Rect.corners(0, 0, 0, -BUTTON_HEIGHT - SPACING), new SummaryPanel());
        addChild(RectD.corners(0d, 1d, 0d, 1d), Rect.corners(0, -BUTTON_HEIGHT, BUTTON_WIDTH, 0), executeButton);
        addChild(RectD.corners(1d, 1d, 1d, 1d), Rect.corners(-BUTTON_WIDTH, -BUTTON_HEIGHT, 0, 0), cancelButton);
    }

    public void onPreviewSync(AutocraftPreviewSyncPacket packet) {
        executeButton.disabled = packet.state() != AutocraftPreviewSyncPacket.PreviewState.PREVIEW_READY;
        summary.clear();
        packet.summary().entries().entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(summary::add);
    }
}
