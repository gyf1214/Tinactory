package org.shsts.tinactory.content.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.shsts.tinactory.api.logistics.IStackKey;
import org.shsts.tinactory.content.gui.sync.MECraftEventPacket;
import org.shsts.tinactory.content.gui.sync.MECraftRequestSyncPacket;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.integration.gui.client.ButtonPanel;
import org.shsts.tinactory.integration.gui.client.Panel;
import org.shsts.tinactory.integration.gui.client.RenderUtil;
import org.shsts.tinactory.integration.gui.client.VanillaButton;
import org.shsts.tinactory.integration.gui.client.Widgets;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.shsts.tinactory.AllMenus.ME_CRAFT_ACTION;
import static org.shsts.tinactory.content.gui.client.MECraftTerminalScreen.BUTTON_WIDTH;
import static org.shsts.tinactory.content.gui.client.MECraftTerminalScreen.tr;
import static org.shsts.tinactory.core.gui.Menu.EDIT_HEIGHT;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Menu.SPACING;
import static org.shsts.tinactory.core.gui.Texture.RECIPE_BUTTON;
import static org.shsts.tinactory.core.gui.Texture.SLOT_BACKGROUND;
import static org.shsts.tinactory.integration.gui.client.Widgets.BUTTON_HEIGHT;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MECraftRequestPanel extends Panel {
    private final List<IStackKey> requestables = new ArrayList<>();
    private final EditBox quantityEdit;
    private int selected = -1;

    private class RequestablesPanel extends ButtonPanel {
        public RequestablesPanel() {
            super(MECraftRequestPanel.this.screen, SLOT_SIZE, SLOT_SIZE, 0);
        }

        @Override
        protected int getItemCount() {
            var count = requestables.size();
            var slotCount = gridViewGroup.getSlotCount();
            return Math.max(1, (count + slotCount - 1) / slotCount) * slotCount;
        }

        @Override
        protected void renderButton(GuiGraphics graphics, int mouseX, int mouseY,
            float partialTick, Rect rect, int index, boolean isHovering) {
            if (index == selected) {
                RenderUtil.blit(graphics, RECIPE_BUTTON, rect, 22, 1);
            } else {
                RenderUtil.blit(graphics, SLOT_BACKGROUND, rect);
            }
            if (index >= requestables.size()) {
                return;
            }
            var requestable = requestables.get(index);
            var display = requestable.display();
            var rect1 = rect.offset(1, 1).enlarge(-2, -2);
            RenderUtil.renderDescriptor(graphics, display, rect1);
        }

        @Override
        protected void onSelect(int index, double mouseX, double mouseY, int button) {
            if (index < requestables.size()) {
                selected = index;
            }
        }

        @Override
        protected Optional<List<Component>> buttonTooltip(int index, double mouseX, double mouseY) {
            return index < requestables.size() ? requestables.get(index).tooltip() : Optional.empty();
        }
    }

    private final RequestablesPanel buttonPanel = new RequestablesPanel();

    public MECraftRequestPanel(MECraftTerminalScreen screen) {
        super(screen);
        this.quantityEdit = Widgets.editBox();
        var previewButton = new VanillaButton(menu, tr("preview"), null, this::requestPreview);

        addGroup(Rect.corners(0, 0, 0, -BUTTON_HEIGHT - SPACING), buttonPanel);
        addVanillaWidget(RectD.corners(0d, 1d, 1d, 1d),
            Rect.corners(0, -EDIT_HEIGHT - SPACING, -BUTTON_WIDTH - SPACING, -SPACING),
            0, quantityEdit);
        addChild(RectD.corners(1d, 1d, 1d, 1d), Rect.corners(-BUTTON_WIDTH, -BUTTON_HEIGHT, 0, 0), previewButton);
    }

    public void updateRequestables(MECraftRequestSyncPacket packet) {
        requestables.clear();
        requestables.addAll(packet.requestables());
        buttonPanel.refresh();
    }

    private void requestPreview() {
        if (selected < 0 || selected >= requestables.size()) {
            return;
        }
        var quantity = 0;
        try {
            quantity = Integer.parseInt(quantityEdit.getValue());
        } catch (NumberFormatException ignored) {
        }
        if (quantity < 0) {
            return;
        }
        var packet = MECraftEventPacket.preview(requestables.get(selected), quantity);
        screen.menu().triggerEvent(ME_CRAFT_ACTION, () -> packet);
    }
}
