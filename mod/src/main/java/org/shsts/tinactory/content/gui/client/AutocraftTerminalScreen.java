package org.shsts.tinactory.content.gui.client;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.content.gui.AutocraftTerminalMenu;
import org.shsts.tinactory.content.gui.sync.AutocraftTerminalActionPacket;
import org.shsts.tinactory.content.gui.sync.AutocraftTerminalCpuSyncSlot;
import org.shsts.tinactory.content.gui.sync.AutocraftTerminalPreviewSyncSlot;
import org.shsts.tinactory.content.gui.sync.AutocraftTerminalRequestablesSyncSlot;
import org.shsts.tinactory.core.autocraft.integration.AutocraftRequestableEntry;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.core.gui.client.MenuScreen;

import java.util.List;
import java.util.UUID;

import static org.shsts.tinactory.AllMenus.AUTOCRAFT_TERMINAL_ACTION;
import static org.shsts.tinactory.content.gui.AutocraftTerminalMenu.CPU_SYNC;
import static org.shsts.tinactory.content.gui.AutocraftTerminalMenu.PREVIEW_SYNC;
import static org.shsts.tinactory.content.gui.AutocraftTerminalMenu.REQUESTABLES_SYNC;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AutocraftTerminalScreen extends MenuScreen<AutocraftTerminalMenu> {
    private final AutocraftRequestPanel requestPanel;
    private final AutocraftPreviewPanel previewPanel;

    private List<AutocraftRequestableEntry> requestables = List.of();
    private List<UUID> availableCpus = List.of();
    @Nullable
    private UUID previewPlanId;

    public AutocraftTerminalScreen(AutocraftTerminalMenu menu, Component title) {
        super(menu, title);
        this.requestPanel = new AutocraftRequestPanel(this);
        this.previewPanel = new AutocraftPreviewPanel(this);

        addPanel(RectD.corners(0d, 0d, 1d, 0d), Rect.corners(0, 0, 0, 52), requestPanel);
        addPanel(RectD.corners(0d, 0d, 1d, 1d), Rect.corners(0, 56, 0, -88), previewPanel);
        this.contentHeight = 180;

        menu.onSyncPacket(REQUESTABLES_SYNC, this::onRequestablesSync);
        menu.onSyncPacket(CPU_SYNC, this::onCpuSync);
        menu.onSyncPacket(PREVIEW_SYNC, this::onPreviewSync);
        refreshRequestTitle();
    }

    public void requestPreview() {
        if (requestables.isEmpty() || availableCpus.isEmpty()) {
            return;
        }
        var quantity = requestPanel.quantity().orElse(1L);
        menu.triggerEvent(AUTOCRAFT_TERMINAL_ACTION, () -> AutocraftTerminalActionPacket.preview(
            requestables.get(0).key(),
            Math.max(1L, quantity),
            availableCpus.get(0)));
    }

    public void executePreview() {
        if (previewPlanId == null || availableCpus.isEmpty()) {
            return;
        }
        menu.triggerEvent(AUTOCRAFT_TERMINAL_ACTION,
            () -> AutocraftTerminalActionPacket.execute(previewPlanId, availableCpus.get(0)));
    }

    public void cancelPreview() {
        if (previewPlanId == null) {
            return;
        }
        menu.triggerEvent(AUTOCRAFT_TERMINAL_ACTION, () -> AutocraftTerminalActionPacket.cancel(previewPlanId));
    }

    private void onRequestablesSync(AutocraftTerminalRequestablesSyncSlot packet) {
        requestables = packet.requestables();
        refreshRequestTitle();
    }

    private void onCpuSync(AutocraftTerminalCpuSyncSlot packet) {
        availableCpus = packet.availableCpus();
        refreshRequestTitle();
    }

    private void onPreviewSync(AutocraftTerminalPreviewSyncSlot packet) {
        previewPlanId = packet.planId();
        if (packet.previewError() != null) {
            previewPanel.setSummary(new TextComponent("Preview error: " + packet.previewError().name()));
            return;
        }
        if (packet.executeError() != null) {
            previewPanel.setSummary(new TextComponent("Execute error: " + packet.executeError().name()));
            return;
        }
        if (previewPlanId != null) {
            previewPanel.setSummary(new TextComponent(
                "Plan " + previewPlanId + " outputs: " + packet.summaryOutputs().size()));
        } else {
            previewPanel.setSummary(new TextComponent("No Preview"));
        }
    }

    private void refreshRequestTitle() {
        requestPanel.setTitle(new TextComponent(
            "Requestables: " + requestables.size() + ", CPUs: " + availableCpus.size()));
    }
}
